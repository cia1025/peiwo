package me.peiwo.peiwo.adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.activity.SayHelloActivity;
import me.peiwo.peiwo.callback.DownloadCallback;
import me.peiwo.peiwo.db.MsgDBCenterService;
import me.peiwo.peiwo.im.MessageModel;
import me.peiwo.peiwo.model.SayHelloModel;
import me.peiwo.peiwo.model.VoiceModel;
import me.peiwo.peiwo.net.PWDownloader;
import me.peiwo.peiwo.net.TcpProxy;
import me.peiwo.peiwo.service.PlayerService;
import me.peiwo.peiwo.util.CustomLog;
import me.peiwo.peiwo.util.FileManager;
import me.peiwo.peiwo.util.Md5Util;
import me.peiwo.peiwo.util.PWUtils;
import me.peiwo.peiwo.widget.PWTextViewCompat;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class SayHelloAdapter extends PPBaseAdapter<SayHelloModel> {
    private LayoutInflater inflater;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private Handler mHandler;
    private int faceSize;
    private int last_location = -1;
    public static final int PLAY_STATUS_PLAYING = 1;
    public static final int PLAY_STATUS_PAUSE = 2;
    public static final int PLAY_STATUS_IDLE = 3;
    public static final int PLAY_STATUS_RESUME = 4;

    public SayHelloAdapter(List<SayHelloModel> mList, Context context, Handler handler) {
        super(mList);
        this.mHandler = handler;
        inflater = LayoutInflater.from(context);
        imageLoader = ImageLoader.getInstance();
        options = getRoundOptions(true);
        faceSize = PWUtils.getFaceSizeFromScreen(context);
    }

    @Override
    public Object getItem(int position) {
        return super.getItem(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SayHelloModel model = mList.get(position);
        int dialog_type = MsgDBCenterService.getInstance().findDialogTypeByUid(model.uid);
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.call_request_list_item, parent, false);
            holder = new ViewHolder();
//            if(dialog_type == MessageModel.DIALOG_TYPE_FOCUS) {
                holder.tv_msg = (PWTextViewCompat) convertView.findViewById(R.id.tv_hello_content);
//                holder.v_gender_age = (GenderWithAgeView) convertView.findViewById(R.id.v_gender_age);
//            } else {
                holder.voice_player_layout = (RelativeLayout) convertView.findViewById(R.id.voice_player_layout);
                holder.play_btn = (ImageView) convertView.findViewById(R.id.iv_play_btn);
                holder.chronometer_voice = (Chronometer) convertView.findViewById(R.id.chronometer_voice);
//                holder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
//            }
            holder.iv_uface = (ImageView) convertView.findViewById(R.id.iv_uface);
            holder.rl_info = (RelativeLayout) convertView.findViewById(R.id.rl_info);
            holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            holder.btn_doblock_report = (TextView) convertView.findViewById(R.id.btn_doblock_report);
//            holder.tv_badge = (TextView) convertView.findViewById(R.id.tv_badge);
//            holder.tv_find_search = (TextView) convertView.findViewById(R.id.tv_find_search);
//            holder.btn_re_msg = (TextView) convertView.findViewById(R.id.btn_re_msg);
            holder.tv_accept = (TextView) convertView.findViewById(R.id.btn_accept);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        imageLoader.displayImage(model.userModel.avatar_thumbnail, holder.iv_uface, options);
        holder.tv_name.setFilters(new InputFilter[]{new InputFilter.LengthFilter(7)});
        holder.tv_name.setText(model.userModel.name);

        //SpannableString spannableString = ExpressionUtil.getInstance().getExpressionString(model.content, faceSize);
        if(dialog_type == MessageModel.DIALOG_TYPE_FOCUS){
            holder.tv_msg.setTextCompat(model.content, faceSize);
            holder.tv_msg.setVisibility(View.VISIBLE);
            holder.voice_player_layout.setVisibility(View.GONE);
            holder.play_btn.setVisibility(View.GONE);
            holder.chronometer_voice.setVisibility(View.GONE);
        }

//		if (model.from == 1) {
//            holder.tv_find_search.setText("发现列表");		//从发现页
//		} else if (model.from == 2) {
//            holder.tv_find_search.setText("好友搜索");		//其他路径
//        } else if (model.from == 3) {
//            holder.tv_find_search.setText("动态内容");
//        } else if (model.from == 4) {
//            holder.tv_find_search.setText("匿名记录");
//        }
//        holder.tv_time.setText(TimeUtil.getMsgTimeDisplay(model.update_time, false));
//        if (model.unread_count > 0) {
//        	if(model.unread_count == 1){
//                holder.tv_badge.setVisibility(View.GONE);
//                holder.tv_badge.setText(null);
//        	}else{
//                holder.tv_badge.setVisibility(View.VISIBLE);
//                holder.tv_badge.setText(String.valueOf(model.unread_count));
//        	}
//        } else {
//            holder.tv_badge.setVisibility(View.INVISIBLE);
//            holder.tv_badge.setText(null);
//        }
//        holder.v_gender_age.displayGenderWithAge(model.userModel.gender, TimeUtil.getAgeByBirthday(model.userModel.birthday));
//        holder.btn_re_msg.setOnClickListener(new OnApplyClickListener(position));


        holder.btn_doblock_report.setOnClickListener(new OnApplyClickListener(position));
        holder.iv_uface.setOnClickListener(new OnApplyClickListener(position));
        holder.rl_info.setOnClickListener(new OnApplyClickListener(position));
        holder.rl_info.setOnLongClickListener(new onFocusInfoLongClickListener(position));
        holder.tv_accept.setOnClickListener(new OnApplyClickListener(position));
        if(dialog_type == MessageModel.DIALOG_TYPE_VOICE_MESSAGE) {
            if(model.voice == null)
                return convertView;
            String ms = PWUtils.FormatHMS(model.voice.length);
            CustomLog.d("voice has " + model.voice.length + " seconds.");
            CustomLog.d("voice_length is : " + ms);
            if(model.voice.play_status == PLAY_STATUS_IDLE)
                holder.chronometer_voice.setText(ms);
            holder.tv_msg.setVisibility(View.GONE);
            holder.voice_player_layout.setVisibility(View.VISIBLE);
            holder.play_btn.setVisibility(View.VISIBLE);
            holder.chronometer_voice.setVisibility(View.VISIBLE);
            setVoiceStatus(holder.play_btn, holder.chronometer_voice, model.voice.play_status, ms);
            holder.voice_player_layout.setOnClickListener(v -> actionPlay(position));
        }
        return convertView;
    }

    class onFocusInfoLongClickListener implements View.OnLongClickListener {
        private int position;

        public onFocusInfoLongClickListener(int position) {
            this.position = position;
        }

        @Override
        public boolean onLongClick(View v) {
            if (mHandler != null) {
                Message message = mHandler.obtainMessage();
                message.what = SayHelloActivity.WHAT_DATA_LONGCLICK;
                message.obj = position;
                mHandler.sendMessage(message);
            }
            return true;
        }
    }

    private void setVoiceStatus(ImageView iv_play_voice, Chronometer chronometer_voice, int play_status, String ms) {
        long baseTime = convertStrToLong(chronometer_voice.getText().toString());
        CustomLog.d("setVoiceStatus. playstatus is : "+play_status);
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
                PlayerService.getInstance().setOnPrepareCompletedListener(() -> {
                    chronometer_voice.setBase(SystemClock.elapsedRealtime());
                    chronometer_voice.start();
                });
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
            SayHelloModel model = mList.get(last_location);
            if (model.voice.play_status != PLAY_STATUS_IDLE) {
                model.voice.play_status = PLAY_STATUS_IDLE;
            }
        }
        last_location = location;
        SayHelloModel model = mList.get(location);
        CustomLog.d("actionPlay. playstatus is : "+model.voice.play_status);
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
                }
            });
        }
    }

    private void playLocalVoice(VoiceModel voice, String file_path) {
        PlayerService playerService = PlayerService.getInstance();
        playerService.setOnCompletionListener(mp -> {
            voice.play_status = VoiceModel.PLAY_STATUS_IDLE;
            notifyDataSetChanged();
        });
        playerService.setOnErrorListener((mp, what, extra) -> {
            voice.play_status = VoiceModel.PLAY_STATUS_IDLE;
            notifyDataSetChanged();
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

    class OnApplyClickListener implements View.OnClickListener {
        private int position;

        public OnApplyClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_doblock_report:
                    if (mHandler != null) {
                        Message message = mHandler.obtainMessage();
                        message.what = SayHelloActivity.WHAT_DATA_DOBLOCK_REPORT;
                        message.obj = position;
                        mHandler.sendMessage(message);
                    }
                    break;
//			case R.id.btn_re_msg:
//			case R.id.rl_info:
//	            if (mHandler != null) {
//	                Message message = mHandler.obtainMessage();
//	                message.what = SayHelloActivity.WHAT_DATA_REPLY_MESSAGE;
//	                message.obj = position;
//	                mHandler.sendMessage(message);
//	            }
//				break;
                case R.id.iv_uface:
                    if (mHandler != null) {
                        Message message = mHandler.obtainMessage();
                        message.what = SayHelloActivity.WHAT_DATA_START_USERINFO;
                        message.obj = position;
                        mHandler.sendMessage(message);
                    }
                    break;
                case R.id.btn_accept:
                    JSONObject jobj = new JSONObject();
                    try {
                        jobj.put("from", 1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    SayHelloModel model = mList.get(position);
                    TcpProxy tcpProxy = TcpProxy.getInstance();
                    tcpProxy.focusUser(model.uid, jobj);
                    if(mHandler != null) {
                        Message message = mHandler.obtainMessage(SayHelloActivity.WHAT_DATA_ACCEPT_TO_BE_FRIENDS);
                        message.obj = model;
                        mHandler.sendMessage(message);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    public void resetToIdle() {
        if (last_location >= 0 && mList != null && mList.size() > 0) {
            PlayerService.getInstance().releaseIgnoreCaseCommand();
            SayHelloModel model = mList.get(last_location);
            if (model.voice.play_status != VoiceModel.PLAY_STATUS_IDLE) {
                model.voice.play_status = VoiceModel.PLAY_STATUS_IDLE;
                notifyDataSetChanged();
            }
            last_location = -1;
        }
    }

    static class ViewHolder {
        ImageView iv_uface;
        TextView tv_name;
        PWTextViewCompat tv_msg;
        ImageView play_btn;
//        TextView voice_length;
//        TextView tv_badge;
//        TextView tv_find_search;
//        TextView tv_time;
        TextView btn_doblock_report;
//        TextView btn_re_msg;
//        GenderWithAgeView v_gender_age;
        TextView tv_accept;
        RelativeLayout rl_info;
        Chronometer chronometer_voice;
        RelativeLayout voice_player_layout;
        //记录一下当前holder的时间，不应放到ViewHolder中，以后再想更好地放
        String ms;
    }
}
