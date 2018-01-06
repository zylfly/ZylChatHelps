package com.zyl.zylchathelps.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.lqr.imagepicker.ImagePicker;
import com.lqr.imagepicker.bean.ImageItem;
import com.lqr.imagepicker.ui.ImageGridActivity;
import com.lqr.imagepicker.ui.ImagePreviewActivity;
import com.melink.bqmmsdk.bean.BQMMGif;
import com.melink.bqmmsdk.bean.Emoji;
import com.melink.bqmmsdk.sdk.BQMM;
import com.melink.bqmmsdk.sdk.BQMMMessageHelper;
import com.melink.bqmmsdk.sdk.IBqmmSendMessageListener;
import com.melink.bqmmsdk.ui.keyboard.BQMMKeyboard;
import com.melink.bqmmsdk.ui.keyboard.IGifButtonClickListener;
import com.melink.bqmmsdk.widget.BQMMEditView;
import com.melink.bqmmsdk.widget.BQMMSendButton;
import com.zyl.chathelp.audio.AudioRecordManager;
import com.zyl.chathelp.audio.IAudioRecordListener;
import com.zyl.chathelp.utils.EmotionKeyboard;
import com.zyl.chathelp.video.CameraActivity;
import com.zyl.zylchathelps.R;
import com.zyl.zylchathelps.adapter.ChatRAdapter;
import com.zyl.zylchathelps.adapter.Message;
import com.zyl.zylchathelps.bqmmgif.BQMMGifManager;
import com.zyl.zylchathelps.bqmmgif.IBqmmSendGifListener;

import org.json.JSONArray;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

/**
 * Author: Zhaoyl
 * Date: 2018/01/05 11:50
 * Description: 没有动态申请权限（自己解决）
 * Copyright: （个人所有）
 **/

public class MainActivity extends AppCompatActivity {


    public static final int REQUEST_IMAGE_PICKER = 1000;
    public final static int REQUEST_TAKE_PHOTO = 1001;
    public final static int REQUEST_MY_LOCATION = 1002;

    @Bind(R.id.llContent)
    LinearLayout mLlContent;
    @Bind(R.id.llRoot)
    LinearLayout mLlRoot;
    @Bind(R.id.ivAudio)
    ImageView mIvAudio;
    @Bind(R.id.btnAudio)
    Button mBtnAudio;
    @Bind(R.id.etContent)
    BQMMEditView mEtContent;
    @Bind(R.id.ivEmo)
    ImageView mIvEmo;
    @Bind(R.id.ivMore)
    ImageView mIvMore;
    @Bind(R.id.btnSend)
    BQMMSendButton mBtnSend;
    @Bind(R.id.rv)
    RecyclerView rv;
    @Bind(R.id.flEmotionView)
    FrameLayout mFlEmotionView;
    @Bind(R.id.bqmm_keyboard)
    BQMMKeyboard mElEmotion;
    @Bind(R.id.llMore)
    LinearLayout mLlMore;
    List<Message> datas = new ArrayList<>();

