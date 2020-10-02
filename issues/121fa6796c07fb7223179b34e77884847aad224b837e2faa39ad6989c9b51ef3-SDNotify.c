#include <systemd/sd-daemon.h>

#include "org_dogtagpki_systemd_SDNotify.h"

JNIEXPORT jint JNICALL
Java_org_dogtagpki_systemd_SDNotify_sd_1booted
    (JNIEnv *env, jclass cls)
{
    return sd_booted();
}

JNIEXPORT jint JNICALL
Java_org_dogtagpki_systemd_SDNotify_sd_1notify
    (JNIEnv *env, jclass cls, jint junset_environment, jstring jstate)
{
     int result;
     const char *state;

     state = (*env)->GetStringUTFChars(env, jstate, NULL);
     result = sd_notify(junset_environment, state);
     (*env)->ReleaseStringUTFChars(env, jstate, state);

     return result;
}
