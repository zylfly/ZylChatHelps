package com.zyl.zylchathelps.activity;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.zyl.zylchathelps.R;

/**
 * Author: Zhaoyl
 * Date: 2018/01/06 14:56
 * Description: 视频播放
 * Copyright: （个人所有）
 **/

public class VideoPlayerActivity extends AppCompatActivity {

    MediaController mediaController;
    VideoView vvMyPlayer;
    ProgressBar pbVideo;
    ImageView ivFirstPhoto;
    RelativeLayout rlVideo;
    Uri url;
    Uri videoImage;


    protected <T extends View> T $(@IdRes int id) {
        return (T) findViewById(id);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videoplayer);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        init();
    }


    private void init() {
        vvMyPlayer = $(R.id.vvMyPlayer);
        ivFirstPhoto = $(R.id.ivFirstPhoto);
        pbVideo = $(R.id.pbVideo);
        rlVideo = $(R.id.rlVideo);
        if (getIntent().getStringExtra("videoPath") != null) {
            url = Uri.parse(getIntent().getStringExtra("videoPath"));
            videoImage = Uri.parse(getIntent().getStringExtra("videoImage"));
        }
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        vvMyPlayer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                finish();
                return false;
            }
        });
        initData();
    }

    private void initData() {
        Glide.with(VideoPlayerActivity.this).load(videoImage).into(ivFirstPhoto);
        play(url);
    }

    private void play(final Uri path) {
        mediaController = new MediaController(this);
        vvMyPlayer.setVideoURI(path);
        // 设置VideView与MediaController建立关联
        vvMyPlayer.setMediaController(mediaController);
//        // 设置MediaController与VideView建立关联
        mediaController.setMediaPlayer(vvMyPlayer);
        mediaController.setVisibility(View.INVISIBLE);
        // 让VideoView获取焦点
//        videoView.requestFocus();
        // 开始播放
//        vvMyPlayer.start();
        vvMyPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                rlVideo.setVisibility(View.GONE);
                ivFirstPhoto.setVisibility(View.GONE);
                mp.start();
                mp.setLooping(true);
            }
        });

        vvMyPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                vvMyPlayer.setVideoURI(path);
                vvMyPlayer.start();
            }
        });

        vvMyPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {


                return false;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (vvMyPlayer != null) {
            vvMyPlayer.suspend();  //将VideoView所占用的资源释放掉
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (vvMyPlayer.isPlaying())
            vvMyPlayer.suspend();

    }

    @Override
    protected void onResume() {
        super.onResume();
        vvMyPlayer.resume();
        vvMyPlayer.start();
    }
}
