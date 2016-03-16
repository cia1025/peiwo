package me.peiwo.peiwo.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
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
import me.peiwo.peiwo.activity.FeedFlowActivity;
import me.peiwo.peiwo.activity.ImagePagerActivity;
import me.peiwo.peiwo.activity.UserInfoActivity;
import me.peiwo.peiwo.eventbus.EventBus;
import me.peiwo.peiwo.eventbus.event.ImageModelIndexEvent;
import me.peiwo.peiwo.model.FeedFlowModel;
import me.peiwo.peiwo.util.ImageUtil;
import me.peiwo.peiwo.util.TimeUtil;
import me.peiwo.peiwo.widget.FeedFlowGridView;
import me.peiwo.peiwo.widget.FeedFlowSayHelloView;
import me.peiwo.peiwo.widget.FeedFlowSayHelloView.onImageViewViewClickListener;
import me.peiwo.peiwo.widget.FeedFlowSayHelloView.onTextViewClickListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FeedFlowAdapter extends PPBaseAdapter<FeedFlowModel> {
    private List<FeedFlowModel> mList;
    private LayoutInflater inflater;
    private ImageLoader imageLoader;
    private Handler mHandle;
    private Context context;
    private boolean isMainId = false;
    private ArrayList<String> allContentList = new ArrayList<String>();

    public FeedFlowAdapter(List<FeedFlowModel> mList, Context context, Handler mHandle) {
        super(mList);
        this.mList = mList;
        this.context = context;
        inflater = LayoutInflater.from(context);
        imageLoader = ImageLoader.getInstance();
        this.mHandle = mHandle;
    }

    private DisplayImageOptions options = ImageUtil.getRoundedOptions();


    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return mList.get(position).view_type;
    }


    private boolean isShowTopicContent = true;

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        NewTopicHolder footHolder = null;
        int viewType = getItemViewType(position);
        if (convertView == null) {
            switch (viewType) {
                case 0: {
                    convertView = inflater.inflate(R.layout.feed_flow_item, parent, false);
                    holder = new ViewHolder();
                    holder.iv_uface = (ImageView) convertView.findViewById(R.id.iv_uface);
                    holder.tv_uname = (TextView) convertView.findViewById(R.id.tv_uname);
                    holder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
                    holder.tv_concern = (TextView) convertView.findViewById(R.id.tv_concern);
                    holder.tv_content = (TextView) convertView.findViewById(R.id.tv_content);
                    holder.tv_alltext = (TextView) convertView.findViewById(R.id.tv_alltext);
                    holder.fgv_picture = (FeedFlowGridView) convertView.findViewById(R.id.fgv_picture);
                    holder.tv_position = (TextView) convertView.findViewById(R.id.tv_position);
                    holder.tv_like = (TextView) convertView.findViewById(R.id.tv_like);
                    holder.ffsh_sayhello_image = (FeedFlowSayHelloView) convertView.findViewById(R.id.ffsh_sayhello_image);
                    holder.tv_left_menu = convertView.findViewById(R.id.tv_left_menu);
                    holder.iv_topic_content = (TextView) convertView.findViewById(R.id.iv_topic_content);
                    holder.view_feed_title = convertView.findViewById(R.id.view_feed_title);
                    convertView.setTag(holder);
                }
                break;
                case 1: {
                    convertView = inflater.inflate(R.layout.feed_flow_foot_view, parent, false);
                    footHolder = new NewTopicHolder();
                    footHolder.iv_button = (Button) convertView.findViewById(R.id.btn_pub_topic);
                    convertView.setTag(footHolder);
                }
                break;
            }
        } else {
            switch (viewType) {
                case 0:
                    holder = (ViewHolder) convertView.getTag();
                    break;
                case 1:
                    footHolder = (NewTopicHolder) convertView.getTag();
                    break;

            }
        }
        if (footHolder != null) {
            footHolder.iv_button.setOnClickListener(arg0 -> {
                Intent topicIntent = new Intent(context, FeedFlowActivity.class);
                context.startActivity(topicIntent);
            });
            return convertView;
        }
        final FeedFlowModel model = mList.get(position);
        imageLoader.displayImage(model.userModel.avatar_thumbnail, holder.iv_uface, options);
        holder.iv_uface.setOnClickListener(view -> {
            Intent intent = new Intent(context, UserInfoActivity.class);
            intent.putExtra(UserInfoActivity.TARGET_UID, model.userModel.uid);
            intent.putExtra(UserInfoActivity.TARGET_NAME, model.userModel.name);
            intent.putExtra(UserInfoActivity.MESSAGE_FROM, 3);
            {
                Serializable data = model.userModel;
                intent.putExtra(UserInfoActivity.USER_INFO, data);
            }
            context.startActivity(intent);
        });
        holder.tv_uname.setText(model.userModel.name);
        holder.tv_time.setText(TimeUtil.getMsgTimeDisplay(model.getCreate_time()));

        /** 信息流文字  */
        if (isMainId) {
            holder.tv_alltext.setVisibility(View.GONE);
            if (TextUtils.isEmpty(model.getContent())) {
                holder.tv_content.setVisibility(View.GONE);
            } else {
                holder.tv_content.setText(model.getContent());
                holder.tv_content.setVisibility(View.VISIBLE);
            }
            holder.tv_content.setMaxLines(Integer.MAX_VALUE);
        } else {
            if (TextUtils.isEmpty(model.getContent())) {
                holder.tv_content.setVisibility(View.GONE);
                holder.tv_alltext.setVisibility(View.GONE);
            } else {
                holder.tv_content.setVisibility(View.VISIBLE);
                holder.tv_content.setText(model.getContent());

                final TextView contextView = holder.tv_content;
                final TextView allView = holder.tv_alltext;
                holder.tv_content.post(new Runnable() {
                    @Override
                    public void run() {
                        /** 是否显示全文  */
                        if (allContentList.contains(model.getId())) {
                            contextView.setMaxLines(Integer.MAX_VALUE);
                            allView.setText("收起");
                            allView.setVisibility(View.VISIBLE);
                        } else {
                            if (contextView.getLineCount() > 6) {
                                contextView.setMaxLines(6);
                                allView.setVisibility(View.VISIBLE);
                            } else {
                                contextView.setMaxLines(Integer.MAX_VALUE);
                                allView.setVisibility(View.GONE);
                            }
                        }
                    }
                });
                holder.tv_alltext.setOnClickListener(new MyClickListener(holder, model));
            }
        }

        /** 显示单图/多图   */
        if (model.getImageList() != null) {
            holder.fgv_picture.setVisibility(View.VISIBLE);
            holder.fgv_picture.displayImages(model.getImageList(), model.imageWidth, model.imageHeight);
        } else {
            holder.fgv_picture.setVisibility(View.GONE);
        }
        holder.fgv_picture.setOnImgItemClickListener(index -> {
            Intent intent = new Intent(context, ImagePagerActivity.class);
            //intent.putExtra(ImagePagerActivity.KEY_URL_LIST, model.getImageList());
            intent.putParcelableArrayListExtra(ImagePagerActivity.KEY_URL_LIST, model.getImageList());
            intent.putExtra(ImagePagerActivity.KEY_POS, index);
            context.startActivity(intent);
        });
        if (TextUtils.isEmpty(model.getLocation())) {
            holder.tv_position.setVisibility(View.GONE);
        } else {
            holder.tv_position.setVisibility(View.VISIBLE);
            holder.tv_position.setText(model.getLocation());
        }
        /** 显示打招呼或者点赞的人的图片列表  */
        if (model.isMy()) {
            if (model.getLikerList() != null && model.getLikerList().size() > 0) {
                holder.ffsh_sayhello_image.disPlayUserFaces(context, model.getLikerList());
            } else {
                holder.ffsh_sayhello_image.disPlayUserFaces(context, null);
            }
        } else {
            holder.ffsh_sayhello_image.disPlayTextView(context);
        }
        holder.tv_content.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View arg0) {
                new AlertDialog.Builder(context).setTitle(model.userModel.name).setItems(new String[]{"复制", "取消"}, new OnClickListener() {
                    @SuppressLint("NewApi")
                    @Override
                    public void onClick(DialogInterface view, int position) {
                        switch (position) {
                            case 0:
                                ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                                cm.setText(model.getContent());
                                break;
                        }
                    }
                }).create().show();
                return true;
            }
        });
        if (model.isMy()) {
            holder.ffsh_sayhello_image.setOnImageViewViewClickListener(new onImageViewViewClickListener() {
                @Override
                public void onImageViewClick() {
                    Message msg = mHandle.obtainMessage(FeedFlowActivity.HANDLE_MSG_WHO_LIKE_LIST);
                    msg.obj = model.getId();
                    mHandle.sendMessage(msg);
                }
            });
        } else {
            holder.ffsh_sayhello_image.setOnTextViewClickListener(new onTextViewClickListener() {
                @Override
                public void onTextViewClick() {
                    Message msg = mHandle.obtainMessage(FeedFlowActivity.HANDLE_MSG_SAY_HELLO);
                    msg.obj = model;
                    mHandle.sendMessage(msg);
                }
            });
        }

        if (!isMainId) {
            /*mHolder.feed_flow_layout.setOnClickListener(new View.OnClickListener() {
                @Override
				public void onClick(View arg0) {
					enterDetails(model);
				}
			});*/
        }

        /** 举报或者删除信息流  */
        holder.tv_left_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Message msg = mHandle.obtainMessage();
                msg.obj = position;
                if (model.isMy()) {
                    msg.what = FeedFlowActivity.HANDLE_MSG_DELETE_FEED_FLOW;
                    mHandle.sendMessage(msg);
                } else {
                    msg.what = FeedFlowActivity.HANDLE_MSG_REPORT_FEED_FLOW;
                    mHandle.sendMessage(msg);
                }
            }
        });

        if (isShowTopicContent) {
            holder.view_feed_title.setVisibility(View.VISIBLE);
            holder.iv_topic_content.setText("#" + model.getTopicContent());
        } else {
            holder.view_feed_title.setVisibility(View.GONE);
        }

        /** 判断与他的关系 */
        if (model.getIs_top() == 1) {
            holder.tv_concern.setVisibility(View.VISIBLE);
            //mHolder.tv_concern.setBackgroundResource(R.drawable.icon_top_official);
        } else {
            //mHolder.tv_concern.setBackgroundResource(R.drawable.icon_concern);
            holder.tv_concern.setVisibility(View.GONE);
//            if (model.isMy()) {
//            } else {
//                if (model.userModel.relation == 0 || model.userModel.relation == 2) {
//                    mHolder.tv_concern.setVisibility(View.VISIBLE);
//                } else {
//                    mHolder.tv_concern.setVisibility(View.GONE);
//                }
//            }
        }

        Drawable img = null;
        int showLikeNum = model.getLike_number();
        if (model.getIs_like() == 1) {
            img = context.getResources().getDrawable(R.drawable.icon_flow_like_p);
        } else {
            img = context.getResources().getDrawable(R.drawable.icon_flow_like_n);
        }
        img.setBounds(0, 0, img.getMinimumWidth(), img.getMinimumHeight());
        holder.tv_like.setCompoundDrawables(img, null, null, null);

        /** 点赞 */
        if (showLikeNum >= 1) {
            holder.tv_like.setText(String.valueOf(showLikeNum));
        } else {
            holder.tv_like.setText("赞");
        }

        holder.tv_like.setOnClickListener(new MyClickListener(position));
        holder.iv_topic_content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Message msg = mHandle.obtainMessage();
                msg.what = FeedFlowActivity.HANDLE_VIEW_TOPIC;
                msg.arg1 = model.getTopicId();
                msg.obj = model.getTopicContent();
                mHandle.sendMessage(msg);
            }
        });
        return convertView;
    }

    public void setShowTopicContent(boolean isShow) {
        isShowTopicContent = isShow;
    }

    public void setMainId(boolean isMainId) {
        this.isMainId = isMainId;
    }

    private static class ViewHolder {
        ImageView iv_uface;
        TextView tv_uname;
        TextView tv_time;
        TextView tv_concern;
        TextView tv_content;
        TextView tv_alltext;
        FeedFlowGridView fgv_picture;
        TextView tv_position;
        TextView tv_like;
        FeedFlowSayHelloView ffsh_sayhello_image;
        View tv_left_menu;
        TextView iv_topic_content;
        View view_feed_title;
    }

    private static class NewTopicHolder {
        Button iv_button;
    }

    private class MyClickListener implements View.OnClickListener {
        private ViewHolder mHolder;
        private FeedFlowModel mModel;
        private int position;

        public MyClickListener(int position) {
            this.position = position;
        }

        public MyClickListener(ViewHolder mHolder, FeedFlowModel mModel) {
            this.mHolder = mHolder;
            this.mModel = mModel;
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.tv_like: {
                    Message msg = mHandle.obtainMessage();
                    msg.obj = position;
                    msg.what = FeedFlowActivity.HANDLE_MSG_LIKE;
                    mHandle.sendMessage(msg);
                }
                break;

                case R.id.tv_alltext:
                    if (mModel.getContent().length() >= 500) {
                        enterDetails(mModel);
                        return;
                    }
                    if (allContentList.contains(mModel.getId())) {
                        mHolder.tv_content.setMaxLines(6);
                        mHolder.tv_alltext.setText("全文");
                        allContentList.remove(mModel.getId());
                    } else {
                        mHolder.tv_content.setMaxLines(Integer.MAX_VALUE);
                        mHolder.tv_alltext.setText("收起");
                        allContentList.add(mModel.getId());
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void enterDetails(FeedFlowModel model) {
        Intent intent = new Intent(context, FeedFlowActivity.class);
        intent.putExtra("feed_id", model.getId());
        ((Activity) context).startActivityForResult(intent, FeedFlowActivity.VIEW_DETAILS_CODE);
    }

}
