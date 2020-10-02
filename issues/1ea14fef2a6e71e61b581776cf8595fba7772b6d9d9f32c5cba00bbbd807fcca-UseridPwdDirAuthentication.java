// --- BEGIN COPYRIGHT BLOCK ---
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; version 2 of the License.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, write to the Free Software Foundation, Inc.,
// 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
//
// (C) 2007 Red Hat, Inc.
// All rights reserved.
// --- END COPYRIGHT BLOCK ---
package com.netscape.cms.authentication;


// ldap java sdk
import netscape.ldap.*;

// cert server imports.
import com.netscape.certsrv.base.IConfigStore;
import com.netscape.certsrv.base.EBaseException;
import com.netscape.certsrv.base.IExtendedPluginInfo;
import com.netscape.certsrv.ldap.ELdapException;
import com.netscape.certsrv.ldap.LdapResources;
import com.netscape.certsrv.logging.ILogger;
import com.netscape.certsrv.authentication.*;
import com.netscape.certsrv.profile.*;
import com.netscape.certsrv.request.*;
import com.netscape.certsrv.property.*;
import com.netscape.certsrv.apps.*;
import com.netscape.certsrv.usrgrp.*;

// cert server x509 imports
import netscape.security.x509.X509CertInfo;
import netscape.security.x509.X500Name;
import netscape.security.x509.CertificateSubjectName;
import java.security.cert.CertificateException;

// java sdk imports.
import java.util.Hashtable;
import java.util.Vector;
import java.util.Locale;
import java.util.Enumeration;
import java.io.IOException;


/**
 * uid/pwd directory based authentication manager
 * This version also finds and adds group membership info into
 * the AuthToken
 * <P>
 *
 * @author cfu
 * @version $Revision: 1212 $, $Date: 2010-08-18 10:36:51 -0700 (Wed, 18 Aug 2010) $
 */
