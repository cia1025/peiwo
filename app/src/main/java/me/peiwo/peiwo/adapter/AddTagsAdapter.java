package me.peiwo.peiwo.adapter;

import java.util.ArrayList;

import me.peiwo.peiwo.R;
import me.peiwo.peiwo.activity.AddTagsActivity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AddTagsAdapter extends BaseAdapter {
	private Context mContext;
	private ArrayList<TagModel> tagsList = null;

	private Handler mHandle;
	public AddTagsAdapter(Context context, ArrayList<TagModel> tagsList, Handler mHandle) {
		mContext = context;
		this.tagsList = tagsList;
		this.mHandle = mHandle;
	}

	@Override
	public int getCount() {
		return tagsList.size();
	}

	@Override
	public Object getItem(int arg0) {
		return tagsList.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public View getView(final int arg0, View view, ViewGroup arg2) {
		ViewHolder holder = null;
		if (view == null) {
			view = LayoutInflater.from(mContext).inflate(R.layout.add_tags_item, arg2, false);
			holder = new ViewHolder();
			holder.tags_item_image = (ImageView) view.findViewById(R.id.tags_item_image);
			holder.tags_item_text = (TextView) view.findViewById(R.id.tags_item_text);
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}
		
		TagModel model = tagsList.get(arg0);
		holder.tags_item_text.setText(model.tagContent);
		holder.tags_item_image.setVisibility(model.isSelected ? View.VISIBLE : View.GONE);
		
		view.setOnClickListener(view1 -> {
            Message msg = mHandle.obtainMessage(AddTagsActivity.HANDLE_SELECT_TAGS);
            msg.arg1 = arg0;
            mHandle.sendMessage(msg);
        });
		return view;
	}

	static class ViewHolder {
		ImageView tags_item_image;
		TextView tags_item_text;
	}
	
	public static class TagModel {
		public String tagContent;
		public boolean isSelected;
		public int id = 0;
		public TagModel(String tagContent, boolean isSelected) {
			this.tagContent = tagContent;
			this.isSelected = isSelected;
		}
		public TagModel(String tagContent, boolean isSelected, int id) {
			this.tagContent = tagContent;
			this.isSelected = isSelected;
			this.id = id;
		}
	}
}
