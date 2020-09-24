= TPS Operation

* op=save_config_changes&ptype=Profiles
* op=confirm_config_changes&ptype=Profiles

= REST Operation

* PATCH /tps/rest/profiles/<Profile ID>

= Request

* Profile ID
* Contents
* ETag
* Nonce

= Response

* HTTP 200 OK
* Profile ID
* Status
* Contents
* ETag

= Access Control

* operators: denied
* agents: allowed
* admins: allowed
* default: denied
