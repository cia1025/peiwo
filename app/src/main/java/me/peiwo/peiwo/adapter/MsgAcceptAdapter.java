package me.peiwo.peiwo.adapter;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.*;
import android.graphics.Color;
import android.os.Build;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.*;
import com.nostra13.universalimageloader.core.ImageLoader;
import me.peiwo.peiwo.DfineAction;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.activity.FeedFlowActivity;
import me.peiwo.peiwo.activity.FullScreenImgActivity;
import me.peiwo.peiwo.activity.MsgAcceptedMsgActivity;
import me.peiwo.peiwo.callback.DownloadCallback;
import me.peiwo.peiwo.constans.PWDBConfig;
import me.peiwo.peiwo.im.MessageModel;
import me.peiwo.peiwo.model.MsgAcceptModel;
import me.peiwo.peiwo.model.PWUserModel;
import me.peiwo.peiwo.model.VoiceModel;
import me.peiwo.peiwo.net.PWDownloader;
import me.peiwo.peiwo.service.PlayerService;
import me.peiwo.peiwo.util.*;
import me.peiwo.peiwo.util.group.ExpressionData;
import me.peiwo.peiwo.widget.PWTextViewCompat;
import pl.droidsonroids.gif.GifImageView;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MsgAcceptAdapter extends PPBaseAdapter<MsgAcceptModel> {

    public static final int VIEW_TYPE_ME = 0;
    public static final int VIEW_TYPE_OTHER = 1;
    public static final int VIEW_TYPE_ME_FEED = 2;
    public static final int VIEW_TYPE_OTHER_FEED = 3;

    public static final int VIEW_TYPE_ME_GIF = 4;
    public static final int VIEW_TYPE_OTHER_GIF = 5;

    public static final int VIEW_TYPE_ATTENTION_PROMPT = 6;

    public static final int VIEW_TYPE_ME_IMG = 7;
    public static final int VIEW_TYPE_OTHER_IMG = 8;
    public static final int VIEW_TYPE_OTHER_VOICE = 9;


    private List<MsgAcceptModel> mList;
    private LayoutInflater inflater;
    private static final int MAX_COUNT = 10;
    private ImageLoader imageLoader;
    private Context mContext;
    //private int color_id_black;
    //private int color_id_red;

    private PWUserModel meModel;
    private PWUserModel otherModel;

    private int mMaxImageWidth;
    private int mMaxImageHeight;

    private int faceSize = 0;
    private int last_location = -1;
    public static final int PLAY_STATUS_PLAYING = 1;
    public static final int PLAY_STATUS_PAUSE = 2;
    public static final int PLAY_STATUS_IDLE = 3;
    public static final int PLAY_STATUS_RESUME = 4;

    private int c_red;
    private int c_nomal;
    private int c_white;

    public enum State {
        IDLE, PLAYING, PAUSE, RESUME
    }

//	private static final DisplayImageOptions OPTIONS_F = new DisplayImageOptions.Builder()
//			.showImageOnLoading(R.drawable.head_default_f)
//			.showImageForEmpty(R.drawable.head_default_f)
//			.showImageOnFail(R.drawable.head_default_f).cacheInMemory(true)
//			.cacheOnDisk(true).displayer(new RoundedBitmapDisplayer(15))
//			.build();

    private ExpressionData expressionData;
    private int bounds;

    private ListView listView;

    public MsgAcceptAdapter(List<MsgAcceptModel> mList, Context context, ListView listView) {
        super(mList);
        c_red = Color.RED;
        c_nomal = Color.parseColor("#4d4d4d");
        c_white = Color.parseColor("#ffffff");
        this.listView = listView;
        this.mContext = context;
        this.mList = mList;
        this.inflater = LayoutInflater.from(context);
        imageLoader = ImageLoader.getInstance();
        //color_id_black = context.getResources().getColor(R.color.c_white);
        //color_id_red = context.getResources().getColor(R.color.c_white);
        this.faceSize = PWUtils.getFaceSizeFromScreen(context);

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mMaxImageWidth = wm.getDefaultDisplay().getWidth() / 5;
        mMaxImageHeight = mMaxImageWidth * 3;
        expressionData = ExpressionData.getInstance(context);
        bounds = PWUtils.getFaceSizeFromScreen(context);
    }

    @Override
    public int getViewTypeCount() {
        return MAX_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        return mList.get(position).view_type;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        FeedViewHolder feedHolder = null;
        GifViewHolder gifHolder = null;
        AttentionHolder attentionHolder = null;
        ImageViewHolder imageHolder = null;
        VoicePlayerHolder voiceHolder = null;
        final int viewType = getItemViewType(position);
        CustomLog.d("getView(), position is : " + position + ", \t viewType is : " + viewType);
        if (convertView == null) {
            switch (viewType) {
                case VIEW_TYPE_ME:
                    convertView = inflater.inflate(R.layout.activity_msgaccept_item_me, parent, false);
                    holder = new ViewHolder();
                    holder.sending_bar = (ProgressBar) convertView.findViewById(R.id.iv_pro_bar); //
                    holder.iv_remind = (ImageView) convertView.findViewById(R.id.iv_remind);  //
                    break;
                case VIEW_TYPE_OTHER:
                    convertView = inflater.inflate(R.layout.activity_msgaccept_item_other, parent, false);
                    holder = new ViewHolder();
                    break;
                case VIEW_TYPE_OTHER_FEED:
                case VIEW_TYPE_ME_FEED:
                    if (viewType == VIEW_TYPE_OTHER_FEED) {
                        convertView = inflater.inflate(R.layout.activity_msgsystem_item_redbag, parent, false);
                    } else {
                        convertView = inflater.inflate(R.layout.activity_msgsystem_item_redbag_me, parent, false);
                    }

                    feedHolder = new FeedViewHolder();
                    feedHolder.tv_redbag_content = (TextView) convertView.findViewById(R.id.tv_redbag_content);
                    feedHolder.tv_redbag_title = (TextView) convertView.findViewById(R.id.tv_redbag_title);
                    feedHolder.iv_redbag_icon = (ImageView) convertView.findViewById(R.id.iv_redbag_icon);
                    //feedHolder.iv_uface =  (ImageView) convertView.findViewById(R.id.iv_uface);
                    feedHolder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
                    feedHolder.ll_feed_content = convertView.findViewById(R.id.ll_feed_content);
                    convertView.setTag(feedHolder);
                    break;
                case VIEW_TYPE_ME_GIF:
                case VIEW_TYPE_OTHER_GIF:
                    if (viewType == VIEW_TYPE_OTHER_GIF) {
                        convertView = inflater.inflate(R.layout.activity_msgaccept_gif_other, parent, false);
                    } else {
                        convertView = inflater.inflate(R.layout.activity_msgaccept_gif_me, parent, false);
                    }

                    gifHolder = new GifViewHolder();
                    gifHolder.iv_gif_face_view = (GifImageView) convertView.findViewById(R.id.iv_gif_face_view);
                    //gifHolder.iv_uface =  (ImageView) convertView.findViewById(R.id.iv_uface);
                    gifHolder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
                    gifHolder.iv_remind = (ImageView) convertView.findViewById(R.id.iv_remind);
                    gifHolder.iv_pro_bar = (ProgressBar) convertView.findViewById(R.id.iv_pro_bar);

                    convertView.setTag(gifHolder);
                    break;
                case VIEW_TYPE_ATTENTION_PROMPT:
                    convertView = inflater.inflate(R.layout.activity_msgaccept_attention_prompt, parent, false);
                    attentionHolder = new AttentionHolder();
                    attentionHolder.attention_btn = (TextView) convertView.findViewById(R.id.attentior_btn);
                    attentionHolder.attention_icon = (ImageView) convertView.findViewById(R.id.attentior_icon);
                    attentionHolder.attention_layout = convertView.findViewById(R.id.attentior_layout);
                    attentionHolder.attentior_default_text = (TextView) convertView.findViewById(R.id.attentior_default_text);
                    convertView.setTag(attentionHolder);
                    break;
                case VIEW_TYPE_ME_IMG:
                case VIEW_TYPE_OTHER_IMG:
                    if (viewType == VIEW_TYPE_OTHER_IMG) {
                        convertView = inflater.inflate(R.layout.activity_msgaccept_img_other, parent, false);
                    } else {
                        convertView = inflater.inflate(R.layout.activity_msgaccept_img_me, parent, false);
                    }
                    imageHolder = new ImageViewHolder();
                    imageHolder.iv_image_view = (ImageView) convertView.findViewById(R.id.iv_img_view);
                    imageHolder.ll_content = (LinearLayout) convertView.findViewById(R.id.ll_content);
                    imageHolder.ll_content.setBackground(null);
                    //imageHolder.iv_uface =  (ImageView) convertView.findViewById(R.id.iv_uface);
                    imageHolder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
                    imageHolder.iv_remind = (ImageView) convertView.findViewById(R.id.iv_remind);
                    imageHolder.iv_pro_bar = (ProgressBar) convertView.findViewById(R.id.iv_pro_bar);

                    convertView.setTag(imageHolder);
                    break;
                case VIEW_TYPE_OTHER_VOICE:
                    convertView = inflater.inflate(R.layout.voice_play_layout, parent, false);
                    voiceHolder = new VoicePlayerHolder();
                    voiceHolder.iv_play_btn = (ImageView) convertView.findViewById(R.id.iv_play_btn);
                    voiceHolder.chronometer_voice = (Chronometer) convertView.findViewById(R.id.chronometer_voice);
                    voiceHolder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
                    voiceHolder.voice_player_layout = (RelativeLayout) convertView.findViewById(R.id.voice_player_layout);
                    convertView.setTag(voiceHolder);
                    break;
            }
            if (holder != null) {
                holder.iv_layout = (RelativeLayout) convertView.findViewById(R.id.iv_layout);
                //mHolder.iv_uface = (ImageView) convertView.findViewById(R.id.iv_uface);
                holder.iv_icon = (ImageView) convertView.findViewById(R.id.iv_icon);
                holder.tv_content = (PWTextViewCompat) convertView.findViewById(R.id.tv_content);
                holder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
                convertView.setTag(holder);
            }
        } else {
            switch (viewType) {
                case VIEW_TYPE_ME:
                case VIEW_TYPE_OTHER:
                    holder = (ViewHolder) convertView.getTag();
                    break;
                case VIEW_TYPE_OTHER_FEED:
                case VIEW_TYPE_ME_FEED:
                    feedHolder = (FeedViewHolder) convertView.getTag();
                    break;
                case VIEW_TYPE_ME_GIF:
                case VIEW_TYPE_OTHER_GIF:
                    gifHolder = (GifViewHolder) convertView.getTag();
                    break;
                case VIEW_TYPE_ME_IMG:
                case VIEW_TYPE_OTHER_IMG:
                    imageHolder = (ImageViewHolder) convertView.getTag();
                    break;
                case VIEW_TYPE_ATTENTION_PROMPT:
                    attentionHolder = (AttentionHolder) convertView.getTag();
                    break;
                case VIEW_TYPE_OTHER_VOICE:
                    voiceHolder = (VoicePlayerHolder) convertView.getTag();
                    break;
                default:
                    break;
            }
        }
        final MsgAcceptModel model = mList.get(position);

        if (feedHolder != null) {
            if (otherModel.uid == DfineAction.SYSTEM_UID) {
                //feedHolder.iv_uface.setImageResource(R.drawable.icon_sys);
                if (model.read_status == 0) {
                    feedHolder.tv_redbag_content.setText(model.redbag_content);
                } else {
                    feedHolder.tv_redbag_content.setText(model.redbag_extra);
                }
                String name = PWUtils.getFileNameNoEx(model.icon_name);
                int icon_id = 0;
                if (!TextUtils.isEmpty(name)) {
                    PWUtils.getResId(mContext, name, "drawable");
                }
                if (icon_id != 0) {
                    feedHolder.iv_redbag_icon.setImageResource(icon_id);
                } else {
                    imageLoader.displayImage(model.icon_url, feedHolder.iv_redbag_icon);
                }
            } else {
                if (viewType == VIEW_TYPE_OTHER_FEED) {
                    //imageLoader.displayImage(otherModel.avatar_thumbnail, feedHolder.iv_uface);
                } else {
                    //imageLoader.displayImage(meModel.avatar_thumbnail, feedHolder.iv_uface);
                }
                if (TextUtils.isEmpty(model.icon_url)) {
                    feedHolder.iv_redbag_icon.setImageResource(R.drawable.bg_font);
                } else {
                    imageLoader.displayImage(model.icon_url, feedHolder.iv_redbag_icon);
                }
                feedHolder.tv_redbag_content.setText(model.redbag_content);
            }
            feedHolder.tv_redbag_title.setText("#" + model.redbag_title + "#");
            feedHolder.tv_time.setText(TimeUtil.getMsgTimeDisplay(model.update_time, true));
            feedHolder.ll_feed_content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    Intent intent = new Intent(mContext, FeedFlowActivity.class);
                    intent.putExtra("feed_id", model.feed_id);
                    mContext.startActivity(intent);
                }
            });
