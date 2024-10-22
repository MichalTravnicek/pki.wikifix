# Introduction

PKI 9 introduced new serial number range management for requests,
certificates, and replica IDs. This allowed to automate cloning for IPA
installation. Option 3 from the original proposal drafts current CS
implementation of serial number management.

There are no known attacks on SHA2 hashes, which are supported by
PKI. Reviews of recent [attacks on cryptographic
hashes](http://www.ietf.org/rfc/rfc4270.txt) like MD5 or SHA1 suggested
that those attacks could have been prevented by making signed parts of
the certificate random enough. One of the suggested solution was to make
part of the certificate serial number unpredictable to an attacker.

[RFC 4270](http://tools.ietf.org/html/rfc4270) (written by P. Hoffman
and B. Schneier) provides three suggestions to [reduce the likelihood of
hash-based attacks](http://tools.ietf.org/html/rfc4270#section-5.1):

  - making part of the certificate serial number unpredictable to the
    attacker
  - adding a randomly chosen component to the identity
  - making the validity dates unpredictable to the attacker by skewing
    each one forwards or backwards

# Assumptions

  - Certificate serial numbers are represented by Java's [big
    integers](http://docs.oracle.com/javase/6/docs/api/index.html?java/math/BigInteger.html).
  - Automatic serial number range management developed for PKI 9 is
    suggested for continuous issuing of random certificate serial
    numbers but range management and random certificate serial numbers
    are two separate features.

# Current Serial Number Range Management

For each subsystem:

  - Current serial number management is based on assigning ranges of
    sequential serial numbers.
  - Subsystem is requesting new range when crossing below defined
    thresholds.
  - Subsystem stores information about newly acquired range once it is
    assigned to the subsystem.
  - Subsystem continues using old range till all numbers are exhausted
    from it and then it moves to the new range.
  - Cloned subsystems are synchronizing range assignment through
    replication conflicts

For new clones:

  - Part of the current range of the subsystem is transfered to new
    clone in the process of cloning.
  - New clone may request new range if transfered range is below defined
    threshold.

# Requirements

There is no reason to change existing management for request and replica
IDs.

Adding support for random certificate serial numbers should:

  - work with cloning
  - allow to solve conflicts
  - provide some level of compatibility with current serial number
    managment
  - provide some level of compatibility with current workflows for
    admins, agents and end entities (UI considerations)
  - fix all existing bugs in sequential serial number management

# Adding Random Serial Number Support

Sequential serial number assignment method will stay as default method
of serial number management due to its efficiency. Random certificate
serial numbers will be provided as selectable option.

## Technical Details

### Serial Number Assignment

CAs are assigning serial numbers to certificates in the random fashion
or sequentially using number ranges assigned to CAs. CAs are resolving
serial number conflicts between clones by using serial number ranges
assigned to them. Automatic number range management allows for continues
assigning serial numbers to certificates and requests.

By default, automatic serial number range management is only enabled for
clones only.

CAs assigning serial numbers to certificates in the random fashion
requires automatic serial number range management to be enabled.

### Range Assignment and Range Switching

Random certificate serial numbers are only available through CAs
certificate repositories, where sequential serial number management
applies to all other repositories.

  - Current range  
    Each CA has range of certificate serial numbers reserved for their
    use

> 
> 
>     dbs.beginSerialNumber=N1
>     dbs.endSerialNumber=N2

  - Serial number assigning method  
    CA can assigned serial numbers to certificates either sequentially
    or in the random fashion.
      - **Sequentially** assigned serial numbers are allocated starting
        from **N1** and ending at **N2**
      - **Randomly** assigned serial numbers are picked at random from
        range starting at **N1** and ending at **N2**

  - Serial number range request  
    CA is requesting new range when crossing a threshold called
    LowWaterMark

> 
> 
>     dbs.serialLowWaterMark=N5

  -   
    CA is requesting new range when based on value of certificate
    repository attribute **nextRange**

> 
> 
>     ldapsearch -H ldap://<hostname> -D 'cn=directory manager' -w ... -s base
>                -b 'ou=certificateRepository,ou=ca,dc=<hostname>-pki-ca' 'objectclass=*' nextRange
>     version: 1
>     dn: ou=certificateRepository,ou=ca,dc=<hostname>-pki-ca
>     nextRange: nnnnn

  - Serial number range assignment  
    New range is assigned to CA by setting

> 
> 
>     dbs.nextBeginSerialNumber=N3
>     dbs.nextEndSerialNumber=N4

  - Serial number range replacement  
    CA will switch to new range by replacing new old range limits with
    the new limits

> 
> 
>     dbs.beginSerialNumber=N3
>     dbs.endSerialNumber=N4

  -   
    and next range configuration is erased

> 
> 
>     dbs.nextBeginSerialNumber=N3
>     dbs.nextEndSerialNumber=N4

  - CA will switch to new range after exhausting all numbers from
    its current range when serial numbers are assigned sequentially.
    CA will switch to new range after crossing below defined
    thresholds (**N5/2**) which is 50% of the thresholds at which CA
    requests new range.

### Certificate Repository Extension

#### Schema Considerations

Currently CA's certificate repository is located at

`ou=certificateRepository, ou=ca, cn=dc=`<hostname>`-pki-ca`

Current repository definition:

    objectClasses: ( repository-oid NAME 'repository' DESC 'CMS defined class' SUP top STRUCTURAL
      MUST ou MAY ( serialno $ description $ nextRange $ publishingStatus ) X-ORIGIN 'user defined' )

CA's certificate repository stores description of its certificate
repository in **description** attribute. Repository **description**
attribute was previously not used. Repository **description** attribute
can be either absent or have two of the following values: **sequential**
or **random**. This attribute is used to propagate configuration changes
between clones therefore its value has higher priority than internal CA
configuration. Internal CA configuration stays unchanged in the absence
of repository **description** attribute.

#### Code Considerations

Currently methods assigning next serial numbers are in Repository
package, which makes them available to all repository types. Certificate
Repository has to be extended to provide methods assigning next random
serial numbers for certificates.

### Serial Number Assignment Method Switch

In case of cloned CAs, serial number assignment method change is based
on existing certificate repository maintenance thread, which discovers
replicated change of repository's description attribute.

Here are steps explaining serial number assignment method switching in
case of cloned CAs:

1.  Configuration UI enables or disables *random serial numbers*
2.  CA's configuration is updated and CA follows the new rule of serial
    number assignment
3.  CA's clone discovers replicated assignment method change through
    repository maintenance thread and it updates its configuration and
    serial number assignment method

<!-- end list -->

  -   
    Note: frequency of CA's repository maintenance thread is controlled
    by the following setting:

> 
> 
>     ca.certStatusUpdateInterval=<minutes>

### Random Serial Number Assignment Method

  - For random serial number assignment method next range request will
    be based on [load
    factor](http://en.wikipedia.org/wiki/Hash_table#Load_factor). [Load
    factor](http://docs.oracle.com/javase/6/docs/api/java/util/Hashtable.html)
    method allows to avoid high costs of conflict resolution which is
    currently used by [hash
    tables](http://en.wikipedia.org/wiki/Hash_tabl).
  - Store certificate in range counter allowing to calculate load
    factor.

<!-- end list -->

  -   
    Sample configuration are representing counter value and the time at
    which counter was saved:

> 
> 
>     dbs.randomSerialNumberCounter=254916538,1362519262090

  - Switching serial number assignment method to random will set random
    serial number counter value to the difference between the last
    assigned serial number in the range and the lower limit of the
    range. This simplification is assumed to avoid long certificate
    record queries.
  - Switching serial number assignment method to sequential will set
    random serial number counter value to -1.
  - To recalculate random serial number counter value:
      - stop CA
      - set random serial number counter value to -2

        >     dbs.randomSerialNumberCounter=-2

      - start CA

### CA's Configuration File

Adding support for random serial numbers will require extension of 'dbs'
section of CA's configuration file .

Here is a sample of existing configuration related to certificate serial
numbers:

> 
> 
>     dbs.enableSerialManagement=true
>     
>     dbs.beginSerialNumber=10000001
>     dbs.endSerialNumber=20000000
>     
>     dbs.nextBeginSerialNumber=30000001
>     dbs.nextEndSerialNumber=40000000
>     
>     dbs.serialIncrement=10000000
>     dbs.serialLowWaterMark=2000000
>     
>     dbs.serialCloneTransferNumber=10000
>     
>     dbs.serialRangeDN=ou=certificateRepository, ou=ranges
>     
>     dbs.serialDN=ou=certificateRepository, ou=ca

Here is a sample of configuration extension related to random
certificate serial numbers, which is exposed by console UI:

> 
> 
>     dbs.enableRandomSerialNumbers=true

Here is a sample of less common configuration extension related to
random certificate serial numbers:

> 
> 
>     dbs.randomSerialNumberCounter=5280,1362599921072
>     
>     dbs.collisionRecoveryRegenerations=3
>     dbs.collisionRecoverySteps=10
>     
>     dbs.forceModeChange=true
>     
>     dbs.minimumRandomBits=4

### Console UI Change

Existing Certificate Serial Number Configuration:

![Existing Certificate Serial Number
Configuration](CertSerialNumberConfig.png
"Existing Certificate Serial Number Configuration")

New Certificate Serial Number Configuration:

![New Certificate Serial Number Configuration](enable-ranges.png
"New Certificate Serial Number Configuration")

## How to Use Random Certificate Serial Numbers

### How to Enable Random Certificate Serial Numbers

See [Configuring CA with Random Serial Numbers v1](Configuring-CA-with-Random-Serial-Numbers-v1).

### How to Enable Random Certificate Serial Numbers during CA Configuration

Here are steps allowing to enable random certificate serial numbers
during CA configuration for PKI 9:

  - Run pkicreate
  - Stop CA
  - Update CA's CS.cfg file by setting:

> 
> 
>     dbs.enableSerialManagement=true
>     dbs.enableRandomSerialNumbers=true
>     dbs.randomSerialNumberCounter=0

  - Start CA
  - Run CA's configuration wizard

  
Random certificate serial numbers are inherited via CA cloning, so CA
clones require no special configuration.  

![CA with Random Serial Numbers](https://www.dogtagpki.org/images/8/8b/Random-syscerts.png
"CA with Random Serial Numbers")

For PKI 10 see [Installing CA with Random Serial Numbers v1](Installing-CA-with-Random-Serial-Numbers-v1).

### Agent and End Entity User Interface

Enabling random serial number assignment method breaks usability of
certificate lists on agent and end entity interfaces.

Agents and end entity may use certificate searches *showing certificates
issued during specified period of time*.

![Agent Certificate Search](AgentSearch.png "Agent Certificate Search")

![End Entity Certificate Search](EeSearch.png
"End Entity Certificate Search")

Issue [854420](https://bugzilla.redhat.com/show_bug.cgi?id=854420) -
Agent and EE certificate searches based on time have "fixed" year range
ending in 2012.

### How to Reset Random Serial Number Counter

See [Resetting Random Serial Numbers v1 Counter](Resetting-Random-Serial-Numbers-v1-Counter).

### Reverting to Normal Serial Number

See [Configuring CA with Sequential Serial Numbers](Configuring-CA-with-Sequential-Serial-Numbers).

# Issues

## Fixed Issues

  - Issue [854420](https://bugzilla.redhat.com/show_bug.cgi?id=854420) -
    Agent and EE certificate searches based on time have "fixed" year
    range ending in 2012.
  - [Issue #1066](https://github.com/dogtagpki/pki/issues/1066) - CA's
    automatic range management is broken when switching to new range in
    certificate repository.

<!-- end list -->

  -   
    Here is a sample configuration explaining this issue

> 
> 
>     dbs.beginSerialNumber=1
>     dbs.endSerialNumber=8000000
>     dbs.nextBeginSerialNumber=20000001
>     dbs.nextEndSerialNumber=30000001

  -   
    incorrectly switched to

> 
> 
>     dbs.beginSerialNumber=536870913
>     dbs.endSerialNumber=805306369

  -   
    where it should be switched to

> 
> 
>     dbs.beginSerialNumber=20000001
>     dbs.endSerialNumber=30000001

  - [Issue #1067](https://github.com/dogtagpki/pki/issues/1067) - CA's
    automatic range management is broken by creating overlapping ranges.
  - Reviewed CRL cache recovery

## Known Issues

  - Assigned replica IDs are outside ranges.
      - Original CA has replica ID 96 and it owns replica ID range 1 -
        95.
      - Clone of CA has replica ID 97 and it owns replica ID range 98 -
        100.
  - Random serial numbers require a minimum range of eight serial
    numbers.
      - [Issue #2898 - Check for minimum serial number
        range when using random serial
        numbers](https://github.com/dogtagpki/pki/issues/2898)

# See Also

  - [Random Serial Numbers](Random-Serial-Numbers "wikilink")