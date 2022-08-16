= Overview =

The UpdateDir service can be used to force the update of the certs/CRLs in Directory Server.

= Request =

* *Operation:* `POST /ca/agent/ca/updateDir`
* *Authentication:* Client certificate

= Response =

== XML Response ==

----
$ curl \
  -d "updateCRL=yes&updateCA=yes&xml=true" \
  --cert-type P12 --cert ~/.dogtag/pki-tomcat/ca_admin_cert.p12:Secret.123 \
  -k https://localhost.localdomain:8443/ca/agent/ca/updateDir \
  | xmllint --format -
----