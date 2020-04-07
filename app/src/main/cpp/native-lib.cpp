#include <jni.h>
#include <string>
#include <pthread.h>


extern "C" {
#include <libavcodec/avcodec.h>
#include "ffmpeg.h"
    int cmd_info_length_g=0;
    char** cmd_info_g=NULL;
pthread_t ntid;
    void* thread(void*){
        if(cmd_info_g==NULL||cmd_info_length_g<1){
            return (void*) 0;
        }
        ffmpeg_command(cmd_info_length_g,cmd_info_g);
        pthread_exit((void*)"ffmpeg_thread_exit");
    }
int ffmpeg_thread_run_cmd(int cmdnum,char **argv){
    cmd_info_length_g=cmdnum;
    cmd_info_g=argv;

    int temp =pthread_create(&ntid,NULL,thread,NULL);
    if(temp!=0)
    {
        //LOGE("can't create thread: %s ",strerror(temp));
        return 1;
    }
    return 0;
}
JNIEXPORT jstring JNICALL Java_com_chen_chat_videoinfo_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = avcodec_configuration();
//    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
JNIEXPORT jint JNICALL
Java_com_chen_chat_videoinfo_VideoManager_videoCommand(JNIEnv *env, jclass clazz,
                                                       jobjectArray cmd) {
    int length=env->GetArrayLength(cmd);
    char** cmd_info=(char**)malloc(sizeof(char*)*length);
    for (int i = 0; i < length; ++i) {
        jstring item=(jstring)env->GetObjectArrayElement(cmd,i);
        const char* item_info=env->GetStringUTFChars(item,false);
        int item_info_length=strlen(item_info);
        cmd_info[i]=(char*)malloc(sizeof(char)*(item_info_length+1));
        strcpy(cmd_info[i],item_info);
        LOGE("item index %d and value is %s",i,cmd_info[i]);
        env->ReleaseStringUTFChars(item,item_info);
    }
    LOGE("cmd length is %d",length);
    int ret=ffmpeg_command(length,cmd_info);
    LOGE("cmd result is %d",ret);
    for (int i = 0; i < length; ++i) {
        free(cmd_info[i]);
    }
    free(cmd_info);
//    ffmpeg -ss %s -t %s -i %s -vcodec copy -acodec copy %s
//    char* cmd_info[]={"ffmpeg","-ss","00:00:00","-t","00:00:10","-i","/sdcard/17.mp4","-vcodec","copy","-acodec","copy","/sdcard/demo.mp4"};
//    int ret=ffmpeg_command(12,cmd_info);
    LOGE("返回的参数是：%d",ret);
    return ret;

}
}

