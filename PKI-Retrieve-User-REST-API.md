= Request

* Path: `/<subsystem>/rest/admin/users`
* Method: `GET`
* Authentication: Client certificate
* Parameters:
** `filter`: string
** `start`: integer
** `size`: integer
* Content: None
* Success code: `200`

= Source
https://github.com/dogtagpki/pki/blob/0db142b60ae7c40ec82cffb7fb4d44189b4c3804/base/server/src/main/java/org/dogtagpki/server/rest/UserService.java#L176-L188[UserService.getUser()]

= Examples

.CA example
[%collapsible]
====
----
$ curl \
  -k \
  -s \
  -H "Accept: application/json" \
  --user caadmin:Secret.123 \
  https://localhost.localdomain:8443/ca/rest/admin/users | python -m json.tool
{
    "total": 3,
    "entries": [
        {
            "id": "CA-localhost.localdomain-8443",
            "UserID": "CA-localhost.localdomain-8443",
            "FullName": "CA-localhost.localdomain-8443",
            "Link": {
                "rel": "self",
                "href": "https://localhost.localdomain:8443/ca/rest/admin/users/CA-localhost.localdomain-8443",
                "type": "application/xml"
            },
            "Attributes": {
                "Attribute": []
            }
        },
        {
            "id": "caadmin",
            "UserID": "caadmin",
            "FullName": "caadmin",
            "Link": {
                "rel": "self",
                "href": "https://localhost.localdomain:8443/ca/rest/admin/users/caadmin",
                "type": "application/xml"
            },
            "Attributes": {
                "Attribute": []
            }
        },
        {
            "id": "pkidbuser",
            "UserID": "pkidbuser",
            "FullName": "pkidbuser",
            "Link": {
                "rel": "self",
                "href": "https://localhost.localdomain:8443/ca/rest/admin/users/pkidbuser",
                "type": "application/xml"
            },
            "Attributes": {
                "Attribute": []
            }
        }
    ],
    "Link": []
}
----
====
.KRA example
[%collapsible]
====
----
$ curl \
  -k \
  -s \
  -H "Accept: application/json" \
  --user kraadmin:Secret.123 \
  https://localhost.localdomain:8443/kra/rest/admin/users | python -m json.tool
{
    "total": 2,
    "entries": [
        {
            "id": "kraadmin",
            "UserID": "kraadmin",
            "FullName": "kraadmin",
            "Link": {
                "rel": "self",
                "href": "https://localhost.localdomain:8443/kra/rest/admin/users/kraadmin",
                "type": "application/xml"
            },
            "Attributes": {
                "Attribute": []
            }
        },
        {
            "id": "CA-localhost.localdomain-8443",
            "UserID": "CA-localhost.localdomain-8443",
            "FullName": "CA-localhost.localdomain-8443",
            "Link": {
                "rel": "self",
                "href": "https://localhost.localdomain:8443/kra/rest/admin/users/CA-localhost.localdomain-8443",
                "type": "application/xml"
            },
            "Attributes": {
                "Attribute": []
            }
        }
    ],
    "Link": []
}
----
====
.OCSP example
[%collapsible]
====
----
$ curl \
  -k \
  -s \
  -H "Accept: application/json" \
  --user ocspadmin:Secret.123 \
  https://localhost.localdomain:8443/ocsp/rest/admin/users | python -m json.tool
{
    "total": 2,
    "entries": [
        {
            "id": "ocspadmin",
            "UserID": "ocspadmin",
            "FullName": "ocspadmin",
            "Link": {
                "rel": "self",
                "href": "https://localhost.localdomain:8443/ocsp/rest/admin/users/ocspadmin",
                "type": "application/xml"
            },
            "Attributes": {
                "Attribute": []
            }
        },
        {
            "id": "CA-localhost.localdomain-8443",
            "UserID": "CA-localhost.localdomain-8443",
            "FullName": "CA-localhost.localdomain-8443",
            "Link": {
                "rel": "self",
                "href": "https://localhost.localdomain:8443/ocsp/rest/admin/users/CA-localhost.localdomain-8443",
                "type": "application/xml"
            },
            "Attributes": {
                "Attribute": []
            }
        }
    ],
    "Link": []
}
----
====
.TKS example
[%collapsible]
====
----
$ curl \
  -k \
  -s \
  -H "Accept: application/json" \
  --user tksadmin:Secret.123 \
  https://localhost.localdomain:8443/tks/rest/admin/users | python -m json.tool
{
    "total": 2,
    "entries": [
        {
            "id": "tksadmin",
            "UserID": "tksadmin",
            "FullName": "tksadmin",
            "Link": {
                "rel": "self",
                "href": "https://localhost.localdomain:8443/tks/rest/admin/users/tksadmin",
                "type": "application/xml"
            },
            "Attributes": {
                "Attribute": []
            }
        },
        {
            "id": "TPS-localhost.localdomain-8443",
            "UserID": "TPS-localhost.localdomain-8443",
            "FullName": "TPS localhost.localdomain 8443",
            "Link": {
                "rel": "self",
                "href": "https://localhost.localdomain:8443/tks/rest/admin/users/TPS-localhost.localdomain-8443",
                "type": "application/xml"
            },
            "Attributes": {
                "Attribute": []
            }
        }
    ],
    "Link": []
}

----
====

= See also
The TPS subsystem has a different get user process documented link:PKI-TPS-Get-User-REST-API[here].