//			feedHolder.iv_uface.setOnClickListener(new View.OnClickListener() {
//				@Override
//				public void onClick(View v) {
//					if (mContext instanceof MsgAcceptedMsgActivity) {
//						((MsgAcceptedMsgActivity) mContext).startUserinfoForResult(
//								viewType == VIEW_TYPE_ME || viewType == VIEW_TYPE_ME_FEED);
//					}
//				}
//			});
        } else if (gifHolder != null) {
            if (!TextUtils.isEmpty(model.displayTime)) {
                gifHolder.tv_time.setVisibility(View.VISIBLE);
                gifHolder.tv_time.setText(model.displayTime);
            } else {
                gifHolder.tv_time.setText(null);
                gifHolder.tv_time.setVisibility(View.GONE);
            }
            if (viewType == VIEW_TYPE_ME_GIF) {
                //imageLoader.displayImage(meModel.avatar_thumbnail, gifHolder.iv_uface);
                gifHolder.iv_pro_bar.setVisibility(View.GONE);
                gifHolder.iv_remind.setVisibility(View.GONE);
                if (model.send_status == MessageModel.SEND_STATUS_SUCCESS) {
                    gifHolder.iv_pro_bar.setVisibility(View.VISIBLE);
                } else if (model.send_status == MessageModel.SEND_STATUS_FAIL) {
                    gifHolder.iv_remind.setVisibility(View.VISIBLE);
                }
            } else {
                //imageLoader.displayImage(otherModel.avatar_thumbnail, gifHolder.iv_uface);
            }
            if (expressionData.getGifMappingData().containsKey(model.content)) {
                gifHolder.iv_gif_face_view.setImageResource(expressionData.getGifMappingData().get(model.content).movie_res_id);
            }
//            if (ExpressionUtil.getInstance().gifFaceMap.get(model.content) > 0) {
//                gifHolder.iv_gif_face_view.setImageResource(ExpressionUtil.getInstance().gifFaceMap.get(model.content));
//            } else {
//
//            }
//			gifHolder.iv_uface.setOnClickListener(new View.OnClickListener() {
//				@Override
//				public void onClick(View v) {
//					if (mContext instanceof MsgAcceptedMsgActivity) {
//						((MsgAcceptedMsgActivity) mContext).startUserinfoForResult(viewType == VIEW_TYPE_ME_GIF);
//					}
//				}
//			});
            gifHolder.iv_gif_face_view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View arg0) {
                    longGifMenu(model, position);
                    return true;
                }
            });
        } else if (attentionHolder != null) {
            if (model.send_status == MessageModel.SEND_STATUS_FAIL) {
                attentionHolder.attentior_default_text.setBackgroundResource(R.drawable.bg_chat_follow_n);
                attentionHolder.attention_layout.setBackgroundResource(R.drawable.btn_chat_follow_n);
                attentionHolder.attention_icon.setVisibility(View.VISIBLE);
                attentionHolder.attention_btn.setText("关注");
                attentionHolder.attention_layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
//                        ((MsgAcceptedMsgActivity) mContext).focusContact(model.id, model.dialog_id);
                        ((MsgAcceptedMsgActivity) mContext).sendVoiceRequest();
                    }
                });
            } else {
                attentionHolder.attentior_default_text.setBackgroundResource(R.drawable.bg_chat_follow_p);
                attentionHolder.attention_layout.setBackgroundResource(R.drawable.btn_chat_follow_p);
                attentionHolder.attention_icon.setVisibility(View.GONE);
                attentionHolder.attention_btn.setText("已关注");
                attentionHolder.attention_layout.setOnClickListener(null);
            }
        } else if (imageHolder != null) {
            if (!TextUtils.isEmpty(model.displayTime)) {
                imageHolder.tv_time.setVisibility(View.VISIBLE);
                imageHolder.tv_time.setText(model.displayTime);
            } else {
                imageHolder.tv_time.setText(null);
                imageHolder.tv_time.setVisibility(View.GONE);
            }
            if (viewType == VIEW_TYPE_ME_IMG) {
                //imageLoader.displayImage(meModel.avatar_thumbnail, imageHolder.iv_uface);
                imageHolder.iv_pro_bar.setVisibility(View.GONE);
                imageHolder.iv_remind.setVisibility(View.GONE);
                if (model.send_status == MessageModel.SEND_STATUS_SUCCESS) {
                    imageHolder.iv_pro_bar.setVisibility(View.VISIBLE);
                } else if (model.send_status == MessageModel.SEND_STATUS_FAIL) {
                    imageHolder.iv_remind.setVisibility(View.VISIBLE);
                }
                //fix bug No.12
//				imageHolder.iv_uface.setOnClickListener(new View.OnClickListener() {
//					@Override
//					public void onClick(View v) {
//						if (mContext instanceof MsgAcceptedMsgActivity) {
//							((MsgAcceptedMsgActivity) mContext).startUserinfoForResult(true);
//						}
//					}
//				});
            } else {
                //imageLoader.displayImage(otherModel.avatar_thumbnail, imageHolder.iv_uface);
                //fix bug No.12
//				imageHolder.iv_uface.setOnClickListener(new View.OnClickListener() {
//					@Override
//					public void onClick(View v) {
//						if (mContext instanceof MsgAcceptedMsgActivity) {
//							((MsgAcceptedMsgActivity) mContext).startUserinfoForResult(false);
//						}
//					}
//				});
            }
            int realWidth = 0;
            int realHeight = 0;
            float ratio = (float) model.imageWidth / (float) mMaxImageWidth; // 300 / 150

            realWidth = mMaxImageWidth; // 150
            realHeight = (int) (model.imageHeight / ratio); //

            ViewGroup.LayoutParams params = imageHolder.iv_image_view.getLayoutParams();
            if (realHeight > mMaxImageHeight) {
                //居中
                //imageHolder.iv_image_view.setScaleType(ScaleType.CENTER_CROP);
                params.width = realWidth;
                params.height = realWidth;
            } else {
                //等比例取图
                //imageHolder.iv_image_view.setScaleType(ScaleType.FIT_XY);
                params.width = realWidth;
                params.height = realHeight;
            }

            imageHolder.iv_image_view.setLayoutParams(params);
            if (TextUtils.isEmpty(model.local_path)) {
                imageLoader.displayImage(model.thumbnail_url, imageHolder.iv_image_view);
            } else {
                imageLoader.displayImage("file://" + model.local_path, imageHolder.iv_image_view);
            }

//			//fix bug No.12
//			imageHolder.iv_uface.setOnClickListener(new View.OnClickListener() {
//				@Override
//				public void onClick(View v) {
//					if (mContext instanceof MsgAcceptedMsgActivity) {
//						((MsgAcceptedMsgActivity) mContext).startUserinfoForResult(viewType == VIEW_TYPE_ME);
//					}
//				}
//			});
            imageHolder.iv_image_view.setOnClickListener((arg0) -> {
                        Intent it = new Intent(mContext, FullScreenImgActivity.class);
                        it.putExtra("local_path", model.local_path);
                        it.putExtra("image_url", model.image_url);
                        it.putExtra("thumbnail_url", model.thumbnail_url);
                        mContext.startActivity(it);
                    }
            );
            imageHolder.iv_image_view.setOnLongClickListener((arg0) -> {
                        longClickMenu(model, position);
                        return true;
                    }
            );
            if (imageHolder.iv_remind != null) {
                imageHolder.iv_remind.setOnClickListener((arg0) -> {
                            resendMessage(model, position);
                        }
                );
            }
        } else if (voiceHolder != null) {
            Chronometer voice_chronometer = voiceHolder.chronometer_voice;
            TextView tv_time = voiceHolder.tv_time;
            if (voice_chronometer != null && model.voice.play_status == PLAY_STATUS_IDLE) {
                String ms = PWUtils.FormatHMS(model.voice.length);
                voice_chronometer.setText(ms);
            }
            if (!TextUtils.isEmpty(model.displayTime)) {
                tv_time.setVisibility(View.VISIBLE);
                tv_time.setText(model.displayTime);
            } else {
                tv_time.setText(null);
                tv_time.setVisibility(View.GONE);
            }
            String ms = PWUtils.FormatHMS(model.voice.length);
            CustomLog.d("voice length is : " + model.voice.length);
            CustomLog.d("voice key is : " + model.voice.voice_key);
            CustomLog.d("voice url is : " + model.voice.voice_url);
            setVoiceStatus(voiceHolder.iv_play_btn, voiceHolder.chronometer_voice, model.voice.play_status, ms);
            voiceHolder.voice_player_layout.setOnClickListener(v -> actionPlay(position));
        } else {
            if (!TextUtils.isEmpty(model.displayTime)) {
                holder.tv_time.setVisibility(View.VISIBLE);
                holder.tv_time.setText(model.displayTime);
            } else {
                holder.tv_time.setText(null);
                holder.tv_time.setVisibility(View.GONE);
            }

            if (viewType == VIEW_TYPE_ME) {
                //imageLoader.displayImage(meModel.avatar_thumbnail, mHolder.iv_uface);
                holder.sending_bar.setVisibility(View.GONE);
                holder.iv_remind.setVisibility(View.GONE);
                if (model.send_status == MessageModel.SEND_STATUS_SUCCESS) {
                    holder.sending_bar.setVisibility(View.VISIBLE);
                } else if (model.send_status == MessageModel.SEND_STATUS_FAIL) {
                    holder.iv_remind.setVisibility(View.VISIBLE);
                }
                holder.iv_remind.setOnClickListener((arg0) -> {
                            resendMessage(model, position);
                        }
                );
            } else {
                if (otherModel.uid == DfineAction.SYSTEM_UID) {
                    //mHolder.iv_uface.setImageResource(R.drawable.icon_sys);
                } else {
                    //imageLoader.displayImage(otherModel.avatar_thumbnail, mHolder.iv_uface);
                }
            }
            if (MessageModel.DIALOG_TYPE_CALL_HISTORY == model.dialog_type && ("对方已拒绝".equals(model.content) || "已取消".equals(model.content) || "未接听".equals(model.content)) && viewType != VIEW_TYPE_ME) {
                holder.tv_content.setTextColor(c_red);
            } else {
                if (viewType == VIEW_TYPE_ME) {
                    holder.tv_content.setTextColor(c_white);
                } else {
                    holder.tv_content.setTextColor(c_nomal);
                }
            }
            holder.iv_layout.setVisibility(View.VISIBLE);
            switch (model.dialog_type) {
                case MessageModel.DIALOG_TYPE_CALL_HISTORY: {
                    holder.iv_icon.setVisibility(View.VISIBLE);
                    if ("对方已拒绝".equals(model.content)) {
                        holder.iv_icon.setImageResource(R.drawable.cancel_icon);
                        //holder.tv_content.setTextColor(color_id_black);
                    } else if ("已取消".equals(model.content)) {
                        holder.iv_icon.setImageResource(R.drawable.refused_icon);
                        //holder.tv_content.setTextColor(color_id_black);
                    } else if ("未接听".equals(model.content)) {
                        holder.iv_icon.setImageResource(R.drawable.not_answer_icon);
                        //holder.tv_content.setTextColor(color_id_red);
                    } else {
                        if (viewType == VIEW_TYPE_ME) {
                            holder.iv_icon.setImageResource(R.drawable.through_icon_white);
                        } else {
                            holder.iv_icon.setImageResource(R.drawable.through_icon);
                        }
                        //holder.tv_content.setTextColor(color_id_black);
                    }
                    holder.tv_content.setText(model.content);
                }
                break;
                case MessageModel.DIALOG_TYPE_DEFAULT:
                case MessageModel.DIALOG_TYPE_FOCUS: // 关注
                case MessageModel.DIALOG_TYPE_IM: {
                    holder.iv_icon.setVisibility(View.GONE);
                    //holder.tv_content.setTextColor(color_id_black);
                    //SpannableString spannableString = ExpressionUtil.getInstance().getExpressionString(model.content, faceSize);
                    holder.tv_content.setTextCompat(model.content, bounds);
                }
                break;
                case MessageModel.DIALOG_TYPE_TIP: {
                    holder.iv_layout.setVisibility(View.GONE);
                    holder.tv_time.setVisibility(View.VISIBLE);
                    holder.tv_time.setText(model.content);
                }
                break;
                case MessageModel.DIALOG_TYPE_HOTVALUE: {
                    // 没有发送权限
                    holder.iv_layout.setVisibility(View.GONE);
                    holder.tv_time.setVisibility(View.VISIBLE);
                    holder.tv_time.setText(model.content);
                }
                break;
                default:
                    holder.tv_content.setText(model.content);
                    break;
            }

//			mHolder.iv_uface.setOnClickListener(new View.OnClickListener() {
//				@Override
//				public void onClick(View v) {
//					if (mContext instanceof MsgAcceptedMsgActivity) {
//						((MsgAcceptedMsgActivity) mContext).startUserinfoForResult(viewType == VIEW_TYPE_ME);
//					}
//				}
//			});
            if (model.dialog_type != MessageModel.DIALOG_TYPE_IM) {
                holder.iv_layout.setOnLongClickListener(null);
                holder.tv_content.setOnLongClickListener(null);
            } else {
                holder.iv_layout.setOnLongClickListener((arg0) -> {
                            longClickMenu(model, position);
                            return false;
                        }
                );
                holder.tv_content.setOnLongClickListener((arg0) -> {
                            longClickMenu(model, position);
                            return true;
                        }
                );
            }
        }
        return convertView;
    }

    private void setVoiceStatus(ImageView iv_play_voice, Chronometer chronometer_voice, int play_status, String ms) {
        long baseTime = convertStrToLong(chronometer_voice.getText().toString());
        switch (play_status) {
            case PLAY_STATUS_IDLE:
                iv_play_voice.setEnabled(true);
                iv_play_voice.setImageResource(R.drawable.play_bg);
                chronometer_voice.stop();
                chronometer_voice.setBase(SystemClock.elapsedRealtime());
                chronometer_voice.setText(ms);
                break;
            case VoiceModel.PLAY_STATUS_LOADING:
                iv_play_voice.setEnabled(false);
                iv_play_voice.setImageResource(R.drawable.pause_bg);
                break;
            case PLAY_STATUS_PAUSE:
                iv_play_voice.setEnabled(true);
                iv_play_voice.setImageResource(R.drawable.play_bg);
                chronometer_voice.stop();
                break;
            case PLAY_STATUS_PLAYING:
                iv_play_voice.setEnabled(true);
                iv_play_voice.setImageResource(R.drawable.pause_bg);
                chronometer_voice.setBase(SystemClock.elapsedRealtime());
                chronometer_voice.start();
                break;
            case PLAY_STATUS_RESUME:
                iv_play_voice.setEnabled(true);
                iv_play_voice.setImageResource(R.drawable.pause_bg);
                chronometer_voice.setBase(baseTime);
                chronometer_voice.start();
                break;
            default:
                break;
        }
    }

    private void actionPlay(int location) {
        if (last_location >= 0 && last_location != location) {
            MsgAcceptModel model = mList.get(last_location);
            if (model.voice.play_status != PLAY_STATUS_IDLE) {
                model.voice.play_status = PLAY_STATUS_IDLE;
            }
        }
        last_location = location;
        MsgAcceptModel model = mList.get(location);
        switch (model.voice.play_status) {
            case PLAY_STATUS_IDLE:
                model.voice.play_status = PLAY_STATUS_PLAYING;
                preparePlayVoice(model.voice);
                break;
            case PLAY_STATUS_PLAYING:
                model.voice.play_status = PLAY_STATUS_PAUSE;
                PlayerService.getInstance().playPauseCommand();
                break;
            case PLAY_STATUS_PAUSE:
                model.voice.play_status = PLAY_STATUS_RESUME;
                PlayerService.getInstance().playResumeCommand();
                break;
            case PLAY_STATUS_RESUME:
                model.voice.play_status = PLAY_STATUS_PAUSE;
                PlayerService.getInstance().playPauseCommand();
                break;
            default:
                break;
        }
        setAutoScroll(false);
        notifyDataSetChanged();
    }

    private void preparePlayVoice(VoiceModel voice) {
        File voiceFile = new File(FileManager.getVoicePath(), Md5Util.getMd5code(voice.voice_url));
        if (voiceFile.exists() && voiceFile.length() > 0) {
            playLocalVoice(voice, voiceFile.getAbsolutePath());
        } else {
            PWDownloader downloader = PWDownloader.getInstance();
            downloader.add(voice.voice_url, voiceFile, new DownloadCallback() {
                @Override
                public void onComplete(String path) {
                    playLocalVoice(voice, voiceFile.getAbsolutePath());
                }

                @Override
                public void onFailure(String path, IOException e) {
                    voice.play_status = VoiceModel.PLAY_STATUS_IDLE;
                    notifyDataSetChanged();
                    //setAutoScroll(true);
                }
            });
        }
    }

    private void playLocalVoice(VoiceModel voice, String file_path) {
        PlayerService playerService = PlayerService.getInstance();
        playerService.setOnCompletionListener(mp -> {
            voice.play_status = VoiceModel.PLAY_STATUS_IDLE;
            notifyDataSetChanged();
            //setAutoScroll(true);
        });
        playerService.setOnErrorListener((mp, what, extra) -> {
            voice.play_status = VoiceModel.PLAY_STATUS_IDLE;
            notifyDataSetChanged();
            //setAutoScroll(true);
            return false;
        });
        playerService.playCommand(file_path, false);
    }

    private long convertStrToLong(String strTime) {
        String[] timeArray = strTime.split(":");
        long longTime = 0;
        //Time format is MM:SS
        if (timeArray.length == 2) {
            longTime = Integer.parseInt(timeArray[0]) * 1000 * 60 + Integer.parseInt(timeArray[1]) * 1000;
        }
        return SystemClock.elapsedRealtime() - longTime;
    }

    private void resendMessage(final MsgAcceptModel model, final int position) {
        new AlertDialog.Builder(mContext).setTitle("重发消息")
                .setPositiveButton("是", (arg0, arg1) -> {
                            if (mContext instanceof MsgAcceptedMsgActivity) {
                                if (model.view_type == VIEW_TYPE_ME_IMG) {
                                    ((MsgAcceptedMsgActivity) mContext).resendImg(model);
                                    //delete the failed one msg
                                    deleteMsg(model, position);
                                } else {
                                    ((MsgAcceptedMsgActivity) mContext).resendMsg(model);
                                }


                            }
                        }
                ).setNegativeButton("否", null).create().show();
    }

    private void longClickMenu(final MsgAcceptModel model, final int position) {
        String[] menuString = null;
        if (model.dialog_type == MessageModel.DIALOG_TYPE_CALL_HISTORY
                || model.dialog_type == MessageModel.DIALOG_TYPE_IMAGE_MESSAGE) {
            menuString = new String[]{"删除", "取消"};
        } else {
            menuString = new String[]{"复制", "删除", "取消"};
        }
        String name = "";
        if (model.view_type == VIEW_TYPE_ME) {
            name = meModel.name;
            if (TextUtils.isEmpty(name)) {
                name = String.valueOf(meModel.uid);
            }
        } else {
            name = otherModel.name;
            if (TextUtils.isEmpty(name)) {
                name = String.valueOf(otherModel.uid);
            }
        }
        new AlertDialog.Builder(mContext).setTitle(name)
                .setItems(menuString, (arg0, arg1) -> {
                            switch (arg1) {
                                case 0:
                                    if (model.dialog_type == MessageModel.DIALOG_TYPE_CALL_HISTORY
                                            || model.dialog_type == MessageModel.DIALOG_TYPE_IMAGE_MESSAGE) {
                                        deleteMsg(model, position);
                                    } else {
                                        ClipboardManager cm = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                                        cm.setText(model.content);
                                    }
                                    break;
                                case 1:
                                    if (model.dialog_type != MessageModel.DIALOG_TYPE_CALL_HISTORY
                                            && model.dialog_type != MessageModel.DIALOG_TYPE_IMAGE_MESSAGE) {
                                        deleteMsg(model, position);
                                    }
                                    break;
                            }
                        }
                ).create().show();
    }

    private void longGifMenu(final MsgAcceptModel model, final int position) {
        String[] menuString = new String[]{"删除", "取消"};
        String name = "";
        if (model.view_type == VIEW_TYPE_ME_GIF) {
            name = meModel.name;
            if (TextUtils.isEmpty(name)) {
                name = String.valueOf(meModel.uid);
            }
        } else {
            name = otherModel.name;
            if (TextUtils.isEmpty(name)) {
                name = String.valueOf(otherModel.uid);
            }
        }
        new AlertDialog.Builder(mContext).setTitle(name)
                .setItems(menuString, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        switch (arg1) {
                            case 0:
                                deleteMsg(model, position);
                                break;
                        }
                    }
                }).create().show();
    }

    private void deleteMsg(MsgAcceptModel model, int position) {
        int currentCount = getCount();
        if (position == currentCount - 1) {
            // 如果删除的是最后一条消息 ，需要同步更新Message表
            ContentValues values = new ContentValues();
            if (currentCount > 1) {
                values.put(PWDBConfig.MessagesTable.CONTENT, mList.get(currentCount - 2).content);
                values.put(PWDBConfig.MessagesTable.UPDATE_TIME, mList.get(currentCount - 2).update_time);
                values.put(PWDBConfig.MessagesTable.TYPE, mList.get(currentCount - 2).view_type);
            } else {
                values.put(PWDBConfig.MessagesTable.CONTENT, "");
            }
            String where = PWDBConfig.MessagesTable.UID + " = ?";
            String[] selectionArgs = new String[]{String.valueOf(otherModel.uid)};
            mContext.getContentResolver().update(PWDBConfig.MessagesTable.CONTENT_URI, values, where, selectionArgs);
        }
        String where = PWDBConfig.DialogsTable.ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(model.id)};
        mContext.getContentResolver().delete(PWDBConfig.DialogsTable.CONTENT_URI, where, selectionArgs);
    }

    public void setUserData(PWUserModel meModel, PWUserModel otherModel) {
        this.meModel = meModel;
        this.otherModel = otherModel;
    }

    public void resetToIdle() {
        if (last_location >= 0 && mList != null && mList.size() > 0) {
            PlayerService.getInstance().releaseIgnoreCaseCommand();
            MsgAcceptModel model = mList.get(last_location);
            if (model.voice.play_status != VoiceModel.PLAY_STATUS_IDLE) {
                model.voice.play_status = VoiceModel.PLAY_STATUS_IDLE;
                notifyDataSetChanged();
            }
            last_location = -1;
        }
    }

    static class ViewHolder {
        PWTextViewCompat tv_content;
        TextView tv_time;
        //ImageView iv_uface;
        ImageView iv_icon;
        RelativeLayout iv_layout;
        ProgressBar sending_bar;
        ImageView iv_remind;
    }

    static class FeedViewHolder {
        TextView tv_redbag_title;
        TextView tv_redbag_content;
        ImageView iv_redbag_icon;
        //ImageView iv_uface;
        TextView tv_time;
        View ll_feed_content;
    }

    static class GifViewHolder {
        GifImageView iv_gif_face_view;
        //ImageView iv_uface;
        TextView tv_time;
        ImageView iv_remind;
        ProgressBar iv_pro_bar;
    }

    static class ImageViewHolder {
        ImageView iv_image_view;
        LinearLayout ll_content;
        //ImageView iv_uface;
        TextView tv_time;

        ProgressBar iv_pro_bar;
        ImageView iv_remind;

    }

    static class AttentionHolder {
        TextView attention_btn;
        TextView attentior_default_text;
        ImageView attention_icon;
        View attention_layout;
    }

    static class VoicePlayerHolder {
        RelativeLayout voice_player_layout;
        Chronometer chronometer_voice;
        ImageView iv_play_btn;
        TextView tv_time;
        String ms;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
//        if (needAutoScroll) {
//            if (this.listView != null) {
//                if (getCount() > 0)
//                    this.listView.setSelection(getCount() - 1);
//            }
//        }
    }

    public void setAutoScroll(boolean b) {
        if (this.listView != null) {
            if (b) {
                this.listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
            } else {
                this.listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_NORMAL);
            }
        }
    }
}
