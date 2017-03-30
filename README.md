# CricleWaveView 自定义view来实现一个圆形水波纹进度框。
是使用自定义view实现的。代码中涉及到自定义属性、贝塞尔曲线、valueAnimator(属性动画)、xfermode等知识。使用说明如下：
布局：
<com.criclewaveview_master.CustomWaveView
        android:id="@+id/custom_circle_wave_view"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        wave:circle_color = "@color/circle_color"
        android:layout_centerInParent="true"
        wave:circle_background_color = "@color/circle_bg_color" //设置圆的背景色，默认是白色
        wave:progress_wave_color = "@color/colorAccent" //设置水纹的颜色
        wave:progress_text_size = "20sp" //设置进度的文字大小
        wave:progress_text_color = "@color/circle_color"/> //设置园的颜色
Activity：
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
Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what){
                case 0:
                   //调用start方法启动水波纹效果
                    waveView.start();
                    waveView.setCurrentProgress(currentProgress);
                    currentProgress ++;
                    break;
                default:
                    break;
            }
        }
    };

