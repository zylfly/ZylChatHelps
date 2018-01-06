package com.zyl.zylchathelps.activity;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;

import com.bm.library.PhotoView;
import com.bumptech.glide.Glide;
import com.zyl.zylchathelps.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Author: Zhaoyl
 * Date: 2018/01/06 15:56
 * Description: 查看大图
 * Copyright: （个人所有）
 **/
public class ShowBigImageActivity extends AppCompatActivity {

    private String mUrl;
    @Bind(R.id.pv)
    PhotoView mPv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_big_image);
        ButterKnife.bind(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mUrl = getIntent().getStringExtra("url");
        if (TextUtils.isEmpty(mUrl)) {
            finish();
            return;
        }
        mPv.enable();// 启用图片缩放功能
        Glide.with(this).load(Uri.parse(mUrl)).placeholder(R.mipmap.default_image).centerCrop().into(mPv);
        mPv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                supportFinishAfterTransition();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }
}

