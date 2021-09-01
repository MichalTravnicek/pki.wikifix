= Request

* Path: `/<subsystem>/rest/admin/users/{userID}`
* Method: `PATCH`
* Authentication: Client certificate
* Parameters: None
* Content: None
* Success code: `200`

= Source
https://github.com/dogtagpki/pki/blob/894293a6b00b0151120cc08f9eb508764ed24e34/base/server/src/main/java/org/dogtagpki/server/rest/UserService.java#L511-L601[UserService.modifyUser()]

= Examples

.CA Example
[%collapsible]
====
----
$ curl \
  -k \
  -X PATCH \
  -H "Content-Type:application/json" \
  -H "Accept: application/json" \
  -d '{"FullName":"foobar"}' \
  --user caadmin:Secret.123 \
  -s https://localhost.localdomain:8443/ca/rest/admin/users/asdtfg | python -m json.tool
{
    "id": "asdtfg",
    "UserID": "asdtfg",
    "FullName": "foobar",
    "Link": {
        "rel": "self",
        "href": "https://localhost.localdomain:8443/ca/rest/admin/users/asdtfg",
        "type": "application/xml"
    },
    "Attributes": {
        "Attribute": []
    }
}
----
====

.KRA Example
[%collapsible]
====
----
$ curl \
  -k \
  -X PATCH \
  -H "Content-Type:application/json" \
  -H "Accept: application/json" \
  -d '{"FullName":"foobar"}' \
  --user kraadmin:Secret.123 \
  -s https://localhost.localdomain:8443/kra/rest/admin/users/kraadmin | python -m json.tool
{
    "id": "kraadmin",
    "UserID": "kraadmin",
    "FullName": "foobar",
    "Email": "kraadmin@example.com",
    "Type": "adminType",
    "State": "1",
    "Link": {
        "rel": "self",
        "href": "https://localhost.localdomain:8443/kra/rest/admin/users/kraadmin",
        "type": "application/xml"
    },
    "Attributes": {
        "Attribute": []
    }
}
----
====

.OCSP Example
[%collapsible]
====
----
$ curl \
  -k \
  -X PATCH \
  -H "Content-Type:application/json" \
  -H "Accept: application/json" \
  -d '{"FullName":"foobar"}' \
  --user ocspadmin:Secret.123 \
  -s https://localhost.localdomain:8443/ocsp/rest/admin/users/ocspadmin | python -m json.tool
{
    "id": "ocspadmin",
    "UserID": "ocspadmin",
    "FullName": "foobar",
    "Email": "ocspadmin@example.com",
    "Type": "adminType",
    "State": "1",
    "Link": {
        "rel": "self",
        "href": "https://localhost.localdomain:8443/ocsp/rest/admin/users/ocspadmin",
        "type": "application/xml"
    },
    "Attributes": {
        "Attribute": []
    }
}
----
====

.TKS Example
[%collapsible]
====
----
$ curl \ 
  -k \
  -X PATCH \
  -H "Content-Type:application/json" \
  -H "Accept: application/json" \
  -d '{"FullName":"foobar"}' \
  --user tksadmin:Secret.123 \
  -s https://localhost.localdomain:8443/tks/rest/admin/users/tksadmin | python -m json.tool
{
    "id": "tksadmin",
    "UserID": "tksadmin",
    "FullName": "foobar",
    "Email": "tksadmin@example.com",
    "Type": "adminType",
    "State": "1",
    "Link": {
        "rel": "self",
        "href": "https://localhost.localdomain:8443/tks/rest/admin/users/tksadmin",
        "type": "application/xml"
    },
    "Attributes": {
        "Attribute": []
    }
}
----
====

= See also
The TPS subsystem has a different modify user process documented link:PKI-TPS-Modify-User-REST-API[here].