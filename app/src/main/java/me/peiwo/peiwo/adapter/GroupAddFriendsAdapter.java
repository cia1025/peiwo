package me.peiwo.peiwo.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.nostra13.universalimageloader.core.ImageLoader;
import me.peiwo.peiwo.BuildConfig;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.activity.CreateChatGroupActivity;
import me.peiwo.peiwo.constans.ChineseCharacter;
import me.peiwo.peiwo.constans.GroupConstant;
import me.peiwo.peiwo.eventbus.EventBus;
import me.peiwo.peiwo.model.GroupMemberModel;
import me.peiwo.peiwo.model.PWContactsModel;
import me.peiwo.peiwo.util.CustomLog;
import rx.Observable;
import rx.Subscriber;

import java.util.ArrayList;

/**
 * Created by gaoxiang on 2015/12/9.
 */
public class GroupAddFriendsAdapter extends RecyclerView.Adapter<GroupAddFriendsAdapter.FriendViewHolder> {

    private final LayoutInflater inflater;
    private final ArrayList<PWContactsModel> mWholeList;
    private final Context mContext;
    private ArrayList<PWContactsModel> mMembersToBeList = new ArrayList<>();
    private int mMemberCounts;
    private ArrayList<GroupMemberModel> mMembersList = new ArrayList<>();
    private ArrayList<PWContactsModel> mFindOutList = new ArrayList<>();

    public GroupAddFriendsAdapter(Context context, ArrayList<PWContactsModel> wholeList) {
        inflater = LayoutInflater.from(context);
        mWholeList = wholeList;
        mContext = context;
        mFindOutList.clear();
    }

    public GroupAddFriendsAdapter(Context context, ArrayList<PWContactsModel> friendsList, ArrayList<GroupMemberModel> membersList, int curMemberCounts) {
        inflater = LayoutInflater.from(context);
        mWholeList = friendsList;
        mContext = context;
        mMembersList = membersList;
        mFindOutList.clear();
        mMemberCounts = curMemberCounts;
    }

    public GroupAddFriendsAdapter(Context context, ArrayList<PWContactsModel> friendsList, ArrayList<GroupMemberModel> membersList, ArrayList<PWContactsModel> findList) {
        inflater = LayoutInflater.from(context);
        mWholeList = friendsList;
        mContext = context;
        mMembersList = membersList;
        mFindOutList = findList;
    }

