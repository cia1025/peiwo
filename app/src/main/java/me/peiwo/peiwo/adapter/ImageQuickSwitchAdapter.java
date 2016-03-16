package me.peiwo.peiwo.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.design.widget.Snackbar;
import android.support.v4.util.SparseArrayCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.download.ImageDownloader;

import me.peiwo.peiwo.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fuhaidong on 15/10/12.
 */
public class ImageQuickSwitchAdapter extends RecyclerView.Adapter<ImageQuickSwitchAdapter.ImageQuickViewHolder> {
    private static final DisplayImageOptions OPTIONS_F = new DisplayImageOptions.Builder().imageScaleType(ImageScaleType.EXACTLY).showImageOnLoading(R.drawable.ic_default_avatar).bitmapConfig(Bitmap.Config.RGB_565)
            .cacheInMemory(false).considerExifParams(true)
            .cacheOnDisk(false).build();
    private LayoutInflater inflater;
    private List<String> mList;
    private ImageLoader imageLoader;
    private SparseArrayCompat<String> selectedData;

    public ImageQuickSwitchAdapter(Context context, List<String> mList) {
        inflater = LayoutInflater.from(context);
        this.mList = mList;
        imageLoader = ImageLoader.getInstance();
        selectedData = new SparseArrayCompat<>();
    }

    @Override
    public ImageQuickViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        return new ImageQuickViewHolder(inflater.inflate(R.layout.layout_img_quick_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ImageQuickViewHolder holder, final int position) {
        if (mList == null) return;
        final String imageItem = mList.get(position);
        imageLoader.displayImage(ImageDownloader.Scheme.FILE.wrap(imageItem), holder.iv_image, OPTIONS_F);
        if (position != 0) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.iv_image.getLayoutParams();
            params.leftMargin = 10;
            holder.iv_image.setLayoutParams(params);
        }
        holder.iv_switcher.setVisibility(selectedData.indexOfKey(position) >= 0 ? View.VISIBLE : View.GONE);
        holder.iv_image.setOnClickListener(v -> {
            if (selectedData.indexOfKey(position) < 0) {
                if (selectedData.size() >= 5) {
                    Snackbar.make(v, "最多选择5张照片", Snackbar.LENGTH_SHORT).show();
                    return;
                }
            }
            if (selectedData.indexOfKey(position) >= 0) {
                selectedData.remove(position);
            } else {
                selectedData.put(position, imageItem);
            }
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }

    public void resetImageCount() {
        selectedData.clear();
    }

    public List<String> getSelectedImages() {
        List<String> rst = new ArrayList<>();
        for (int i = 0, z = mList.size(); i < z; i++) {
            String path = selectedData.get(i);
            if (path != null) {
                rst.add(path);
            }
        }
        return rst;
    }

    public void clearSelectedData() {
        if (selectedData.size() > 0)
            selectedData.clear();
        notifyDataSetChanged();
    }

    public static class ImageQuickViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_image;
        ImageView iv_switcher;

        public ImageQuickViewHolder(View itemView) {
            super(itemView);
            iv_image = (ImageView) itemView.findViewById(R.id.iv_image);
            iv_switcher = (ImageView) itemView.findViewById(R.id.iv_switcher);
        }
    }
}
