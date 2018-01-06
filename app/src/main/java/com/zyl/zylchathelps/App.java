package com.zyl.zylchathelps;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.lqr.imagepicker.ImagePicker;
import com.lqr.imagepicker.loader.ImageLoader;
import com.lqr.imagepicker.view.CropImageView;
import com.melink.bqmmsdk.sdk.BQMM;


public class App extends Application {

    private static Context mContext;//上下文

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        try {
            Bundle bundle = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA).metaData;
            BQMM.getInstance().initConfig(this, bundle.getString("bqmm_app_id"), bundle.getString("bqmm_app_secret"));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        initImagePicker();
    }

    public static Context getContext() {
        return mContext;
    }


    /**
     * 初始化仿微信控件ImagePicker
     */
    private void initImagePicker() {
        ImagePicker imagePicker = ImagePicker.getInstance();
        imagePicker.setImageLoader(new ImageLoader() {
            @Override
            public void displayImage(Activity activity, String path, ImageView imageView, int width, int height) {
                Glide.with(mContext).load(Uri.parse("file://" + path).toString()).centerCrop().into(imageView);
            }

            @Override
            public void clearMemoryCache() {

            }
        });   //设置图片加载器
        imagePicker.setShowCamera(true);  //显示拍照按钮
        imagePicker.setCrop(true);        //允许裁剪（单选才有效）
        imagePicker.setSaveRectangle(true); //是否按矩形区域保存
        imagePicker.setSelectLimit(9);    //选中数量限制
        imagePicker.setStyle(CropImageView.Style.RECTANGLE);  //裁剪框的形状
        imagePicker.setFocusWidth(800);   //裁剪框的宽度。单位像素（圆形自动取宽高最小值）
        imagePicker.setFocusHeight(800);  //裁剪框的高度。单位像素（圆形自动取宽高最小值）
        imagePicker.setOutPutX(1000);//保存文件的宽度。单位像素
        imagePicker.setOutPutY(1000);//保存文件的高度。单位像素
    }
}
