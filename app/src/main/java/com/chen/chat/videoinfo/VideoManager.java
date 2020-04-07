package com.chen.chat.videoinfo;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class VideoManager {
    private static final String tag="chen_ff";
    private static void log(String msg){
        Log.i(tag,msg);
    }
public static native int videoCommand(String[] cmd);
public static int splitVideo(String inputFile,String outFile,String startTime,String endTime){
    log("input file:"+inputFile);
    log("out file:"+outFile);
    log("start time:"+startTime);
    log("end time:"+endTime);
    if (TextUtils.isEmpty(inputFile)||TextUtils.isEmpty(outFile)||TextUtils.isEmpty(startTime)||TextUtils.isEmpty(endTime)){
        log("param is null");
        return -1;
    }
    startTime=startTime.replaceAll("\\.",":");
    String format="ffmpeg -ss %s -t %s -i %s -vcodec copy -acodec copy %s";
    String cmdInfo= String.format(format, startTime, endTime, inputFile, outFile);
    String[] cmd =cmdInfo.split(" ");
    log("cmd length is :"+cmd.length);

    int ret = videoCommand(cmd);
    log("切割视频结果："+ret);
    return ret;
//
}
private static String getFormatTime(String time){
        try{
            int second=0;
            String[] split = time.split("\\.");
            for (int i=split.length-1;i>=0;--i){
                second+=Math.pow(60,i)*Integer.valueOf(split[i]);
            }
            Log.i("chen","second is "+second);
        }catch (Exception e){

        }
        return null;
}

}
