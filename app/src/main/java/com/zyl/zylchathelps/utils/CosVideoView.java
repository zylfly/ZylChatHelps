package com.zyl.zylchathelps.utils;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.VideoView;

/**
 * Author: Zhaoyl
 * Date: 2018/01/06 16:20
 * Description: 自定义videoView（解决华为手机播放不能全屏）
 * Copyright: （个人所有）
 **/
public class CosVideoView extends VideoView {
    public CosVideoView(Context context) {
        super(context);

    }
    public CosVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }
    public CosVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //主要方法在这里
        int width = getDefaultSize(0, widthMeasureSpec);
        int height = getDefaultSize(0, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    public void setOnPreparedListener(MediaPlayer.OnPreparedListener l) {
        super.setOnPreparedListener(l);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }
}
