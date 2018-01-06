package com.zyl.zylchathelps.adapter;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.melink.baseframe.utils.DensityUtils;
import com.melink.baseframe.utils.StringUtils;
import com.melink.bqmmsdk.sdk.BQMMMessageHelper;
import com.melink.bqmmsdk.widget.BQMMMessageText;
import com.zyl.chathelp.audio.AudioPlayManager;
import com.zyl.chathelp.audio.IAudioPlayListener;
import com.zyl.chathelp.utils.BubbleImageView;
import com.zyl.zylchathelps.R;
import com.zyl.zylchathelps.activity.ShowBigImageActivity;
import com.zyl.zylchathelps.activity.VideoPlayerActivity;
import com.zyl.zylchathelps.utils.VH;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import static com.zyl.zylchathelps.adapter.Message.MSG_TYPE_AUDIO;
import static com.zyl.zylchathelps.adapter.Message.MSG_TYPE_FACE;
import static com.zyl.zylchathelps.adapter.Message.MSG_TYPE_IMAGE;
import static com.zyl.zylchathelps.adapter.Message.MSG_TYPE_MIXTURE;
import static com.zyl.zylchathelps.adapter.Message.MSG_TYPE_WEBSTICKER;


/**
 * Author: Zhaoyl
 * Date: 2018/01/04 16:26
 * Description: 聊天适配器
 * PackageName: ChatRAdapter
 * Copyright: 同城科技
 **/

public class ChatRAdapter extends RecyclerView.Adapter<VH> {

    List<Message> datas = null;
    Context mContext;

    private static final int SEND_TEXT_EMOJI = R.layout.bqmm_chat_item_list_right;
    private static final int RECEIVE_TEXT_EMOJI = R.layout.bqmm_chat_item_list_left;
    private static final int SEND_AUDIO = R.layout.bqmm_chat_item_list_audio;
    private static final int RECEIVE_AUDIO = R.layout.bqmm_chat_item_list_left;
    private static final int SEND_IMAGE = R.layout.bqmm_chat_item_list_image;
    private static final int RECEIVE_IMAGE = R.layout.bqmm_chat_item_list_left;

    public ChatRAdapter(Context context, List<Message> data) {
        this.mContext = context;
        if (data == null) {
            data = new ArrayList<>();
        }
        this.datas = data;
    }

