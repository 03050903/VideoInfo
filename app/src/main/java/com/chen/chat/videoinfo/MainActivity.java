package com.chen.chat.videoinfo;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.util.TimeUtils;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.icu.util.TimeUnit;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private static final String base_dir="/sdcard/myvideo/video/";
    private static final String tag="chen_ff";
    private String fileName = "";
    private String outName = "";
    private String startTime;
    private TextView fileNameTextView;
    private String endTime;
    private EditText startEditText;
    private EditText endEditText;
    private EditText pathEditText;
    private TextView resultTextView;
    private CheckBox pathCheckBox;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startEditText=findViewById(R.id.et_start);
        pathEditText=findViewById(R.id.et_path);
        endEditText=findViewById(R.id.et_end);
        fileNameTextView=findViewById(R.id.tv_file_name);
        resultTextView=findViewById(R.id.tv_result);
        pathCheckBox=findViewById(R.id.cb_path);
        findViewById(R.id.btn_edit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editFileName();
            }
        });
        findViewById(R.id.btn_edit_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteFileName(new File(base_dir));
            }
        });
        findViewById(R.id.btn_select).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectFile();
            }
        });
        findViewById(R.id.btn_split).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                splitFile();
            }
        });




    }

    private void deleteFileName(File dirPath) {
        resultTextView.setText("开始删除文件后缀");
        File[] files = dirPath.listFiles();
        for (File file:files){
            if (file.isDirectory()){
                deleteFileName(file);
                continue;
            }
            String[] split = file.getName().split("\\.");
            if (split.length==2){
                file.renameTo(new File(dirPath.getAbsolutePath(),split[0]));
                Log.i("universal","已经修改了文件名:"+file.getName());
            }
        }
        resultTextView.setText("删除文件后缀成功");
    }

    private void editFileName() {
        resultTextView.setText("开始修改文件名");
        File cacheDir = new File(base_dir);
        File[] files = cacheDir.listFiles();
        for (File file:files){
            if (file.isDirectory()||file.length()<1024*1024*10){
                continue;
            }
            String[] split = file.getName().split("\\.");
            if (split.length==1){
                file.renameTo(new File(file.getAbsolutePath()+".mp4"));
                Log.i("universal","已经修改了文件名:"+file.getName());
            }
        }
        resultTextView.setText("修改文件名成功");
    }

    private void splitFile() {
        String path = pathEditText.getText().toString();
        if (!TextUtils.isEmpty(path)&&pathCheckBox.isChecked()){
            fileName=path;
        }
        outName=getOutFileName();
        fileNameTextView.setText("原文件:"+fileName+"\n输出文件:"+outName);
        if (TextUtils.isEmpty(fileName)||TextUtils.isEmpty(outName)){
            return ;
        }
        getTimeFormat();
        boolean has = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED;
        if (has) {
            Log.i(tag, "申请权限");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        } else {
            split();
        }

    }
    private void getTimeFormat(){
        String startText = startEditText.getText().toString();
        String endText=endEditText.getText().toString();
        if (TextUtils.isEmpty(startText)||TextUtils.isEmpty(endText)){
            return ;
        }
        String[] split = startText.split("\\.");
        int startSize=0;
        for (int i=0;i<split.length;i++){
            startSize+=Math.pow(60,split.length-1-i)*Integer.valueOf(split[i]);
        }
        Log.i(tag,"start size:"+startSize);
        String[] endSplit = endText.split("\\.");
        int endSize=0;
        for (int i=0;i<endSplit.length;i++){
            endSize+=(int)(Math.pow(60,endSplit.length-1-i)*Integer.valueOf(endSplit[i]));
        }
        Log.i(tag,"end size:"+endSize);
        int diff = endSize - startSize;
        startTime=getTimeInfo(startSize);
        endTime=getTimeInfo(diff);


    }
    private String getTimeInfo(int time){
        int hour=time/3600;
        StringBuilder builder=new StringBuilder();
        if (hour<10){
            builder.append("0");
        }
        builder.append(hour);
        builder.append(":");
        int minute=(time%3600)/60;
        if (minute<10){
            builder.append("0");
        }
        builder.append(minute);
        builder.append(":");
        int second=time%60;
        if (second<10){
            builder.append("0");
        }
        builder.append(second);
        return builder.toString();

    }

    private void selectFile() {
        File cache = new File(base_dir);
        File[] files = cache.listFiles();
        final ArrayList<String> splitFiles=new ArrayList<>();
        for (File itemFile:files){
            if (itemFile.isDirectory()){
                continue;
            }
            splitFiles.add(itemFile.getName());
        }

        ArrayAdapter<String> arrayAdapter=new ArrayAdapter(this,android.R.layout.simple_list_item_1,splitFiles );
        AlertDialog.Builder builder=new AlertDialog.Builder(this).setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                fileName=base_dir+splitFiles.get(which);


                fileNameTextView.setText("原文件:"+fileName+"\n输出文件:"+outName);
            }
        });
        builder.create().show();
    }
    private String getOutFileName(){
        File cache = new File(base_dir);
        File splitDir=new File(cache,"split_new");
        if (!splitDir.exists()){
            splitDir.mkdirs();
        }
        File file=new File(fileName);
        int count=0;
        while(true){
            String name=splitDir.getAbsolutePath()+"/"+file.getName().split("\\.")[0]+"_"+count+".mp4";
            File nameFile=new File(name);
            Log.i("universal","out name :"+name);
            if (nameFile.exists()){
                count++;
                continue;
            }
            return name;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            int i = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            if (i == PackageManager.PERMISSION_GRANTED) {
                split();
            }
        }
    }

    private void split() {
        resultTextView.setText("开始切割");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
//                    String[] cmd=new String[12];
//                    cmd[0]="ffmpeg";
//                    cmd[1]="-ss";
//                    cmd[2]= startTime;
//                    cmd[3]="-t";
//                    cmd[4]= endTime;
//                    cmd[5]="-i";
//                    cmd[6]= fileName;
//                    cmd[7]= "-vcodec";
//                    cmd[8]= "copy";
//                    cmd[9]= "-acodec";
//                    cmd[10]= "copy";
//                    cmd[11]= outName;
//                    int decode = decodeArray(cmd);

                    Message message = handler.obtainMessage();
                    long start=System.currentTimeMillis();
                    int decode = decode(fileName, outName, startTime, endTime);
                    long end=System.currentTimeMillis();
                    message.what=decode;
                    message.obj=fileName;
                    message.arg1=(int)(end-start);
                    Log.i("universal","切割结果:"+decode);
                    handler.sendMessage(message);
                } catch (Throwable e) {
                    Log.i("universal", "msg:" + e.getMessage());
                }

            }
        }).start();
    }
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            String fileName=msg.obj.toString();
            int time=msg.arg1;
            if (msg.what==0){
                File outFile=new File(outName);
                resultTextView.setText(String.format(fileName+"\n切割成功\n耗时："+time+"\n文件大小:%.2fM",outFile.length()*1.0f/1014/1024));
            }else{
                resultTextView.setText(fileName+"\n切割失败\n耗时："+time);
            }
        }
    };
    private  int decode(String fileName, String outName, String start, String end){
        try{
            return VideoManager.splitVideo(fileName,outName,start,end);
        }catch (Exception e){
            Log.i(tag,"===============");
            e.printStackTrace();
            Log.i(tag,"error:"+e.getMessage()+","+e.getClass());
            return -2;
        }

    }
}