    @Override
    public GroupAddFriendsAdapter.FriendViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new FriendViewHolder(inflater.inflate(R.layout.group_friend_item, parent, false));
    }

    @Override
    public void onBindViewHolder(FriendViewHolder holder, int position) {
        ImageView iv_selected = holder.iv_contact_selected;
        PWContactsModel model;
        if(BuildConfig.DEBUG) {
            CustomLog.d("onBindViewHolder. position is : " + position + ", tobe list size is : " + mMembersToBeList.size());
        }
        if(mFindOutList.size() > position) {
            model = mFindOutList.get(position);
        } else {
            model = mWholeList.get(position);
        }

        ImageLoader.getInstance().displayImage(model.avatar_thumbnail, holder.iv_u_face);
        holder.tv_uname.setText(model.name);

        //如果已经是群成员，那么就不能再次点选了
        if(mMembersList != null && mMembersList.size() != 0) {
            for (GroupMemberModel memberModel : mMembersList) {
                if (!GroupConstant.MemberType.NEWBIE.equals(memberModel.member_type) &&
                        Integer.valueOf(model.uid) == memberModel.uid) {
                    iv_selected.setImageResource(R.drawable.icon_selected_n);
                    holder.itemView.setOnClickListener(null);
                    return;
                } else {
                    iv_selected.setImageResource(0);
                }
            }
        }

        if(model.is_group_added && position == 0 && !mMembersToBeList.contains(model)) {
            mMembersToBeList.add(model);
        }
        iv_selected.setSelected(model.is_group_added);
        updateSelectImage(iv_selected);
        holder.itemView.setOnClickListener((v -> {
            CustomLog.d("onClick member to be list size is : " + mMembersToBeList.size());
            int totalSize = mMemberCounts + mMembersToBeList.size();
            //创建群组方式需要加上自己充当人数
            if(mMembersList.size() == 0)
                totalSize += 1;
            if (totalSize < CreateChatGroupActivity.GROUP_MEMBER_MAX_COUNTS) {
                iv_selected.setSelected(!iv_selected.isSelected());
                updateSelectImage(iv_selected);
                if (iv_selected.isSelected()) {
                    mMembersToBeList.add(model);
                    model.is_group_added = true;
                } else {
                    if (mMembersToBeList.contains(model)) {
                        mMembersToBeList.remove(model);
                        model.is_group_added = false;
                    }
                }
                CustomLog.d("postData. onclick tobeList size is : "+mMembersToBeList.size());
                updateSelectNameOnView();
            } else if (totalSize == CreateChatGroupActivity.GROUP_MEMBER_MAX_COUNTS) {
                if (iv_selected.isSelected()) {
                    iv_selected.setSelected(!iv_selected.isSelected());
                    updateSelectImage(iv_selected);
                    if (mMembersToBeList.contains(model)) {
                        mMembersToBeList.remove(model);
                        model.is_group_added = false;
                        updateSelectNameOnView();
                    }
                } else {
                    Snackbar.make(holder.itemView, mContext.getResources().getString(R.string.group_member_counts_limited), Snackbar.LENGTH_SHORT).show();
                    return;
                }
            } else {
                if(!iv_selected.isSelected()) {
                    Snackbar.make(holder.itemView, mContext.getResources().getString(R.string.group_member_counts_limited), Snackbar.LENGTH_SHORT).show();
                    return;
                }
            }
            EventBus.getDefault().post(new Intent(CreateChatGroupActivity.ACTION_INVOLVE_MEMBER));
        }));
    }

    @Override
    public int getItemCount() {
        if(mFindOutList.size() > 0)
            return mFindOutList.size();
        else
            return mWholeList.size();
    }

    private void updateSelectImage(ImageView iv) {
        if(iv.isSelected()) {
            iv.setImageResource(R.drawable.image_selected_small_s);
        } else {
            iv.setImageResource(0);
        }
    }

    private void updateSelectNameOnView() {
        if(mContext instanceof CreateChatGroupActivity) {
            CreateChatGroupActivity act = (CreateChatGroupActivity) mContext;
            StringBuilder stringBuilder = new StringBuilder();
            String sep = ChineseCharacter.CHINESE_SEPERATOR;
            Observable.from(mMembersToBeList).subscribe(new Subscriber<PWContactsModel>() {
                @Override
                public void onCompleted() {
                    if(stringBuilder.length() > 0) {
                        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                    }
                    act.showTobeNameList(stringBuilder.toString());

                }

                @Override
                public void onError(Throwable e) {

                }

                @Override
                public void onNext(PWContactsModel pwContactsModel) {
                    stringBuilder.append(pwContactsModel.name).append(sep);
                }
            });
        }
    }

    public ArrayList<PWContactsModel> getMembersToBeList() {
        return mMembersToBeList;
    }

    public ArrayList<GroupMemberModel> getmMembersList() {
        return mMembersList;
    }

    static class FriendViewHolder extends RecyclerView.ViewHolder{
        private ImageView iv_u_face;
        private ImageView iv_contact_selected;
        private TextView tv_uname;
        public FriendViewHolder(View itemView) {
            super(itemView);
            iv_u_face = (ImageView) itemView.findViewById(R.id.iv_u_face);
            iv_contact_selected = (ImageView) itemView.findViewById(R.id.iv_contact_selected);
            tv_uname = (TextView) itemView.findViewById(R.id.tv_uname);
        }
    }
}
