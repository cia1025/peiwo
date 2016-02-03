package me.peiwo.peiwo.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.nostra13.universalimageloader.core.ImageLoader;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import me.peiwo.peiwo.BuildConfig;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.activity.*;
import me.peiwo.peiwo.constans.GroupConstant;
import me.peiwo.peiwo.db.MsgDBCenterService;
import me.peiwo.peiwo.eventbus.EventBus;
import me.peiwo.peiwo.model.GroupMemberModel;
import me.peiwo.peiwo.model.GroupMessageModel;
import me.peiwo.peiwo.model.PWUserModel;
import me.peiwo.peiwo.model.TabfindGroupModel;
import me.peiwo.peiwo.model.groupchat.GroupMessageDecorationModel;
import me.peiwo.peiwo.net.ApiRequestWrapper;
import me.peiwo.peiwo.net.AsynHttpClient;
import me.peiwo.peiwo.net.MsgStructure;
import me.peiwo.peiwo.util.CustomLog;
import me.peiwo.peiwo.util.UserManager;
import me.peiwo.peiwo.util.group.RongMessageParse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

import java.util.ArrayList;

/**
 * Created by gaoxiang on 2015/12/9.
 */
public class GroupMembersNewbiesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final LayoutInflater inflater;
    private final ArrayList<GroupMemberModel> mList;
    private ArrayList<GroupMemberModel> mFindOutList = new ArrayList<>();
    private final Context mContext;
    private TabfindGroupModel mGroupModel;
    private int self_uid;
    private ImageLoader imageLoader;

    public GroupMembersNewbiesAdapter(Context context, ArrayList<GroupMemberModel> list, TabfindGroupModel model) {
        inflater = LayoutInflater.from(context);
        mList = list;
        mContext = context;
        mGroupModel = model;
        self_uid = UserManager.getUid(context);
        imageLoader = ImageLoader.getInstance();
        mFindOutList.clear();
    }

    public GroupMembersNewbiesAdapter(Context context, ArrayList<GroupMemberModel> list, ArrayList<GroupMemberModel> findOutList, TabfindGroupModel model) {
        inflater = LayoutInflater.from(context);
        mList = list;
        mFindOutList = findOutList;
        mContext = context;
        mGroupModel = model;
        self_uid = UserManager.getUid(context);
        imageLoader = ImageLoader.getInstance();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new FriendViewHolder(inflater.inflate(R.layout.group_friend_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        GroupMemberModel model = mList.get(position);
        if(mFindOutList.size() > position) {
            model = mFindOutList.get(position);
        }

        FriendViewHolder friendHolder = (FriendViewHolder)holder;
        if(mContext instanceof GroupMembersNewbiesActivity && (position == 0 || model.isFirstNewbie)) {
            friendHolder.layout_identify_tag.setVisibility(View.VISIBLE);
            if(BuildConfig.DEBUG) {
                CustomLog.d("onBindViewHolder. mFindOutList size is : "+(mFindOutList.size()));
                CustomLog.d("onBindViewHolder. total num is : "+mGroupModel.total_number+", member num is : "+mGroupModel.member_number);
            }
            if(position == 0 && !GroupConstant.MemberType.NEWBIE.equals(model.member_type)) {
                int number = mGroupModel.member_number;
                //成员搜索结果
                if (mFindOutList.size() > 0) {
                    number = 0;
                    for (GroupMemberModel memberModel : mFindOutList) {
                        if(GroupConstant.MemberType.MEMBER.equals(memberModel.member_type)
                                || GroupConstant.MemberType.ADMIN.equals(memberModel.member_type)) {
                            ++number;
                        }
                    }
                }
                friendHolder.tv_identify_tag.setText(String.format(mContext.getResources().getString(R.string.members_with_counts), number));
            } else{
                int number = mGroupModel.total_number - mGroupModel.member_number;
                //游客搜索结果
                if (mFindOutList.size() > 0) {
                    number = 0;
                    for (GroupMemberModel newbieModel : mFindOutList) {
                        if(GroupConstant.MemberType.NEWBIE.equals(newbieModel.member_type)) {
                            ++number;
                        }
                    }
                }
                friendHolder.tv_identify_tag.setText(String.format(mContext.getResources().getString(R.string.newbies_with_counts), number));
            }
            //重置搜索结果，将搜索结果中的第一个游客标签清除
            if(mFindOutList.size() > 0 && GroupConstant.MemberType.NEWBIE.equals(model.member_type)) {
                model.isFirstNewbie = false;
            }
        } else {
            friendHolder.layout_identify_tag.setVisibility(View.GONE);
        }
        imageLoader.displayImage(model.avatar, friendHolder.iv_u_face);
        friendHolder.tv_uname.setText(model.nickname);
        friendHolder.iv_contact_selected.setImageResource(R.drawable.delete_selector);
        if (needShowTick(model)) {
            friendHolder.iv_contact_selected.setVisibility(View.VISIBLE);
        } else {
            friendHolder.iv_contact_selected.setVisibility(View.GONE);
        }
        GroupMemberModel final_model = model;
        if (GroupConstant.MemberType.ADMIN.equals(model.member_type)) {
            friendHolder.iv_contact_selected.setVisibility(View.VISIBLE);
            friendHolder.iv_contact_selected.setImageResource(R.drawable.icon_group_leader);
            friendHolder.iv_contact_selected.setOnClickListener(null);
        } else {
            friendHolder.iv_contact_selected.setOnClickListener((v -> kickoutPeople(final_model, position)));
        }
        friendHolder.layout_person_item.setOnClickListener((v -> {
            if (this.onMemberSelectedListener != null) {
                this.onMemberSelectedListener.onMemberSelected(final_model);
            }
        }));

        if(mContext instanceof GroupHomePageActvity || mContext instanceof GroupExhibitionActivity) {
            CustomLog.d("instanceof position is : "+position+", mList size is : "+mList.size());
            if(position == 2 || position == mList.size() - 1) {
                friendHolder.view_seperator.setVisibility(View.GONE);
            }
        }
    }

    private void kickoutPeople(GroupMemberModel model, int location) {
        Resources res = mContext.getResources();
        AlertDialog.Builder localBuilder = new AlertDialog.Builder(mContext);
        localBuilder.setMessage(res.getString(R.string.kick_this_person));
//        localBuilder.setTitle(res.getString(R.string.prompt));
        localBuilder.setPositiveButton(res.getString(R.string.ok), (dialog, which) -> {
            BaseActivity activity = (BaseActivity) mContext;
            activity.showAnimLoading();
            ArrayList<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair(GroupHomePageActvity.KEY_GROUP_ID, mGroupModel.group_id));
            params.add(new BasicNameValuePair(GroupHomePageActvity.KEY_GROUP_MEMBER_ID, String.valueOf(model.uid)));
            ApiRequestWrapper.openAPIPOST(mContext, params, AsynHttpClient.API_KICK_OUT_PEOPLE_IN_GROUP, new MsgStructure() {

                @Override
                public void onReceive(JSONObject data) {
                    CustomLog.d("kickoutPeople. data is : " + data);
//                    --mGroupModel.total_number;
//                    if(GroupConstant.MemberType.MEMBER.equals(model.member_type)) {
//                        --mGroupModel.member_number;
//                    }
                    Observable.just(data).observeOn(AndroidSchedulers.mainThread()).subscribe(o -> {
                        activity.dismissAnimLoading();
//                        activity.showToast(activity, mContext.getString(R.string.kickout_person_with_username, model.nickname));
                        /**send rong message**/
                        String kicker = UserManager.getPWUser(mContext).name;
                        String rst = model.nickname + mContext.getString(R.string.kickout_person_with_username, kicker);
                        PWUserModel pwUserModel = UserManager.getPWUser(mContext);
                        String body = RongMessageParse.encodeDecorationMessageBody(rst, pwUserModel, mGroupModel, model);


                        GroupMessageModel messageModel = new GroupMessageModel(body);
                        RongIMClient.getInstance().sendMessage(Conversation.ConversationType.GROUP, mGroupModel.group_id, messageModel, rst, null, new RongIMClient.SendMessageCallback() {
                            @Override
                            public void onError(Integer messageId, RongIMClient.ErrorCode errorCode) {
                            }

                            @Override
                            public void onSuccess(Integer messageId) {
                                GroupMessageDecorationModel decorationModel = RongMessageParse.decodeDecorationObjectSelf(body, messageId, GroupMessageDecorationModel.class);
                                MsgDBCenterService.getInstance().insertDialogsWithGroupchat(decorationModel);
                            }
                        }, new RongIMClient.ResultCallback<Message>() {
                            @Override
                            public void onSuccess(Message message) {

                            }

                            @Override
                            public void onError(RongIMClient.ErrorCode errorCode) {
                            }
                        });
                        int newbie_counts = mGroupModel.total_number - mGroupModel.member_number;
                        CustomLog.d("kickoutPeople. location is : " + location + ", newbie counts is : " + newbie_counts);
                        /**send rong message**/
                        //当踢出的是有游客标签的model时候
                        if (model.isFirstNewbie && mList.size() >= location + 1 && newbie_counts > 1) {
                            GroupMemberModel nextNewbie = mList.get(location + 1);
                            nextNewbie.isFirstNewbie = true;
                            notifyItemChanged(location + 1);
                        }

                        --mGroupModel.total_number;
                        if (GroupConstant.MemberType.MEMBER.equals(model.member_type)) {
                            --mGroupModel.member_number;
                            notifyItemChanged(0);
                        }
                        mList.remove(location);
                        notifyItemRemoved(location);
                        notifyDataSetChanged();
                        Intent intent = new Intent(GroupChatActivity.ACTION_POST_MESSAGE);
                        intent.putExtra(GroupChatActivity.K_POST_MESSAGE_TYPE, GroupConstant.MessageType.TYPE_DECORATION);
                        intent.putExtra(GroupChatActivity.K_POST_MESSAGE_DATA, body);
                        intent.putExtra("total_number", mGroupModel.total_number);
                        EventBus.getDefault().post(intent);
                    });
                }

                @Override
                public void onError(int error, Object ret) {
                    CustomLog.d("kickoutPeople. error is : " + error);
                    Observable.just(error).observeOn(AndroidSchedulers.mainThread()).subscribe(integer -> activity.dismissAnimLoading());
                }
            });
        });
        localBuilder.setNegativeButton(res.getString(R.string.cancel), null);
        localBuilder.create().show();
    }

    @Override
    public int getItemCount() {
        if(mFindOutList.size() > 0)
            return mFindOutList.size();
        else
            return mList.size();
    }

    private GroupMembersNewbiesActivity.State mState;

    public void setState(GroupMembersNewbiesActivity.State state) {
        mState = state;
    }

    private boolean needShowTick(GroupMemberModel model) {
        if (mContext instanceof GroupHomePageActvity) {
            return false;
        }
        if (mState == null || mState == GroupMembersNewbiesActivity.State.DEFAULT)
            return false;
        //int mUid = UserManager.getUid(mContext);
        //我自己在组中的身份
        switch (mGroupModel.member_type) {
            case GroupConstant.MemberType.ADMIN:
                return model.uid != self_uid;
            case GroupConstant.MemberType.MEMBER:
                return GroupConstant.MemberType.NEWBIE.equals(model.member_type);
            case GroupConstant.MemberType.NEWBIE:
                return false;
        }
        return true;
    }

    static class FriendViewHolder extends RecyclerView.ViewHolder {
        private ImageView iv_u_face;
        private ImageView iv_contact_selected;
        private TextView tv_uname;
        private TextView tv_identify_tag;
        private RelativeLayout layout_person_item;
        private LinearLayout layout_identify_tag;
        private View view_seperator;

        public FriendViewHolder(View itemView) {
            super(itemView);
            iv_u_face = (ImageView) itemView.findViewById(R.id.iv_u_face);
            tv_uname = (TextView) itemView.findViewById(R.id.tv_uname);
            iv_contact_selected = (ImageView) itemView.findViewById(R.id.iv_contact_selected);
            tv_identify_tag = (TextView) itemView.findViewById(R.id.tv_identify_tag);
            layout_person_item = (RelativeLayout) itemView.findViewById(R.id.layout_person_item);
            layout_identify_tag = (LinearLayout) itemView.findViewById(R.id.layout_identify_tag);
            view_seperator = itemView.findViewById(R.id.view_seperator);
        }
    }

    private OnMemberSelectedListener onMemberSelectedListener;

    public void setOnMemberSelectedListener(OnMemberSelectedListener onMemberSelectedListener) {
        this.onMemberSelectedListener = onMemberSelectedListener;
    }

    public interface OnMemberSelectedListener {
        void onMemberSelected(GroupMemberModel memberModel);
    }
}
