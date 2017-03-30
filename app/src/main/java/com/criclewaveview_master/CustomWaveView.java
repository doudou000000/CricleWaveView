package com.criclewaveview_master;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Xfermode;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * 自定义圆形水波纹进度条
 */

public class CustomWaveView extends View {
    //自定义属性
    private int circle_color,circle_bg_color,wave_color,text_color;
    //字体大小
    private float textSize;
    //振幅
    private float waveRipple = 0;
    //园的半径
    private int radius = 300;
    //画笔
    private Paint mBgCirclePaint,mWavePaint,mTextPaint,mCirclePaint;
    //贝塞尔曲线
    private Path mPath;
    //是否开始测试
    private boolean isRunning = false;
    //画布
    Canvas mCanvas;
    //图片
    Bitmap mBitmap;
    //自定义view的宽和高
    int width,height;
    //贝塞尔曲线起始点
    PointF startP,nextP,threeP,fourP,endP;
    //贝塞尔曲线控制点
    PointF controllerP1,controllerP2,controllerP3,controllerP4;
    //当前进度
    private int currentProgress = 0;
    //总的进度
    private int maxProgress = 100;
    //属性动画
    ValueAnimator animator = null;
    //水位上升的高度
    private float depth;

    public CustomWaveView(Context context) {
        this(context,null);
    }

    public CustomWaveView(Context context, @Nullable AttributeSet attrs) {
        this(context,attrs,0);
    }

    public CustomWaveView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //获取view的属性
        TypedArray ta = context.obtainStyledAttributes(attrs,R.styleable.custom_wave_view_attr);
        //圆的颜色
        circle_color = ta.getColor(R.styleable.custom_wave_view_attr_circle_color,getResources().getColor(android.R.color.black));
        //圆的背景色
        circle_bg_color = ta.getColor(R.styleable.custom_wave_view_attr_circle_background_color,getResources().getColor(android.R.color.white));
        //水波纹颜色
        wave_color = ta.getColor(R.styleable.custom_wave_view_attr_progress_wave_color,getResources().getColor(android.R.color.holo_blue_dark));
        //字体的颜色
        text_color = ta.getColor(R.styleable.custom_wave_view_attr_progress_text_color,getResources().getColor(android.R.color.black));
        //字体的大小
        textSize = ta.getDimension(R.styleable.custom_wave_view_attr_progress_text_size,30f);
        //释放资源
        ta.recycle();
        initData();
    }

    /**
     * 初始化画笔等信息
     */
    private void initData() {
        //初始化背景圆画笔
        mBgCirclePaint = new Paint();
        //抗锯齿
        mBgCirclePaint.setAntiAlias(true);
        //设置背景圆的背景色
        mBgCirclePaint.setColor(circle_bg_color);
        //设置充满
        mBgCirclePaint.setStyle(Paint.Style.FILL);
        //初始化水波纹画笔
        mWavePaint = new Paint();
        //抗锯齿
        mWavePaint.setAntiAlias(true);
        //设置水波纹的背景色
        mWavePaint.setColor(wave_color);
        //设置充满
        mWavePaint.setStyle(Paint.Style.FILL);
        //使用Xfermode剪切图片
        mWavePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        //初始化圆画笔
        mCirclePaint = new Paint();
        //抗锯齿
        mCirclePaint.setAntiAlias(true);
        //设置圆的颜色
        mCirclePaint.setColor(circle_color);
        mCirclePaint.setStyle(Paint.Style.STROKE);
        //初始化字体画笔
        mTextPaint = new Paint();
        //抗锯齿
        mTextPaint.setAntiAlias(true);
        //设置字体的颜色
        mTextPaint.setColor(text_color);
        //设置字体的大小
        mTextPaint.setTextSize(textSize);
        mTextPaint.setStyle(Paint.Style.STROKE);
        //初始化贝塞尔曲线
        mPath = new Path();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if(widthMode == MeasureSpec.EXACTLY){
            width = widthSize;
        }else{
            width = radius * 2;
        }
        if(heightMode == MeasureSpec.EXACTLY){
            height = heightSize;
        }else{
            height = radius * 2;
        }
        //创建一张空白图片
        mBitmap = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        reset();
        setMeasuredDimension(width,height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //在画布上画背景圆
        mCanvas.drawCircle(width/2, height/2, radius, mBgCirclePaint);
        //设置贝塞尔曲线
        mPath.reset();
        mPath.moveTo(startP.x, startP.y);
        mPath.quadTo(controllerP1.x, controllerP1.y, nextP.x, nextP.y);
        mPath.quadTo(controllerP2.x, controllerP2.y, threeP.x, threeP.y);
        mPath.quadTo(controllerP3.x, controllerP3.y, fourP.x, fourP.y);
        mPath.quadTo(controllerP4.x, controllerP4.y, endP.x, endP.y);
        mPath.lineTo(endP.x, height);
        mPath.lineTo(-width, height);
        //绘制水波纹
        mCanvas.drawPath(mPath,mWavePaint);
        //将画好的圆绘制在画布上
        canvas.drawBitmap(mBitmap, 0, 0, null);
        //绘制圆
        canvas.drawCircle(width/2, height/2, radius, mCirclePaint);

        if(currentProgress <= 0){
            waveRipple = 0;
        }else if(currentProgress >0 && currentProgress < maxProgress){
            waveRipple = 35;
        }else if(currentProgress == maxProgress){
            waveRipple = 0;
        }else if(currentProgress > maxProgress && animator.isRunning()){
            currentProgress = maxProgress;
            animator.cancel();
        }
        //绘制进度
        String text = currentProgress + "%";
        canvas.drawText(text, (width/2 - textSize), (height/2 + textSize/2), mTextPaint);
    }

    /**
     * 开始动画
     */
    private void startAnimator() {
        animator = ValueAnimator.ofFloat(startP.x, 0);
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(2000);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                startP.x = (Float) animation.getAnimatedValue();
                startP = new PointF(startP.x, height - depth);
                nextP = new PointF(startP.x + width/2, height - depth);
                threeP = new PointF(nextP.x + width/2, height - depth);
                fourP = new PointF(threeP.x + width/2, height - depth);
                endP = new PointF(fourP.x + width/2, height - depth);
                controllerP1 = new PointF(startP.x + width/4, height - depth + waveRipple);
                controllerP2 = new PointF(nextP.x + width/4, height - depth - waveRipple);
                controllerP3 = new PointF(threeP.x + width/4, height - depth + waveRipple);
                controllerP4 = new PointF(fourP.x + width/4, height - depth - waveRipple);
                invalidate();
            }
        });
        animator.start();
    }

    /**
     * 初始化点
     */
    private void reset() {
        startP = new PointF(-width, height);
        nextP = new PointF(-width/2, height);
        threeP = new PointF(0, height);
        fourP = new PointF(width/2, height);
        endP = new PointF(width, height);

        controllerP1 = new PointF(-width/4, height);
        controllerP2 = new PointF(-width * 3/4, height);
        controllerP3 = new PointF(width/4, height);
        controllerP4 = new PointF(width * 3/4, height);
    }


    public void start(){
        if(animator == null){
            reset();
            //开启动画效果
            startAnimator();
        }
    }


    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public int getCurrentProgress() {
        return currentProgress;
    }

    public void setCurrentProgress(int currentProgress) {
        this.currentProgress = currentProgress;
        depth = (float) currentProgress / (float) maxProgress * (float) height;
    }

    public int getMaxProgress() {
        return maxProgress;
    }

    public void setMaxProgress(int maxProgress) {
        this.maxProgress = maxProgress;
    }
}
