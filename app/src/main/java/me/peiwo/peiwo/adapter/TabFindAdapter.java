package me.peiwo.peiwo.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.activity.BaseFragmentActivity;
import me.peiwo.peiwo.activity.FeedFlowActivity;
import me.peiwo.peiwo.callback.DownloadCallback;
import me.peiwo.peiwo.constans.Constans;
import me.peiwo.peiwo.constans.UMEventIDS;
import me.peiwo.peiwo.fragment.RecorderDialogFragment;
import me.peiwo.peiwo.model.*;
import me.peiwo.peiwo.net.AsynHttpClient;
import me.peiwo.peiwo.net.PWDownloader;
import me.peiwo.peiwo.service.PlayerService;
import me.peiwo.peiwo.util.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class TabFindAdapter extends GroupJoinBaseAdapter<Object> {
    private static final int VIEW_TYPE_COUNT = 4;
    public static final int TYPE_USERITEM = 0;
    public static final int TYPE_TOPIC = 1;
    public static final int TYPE_GROUP = 2;
    public static final int TYPE_GROUP_MORE = 3;
    private List<Object> mList;
    private LayoutInflater inflater;
    private Context context;
    private ImageLoader imageLoader;
    private int color_male;
    private int color_female;
    private int last_location = -1;

    private final DisplayImageOptions OPTIONS_F = getRoundOptions(false);

    public TabFindAdapter(List<Object> mList, Context context) {
        super(context, mList);
        this.context = context;
        this.mList = mList;
        this.inflater = LayoutInflater.from(context);
        imageLoader = ImageLoader.getInstance();
        color_male = context.getResources().getColor(R.color.c_list_male);
        color_female = context.getResources().getColor(R.color.c_list_female);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int currentType = getItemViewType(position);
        ViewHolder holder = null;
        ViewHolderTopic holderTopic = null;
        ViewHolderGroup holderGroup = null;
        ViewHolderGroupMore holderGroupMore = null;

        if (convertView == null) {
            switch (currentType) {
                case TYPE_USERITEM:
                    holder = new ViewHolder();
                    convertView = inflater.inflate(R.layout.user_list_item, parent, false);
                    holder.iv_add_friend = convertView.findViewById(R.id.iv_add_friend);
                    holder.iv_play_voice = (ImageView) convertView.findViewById(R.id.iv_play_voice);
                    holder.v_gender_indicator = convertView.findViewById(R.id.v_gender_indicator);
                    holder.iv_uface = (ImageView) convertView.findViewById(R.id.iv_uface);
                    holder.tv_uname = (TextView) convertView.findViewById(R.id.tv_uname);
                    holder.tv_call_price = (TextView) convertView.findViewById(R.id.tv_call_price);
                    holder.tv_des = (TextView) convertView.findViewById(R.id.tv_des);
                    holder.tv_gender_constellation = (TextView) convertView.findViewById(R.id.tv_gender_constellation);
                    holder.iv_price_icon = (ImageView) convertView.findViewById(R.id.iv_price_icon);
                    convertView.setTag(holder);
                    break;
                case TYPE_TOPIC:
                    holderTopic = new ViewHolderTopic();
                    convertView = inflater.inflate(R.layout.tag_introduce_2, parent, false);
                    holderTopic.tv_tag_name = (TextView) convertView.findViewById(R.id.tv_tag_id2);
                    holderTopic.tv_tag_creator = (TextView) convertView.findViewById(R.id.tv_tag_uid);
                    holderTopic.other_tag_enter = (Button) convertView.findViewById(R.id.btn_tag_enter2);
                    holderTopic.ll_tag_enter2 = convertView.findViewById(R.id.ll_tag_enter2);
                    convertView.setTag(holderTopic);
                    break;
                case TYPE_GROUP:
                    convertView = inflater.inflate(R.layout.layout_find_group_item, parent, false);
                    holderGroup = new ViewHolderGroup(convertView);
                    convertView.setTag(holderGroup);
                    break;
                case TYPE_GROUP_MORE:
                    convertView = inflater.inflate(R.layout.layout_find_group_more_item, parent, false);
                    holderGroupMore = new ViewHolderGroupMore(convertView);
                    convertView.setTag(holderGroupMore);
                    break;
                default:
                    break;
            }
        } else {
            if (currentType == TYPE_USERITEM) {
                holder = (ViewHolder) convertView.getTag();
            } else if (currentType == TYPE_TOPIC) {
                holderTopic = (ViewHolderTopic) convertView.getTag();
            } else if (currentType == TYPE_GROUP) {
                holderGroup = (ViewHolderGroup) convertView.getTag();
            } else if (currentType == TYPE_GROUP_MORE) {
                holderGroupMore = (ViewHolderGroupMore) convertView.getTag();
            }
        }
        if (currentType == TYPE_USERITEM) {
            final TabFindModel model = (TabFindModel) getItem(position);
            //新需求,不需要显示推荐图标"+" 见Tower 2.4.1 群细节优化
//            if (model.recommended) {
//                holder.iv_add_friend.setVisibility(View.VISIBLE);
//                holder.iv_play_voice.setVisibility(View.GONE);
//                holder.iv_add_friend.setOnClickListener((v) -> addFriendAction(model));
//            } else {
            holder.iv_add_friend.setVisibility(View.GONE);
            holder.iv_add_friend.setOnClickListener(null);

            if (model.price > 0 && model.voice != null) {
                holder.iv_play_voice.setVisibility(View.VISIBLE);
                setVoiceStatus(holder.iv_play_voice, model.voice.play_status);
                holder.iv_play_voice.setOnClickListener(v -> actionPlay(position));
            } else {
                holder.iv_play_voice.setVisibility(View.GONE);
                holder.iv_play_voice.setOnClickListener(null);
            }
//            }
            imageLoader.displayImage(model.avatar_thumbnail, holder.iv_uface, OPTIONS_F);
            if (model.gender == AsynHttpClient.GENDER_MASK_FEMALE) {
                holder.v_gender_indicator.setBackgroundColor(color_female);
            } else {
                holder.v_gender_indicator.setBackgroundColor(color_male);
            }
            holder.tv_uname.setText(UserManager.getRealName(model.uid, model.name, context));
            holder.tv_des.setText(model.slogan);
            if (model.price > 0) {
                holder.iv_price_icon.setVisibility(View.VISIBLE);
            } else {
                holder.iv_price_icon.setVisibility(View.GONE);
            }
            holder.tv_gender_constellation.setText(TimeUtil.getConstellation(model.birthday));
        } else if (currentType == TYPE_TOPIC) {
            final TopicFindModel model = (TopicFindModel) getItem(position);
            //{"content":"系统自动生成内容","creator_id":1,"create_time":"2015-04-28 11:03:27","subtitle":"标签由系统创建","id":1}
            holderTopic.tv_tag_creator.setText(model.subtitle);
            if (TextUtils.isEmpty(model.content)) {
                holderTopic.tv_tag_name.setVisibility(View.GONE);
            } else {
                holderTopic.tv_tag_name.setText("“" + model.content + "”");
            }
            holderTopic.other_tag_enter.setOnClickListener((v) -> clickAction(model));
            holderTopic.ll_tag_enter2.setOnClickListener((v) -> clickAction(model));
        } else if (currentType == TYPE_GROUP) {
            fetchDataForGroup(holderGroup, (TabfindGroupModel) getItem(position));
        } else if (currentType == TYPE_GROUP_MORE) {
            fetchDataForGroupMore();//holderGroupMore, getItem(position)
        }

        return convertView;
    }

    private void fetchDataForGroupMore() {

    }

    private void fetchDataForGroup(ViewHolderGroup holderGroup, TabfindGroupModel groupModel) {
        imageLoader.displayImage(TextUtils.isEmpty(groupModel.admin.avatar) ? groupModel.avatar : groupModel.admin.avatar, holderGroup.iv_group_avatar, OPTIONS_F);
        holderGroup.tv_group_des.setText(groupModel.group_name);
        holderGroup.tv_group_name.setText(groupModel.admin.name);
        if (groupModel.ticket_price > 0) {
            holderGroup.iv_price_icon.setVisibility(View.VISIBLE);
        } else {
            holderGroup.iv_price_icon.setVisibility(View.INVISIBLE);
        }
        holderGroup.v_add_group_action.setOnClickListener(v -> joinGroup(v, groupModel));
    }

    private void setVoiceStatus(ImageView iv_play_voice, int play_status) {
        switch (play_status) {
            case VoiceModel.PLAY_STATUS_IDLE:
                iv_play_voice.setEnabled(true);
                iv_play_voice.setImageResource(R.drawable.icon_play_blue);
                break;
            case VoiceModel.PLAY_STATUS_LOADING:
                iv_play_voice.setEnabled(false);
                iv_play_voice.setImageResource(R.drawable.icon_pause_blue);
                break;
            case VoiceModel.PLAY_STATUS_PAUSE:
                iv_play_voice.setEnabled(true);
                iv_play_voice.setImageResource(R.drawable.icon_play_blue);
                break;
            case VoiceModel.PLAY_STATUS_PLAYING:
                iv_play_voice.setEnabled(true);
                iv_play_voice.setImageResource(R.drawable.icon_pause_blue);
                break;
            default:
                break;
        }
    }

    private void actionPlay(int location) {
        if (last_location >= 0 && last_location != location) {
            TabFindModel model = (TabFindModel) mList.get(last_location);
            if (model.voice.play_status != VoiceModel.PLAY_STATUS_IDLE) {
                model.voice.play_status = VoiceModel.PLAY_STATUS_IDLE;
            }
        }
        last_location = location;
        TabFindModel model = (TabFindModel) mList.get(location);
        switch (model.voice.play_status) {
            case VoiceModel.PLAY_STATUS_IDLE:
                model.voice.play_status = VoiceModel.PLAY_STATUS_PLAYING;
                preparePlayVoice(model.voice);
                break;
            case VoiceModel.PLAY_STATUS_PLAYING:
                model.voice.play_status = VoiceModel.PLAY_STATUS_PAUSE;
                PlayerService.getInstance().playPauseCommand();
                break;
            case VoiceModel.PLAY_STATUS_PAUSE:
                model.voice.play_status = VoiceModel.PLAY_STATUS_PLAYING;
                PlayerService.getInstance().playResumeCommand();
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


    private void addFriendAction(final TabFindModel model) {
        BaseFragmentActivity activity = (BaseFragmentActivity) context;
        activity.dismissAnimLoading();
        RecorderDialogFragment fragment = RecorderDialogFragment.newInstance(model.uid, model.name, model.avatar_thumbnail, Constans.PW_MESSAGE_FROM_FIND);
        fragment.show(activity.getSupportFragmentManager(), RecorderDialogFragment.class.getSimpleName());
    }

    private void clickAction(TopicFindModel model) {
        UmengStatisticsAgent.onEvent(context, UMEventIDS.UMEFEEDENTRY);
        Intent intent = new Intent(context, FeedFlowActivity.class);
        intent.putExtra("topic_id", model.id);
        intent.putExtra("topic_content", model.content);
        context.startActivity(intent);
    }

    @Override
    public int getItemViewType(int position) {
        Object item = getItem(position);
        if (item instanceof TopicFindModel) {
            return TYPE_TOPIC;
        } else if (item instanceof TabFindModel) {
            return TYPE_USERITEM;
        } else if (item instanceof TabfindGroupModel) {
            return TYPE_GROUP;
        } else if (item instanceof TabfindGroupMoreModel) {
            return TYPE_GROUP_MORE;
        }
        return -1;
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    public void resetToIdle() {
        if (last_location >= 0) {
            PlayerService.getInstance().releaseIgnoreCaseCommand();
            TabFindModel model = (TabFindModel) mList.get(last_location);
            if (model.voice.play_status != VoiceModel.PLAY_STATUS_IDLE) {
                model.voice.play_status = VoiceModel.PLAY_STATUS_IDLE;
                notifyDataSetChanged();
            }
            last_location = -1;
        }
    }


    static class ViewHolder {
        ImageView iv_uface;
        TextView tv_uname;
        TextView tv_call_price;
        TextView tv_des;
        ImageView iv_price_icon;
        TextView tv_gender_constellation;
        View v_gender_indicator;
        View iv_add_friend;
        ImageView iv_play_voice;
        /********************************/

    }

    static class ViewHolderTopic {
        TextView tv_tag_name;
        Button other_tag_enter;
        View ll_tag_enter2;
        TextView tv_tag_creator;
    }

    static class ViewHolderGroup {
        ImageView iv_group_avatar;
        TextView tv_group_name;
        TextView tv_group_des;
        View iv_price_icon;
        View v_add_group_action;

        public ViewHolderGroup(View convertView) {
            iv_group_avatar = (ImageView) convertView.findViewById(R.id.iv_group_avatar);
            tv_group_name = (TextView) convertView.findViewById(R.id.tv_group_name);
            tv_group_des = (TextView) convertView.findViewById(R.id.tv_group_des);
            iv_price_icon = convertView.findViewById(R.id.iv_price_icon);
            v_add_group_action = convertView.findViewById(R.id.v_add_group_action);
        }
    }

    static class ViewHolderGroupMore {
        public ViewHolderGroupMore(View convertView) {
        }
    }


}