    BQMM bqmmsdk;
    private ChatRAdapter mAdapter;
    private EmotionKeyboard mEmotionKeyboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        bqmmsdk = BQMM.getInstance();
        initEmotionKeyboard();
        initListener();
        initBqsdk();
        initBqListener();
        initRecyView();
        initAudioRecordManager();
        initAudioListener();
    }

    //初始化表情云控件
    private void initBqsdk() {
        bqmmsdk.setEditView(mEtContent);
        //点击Gif的监听
        bqmmsdk.setKeyboard(mElEmotion, new IGifButtonClickListener() {
            @Override
            public void didClickGifTab() {
                closebroad();
                mEtContent.requestFocus();
                showSoftInput(mEtContent);
                mIvEmo.setImageResource(R.mipmap.ic_cheat_emo);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        BQMMGifManager.getInstance(mElEmotion.getContext()).showTrending();
                    }
                }, 500);
            }
        });
        bqmmsdk.setSendButton(mBtnSend);
        bqmmsdk.load();
        BQMMGifManager.getInstance(this).addEditViewListeners();
    }

    //初始化表情云监听
    private void initBqListener() {
        bqmmsdk.setBqmmSendMsgListener(new IBqmmSendMessageListener() {
            //单个大表情消息
            @Override
            public void onSendFace(final Emoji face) {
                Message message = new Message(Message.MSG_TYPE_FACE,
                        Message.MSG_STATE_SUCCESS, "Tom", "avatar", "Jerry",
                        "avatar", BQMMMessageHelper.getFaceMessageData(face), true, true, new Date());
                datas.add(message);
                mAdapter.refesh(datas);
                rv.smoothScrollToPosition(datas.size());
            }

            //图文混排消息（包括文字）
            @Override
            public void onSendMixedMessage(List<Object> emojis, boolean isMixedMessage) {
                final JSONArray msgCodes = BQMMMessageHelper.getMixedMessageData(emojis);
                Message message = new Message(Message.MSG_TYPE_MIXTURE,
                        Message.MSG_STATE_SUCCESS, "Tom", "avatar", "Jerry",
                        "avatar", msgCodes, true, true, new Date());
                datas.add(message);
                mAdapter.refesh(datas);
                rv.smoothScrollToPosition(datas.size());
            }
        });
        BQMMGifManager.getInstance(getBaseContext()).setBQMMSendGifListener(new IBqmmSendGifListener() {
            //gif选择回调
            @Override
            public void onSendBQMMGif(final BQMMGif bqmmGif) {
                Message message = new Message(Message.MSG_TYPE_WEBSTICKER,
                        Message.MSG_STATE_SUCCESS, "Tom", "avatar", "Jerry",
                        "avatar", null, true, true, new Date());
                message.setBqssWebSticker(bqmmGif);
                datas.add(message);
                mAdapter.refesh(datas);
                rv.smoothScrollToPosition(datas.size());
            }
        });
    }

    private void initRecyView() {
        getLinearLayoutManager();
        mAdapter = new ChatRAdapter(this, datas);
        rv.setAdapter(mAdapter);
    }

    //设置线性布局的管理者
    private void getLinearLayoutManager() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rv.setLayoutManager(linearLayoutManager);
    }

    private void initEmotionKeyboard() {
        mEmotionKeyboard = EmotionKeyboard.with(this);
        mEmotionKeyboard.bindToEditText(mEtContent);
        mEmotionKeyboard.bindToContent(mLlContent);
        mEmotionKeyboard.setEmotionLayout(mFlEmotionView);
        mEmotionKeyboard.bindToEmotionButton(mIvEmo, mIvMore);
        mEmotionKeyboard.setOnEmotionButtonOnClickListener(new EmotionKeyboard.OnEmotionButtonOnClickListener() {
            @Override
            public boolean onEmotionButtonOnClickListener(View view) {
                switch (view.getId()) {
                    case R.id.ivEmo:
                        if (!mElEmotion.isShown()) {
                            if (mLlMore.isShown()) {
                                showEmotionLayout();
                                hideMoreLayout();
                                hideAudioButton();
                                return true;
                            }
                        } else if (mElEmotion.isShown() && !mLlMore.isShown()) {
                            mIvEmo.setImageResource(R.mipmap.ic_cheat_emo);
                            return false;
                        }
                        showEmotionLayout();
                        hideMoreLayout();
                        hideAudioButton();
                        break;
                    case R.id.ivMore:
                        if (!mLlMore.isShown()) {
                            if (mElEmotion.isShown()) {
                                showMoreLayout();
                                hideEmotionLayout();
                                hideAudioButton();
                                return true;
                            }
                        }
                        showMoreLayout();
                        hideEmotionLayout();
                        hideAudioButton();
                        break;
                }
                return false;
            }
        });
    }

    public void initListener() {
        rv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        closeBottomAndKeyboard();
                        mEmotionKeyboard.hideSoftInput();
                        mIvEmo.setImageResource(R.mipmap.ic_cheat_emo);
                        break;
                }
                return false;
            }
        });
        mIvAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBtnAudio.isShown()) {
                    hideAudioButton();
                    mEtContent.requestFocus();
                    if (mEmotionKeyboard != null) {
                        mEmotionKeyboard.showSoftInput();
                    }
                } else {
                    showAudioButton();
                    hideEmotionLayout();
                    hideMoreLayout();
                }
            }
        });
        mEtContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mEtContent.getText().toString().trim().length() > 0) {
                    mBtnSend.setVisibility(View.VISIBLE);
                    mIvMore.setVisibility(View.GONE);
                } else {
                    mBtnSend.setVisibility(View.GONE);
                    mIvMore.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    //-------------------------------------------------语音--------------------------------------------------------------

    //初始化录音模块
    private void initAudioRecordManager() {
        AudioRecordManager.getInstance(this).setMaxVoiceDuration(60);
        final File audioDir = new File(Environment.getExternalStorageDirectory(), "AUDIO");
        if (!audioDir.exists()) {
            audioDir.mkdirs();
        }
        AudioRecordManager.getInstance(this).setAudioSavePath(audioDir.getAbsolutePath());
        AudioRecordManager.getInstance(this).setAudioRecordListener(new IAudioRecordListener() {

            private TextView mTimerTV;
            private TextView mStateTV;
            private ImageView mStateIV;
            private PopupWindow mRecordWindow;

            @Override
            public void initTipView() {
                View view = View.inflate(MainActivity.this, R.layout.popup_audio_wi_vo, null);
                mStateIV = (ImageView) view.findViewById(R.id.rc_audio_state_image);
                mStateTV = (TextView) view.findViewById(R.id.rc_audio_state_text);
                mTimerTV = (TextView) view.findViewById(R.id.rc_audio_timer);
                mRecordWindow = new PopupWindow(view, -1, -1);
                mRecordWindow.showAtLocation(mLlRoot, 17, 0, 0);
                mRecordWindow.setFocusable(true);
                mRecordWindow.setOutsideTouchable(false);
                mRecordWindow.setTouchable(false);
            }

            @Override
            public void setTimeoutTipView(int counter) {
                if (this.mRecordWindow != null) {
                    this.mStateIV.setVisibility(View.GONE);
                    this.mStateTV.setVisibility(View.VISIBLE);
                    this.mStateTV.setText(R.string.voice_rec);
                    this.mStateTV.setBackgroundResource(R.drawable.bg_voice_popup);
                    this.mTimerTV.setText(String.format("%s", new Object[]{Integer.valueOf(counter)}));
                    this.mTimerTV.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void setRecordingTipView() {
                if (this.mRecordWindow != null) {
                    this.mStateIV.setVisibility(View.VISIBLE);
                    this.mStateIV.setImageResource(R.mipmap.ic_volume_1);
                    this.mStateTV.setVisibility(View.VISIBLE);
                    this.mStateTV.setText(R.string.voice_rec);
                    this.mStateTV.setBackgroundResource(R.drawable.bg_voice_popup);
                    this.mTimerTV.setVisibility(View.GONE);
                }
            }

            @Override
            public void setAudioShortTipView() {
                if (this.mRecordWindow != null) {
                    mStateIV.setImageResource(R.mipmap.ic_volume_wraning);
                    mStateTV.setText(R.string.voice_short);
                }
            }

            @Override
            public void setCancelTipView() {
                if (this.mRecordWindow != null) {
                    this.mTimerTV.setVisibility(View.GONE);
                    this.mStateIV.setVisibility(View.VISIBLE);
                    this.mStateIV.setImageResource(R.mipmap.ic_volume_cancel);
                    this.mStateTV.setVisibility(View.VISIBLE);
                    this.mStateTV.setText(R.string.voice_cancel);
                    this.mStateTV.setBackgroundResource(R.drawable.corner_voice_style);
                }
            }

            @Override
            public void destroyTipView() {
                if (this.mRecordWindow != null) {
                    this.mRecordWindow.dismiss();
                    this.mRecordWindow = null;
                    this.mStateIV = null;
                    this.mStateTV = null;
                    this.mTimerTV = null;
                }
            }

            @Override
            public void onStartRecord() {//开始发送的状态
            }

            @Override
            public void onFinish(Uri audioPath, int duration) {
                //发送文件
                File file = new File(audioPath.getPath());
                if (!file.exists() || file.length() == 0L) {
                    return;
                }
                JSONArray audioJson = new JSONArray();
                audioJson.put(audioPath);
                audioJson.put(duration);
                Message message = new Message(Message.MSG_TYPE_AUDIO,
                        Message.MSG_STATE_SUCCESS, "Tom", "avatar", "Jerry",
                        "avatar", audioJson, true, true, new Date());
                datas.add(message);
                mAdapter.refesh(datas);
                rv.smoothScrollToPosition(datas.size());
            }

            @Override
            public void onAudioDBChanged(int db) {
                switch (db / 5) {
                    case 0:
                        this.mStateIV.setImageResource(R.mipmap.ic_volume_1);
                        break;
                    case 1:
                        this.mStateIV.setImageResource(R.mipmap.ic_volume_2);
                        break;
                    case 2:
                        this.mStateIV.setImageResource(R.mipmap.ic_volume_3);
                        break;
                    case 3:
                        this.mStateIV.setImageResource(R.mipmap.ic_volume_4);
                        break;
                    case 4:
                        this.mStateIV.setImageResource(R.mipmap.ic_volume_5);
                        break;
                    case 5:
                        this.mStateIV.setImageResource(R.mipmap.ic_volume_6);
                        break;
                    case 6:
                        this.mStateIV.setImageResource(R.mipmap.ic_volume_7);
                        break;
                    default:
                        this.mStateIV.setImageResource(R.mipmap.ic_volume_8);
                }
            }
        });
    }

    //录音监听
    private void initAudioListener() {
        mBtnAudio.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        AudioRecordManager.getInstance(MainActivity.this).startRecord();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (isCancelled(view, motionEvent)) {
                            AudioRecordManager.getInstance(MainActivity.this).willCancelRecord();
                        } else {
                            AudioRecordManager.getInstance(MainActivity.this).continueRecord();
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        AudioRecordManager.getInstance(MainActivity.this).stopRecord();
                        AudioRecordManager.getInstance(MainActivity.this).destroyRecord();
                        break;
                }
                return false;
            }
        });
    }

    private boolean isCancelled(View view, MotionEvent event) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);

        if (event.getRawX() < location[0] || event.getRawX() > location[0] + view.getWidth()
                || event.getRawY() < location[1] - 40) {
            return true;
        }

        return false;
    }


    //----------------------------------------------------切换---------------------------------------------------------------------

    //表情键盘软键盘切换相关 start
    private void closebroad() {
        if (mFlEmotionView.isShown()) {
            if (mEmotionKeyboard != null) {
                mEmotionKeyboard.interceptBackPress();
            }
        } else {
            if (mEmotionKeyboard != null) {
                mEmotionKeyboard.hideSoftInput();
            }
        }
    }

    private void showSoftInput(View view) {
        view.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, 0);
    }

    private void hideSoftInput(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mEtContent.clearFocus();
    }

    private void showAudioButton() {
        mBtnAudio.setVisibility(View.VISIBLE);
        mEtContent.setVisibility(View.GONE);
        mIvAudio.setImageResource(R.mipmap.ic_cheat_keyboard);

        if (mFlEmotionView.isShown()) {
            if (mEmotionKeyboard != null) {
                mEmotionKeyboard.interceptBackPress();
            }
        } else {
            if (mEmotionKeyboard != null) {
                mEmotionKeyboard.hideSoftInput();
            }
        }
    }

    private void hideAudioButton() {
        mBtnAudio.setVisibility(View.GONE);
        mEtContent.setVisibility(View.VISIBLE);
        mIvAudio.setImageResource(R.mipmap.ic_cheat_voice);
    }

    private void showEmotionLayout() {
        mElEmotion.setVisibility(View.VISIBLE);
        mIvEmo.setImageResource(R.mipmap.ic_cheat_keyboard);
    }

    private void hideEmotionLayout() {
        mElEmotion.setVisibility(View.GONE);
        mIvEmo.setImageResource(R.mipmap.ic_cheat_emo);
    }

    private void showMoreLayout() {
        mLlMore.setVisibility(View.VISIBLE);
    }

    private void hideMoreLayout() {
        mLlMore.setVisibility(View.GONE);
    }

    private void closeBottomAndKeyboard() {
        mElEmotion.setVisibility(View.GONE);
        mLlMore.setVisibility(View.GONE);
        if (mEmotionKeyboard != null) {
            mEmotionKeyboard.interceptBackPress();
        }
    }

    //------------------------------------------其他----------------------------------------------------

    @Override
    public void onBackPressed() {
        if (mElEmotion.isShown() || mLlMore.isShown()) {
            mEmotionKeyboard.interceptBackPress();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bqmmsdk.destroy();
    }

    @OnClick({R.id.rlAlbum, R.id.rlTakePhoto, R.id.rlLocation, R.id.rlRedPacket})
    public void onclick(View v) {
        switch (v.getId()) {
            case R.id.rlAlbum:
                Toast.makeText(this, "相册", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, ImageGridActivity.class);
                startActivityForResult(intent, REQUEST_IMAGE_PICKER);
                break;
            case R.id.rlTakePhoto:
                Toast.makeText(this, "拍摄", Toast.LENGTH_SHORT).show();
                intent = new Intent(this, CameraActivity.class);
                startActivityForResult(intent, REQUEST_TAKE_PHOTO);
                break;
            case R.id.rlLocation:
                Toast.makeText(this, "位置", Toast.LENGTH_SHORT).show();
                break;
            case R.id.rlRedPacket:
                Toast.makeText(this, "红包", Toast.LENGTH_SHORT).show();
                break;

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_IMAGE_PICKER:
                if (resultCode == ImagePicker.RESULT_CODE_ITEMS) {//返回多张照片
                    if (data != null) {
                        //是否发送原图
                        boolean isOrig = data.getBooleanExtra(ImagePreviewActivity.ISORIGIN, false);
                        ArrayList<ImageItem> images = (ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
                        Log.e("AAA", isOrig ? "发原图" : "不发原图");//若不发原图的话，需要在自己在项目中做好压缩图片算法
                        for (ImageItem imageItem : images) {
                            File imageFileSource;
                            if (isOrig) {
                                imageFileSource = new File(imageItem.path);
                                JSONArray imageJson = new JSONArray();
                                imageJson.put("image");
                                imageJson.put(Uri.fromFile(imageFileSource));
                                Message message = new Message(Message.MSG_TYPE_IMAGE,
                                        Message.MSG_STATE_SUCCESS, "Tom", "avatar", "Jerry",
                                        "avatar", imageJson, true, true, new Date());
                                datas.add(message);
                                mAdapter.refesh(datas);
                                rv.smoothScrollToPosition(datas.size());
                            } else {
                                //鲁班压缩图片
                                compressWithLs(new File(imageItem.path));
                                //压缩图片
                                //imageFileSource = ImageUtils.genThumbImgFile(imageItem.path);
                            }
                        }
                    }
                }
            case REQUEST_TAKE_PHOTO:
                if (resultCode == 101) {//拍照
                    String path = data.getStringExtra("path");
                    File imageFileSource = new File(path);
                    if (path != null) {
                        JSONArray imageJson = new JSONArray();
                        imageJson.put("image");
                        imageJson.put(Uri.fromFile(imageFileSource));
                        Message message = new Message(Message.MSG_TYPE_IMAGE,
                                Message.MSG_STATE_SUCCESS, "Tom", "avatar", "Jerry",
                                "avatar", imageJson, true, true, new Date());
                        datas.add(message);
                        mAdapter.refesh(datas);
                        rv.smoothScrollToPosition(datas.size());
                    }
                } else if (resultCode == 102) {//拍视频
                    String path = data.getStringExtra("path");
                    String bitmap = data.getStringExtra("bitmap");
                    File videoFileSource = new File(path);
                    File imageFileSource = new File(bitmap);
                    if (path != null) {
                        JSONArray imageJson = new JSONArray();
                        imageJson.put("video");
                        imageJson.put(Uri.fromFile(imageFileSource));
                        imageJson.put(Uri.fromFile(videoFileSource));
                        Message message = new Message(Message.MSG_TYPE_IMAGE,
                                Message.MSG_STATE_SUCCESS, "Tom", "avatar", "Jerry",
                                "avatar", imageJson, true, true, new Date());
                        datas.add(message);
                        mAdapter.refesh(datas);
                        rv.smoothScrollToPosition(datas.size());
                    }
                } else if (resultCode == 103) {
                    Toast.makeText(this, "请检查相机权限~", Toast.LENGTH_SHORT).show();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);

    }

    //压缩
    private void compressWithLs(File file) {
        Luban.with(this)
                .load(file)                     //传人要压缩的图片
                .setCompressListener(new OnCompressListener() { //设置回调
                    @Override
                    public void onStart() {
                    }

                    @Override
                    public void onSuccess(File file) {
                        if (file != null) {
                            JSONArray imageJson = new JSONArray();
                            imageJson.put("image");
                            imageJson.put(Uri.fromFile(file));
                            Message message = new Message(Message.MSG_TYPE_IMAGE,
                                    Message.MSG_STATE_SUCCESS, "Tom", "avatar", "Jerry",
                                    "avatar", imageJson, true, true, new Date());
                            datas.add(message);
                            mAdapter.refesh(datas);
                            rv.smoothScrollToPosition(datas.size());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                    }
                }).launch();
    }


}
