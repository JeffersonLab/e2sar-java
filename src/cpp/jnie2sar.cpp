#include "jnie2sar.hpp"


JNIEXPORT jstring JNICALL Java_org_jlab_hpdf_E2sarUtil_getE2sarVersion
  (JNIEnv *env, jclass jCallObj){
      return env->NewStringUTF(e2sar::get_Version().data());
  }

JNIEXPORT jlong JNICALL Java_org_jlab_hpdf_E2sarUtil_getTotalHeaderLength
  (JNIEnv *env, jclass jCallObj){
    return (jlong) e2sar::TOTAL_HDR_LEN;
  }



