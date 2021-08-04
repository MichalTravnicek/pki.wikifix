## Prerequisites

- Dependencies have been installed:
```
dnf install 389-ds-base dogtag-pki 
```
- A DS instance has been [created](https://github.com/dogtagpki/pki/wiki/DS-Installation).

## Create user 
Use the following command to create a user or use an existing one. 
```commandline
$ useradd <user>
```

Example:
```commandline
$ useradd sysadmin
```
## Create configuration file

Before using `pkispawn`, a config file specifying our new user must be created.
 
```
[DEFAULT]
pki_server_database_password=Secret.123
pki_user=<user>
pki_group=<user>
```

Other examples can be found [here](https://github.com/dogtagpki/pki/tree/master/base/server/examples/installation).

eg `customer_user_ca.cfg`:
```plaintext
[DEFAULT]
pki_server_database_password=Secret.123
pki_user=sysadmin
pki_group=sysadmin

[CA]
pki_admin_email=caadmin@example.com
pki_admin_name=caadmin
pki_admin_nickname=caadmin
pki_admin_password=Secret.123
pki_admin_uid=caadmin

pki_client_database_password=Secret.123
pki_client_database_purge=False
pki_client_pkcs12_password=Secret.123

pki_ds_base_dn=dc=ca,dc=pki,dc=example,dc=com
pki_ds_database=ca
pki_ds_password=Secret.123

pki_security_domain_name=EXAMPLE

pki_ca_signing_nickname=ca_signing
pki_ocsp_signing_nickname=ca_ocsp_signing
pki_audit_signing_nickname=ca_audit_signing
pki_sslserver_nickname=sslserver
pki_subsystem_nickname=subsystem
```

## Installation:
To install the PKI subsystem use:
```commandline
$ pkispawn -f <deployment configuration>
```

```commandline
$ pkispawn -f customer_user.cfg

Subsystem (CA/KRA/OCSP/TKS/TPS) [CA]: 

Begin installation (Yes/No/Quit)? yes

Loading deployment configuration from customer_user_ca.cfg.
Installation log: /var/log/pki/pki-ca-spawn.20210804154737.log
Installing CA into /var/lib/pki/pki-tomcat.

    ==========================================================================
                                INSTALLATION SUMMARY
    ==========================================================================

      Administrator's username:             caadmin
      Administrator's PKCS #12 file:
            /root/.dogtag/pki-tomcat/ca_admin_cert.p12

      Administrator's certificate nickname:
            caadmin
      Administrator's certificate database:
            /root/.dogtag/pki-tomcat/ca/alias

      To check the status of the subsystem:
            systemctl status pki-tomcatd@pki-tomcat.service

      To restart the subsystem:
            systemctl restart pki-tomcatd@pki-tomcat.service

      The URL for the subsystem is:
            https://fedora:8443/ca

      PKI instances will be enabled upon system boot

    ==========================================================================

```

