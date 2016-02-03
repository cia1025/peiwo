package me.peiwo.peiwo.adapter;

import java.util.List;

import me.peiwo.peiwo.R;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

/**
 * Created by Dong Fuhai
 *
 * @modify:
 */

public abstract class PPBaseAdapter<T> extends BaseAdapter {
    List<? extends T> mList;

    public PPBaseAdapter(List<? extends T> mList) {
        this.mList = mList;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }

    void debugLog(String msg) {
    }

    public DisplayImageOptions getRoundOptions(boolean cacheOnDisk) {
        return new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.ic_default_avatar)
                .showImageForEmptyUri(R.drawable.ic_default_avatar)
                .showImageOnFail(R.drawable.ic_default_avatar).cacheInMemory(true)
                .cacheOnDisk(cacheOnDisk).displayer(new RoundedBitmapDisplayer(10))
                .build();
    }
}