public class UseridPwdDirAuthentication extends DirBasedAuthentication 
    implements IProfileAuthenticator {

    /* required credentials to authenticate. uid and pwd are strings. */
    public static final String CRED_UID = "uid";
    public static final String CRED_PWD = "pwd";
    protected static String[] mRequiredCreds = { CRED_UID, CRED_PWD };

    public static final String TOKEN_USERID = "userid";
    public static final String TOKEN_GROUPS = "groups";

    /* Holds configuration parameters accepted by this implementation.
     * This list is passed to the configuration console so configuration
     * for instances of this implementation can be configured through the
     * console.
     */
    protected static String[] mConfigParams = 
        new String[] {	PROP_DNPATTERN,
            PROP_LDAPSTRINGATTRS,
            PROP_LDAPBYTEATTRS,
            "ldap.ldapconn.host",
            "ldap.ldapconn.port",
            "ldap.ldapconn.secureConn",
            "ldap.ldapconn.version",
            "ldap.basedn",
            "ldap.minConns", 
            "ldap.maxConns",
        };

    static {
        mExtendedPluginInfo.add(IExtendedPluginInfo.HELP_TEXT +
            ";Authenticate the username and password provided " +
            "by the user against an LDAP directory. Works with the " +
            "Dir Based Enrollment HTML form");
        mExtendedPluginInfo.add(IExtendedPluginInfo.HELP_TOKEN +
            ";configuration-authrules-uidpwddirauth");
    };

    /**
     * Default constructor, initialization must follow.
     */
    public UseridPwdDirAuthentication() {
        super();
    }

    /**
     * Retrieves group base dn.
     */
    private String getGroupBaseDN() {
        //return "ou=groups," + mBaseDN;
        return mGroups + "," + mGroupsBaseDN;
    }

    /**
     * List groups. only retrieves group names
     */
    public Enumeration listGroups(LDAPConnection ldapconn, String filter) throws EUsrGrpException {
        String method = "UseridPwdDirAuthentication: listGroups:";
        CMS.debug(method + " begins");
        if (filter == null) {
            return null;
        }

        try {
           // String attrs[] = new String[2];
            String attrs[] = new String[1];

            attrs[0] = mGroupIDName;
//            attrs[1] = "description";

            //ldapconn = getConn();
/*
            LDAPSearchResults res = 
                ldapconn.search(getGroupBaseDN(), LDAPv2.SCOPE_SUB, 
                    "(&(objectclass=groupofuniquenames)(cn=" + filter + "))", 
                    attrs, false);
*/
            LDAPSearchResults res = 
                ldapconn.search(getGroupBaseDN(), LDAPv2.SCOPE_SUB, 
                    "(&(objectclass=" +
                    mGroupObjectClass +
                    ")("+ mGroupIDName +"=" + filter + "))", 
                    attrs, false);

            return buildGroups(res);
        } catch (LDAPException e) {
            String errMsg = "listGroups()" + e.toString();

            if (e.getLDAPResultCode() == LDAPException.UNAVAILABLE) {
                errMsg = "listGroups: " + "Internal DB is unavailable";
            }
            CMS.debug(method + e);
/*
        } catch (ELdapException e) {
            String errMsg = 
                "list Groups: Could not get connection to internaldb. Error " + e;
            CMS.debug(method + e);

*/
        } finally {
            CMS.debug(method + " ends");
            /*
            if (ldapconn != null) 
                returnConn(ldapconn);
             */
        }
        return null;
    }

    protected Enumeration buildGroups(LDAPSearchResults res) {
        Vector v = new Vector();

        while (res.hasMoreElements()) {
            LDAPEntry entry = (LDAPEntry) res.nextElement();
            String groupName = null;
/*
            if(mSearchGroupUserByUserdn)
                groupName = entry.getDN();
            else
*/
                groupName = (String)entry.getAttribute(mGroupIDName).getStringValues().nextElement();

            if (groupName != null)
                v.addElement(groupName);
        }
        return v.elements();
    }

    /*
     * userIdent could be either uid or userdn, depending on the 
     *    mSearchGroupUserByUserdn value
     */
    protected boolean isMemberOfLdapGroup(LDAPConnection ldapconn, String userIdent, String groupname)
    {
        /*
         * e.g.
         *  auths.instance.UserDirEnrollment.ldap.basedn=dc=dsdev,dc=sjc,dc=redhat,dc=com
         *  auths.instance.UserDirEnrollment.ldap.groupObjectClass=groupofuniquenames
         *  auths.instance.UserDirEnrollment.ldap.groups=ou=Groups
         *  auths.instance.UserDirEnrollment.ldap.groupsBasedn=dc=dsdev,dc=sjc,dc=redhat,dc=com
         *  auths.instance.UserDirEnrollment.ldap.groupidName=cn
         *  auths.instance.UserDirEnrollment.ldap.useridName=uid
         *  auths.instance.UserDirEnrollment.ldap.groupUseridName=cn
         *  auths.instance.UserDirEnrollment.ldap.searchGroupUserByUserdn=false
         *
         *  where group = "directory administrators"
         *        userIdent = "cfu"
         *  would result in
         *    basedn = "cn=directory administrators,ou=Groups,dc=dsdev,dc=sjc,dc=redhat,dc=com
         */
        String basedn = mGroupIDName + "="+groupname+"," + mGroups + "," +mGroupsBaseDN;
        boolean founduser=false;
        try {
            // the group could potentially have many thousands
            // of members, (many values of the uniquemember
            // attribute). So, we don't want to fetch this
            // list each time. We'll just fetch the CN.
            String attrs[]= new String[1];
            attrs[0] = mGroupUserIDName;

            String filter = null;
            if (mGroupObjectClass.equalsIgnoreCase("groupOfUniqueNames"))
               if(mSearchGroupUserByUserdn)
                   filter = "(uniquemember="+userIdent+")";
               else
                   filter = "(uniquemember=" + mGroupUserIDName +
                       "="+userIdent+")";
            else if (mGroupObjectClass.equalsIgnoreCase("groupOfNames"))
               if(mSearchGroupUserByUserdn)
                   filter = "(member="+userIdent+")";
               else
                   filter = "(member=" + mGroupUserIDName +
                       "="+userIdent+")";
            else {
                CMS.debug("UseridPwdDirAuthentication: isMemberOfLdapGroup: unrecognized mGroupObjectClass: " + mGroupObjectClass);
                return false;
            }

            CMS.debug("UseridPwdDirAuthentication: isMemberOfLdapGroup: search with basedn="+ basedn + " and filter =" + filter); 

            LDAPSearchResults res =
                ldapconn.search(basedn, LDAPv2.SCOPE_BASE,
                       filter,
                       attrs, false);
            // If the result had at least one entry, we know
            // that the filter matched, and so the user correctly
            // authenticated.
            if (res.hasMoreElements()) {
                // actually read the entry
                LDAPEntry entry = (LDAPEntry)res.nextElement();
                founduser=true;
            }
            CMS.debug("UseridPwdDirAuthentication: isMemberOfLdapGroup: "+ groupname + ":" + founduser);
        } catch (LDAPException e) {
           String errMsg =
               "UseridPwdDirAuthentication:isMemberOfLdapGroup: Could not find grou " +
               groupname + ". Error "+e;
           if (e.getLDAPResultCode() == LDAPException.UNAVAILABLE) {
               errMsg = "UseridPwdDirAuthentication:isMemberOfLdapGroup: "+"db unavailable";
           }
           CMS.debug(errMsg);
/*
        } catch (ELdapException e) {
           String errMsg =
               "UseridPwdDirAuthentication:isMemberOfLdapGroup: Could not get connection to db. Error "+e;
           CMS.debug(errMsg);
*/
        } finally {
        }
        return founduser;
    }

    /**
     * Authenticates a user based on uid, pwd in the directory.
     * @param authCreds The authentication credentials.
     * @return The user's ldap entry dn.
     * @exception EInvalidCredentials If the uid and password are not valid
     * @exception EBaseException If an internal error occurs. 
     */
    protected String authenticate(LDAPConnection conn, 
        IAuthCredentials authCreds,
        AuthToken token)
        throws EBaseException {
        String userdn = null;
        String uid = null;

        // authenticate by binding to ldap server with password.
        try {
            // get the uid.
            uid = (String) authCreds.get(CRED_UID);
            CMS.debug("UseridPwdDirAuthentication: Authenticating UID=" + uid);
            if (uid == null) {
                throw new EMissingCredential(CMS.getUserMessage("CMS_AUTHENTICATION_NULL_CREDENTIAL", CRED_UID));
            }
	
            // get the password.
            String pwd = (String) authCreds.get(CRED_PWD);

            if (pwd == null) {
                throw new EMissingCredential(CMS.getUserMessage("CMS_AUTHENTICATION_NULL_CREDENTIAL",CRED_PWD));
            }
            if (pwd.equals("")) {
                // anonymous binding not allowed
                log(ILogger.LL_FAILURE, CMS.getLogMessage("CMS_AUTH_EMPTY_PASSWORD", uid));
                throw new EInvalidCredentials(CMS.getUserMessage("CMS_AUTHENTICATION_INVALID_CREDENTIAL"));
            }

            // bind as user dn and pwd - authenticates user with pwd.
            conn.authenticate(userdn, pwd);

            /*
             * first try and see if the directory server supports "memberOf"
             * if so, use it, if not, then pull all groups to check
             */
            boolean useMemberOf = false;
            String attrs[] = new String[1];
            attrs[0] = "memberOf";

            // get user dn.
            CMS.debug("UseridPwdDirAuthentication: Authenticating: Searching for " +
                    mUserIDName + "=" + uid + " base DN=" + mBaseDN);
            LDAPSearchResults res = conn.search(mBaseDN,
                    LDAPv2.SCOPE_SUB, "(" + mUserIDName +
                        "=" + uid + ")", attrs, false);

            LDAPEntry entry = null;
            if (res.hasMoreElements()) {
                //LDAPEntry entry = (LDAPEntry)res.nextElement();
                entry = res.next();

                userdn = entry.getDN();
                CMS.debug("UseridPwdDirAuthentication: Authenticating: Found User DN=" + userdn);
            } else {
                log(ILogger.LL_SECURITY, CMS.getLogMessage("CMS_AUTH_USER_NOT_EXIST", uid));
                throw new EInvalidCredentials(CMS.getUserMessage("CMS_AUTHENTICATION_INVALID_CREDENTIAL"));
            }

            LDAPAttribute attribute = entry.getAttribute(
                    "memberOf" );
            if ( attribute != null ) {
                useMemberOf = true;
                CMS.debug("UseridPwdDirAuthentication: Authenticate: Found memberOf attribute");
                String grpString = ""; // this goes into AuthToken
                Enumeration e = attribute.getStringValues();
                while ( e.hasMoreElements() ) {
                    String group = (String)e.nextElement();
                    CMS.debug("UseridPwdDirAuthentication: Authenticate: Found user is a memberOf=" + group);
                    if (grpString.length()!=0) {
                        grpString += mGroupsDelimiter;
                    }
                    grpString += group;
                }
                // set groups in string delimited by PROP_GROUPS_DELIMITER 
                if (grpString != null && !grpString.equals("")) {
                    token.set(TOKEN_GROUPS, grpString);
                    CMS.debug("UseridPwdDirAuthentication: authToken set with groups to: " + grpString);
                }
            } else {
                CMS.debug("UseridPwdDirAuthentication: Authenticate: memberOf attribute not found.");
            }

            if (useMemberOf == false) {
                // get list of groups, and get a list of those that this
                //          uid belongs to
                Enumeration groups = null;

                try {
                    groups = listGroups(conn, "*");
                } catch (Exception ex) {
                    CMS.debug("UseridPwdDirAuthentication: authenticate: failed listGroups():" + ex + ". ok to continue");
                }
                if (groups != null) {
                    String grpString = ""; // this goes into AuthToken
                    while (groups.hasMoreElements()) {
                        String group = (String) groups.nextElement();

                        CMS.debug("UseridPwdDirAuthentication: authenticate: checking to see if is member of:" + group);
                        if (isMemberOfLdapGroup(conn, (mSearchGroupUserByUserdn)? userdn:uid, group) == true) {
                            if (grpString.length()!=0) {
                                grpString += mGroupsDelimiter;
                            }
                            grpString += group;
                        }
                    }
                    // set groups in string delimited by PROP_GROUPS_DELIMITER 
                    if (grpString != null && !grpString.equals(""))
                        token.set(TOKEN_GROUPS, grpString);
                }
            } // !useMemberOf

            // set uid in the token.
            token.set(CRED_UID, uid);
            token.set(TOKEN_USERID, uid);

            return userdn;
        } catch (ELdapException e) {
            log(ILogger.LL_FAILURE, CMS.getLogMessage("CANNOT_CONNECT_LDAP", e.toString()));
            throw e;
        } catch (LDAPException e) {
            switch (e.getLDAPResultCode()) {
            case LDAPException.NO_SUCH_OBJECT: 
            case LDAPException.LDAP_PARTIAL_RESULTS: 
                log(ILogger.LL_SECURITY, CMS.getLogMessage("USER_NOT_EXIST", uid));
                throw new EInvalidCredentials(CMS.getUserMessage("CMS_AUTHENTICATION_INVALID_CREDENTIAL"));

            case LDAPException.INVALID_CREDENTIALS:
                log(ILogger.LL_SECURITY, CMS.getLogMessage("CMS_AUTH_BAD_PASSWORD", uid));
                throw new EInvalidCredentials(CMS.getUserMessage("CMS_AUTHENTICATION_INVALID_CREDENTIAL"));

            case LDAPException.SERVER_DOWN:
                log(ILogger.LL_FAILURE, CMS.getLogMessage("LDAP_SERVER_DOWN"));
                throw new ELdapException(
                        CMS.getUserMessage("CMS_LDAP_SERVER_UNAVAILABLE", conn.getHost(), "" + conn.getPort()));

            default: 
                log(ILogger.LL_FAILURE, CMS.getLogMessage("OPERATION_ERROR", e.getMessage()));
                throw new ELdapException(
                        CMS.getUserMessage("CMS_LDAP_OTHER_LDAP_EXCEPTION", 
                            e.errorCodeToString()));
            }
        } 
    }

    /**
     * Returns a list of configuration parameter names. 
     * The list is passed to the configuration console so instances of 
     * this implementation can be configured through the console.
     *
     * @return String array of configuration parameter names.
     */
    public String[] getConfigParams() {
        return (mConfigParams);
    }

    /**
     * Returns array of required credentials for this authentication manager.
     * @return Array of required credentials.
     */
    public String[] getRequiredCreds() {
        return mRequiredCreds;
    }

    // Profile-related methods
      
    public void init(IProfile profile, IConfigStore config)
        throws EProfileException {
    }

    /**
     * Retrieves the localizable name of this policy.
     */
    public String getName(Locale locale) {
        return CMS.getUserMessage(locale, "CMS_AUTHENTICATION_LDAP_UID_NAME");
    }

    /**
     * Retrieves the localizable description of this policy.
     */
    public String getText(Locale locale) {
        return CMS.getUserMessage(locale, "CMS_AUTHENTICATION_LDAP_UID_TEXT");
    }

    /**
     * Retrieves a list of names of the value parameter.
     */
    public Enumeration getValueNames() {
        Vector v = new Vector();

        v.addElement(CRED_UID);
        v.addElement(CRED_PWD);
        return v.elements();
    }

    public boolean isValueWriteable(String name) {
        if (name.equals(CRED_UID)) {
            return true;
        } else if (name.equals(CRED_PWD)) {
            return false;
        }
        return false;
    }

    /**
     * Retrieves the descriptor of the given value
     * parameter by name.
     */
    public IDescriptor getValueDescriptor(Locale locale, String name) {
        if (name.equals(CRED_UID)) { 
            return new Descriptor(IDescriptor.STRING, null, null,
                    CMS.getUserMessage(locale, "CMS_AUTHENTICATION_LDAP_UID"));
        } else if (name.equals(CRED_PWD)) {
            return new Descriptor(IDescriptor.PASSWORD, null, null,
                    CMS.getUserMessage(locale, "CMS_AUTHENTICATION_LDAP_PWD"));
	
        }
        return null;
    }

    public void populate(IAuthToken token, IRequest request) 
        throws EProfileException {
        request.setExtData(IProfileAuthenticator.AUTHENTICATED_NAME,
                token.getInString(USER_DN));
    }

    public boolean isSSLClientRequired() {
        return false;
    }
}
