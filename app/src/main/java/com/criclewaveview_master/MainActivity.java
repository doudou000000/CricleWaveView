package com.criclewaveview_master;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends Activity {

    CustomWaveView waveView;

    int currentProgress = 0;
    int maxProgress = 100;
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what){
                case 0:
                    waveView.start();
                    waveView.setCurrentProgress(currentProgress);
                    currentProgress ++;
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        waveView = (CustomWaveView)findViewById(R.id.custom_circle_wave_view);
        //设置圆的半径
       waveView.setRadius(300);
        //设置进度最大值
        waveView.setMaxProgress(maxProgress);
        //设置进度的当前值
        waveView.setCurrentProgress(currentProgress);
        //模拟下载。每个
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (currentProgress < maxProgress){
                    try {
                        Thread.sleep(3000);
                        handler.sendEmptyMessage(0);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        }).start();
    }
}
