package me.peiwo.peiwo.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.download.ImageDownloader;
import me.peiwo.peiwo.PeiwoApp;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.activity.ImagePagerActivity;
import me.peiwo.peiwo.activity.UserInfoActivity;
import me.peiwo.peiwo.constans.GroupConstant;
import me.peiwo.peiwo.model.ImageModel;
import me.peiwo.peiwo.model.groupchat.*;
import me.peiwo.peiwo.util.PWUtils;
import me.peiwo.peiwo.util.TimeUtil;
import me.peiwo.peiwo.util.group.ChatImageWrapper;
import me.peiwo.peiwo.util.group.ExpressionData;
import me.peiwo.peiwo.widget.PWTextViewCompat;
import pl.droidsonroids.gif.GifImageView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by fuhaidong on 15/12/9.
 */
public class GroupMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    //    private static final DisplayImageOptions OPTIONS_SELF = new DisplayImageOptions.Builder().imageScaleType(ImageScaleType.EXACTLY).showImageOnLoading(R.drawable.ic_default_avatar).bitmapConfig(Bitmap.Config.RGB_565)
//            .cacheInMemory(true).considerExifParams(true)
//            .cacheOnDisk(false).build();
    private static final DisplayImageOptions _OPTIONS = new DisplayImageOptions.Builder().imageScaleType(ImageScaleType.EXACTLY).showImageOnLoading(R.drawable.ic_default_avatar).bitmapConfig(Bitmap.Config.RGB_565)
            .cacheInMemory(true).considerExifParams(true)
            .cacheOnDisk(true).build();
    private List<GroupMessageBaseModel> models;
    private Context context;
    private LayoutInflater inflater;
    private ImageLoader imageLoader;
    private int bounds;
    private ExpressionData expressionData;
    //private int c_female;
    //private int c_male;
    private int show_nickname;

    private SparseArray<String> noteMap;

    public GroupMessageAdapter(Context context, List<GroupMessageBaseModel> models, int show_nickname) {
        inflater = LayoutInflater.from(context);
        this.context = context;
        this.models = models;
        imageLoader = ImageLoader.getInstance();
        bounds = PWUtils.getFaceSizeFromScreen(context);
        expressionData = ExpressionData.getInstance(context);
        //c_female = Color.parseColor("#fd7098");
        //c_male = Color.parseColor("#00b8d0");
        PeiwoApp app = (PeiwoApp) context.getApplicationContext();
        noteMap = app.getNoteMap();
        this.show_nickname = show_nickname;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case GroupConstant.ViewType.TYPE_TEXT_SELF:
                return new GroupTextViewHolderSelf(inflater.inflate(R.layout.layout_groupchat_text_self, parent, false));

            case GroupConstant.ViewType.TYPE_TEXT_OTHER:
                return new GroupTextViewHolderOther(inflater.inflate(R.layout.layout_groupchat_text_other, parent, false));

            case GroupConstant.ViewType.TYPE_IMAGE_SELF:
                return new GroupImageViewHolderSelf(inflater.inflate(R.layout.layout_groupchat_image_self, parent, false));

            case GroupConstant.ViewType.TYPE_IMAGE_OTHER:
                return new GroupImageViewHolderOther(inflater.inflate(R.layout.layout_groupchat_image_other, parent, false));

            case GroupConstant.ViewType.TYPE_GIF_SELF:
                return new GroupGIFViewHolderSelf(inflater.inflate(R.layout.layout_groupchat_gif_self, parent, false));

            case GroupConstant.ViewType.TYPE_GIF_OTHER:
                return new GroupGIFViewHolderOther(inflater.inflate(R.layout.layout_groupchat_gif_other, parent, false));

            case GroupConstant.ViewType.TYPE_REDBAG_SELF:
                return new GroupREDBAGViewHolderSelf(inflater.inflate(R.layout.layout_groupchat_redbag_self, parent, false));

            case GroupConstant.ViewType.TYPE_REDBAG_OTHER:
                return new GroupREDBAGViewHolderOther(inflater.inflate(R.layout.layout_groupchat_redbag_other, parent, false));

            case GroupConstant.ViewType.TYPE_REPUTATION_REDBAG_SELF:
                return new GroupReputationREDBAGViewHolderSelf(inflater.inflate(R.layout.layout_groupchat_repuredbag_self, parent, false));

            case GroupConstant.ViewType.TYPE_REPUTATION_REDBAG_OTHER:
                return new GroupReputationREDBAGViewHolderOther(inflater.inflate(R.layout.layout_groupchat_repuredbag_other, parent, false));

            case GroupConstant.ViewType.TYPE_REDBAG_TIP:
                return new GroupREDBAGTipViewHolder(inflater.inflate(R.layout.layout_groupchat_redbag_tip, parent, false));
            case GroupConstant.ViewType.TYPE_REPUREDBAG_TIP:
                return new GroupRepuREDBAGTipViewHolder(inflater.inflate(R.layout.layout_groupchat_repu_redbag_tip, parent, false));
            case GroupConstant.ViewType.TYPE_DECORATION:
                return new GroupDecorationViewHolder(inflater.inflate(R.layout.layout_groupchat_decoration, parent, false));
            //local extra
            case GroupConstant.ViewType.TYPE_HEADER:
                return new GroupLoadMoreViewHolder(inflater.inflate(R.layout.layout_chat_load_more, parent, false));
            default:
                return new GroupUnKnownViewHolder(inflater.inflate(R.layout.layout_groupchat_unknown, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        GroupMessageBaseModel model = getItemObject(position);
        if (holder instanceof GroupTextViewHolderSelf) {
            GroupTextViewHolderSelf holderSelf = (GroupTextViewHolderSelf) holder;
            GroupMessageTextModel textModel = (GroupMessageTextModel) model;
            fetchDataTextSelf(holderSelf, textModel, position);
        } else if (holder instanceof GroupTextViewHolderOther) {
            GroupTextViewHolderOther holderOther = (GroupTextViewHolderOther) holder;
            GroupMessageTextModel textModel = (GroupMessageTextModel) model;
            fetchDataTextOther(holderOther, textModel, position);
        } else if (holder instanceof GroupImageViewHolderSelf) {
            GroupImageViewHolderSelf holderSelf = (GroupImageViewHolderSelf) holder;
            GroupMessageImageModel imageModel = (GroupMessageImageModel) model;
            fetchDataImageSelf(holderSelf, imageModel, position);
        } else if (holder instanceof GroupImageViewHolderOther) {
            GroupImageViewHolderOther holderOther = (GroupImageViewHolderOther) holder;
            GroupMessageImageModel imageModel = (GroupMessageImageModel) model;
            fetchDataImageOther(holderOther, imageModel, position);
        } else if (holder instanceof GroupGIFViewHolderSelf) {
            GroupGIFViewHolderSelf holderSelf = (GroupGIFViewHolderSelf) holder;
            GroupMessageGIFModel gifModel = (GroupMessageGIFModel) model;
            fetchDataGIFSelf(holderSelf, gifModel, position);
        } else if (holder instanceof GroupGIFViewHolderOther) {
            GroupGIFViewHolderOther holderOther = (GroupGIFViewHolderOther) holder;
            GroupMessageGIFModel gifModel = (GroupMessageGIFModel) model;
            fetchDataGIFOther(holderOther, gifModel, position);
        } else if (holder instanceof GroupREDBAGViewHolderSelf) {
            GroupREDBAGViewHolderSelf holderSelf = (GroupREDBAGViewHolderSelf) holder;
            GroupMessageRedBagModel redBagModel = (GroupMessageRedBagModel) model;
            fetchDataRedBagSelf(holderSelf, redBagModel, position);
        } else if (holder instanceof GroupREDBAGViewHolderOther) {
            GroupREDBAGViewHolderOther holderOther = (GroupREDBAGViewHolderOther) holder;
            GroupMessageRedBagModel redBagModel = (GroupMessageRedBagModel) model;
            fetchDataRedBagOther(holderOther, redBagModel, position);
        } else if (holder instanceof GroupReputationREDBAGViewHolderSelf) {
            GroupReputationREDBAGViewHolderSelf holderSelf = (GroupReputationREDBAGViewHolderSelf) holder;
            GroupMessageRepuRedBagModel repuRedBagModel = (GroupMessageRepuRedBagModel) model;
            fetchDataRepuRedBagSelf(holderSelf, repuRedBagModel, position);
        } else if (holder instanceof GroupReputationREDBAGViewHolderOther) {
            GroupReputationREDBAGViewHolderOther holderOther = (GroupReputationREDBAGViewHolderOther) holder;
            GroupMessageRepuRedBagModel repuRedBagModel = (GroupMessageRepuRedBagModel) model;
            fetchDataRepuRedBagOther(holderOther, repuRedBagModel, position);
        } else if (holder instanceof GroupRepuREDBAGTipViewHolder) {
            GroupRepuREDBAGTipViewHolder repuREDBAGTipViewHolder = (GroupRepuREDBAGTipViewHolder) holder;
            GroupMessageRepuRedBagTipModel repuRedBagTipModel = (GroupMessageRepuRedBagTipModel) model;
            fetchDataRepuRedBagTip(repuREDBAGTipViewHolder, repuRedBagTipModel, position);
        } else if (holder instanceof GroupREDBAGTipViewHolder) {
            GroupREDBAGTipViewHolder REDBAGTipViewHolder = (GroupREDBAGTipViewHolder) holder;
            GroupMessageRedBagTipModel redBagTipModel = (GroupMessageRedBagTipModel) model;
            fetchDataRedBagTip(REDBAGTipViewHolder, redBagTipModel, position);
        } else if (holder instanceof GroupDecorationViewHolder) {
            GroupDecorationViewHolder decorationViewHolder = (GroupDecorationViewHolder) holder;
            GroupMessageDecorationModel decorationModel = (GroupMessageDecorationModel) model;
            fetchDataDecoration(decorationViewHolder, decorationModel, position);
        } else if (holder instanceof GroupUnKnownViewHolder) {
            GroupUnKnownViewHolder unKnownViewHolder = (GroupUnKnownViewHolder) holder;
            fetchDataUnKnown(unKnownViewHolder, model);
        }
    }

    @Override
    public int getItemCount() {
        return this.models.size();
    }

    private GroupMessageBaseModel getItemObject(int position) {
        return this.models.get(position);
    }

    @Override
    public int getItemViewType(int position) {
        GroupMessageBaseModel model = getItemObject(position);
        switch (model.dialog_type) {
            case GroupConstant.MessageType.TYPE_TEXT:
                if (model.direction == GroupConstant.Direction.SELF) {
                    return GroupConstant.ViewType.TYPE_TEXT_SELF;
                } else {
                    return GroupConstant.ViewType.TYPE_TEXT_OTHER;
                }
            case GroupConstant.MessageType.TYPE_IMAGE:
                if (model.direction == GroupConstant.Direction.SELF) {
                    return GroupConstant.ViewType.TYPE_IMAGE_SELF;
                } else {
                    return GroupConstant.ViewType.TYPE_IMAGE_OTHER;
                }
            case GroupConstant.MessageType.TYPE_GIF:
                if (model.direction == GroupConstant.Direction.SELF) {
                    return GroupConstant.ViewType.TYPE_GIF_SELF;
                } else {
                    return GroupConstant.ViewType.TYPE_GIF_OTHER;
                }
            case GroupConstant.MessageType.TYPE_REDBAG:
                if (model.direction == GroupConstant.Direction.SELF) {
                    return GroupConstant.ViewType.TYPE_REDBAG_SELF;
                } else {
                    return GroupConstant.ViewType.TYPE_REDBAG_OTHER;
                }
            case GroupConstant.MessageType.TYPE_REPUTATION_REDBAG:
                if (model.direction == GroupConstant.Direction.SELF) {
                    return GroupConstant.ViewType.TYPE_REPUTATION_REDBAG_SELF;
                } else {
                    return GroupConstant.ViewType.TYPE_REPUTATION_REDBAG_OTHER;
                }
            case GroupConstant.MessageType.TYPE_REDBAG_TIP:
                return GroupConstant.ViewType.TYPE_REDBAG_TIP;

            case GroupConstant.MessageType.TYPE_REPUREDBAG_TIP:
                return GroupConstant.ViewType.TYPE_REPUREDBAG_TIP;

            case GroupConstant.MessageType.TYPE_DECORATION:
                return GroupConstant.ViewType.TYPE_DECORATION;
            //local extra
            case GroupConstant.MessageType.TYPE_HEADER:
                return GroupConstant.ViewType.TYPE_HEADER;
            default:
                return GroupConstant.ViewType.TYPE_UNKNOWN;
        }
    }

    /***********/
    private void fetchDataTextSelf(GroupTextViewHolderSelf holderSelf, GroupMessageTextModel textModel, int location) {
        if (!holderSelf.tv_content.getText().equals(textModel.text.content)) {
            holderSelf.tv_content.setTextCompat(textModel.text.content, bounds);
        }
        setIndiStatus(textModel, holderSelf.v_resend, holderSelf.progressBar, location);
        setTimeUnit(holderSelf.tv_time, textModel, location);
        handleMessageLongClick(holderSelf.tv_content, textModel, location);
    }

    private void fetchDataTextOther(GroupTextViewHolderOther holderOther, GroupMessageTextModel textModel, int location) {
        imageLoader.displayImage(textModel.user.avatar_thumbnail, holderOther.iv_avatar);
        holderOther.tv_content.setTextCompat(textModel.text.content, bounds);
        setOtherMemberType(holderOther.tv_identity, textModel.user.member_type);
        displayMemberName(holderOther.tv_extra_name, textModel.user.name, textModel.user.nickname, textModel.user.uid, textModel.user.gender);
        setTimeUnit(holderOther.tv_time, textModel, location);
        handleMessageLongClick(holderOther.tv_content, textModel, location);
        holderOther.iv_avatar.setOnClickListener(v -> startUserInfo(textModel.user.uid));
        holderOther.iv_avatar.setOnLongClickListener(v -> {
            handleUserAvatarLongClick(textModel, location);
            return true;
        });
    }

    private void fetchDataImageSelf(GroupImageViewHolderSelf holderSelf, GroupMessageImageModel imageModel, int location) {
        setIndiStatus(imageModel, holderSelf.v_resend, holderSelf.progressBar, location);
        loadImageWithSize(imageModel, holderSelf.iv_image);
        setTimeUnit(holderSelf.tv_time, imageModel, location);
        handleMessageLongClick(holderSelf.iv_image, imageModel, location);
        holderSelf.iv_image.setOnClickListener(v -> handleImageBrowse(imageModel));
    }


    private void fetchDataImageOther(GroupImageViewHolderOther holderOther, GroupMessageImageModel imageModel, int location) {
        imageLoader.displayImage(imageModel.user.avatar_thumbnail, holderOther.iv_avatar, _OPTIONS);
        loadImageWithSize(imageModel, holderOther.iv_image);
        setOtherMemberType(holderOther.tv_identity, imageModel.user.member_type);
        displayMemberName(holderOther.tv_extra_name, imageModel.user.name, imageModel.user.nickname, imageModel.user.uid, imageModel.user.gender);
        setTimeUnit(holderOther.tv_time, imageModel, location);
        handleMessageLongClick(holderOther.iv_image, imageModel, location);
        holderOther.iv_avatar.setOnClickListener(v -> startUserInfo(imageModel.user.uid));
        holderOther.iv_avatar.setOnLongClickListener(v -> {
            handleUserAvatarLongClick(imageModel, location);
            return true;
        });
        holderOther.iv_image.setOnClickListener(v -> handleImageBrowse(imageModel));
    }

    private void fetchDataGIFSelf(GroupGIFViewHolderSelf holderSelf, GroupMessageGIFModel gifModel, int location) {
        setIndiStatus(gifModel, holderSelf.v_resend, holderSelf.progressBar, location);
        holderSelf.giv_gif.setImageResource(gifModel.gif.movie_res_id);
        setTimeUnit(holderSelf.tv_time, gifModel, location);
        handleMessageLongClick(holderSelf.giv_gif, gifModel, location);
    }

    private void fetchDataGIFOther(GroupGIFViewHolderOther holderOther, GroupMessageGIFModel gifModel, int location) {
        imageLoader.displayImage(gifModel.user.avatar_thumbnail, holderOther.iv_avatar);
        if (expressionData.getGifMappingData().containsKey(gifModel.gif.gif_name)) {
            int movie_res_id = expressionData.getGifMappingData().get(gifModel.gif.gif_name).movie_res_id;
            holderOther.giv_gif.setImageResource(movie_res_id);
        } else {
            holderOther.giv_gif.setImageBitmap(null);
        }
        setOtherMemberType(holderOther.tv_identity, gifModel.user.member_type);
        displayMemberName(holderOther.tv_extra_name, gifModel.user.name, gifModel.user.nickname, gifModel.user.uid, gifModel.user.gender);
        setTimeUnit(holderOther.tv_time, gifModel, location);
        holderOther.iv_avatar.setOnClickListener(v -> startUserInfo(gifModel.user.uid));
        holderOther.iv_avatar.setOnLongClickListener(v -> {
            handleUserAvatarLongClick(gifModel, location);
            return true;
        });
        handleMessageLongClick(holderOther.giv_gif, gifModel, location);
    }

    private void fetchDataRedBagSelf(GroupREDBAGViewHolderSelf holderSelf, GroupMessageRedBagModel redBagModel, int location) {
        setTimeUnit(holderSelf.tv_time, redBagModel, location);
        setIndiStatus(redBagModel, holderSelf.v_resend, holderSelf.progressBar, location);
        imageLoader.displayImage(redBagModel.packet.icon_url, holderSelf.iv_redbag_icon);
        holderSelf.tv_redbag_msg.setText(redBagModel.packet.msg);
        holderSelf.iv_redbag_icon.setOnClickListener(v -> {
            if (this.onRedBagReceiveListener != null) {
                this.onRedBagReceiveListener.onRedBagReceive(redBagModel, location);
            }
        });
        handleMessageLongClick(holderSelf.iv_redbag_icon, redBagModel, location);
    }

    private void fetchDataRedBagOther(GroupREDBAGViewHolderOther holderOther, GroupMessageRedBagModel redBagModel, int location) {
        imageLoader.displayImage(redBagModel.user.avatar_thumbnail, holderOther.iv_avatar);
        imageLoader.displayImage(redBagModel.packet.icon_url, holderOther.iv_redbag_icon);
        holderOther.tv_redbag_msg.setText(redBagModel.packet.msg);
        setOtherMemberType(holderOther.tv_identity, redBagModel.user.member_type);
        displayMemberName(holderOther.tv_extra_name, redBagModel.user.name, redBagModel.user.nickname, redBagModel.user.uid, redBagModel.user.gender);
        setTimeUnit(holderOther.tv_time, redBagModel, location);
        holderOther.iv_avatar.setOnClickListener(v -> startUserInfo(redBagModel.user.uid));
        holderOther.iv_avatar.setOnLongClickListener(v -> {
            handleUserAvatarLongClick(redBagModel, location);
            return true;
        });
        holderOther.iv_redbag_icon.setOnClickListener(v -> {
            if (this.onRedBagReceiveListener != null) {
                this.onRedBagReceiveListener.onRedBagReceive(redBagModel, location);
            }
        });
        handleMessageLongClick(holderOther.iv_redbag_icon, redBagModel, location);
    }

    private void fetchDataRepuRedBagSelf(GroupReputationREDBAGViewHolderSelf holderSelf, GroupMessageRepuRedBagModel repuRedBagModel, int location) {
        setTimeUnit(holderSelf.tv_time, repuRedBagModel, location);
        setIndiStatus(repuRedBagModel, holderSelf.v_resend, holderSelf.progressBar, location);
        imageLoader.displayImage(repuRedBagModel.packet.icon_url, holderSelf.iv_redbag_icon);
        holderSelf.tv_redbag_msg.setText(repuRedBagModel.packet.msg);
        holderSelf.iv_redbag_icon.setOnClickListener(v -> {
            if (this.onRedBagReceiveListener != null) {
                this.onRedBagReceiveListener.onRedBagReceive(repuRedBagModel, location);
            }
        });
        handleMessageLongClick(holderSelf.iv_redbag_icon, repuRedBagModel, location);
    }

    private void fetchDataRepuRedBagOther(GroupReputationREDBAGViewHolderOther holderOther, GroupMessageRepuRedBagModel repuRedBagModel, int location) {
        imageLoader.displayImage(repuRedBagModel.user.avatar_thumbnail, holderOther.iv_avatar);
        imageLoader.displayImage(repuRedBagModel.packet.icon_url, holderOther.iv_redbag_icon);
        holderOther.tv_redbag_msg.setText(repuRedBagModel.packet.msg);
        setOtherMemberType(holderOther.tv_identity, repuRedBagModel.user.member_type);
        displayMemberName(holderOther.tv_extra_name, repuRedBagModel.user.name, repuRedBagModel.user.nickname, repuRedBagModel.user.uid, repuRedBagModel.user.gender);
        setTimeUnit(holderOther.tv_time, repuRedBagModel, location);
        holderOther.iv_avatar.setOnClickListener(v -> startUserInfo(repuRedBagModel.user.uid));
        holderOther.iv_avatar.setOnLongClickListener(v -> {
            handleUserAvatarLongClick(repuRedBagModel, location);
            return true;
        });
        holderOther.iv_redbag_icon.setOnClickListener(v -> {
            if (this.onRedBagReceiveListener != null) {
                this.onRedBagReceiveListener.onRedBagReceive(repuRedBagModel, location);
            }
        });
        handleMessageLongClick(holderOther.iv_redbag_icon, repuRedBagModel, location);
    }

    private void fetchDataRedBagTip(GroupREDBAGTipViewHolder REDBAGTipViewHolder, GroupMessageRedBagTipModel redBagTipModel, int location) {
        setTimeUnit(REDBAGTipViewHolder.tv_time, redBagTipModel, location);
        REDBAGTipViewHolder.tv_decoration.setText(redBagTipModel.text.content);
    }

    private void fetchDataRepuRedBagTip(GroupRepuREDBAGTipViewHolder repuREDBAGTipViewHolder, GroupMessageRepuRedBagTipModel repuRedBagTipModel, int location) {
        setTimeUnit(repuREDBAGTipViewHolder.tv_time, repuRedBagTipModel, location);
        repuREDBAGTipViewHolder.tv_decoration.setText(repuRedBagTipModel.text.content);
    }

    private void fetchDataDecoration(GroupDecorationViewHolder decorationViewHolder, GroupMessageDecorationModel decorationModel, int location) {
        setTimeUnit(decorationViewHolder.tv_time, decorationModel, location);
        decorationViewHolder.tv_decoration.setText(decorationModel.text.content);
    }

    private void fetchDataUnKnown(GroupUnKnownViewHolder unKnownViewHolder, GroupMessageBaseModel unKnownModel) {
        unKnownViewHolder.tv_content.setText(unKnownModel.extra.content);
    }

    private void startUserInfo(String uid) {
        Intent intent = new Intent(context, UserInfoActivity.class);
        intent.putExtra(UserInfoActivity.TARGET_UID, Integer.valueOf(uid));
        context.startActivity(intent);
    }

    private void setIndiStatus(GroupMessageBaseModel model, View v_resend, View progressBar, int location) {
        if (model.send_status == GroupConstant.SendStatus.SENDING) {
            v_resend.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        } else if (model.send_status == GroupConstant.SendStatus.ERROR) {
            progressBar.setVisibility(View.GONE);
            v_resend.setVisibility(View.VISIBLE);
            v_resend.setOnClickListener(v -> {
                if (this.recendMessageListener != null) {
                    this.recendMessageListener.onRecendMessage(model, location);
                }
            });
        } else if (model.send_status == GroupConstant.SendStatus.SUCCESS) {
            v_resend.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
        }
    }

    private void setTimeUnit(TextView tv_time, GroupMessageBaseModel baseModel, int location) {
        Date currDate = TimeUtil.getGroupchatDate(baseModel.update_time);
        if (location == 0) {
            baseModel.isshowtime = true;
        } else {
            GroupMessageBaseModel preModel = getItemObject(location - 1);
            Date pre_date = TimeUtil.getGroupchatDate(preModel.update_time);
            baseModel.isshowtime = currDate.getTime() - pre_date.getTime() > 60000;
        }
        if (baseModel.isshowtime) {
            tv_time.setVisibility(View.VISIBLE);
            tv_time.setText(TimeUtil.getGroupchatFormatTime(currDate));
        } else {
            tv_time.setText(null);
            tv_time.setVisibility(View.GONE);
        }
    }

    private void setOtherMemberType(TextView tv_identity, String member_type) {
        if (member_type == null) member_type = "";
        switch (member_type) {
            case GroupConstant.MemberType.ADMIN:
                //tv_identity.setText("群主");
                tv_identity.setText(null);
                tv_identity.setVisibility(View.GONE);
                break;
            case GroupConstant.MemberType.MEMBER:
                //tv_identity.setText("成员");
                tv_identity.setText(null);
                tv_identity.setVisibility(View.GONE);
                break;
            case GroupConstant.MemberType.NEWBIE:
                tv_identity.setText("游客");
                tv_identity.setVisibility(View.VISIBLE);
                break;
            default:
                tv_identity.setText(null);
                tv_identity.setVisibility(View.GONE);
                break;
        }
    }

    private void handleImageBrowse(GroupMessageImageModel currImageModel) {
        ArrayList<ImageModel> models = new ArrayList<>();
        for (int i = 0, z = getItemCount(); i < z; i++) {
            GroupMessageBaseModel baseModel = getItemObject(i);
            if (baseModel.dialog_type == GroupConstant.MessageType.TYPE_IMAGE) {
                GroupMessageImageModel imageModel = (GroupMessageImageModel) baseModel;
                models.add(new ImageModel(imageModel.image.image_url));
            }
        }
        int position = 0;
        String curr_image_url = currImageModel.image.image_url;
        for (int i = 0, z = models.size(); i < z; i++) {
            if (curr_image_url.equals(models.get(i).image_url)) {
                position = i;
                break;
            }
        }
        Intent intent = new Intent(context, ImagePagerActivity.class);
        intent.putParcelableArrayListExtra(ImagePagerActivity.KEY_URL_LIST, models);
        intent.putExtra(ImagePagerActivity.KEY_POS, position);
        context.startActivity(intent);
    }


    public void refreshMemberName(int show_nickname) {
        if (this.show_nickname != show_nickname) {
            this.show_nickname = show_nickname;
            notifyDataSetChanged();
        }
    }

    private void displayMemberName(TextView tv_extra_name, String name, String nickname, String uid, int gender) {
//        if (gender == 2)
//            //female
//            tv_extra_name.setTextColor(c_female);
//        else
//            tv_extra_name.setTextColor(c_male);
        if (show_nickname == 1) {
            //显示群名号
            tv_extra_name.setText(nickname);
        } else {
            if (noteMap != null) {
                String notename = noteMap.get(Integer.valueOf(uid), name);
                tv_extra_name.setText(notename);
            } else {
                tv_extra_name.setText(name);
            }
        }
    }

    private OnRedBagReceiveListener onRedBagReceiveListener;

    public void setOnRedBagReceiveListener(OnRedBagReceiveListener onRedBagReceiveListener) {
        this.onRedBagReceiveListener = onRedBagReceiveListener;
    }

    public interface OnRedBagReceiveListener {
        void onRedBagReceive(GroupMessageBaseModel baseModel, int location);
    }

    private void handleUserAvatarLongClick(GroupMessageBaseModel baseModel, int location) {
        if (this.onUserAvatarLongClickListener != null) {
            this.onUserAvatarLongClickListener.onUserAvatarLongClick(baseModel, location);
        }
    }

    private OnRecendMessageListener recendMessageListener;

    public void setOnRecendMessageListener(OnRecendMessageListener recendMessageListener) {
        this.recendMessageListener = recendMessageListener;
    }

    public interface OnRecendMessageListener {
        void onRecendMessage(GroupMessageBaseModel model, int location);
    }

    private OnMessageLongClickListener onMessageLongClickListener;

    public void setOnMessageLongClickListener(OnMessageLongClickListener onMessageLongClickListener) {
        this.onMessageLongClickListener = onMessageLongClickListener;
    }

    public interface OnMessageLongClickListener {
        void onMessageLongClick(GroupMessageBaseModel model, int location);
    }

    private OnUserAvatarLongClickListener onUserAvatarLongClickListener;

    public void setOnUserAvatarLongClickListener(OnUserAvatarLongClickListener onUserAvatarLongClickListener) {
        this.onUserAvatarLongClickListener = onUserAvatarLongClickListener;
    }

    public interface OnUserAvatarLongClickListener {
        void onUserAvatarLongClick(GroupMessageBaseModel model, int location);
    }

    /***********/
    private void loadImageWithSize(GroupMessageImageModel imageModel, ImageView iv_image) {
        String r_url;
        if (!imageModel.image.thumbnail_url.startsWith("http")) {
            r_url = ImageDownloader.Scheme.FILE.wrap(imageModel.image.thumbnail_url);
        } else {
            r_url = imageModel.image.thumbnail_url;
        }
        ImageSize size = ChatImageWrapper.computeChatImageViewSize(imageModel.image.width, imageModel.image.height);
        ViewGroup.LayoutParams params = iv_image.getLayoutParams();
        params.width = size.getWidth();
        params.height = size.getHeight();
        iv_image.setLayoutParams(params);
        imageLoader.displayImage(r_url, iv_image, _OPTIONS);
    }

    private void handleMessageLongClick(View pendingView, GroupMessageBaseModel baseModel, int location) {
        pendingView.setOnLongClickListener(v -> {
            if (this.onMessageLongClickListener != null) {
                this.onMessageLongClickListener.onMessageLongClick(baseModel, location);
            }
            return true;
        });
    }

    /***********/
    static class GroupTextViewHolderSelf extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_time)
        TextView tv_time;
        @Bind(R.id.v_resend)
        View v_resend;
        @Bind(R.id.progressBar)
        ProgressBar progressBar;
        @Bind(R.id.tv_content)
        PWTextViewCompat tv_content;

        public GroupTextViewHolderSelf(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    static class GroupTextViewHolderOther extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_time)
        TextView tv_time;
        @Bind(R.id.iv_avatar)
        ImageView iv_avatar;
        @Bind(R.id.tv_identity)
        TextView tv_identity;
        @Bind(R.id.tv_extra_name)
        TextView tv_extra_name;
        @Bind(R.id.tv_content)
        PWTextViewCompat tv_content;

        public GroupTextViewHolderOther(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    static class GroupImageViewHolderSelf extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_time)
        TextView tv_time;
        @Bind(R.id.v_resend)
        View v_resend;
        @Bind(R.id.progressBar)
        ProgressBar progressBar;
        @Bind(R.id.iv_image)
        ImageView iv_image;

        public GroupImageViewHolderSelf(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    static class GroupImageViewHolderOther extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_time)
        TextView tv_time;
        @Bind(R.id.iv_avatar)
        ImageView iv_avatar;
        @Bind(R.id.tv_identity)
        TextView tv_identity;
        @Bind(R.id.tv_extra_name)
        TextView tv_extra_name;
        @Bind(R.id.iv_image)
        ImageView iv_image;

        public GroupImageViewHolderOther(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    static class GroupGIFViewHolderSelf extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_time)
        TextView tv_time;
        @Bind(R.id.v_resend)
        View v_resend;
        @Bind(R.id.progressBar)
        ProgressBar progressBar;
        @Bind(R.id.giv_gif)
        GifImageView giv_gif;

        public GroupGIFViewHolderSelf(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    static class GroupGIFViewHolderOther extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_time)
        TextView tv_time;
        @Bind(R.id.iv_avatar)
        ImageView iv_avatar;
        @Bind(R.id.tv_identity)
        TextView tv_identity;
        @Bind(R.id.tv_extra_name)
        TextView tv_extra_name;
        @Bind(R.id.giv_gif)
        GifImageView giv_gif;

        public GroupGIFViewHolderOther(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    static class GroupREDBAGViewHolderSelf extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_time)
        TextView tv_time;
        @Bind(R.id.iv_redbag_icon)
        ImageView iv_redbag_icon;
        @Bind(R.id.tv_redbag_msg)
        TextView tv_redbag_msg;
        @Bind(R.id.v_resend)
        View v_resend;
        @Bind(R.id.progressBar)
        ProgressBar progressBar;

        public GroupREDBAGViewHolderSelf(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    static class GroupREDBAGViewHolderOther extends RecyclerView.ViewHolder {
        @Bind(R.id.iv_avatar)
        ImageView iv_avatar;
        @Bind(R.id.tv_identity)
        TextView tv_identity;
        @Bind(R.id.tv_extra_name)
        TextView tv_extra_name;
        @Bind(R.id.tv_time)
        TextView tv_time;
        @Bind(R.id.iv_redbag_icon)
        ImageView iv_redbag_icon;
        @Bind(R.id.tv_redbag_msg)
        TextView tv_redbag_msg;

        public GroupREDBAGViewHolderOther(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    static class GroupReputationREDBAGViewHolderSelf extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_time)
        TextView tv_time;
        @Bind(R.id.iv_redbag_icon)
        ImageView iv_redbag_icon;
        @Bind(R.id.tv_redbag_msg)
        TextView tv_redbag_msg;
        @Bind(R.id.v_resend)
        View v_resend;
        @Bind(R.id.progressBar)
        ProgressBar progressBar;

        public GroupReputationREDBAGViewHolderSelf(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    static class GroupReputationREDBAGViewHolderOther extends RecyclerView.ViewHolder {
        @Bind(R.id.iv_avatar)
        ImageView iv_avatar;
        @Bind(R.id.tv_identity)
        TextView tv_identity;
        @Bind(R.id.tv_extra_name)
        TextView tv_extra_name;
        @Bind(R.id.tv_time)
        TextView tv_time;
        @Bind(R.id.iv_redbag_icon)
        ImageView iv_redbag_icon;
        @Bind(R.id.tv_redbag_msg)
        TextView tv_redbag_msg;

        public GroupReputationREDBAGViewHolderOther(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    static class GroupREDBAGTipViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_decoration)
        TextView tv_decoration;
        @Bind(R.id.tv_time)
        TextView tv_time;

        public GroupREDBAGTipViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    static class GroupRepuREDBAGTipViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_decoration)
        TextView tv_decoration;
        @Bind(R.id.tv_time)
        TextView tv_time;

        public GroupRepuREDBAGTipViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    static class GroupDecorationViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_decoration)
        TextView tv_decoration;
        @Bind(R.id.tv_time)
        TextView tv_time;

        public GroupDecorationViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    static class GroupUnKnownViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_time)
        TextView tv_time;
        @Bind(R.id.iv_avatar)
        ImageView iv_avatar;
        @Bind(R.id.tv_identity)
        TextView tv_identity;
        @Bind(R.id.tv_extra_name)
        TextView tv_extra_name;
        @Bind(R.id.tv_content)
        TextView tv_content;

        public GroupUnKnownViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    static class GroupLoadMoreViewHolder extends RecyclerView.ViewHolder {
        public GroupLoadMoreViewHolder(View itemView) {
            super(itemView);
        }
    }
}
