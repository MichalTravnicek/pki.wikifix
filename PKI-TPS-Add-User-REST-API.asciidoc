= TPS Operation

* op=addUser

= REST Operation

* POST /tps/rest/users

= Request

* User ID
* First name
* Last name
* Roles (operator, agent, admin)
* Base-64 encoded user certificate
* Profiles
* Nonce

= Response

* HTTP 201 Created
* Location: <User URL>
* User ID
* First name
* Last name
* Roles (operator, agent, admin)
* Base-64 encoded user certificate
* Profiles
* ETag