    public void refesh(List<Message> data) {
        if (data == null) {
            data = new ArrayList<>();
        }
        this.datas = data;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        int type = datas.get(position).getType();
        boolean isSend = datas.get(position).getIsSend();
        if (type == MSG_TYPE_FACE || type == MSG_TYPE_MIXTURE || type == MSG_TYPE_WEBSTICKER) {
            return isSend ? SEND_TEXT_EMOJI : RECEIVE_TEXT_EMOJI;
        } else if (type == MSG_TYPE_AUDIO) {
            return isSend ? SEND_AUDIO : RECEIVE_AUDIO;
        } else if (type == MSG_TYPE_IMAGE) {
            return isSend ? SEND_IMAGE : RECEIVE_IMAGE;
        } else {
            return 99999;
        }
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        VH vh = null;
        switch (viewType) {
            case SEND_TEXT_EMOJI:
                vh = new EmojiViewSendHolder(LayoutInflater.from(mContext).inflate(SEND_TEXT_EMOJI, parent, false));
                break;
            case RECEIVE_TEXT_EMOJI:
                vh = new EmojiViewRHolder(LayoutInflater.from(mContext).inflate(RECEIVE_TEXT_EMOJI, parent, false));
                break;
            case SEND_AUDIO:
                vh = new AudioViewRHolder(LayoutInflater.from(mContext).inflate(SEND_AUDIO, parent, false));
                break;
            case SEND_IMAGE:
                vh = new ImageViewRHolder(LayoutInflater.from(mContext).inflate(SEND_IMAGE, parent, false));
                break;
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        holder.bindData(datas.get(position), position);
    }

    @Override
    public int getItemCount() {
        return datas.size() > 0 ? datas.size() : 0;
    }

    public class EmojiViewSendHolder extends VH {

        ImageView img_sendfail;
        ProgressBar progress;
        TextView tv_date;
        BQMMMessageText message;

        public EmojiViewSendHolder(View itemView) {
            super(itemView);
            img_sendfail = $(R.id.chat_item_fail);
            progress = $(R.id.chat_item_progress);
            tv_date = $(R.id.chat_item_date);
            message = $(R.id.chat_item_content);
            message.setStickerSize(DensityUtils.dip2px(100));
            message.setEmojiSize(DensityUtils.dip2px(20));
            message.setUnicodeEmojiSpanSizeRatio(1.5f);//让emoji显示得比一般字符大一点
        }

        @Override
        public void bindData(Object o, int pos) {
            super.bindData(o, pos);
            Message messages = (Message) o;
            tv_date.setText(StringUtils.friendlyTime(StringUtils.getDateTime("yyyy-MM-dd " + "HH:mm:ss")));
            tv_date.setVisibility(View.VISIBLE);

            if (messages.getType() == MSG_TYPE_FACE) {//大表情
                message.showMessage(BQMMMessageHelper.getMsgCodeString(messages.getContentArray()), BQMMMessageText.FACETYPE, messages.getContentArray());
                message.getBackground().setAlpha(0);
            } else if (messages.getType() == MSG_TYPE_WEBSTICKER) {
                message.showBQMMGif(messages.getBqssWebSticker().getSticker_id(), messages.getBqssWebSticker().getSticker_url(), messages.getBqssWebSticker().getSticker_width(), messages.getBqssWebSticker().getSticker_height(), messages.getBqssWebSticker().getIs_gif());
                message.getBackground().setAlpha(0);
            } else {//小表情或文字或图文混排
                message.showMessage(BQMMMessageHelper.getMsgCodeString(messages.getContentArray()), BQMMMessageText.EMOJITYPE, messages.getContentArray());
                message.getBackground().setAlpha(255);
            }
            // 消息发送的状态
            switch (messages.getState()) {
                case Message.MSG_STATE_FAIL:
                    progress.setVisibility(View.GONE);
                    img_sendfail.setVisibility(View.VISIBLE);
                    break;
                case Message.MSG_STATE_SUCCESS:
                    progress.setVisibility(View.GONE);
                    img_sendfail.setVisibility(View.GONE);
                    break;
                case Message.MSG_STATE_SENDING:
                    progress.setVisibility(View.VISIBLE);
                    img_sendfail.setVisibility(View.GONE);
                    break;
            }
        }
    }

    public class EmojiViewRHolder extends VH {

        public EmojiViewRHolder(View itemView) {
            super(itemView);

        }
    }

    public class AudioViewRHolder extends VH {

        ImageView img_sendfail, ivAudio;
        ProgressBar progress;
        TextView tv_date, tvDuration;
        RelativeLayout rlAudio;

        public AudioViewRHolder(View itemView) {
            super(itemView);
            img_sendfail = $(R.id.chat_item_fail);
            progress = $(R.id.chat_item_progress);
            tv_date = $(R.id.chat_item_date);
            rlAudio = $(R.id.rlAudio);
            ivAudio = $(R.id.ivAudio);
            tvDuration = $(R.id.tvDuration);
        }

        @Override
        public void bindData(Object o, int pos) {
            super.bindData(o, pos);
            final Message messages = (Message) o;

            try {
                Log.e("AAA", "bindData: " + messages.getContentArray().get(0));
                Log.e("AAA", "bindData: " + messages.getContentArray().get(1));
                tvDuration.setText(messages.getContentArray().get(1) + "''");
                //int increment = getWidth(mContext) / 2 / 60 * (int) messages.getContentArray().get(1);
                int increment = (getWidth(mContext) - dip2px(165)) * (int) messages.getContentArray().get(1) / 60;

                Log.e("AAA", "bindData: -->increment = " + increment);
                ViewGroup.LayoutParams params = rlAudio.getLayoutParams();
//                params.width = dip2px(65) + dip2px(increment);
                params.width = increment + dip2px(65);
                rlAudio.setLayoutParams(params);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            // 消息发送的状态
            switch (messages.getState()) {
                case Message.MSG_STATE_FAIL:
                    progress.setVisibility(View.GONE);
                    img_sendfail.setVisibility(View.VISIBLE);
                    break;
                case Message.MSG_STATE_SUCCESS:
                    progress.setVisibility(View.GONE);
                    img_sendfail.setVisibility(View.GONE);
                    break;
                case Message.MSG_STATE_SENDING:
                    progress.setVisibility(View.VISIBLE);
                    img_sendfail.setVisibility(View.GONE);
                    break;
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AudioPlayManager.getInstance().stopPlay();
                    Uri audioUri = null;
                    try {
                        audioUri = (Uri) messages.getContentArray().get(0);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.e("AAA", audioUri.toString());
                    AudioPlayManager.getInstance().startPlay(mContext, audioUri, new IAudioPlayListener() {
                        @Override
                        public void onStart(Uri var1) {
                            if (ivAudio != null && ivAudio.getBackground() instanceof AnimationDrawable) {
                                AnimationDrawable animation = (AnimationDrawable) ivAudio.getBackground();
                                animation.start();
                            }
                        }

                        @Override
                        public void onStop(Uri var1) {
                            if (ivAudio != null && ivAudio.getBackground() instanceof AnimationDrawable) {
                                AnimationDrawable animation = (AnimationDrawable) ivAudio.getBackground();
                                animation.stop();
                                animation.selectDrawable(0);
                            }

                        }

                        @Override
                        public void onComplete(Uri var1) {
                            if (ivAudio != null && ivAudio.getBackground() instanceof AnimationDrawable) {
                                AnimationDrawable animation = (AnimationDrawable) ivAudio.getBackground();
                                animation.stop();
                                animation.selectDrawable(0);
                            }
                        }
                    });
                }
            });
        }
    }

    public class ImageViewRHolder extends VH {

        ImageView img_sendfail, ivVideo;
        ProgressBar progress;
        TextView tv_date;
        BubbleImageView bivPic;

        public ImageViewRHolder(View itemView) {
            super(itemView);

            img_sendfail = $(R.id.chat_item_fail);
            progress = $(R.id.chat_item_progress);
            tv_date = $(R.id.chat_item_date);
            bivPic = $(R.id.bivPic);
            ivVideo = $(R.id.ivVideo);

        }

        @Override
        public void bindData(Object o, int pos) {
            super.bindData(o, pos);
            final Message messages = (Message) o;
            try {
                Glide.with(mContext).load((Uri) messages.getContentArray().get(1)).error(R.mipmap.default_img_failed).override(dip2px(80), dip2px(150)).centerCrop().into(bivPic);
                ivVideo.setVisibility(messages.getContentArray().get(0).toString().equals("image") ? View.GONE : View.VISIBLE);
                bivPic.setProgressVisible(false);
                bivPic.showShadow(false);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            // 消息发送的状态
            switch (messages.getState()) {
                case Message.MSG_STATE_FAIL:
                    progress.setVisibility(View.GONE);
                    img_sendfail.setVisibility(View.VISIBLE);
                    break;
                case Message.MSG_STATE_SUCCESS:
                    progress.setVisibility(View.GONE);
                    img_sendfail.setVisibility(View.GONE);
                    break;
                case Message.MSG_STATE_SENDING:
                    progress.setVisibility(View.VISIBLE);
                    img_sendfail.setVisibility(View.GONE);
                    break;
            }

            bivPic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        if (messages.getContentArray().get(0).toString().equals("image")) {
                            Intent intent = new Intent(mContext, ShowBigImageActivity.class);
                            intent.putExtra("url", messages.getContentArray().get(1).toString());
                            if (Build.VERSION.SDK_INT > 21) {
                                mContext.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation((Activity) mContext, bivPic, "img").toBundle());
                            } else {
                                mContext.startActivity(intent);
                            }
                        } else {
                            Intent in = new Intent(mContext, VideoPlayerActivity.class);
                            in.putExtra("videoPath", messages.getContentArray().get(2).toString());
                            in.putExtra("videoImage", messages.getContentArray().get(1).toString());
                            mContext.startActivity(in);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }


    public class ViewHolder extends VH {

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }


    //获取屏幕宽度
    private int getWidth(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        return width;
    }

    //dip转像素值
    private int dip2px(double d) {
        final float scale = mContext.getResources().getDisplayMetrics().density;
        return (int) (d * scale + 0.5f);
    }
}
