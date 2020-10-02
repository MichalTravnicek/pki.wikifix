
########################################################
# Description:
#    This data file tests enrollment operation.
#
# Execution:
#    tpsclient < enroll.test
#
########################################################
op=var_set name=ra_host value=glyph.dsdev.sjc.redhat.com
op=var_set name=ra_port value=7891
op=var_set name=ra_uri value=/nk_service
# print original token status
op=token_set cuid=a00192030405060708c9 msn=01020304 app_ver=6FBBC105 key_info=0101 major_ver=0 minor_ver=0
op=token_set auth_key=404142434445464748494a4b4c4d4e4f
op=token_set mac_key=404142434445464748494a4b4c4d4e4f
op=token_set kek_key=404142434445464748494a4b4c4d4e4f
op=token_status
#op=ra_enroll uid=test pwd=password new_pin=password
op=ra_enroll uid=pinmanager num_threads=1 pwd=netscape new_pin=netscape extensions=tokenType=userKey keygen=true slotnamefile=tokenname tokpasswd=netscape
# print changed token status
op=token_status
op=exit

