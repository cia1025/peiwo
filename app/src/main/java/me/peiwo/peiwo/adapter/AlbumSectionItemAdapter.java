package me.peiwo.peiwo.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.design.widget.Snackbar;
import android.support.v4.util.SparseArrayCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.download.ImageDownloader;
import me.peiwo.peiwo.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fuhaidong on 15/12/14.
 */
public class AlbumSectionItemAdapter extends RecyclerView.Adapter<AlbumSectionItemAdapter.SectionViewHolder> {
    private static final DisplayImageOptions OPTIONS_F = new DisplayImageOptions.Builder().imageScaleType(ImageScaleType.EXACTLY).showImageOnLoading(R.drawable.ic_default_avatar).bitmapConfig(Bitmap.Config.RGB_565)
            .cacheInMemory(true).considerExifParams(true)
            .cacheOnDisk(false).build();
    private List<String> mList;
    private Context context;
    private LayoutInflater inflater;
    private ImageLoader imageLoader;
    private int max_select;
   // private SparseArrayCompat<String> selectedData;
    private ArrayList<String> newSelectedData;

    public AlbumSectionItemAdapter(Context context, List<String> mList, int max_select) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.mList = mList;
        imageLoader = ImageLoader.getInstance();
       // selectedData = new SparseArrayCompat<>();
        newSelectedData=new ArrayList<>();
        this.max_select = max_select;
    }

    @Override
    public SectionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SectionViewHolder(inflater.inflate(R.layout.item_image_list, parent, false), context);
    }

    @Override
    public void onBindViewHolder(SectionViewHolder holder, int position) {
        String path = mList.get(position);
        if (TextUtils.isEmpty(path)) {
            holder.image.setImageResource(R.drawable.icon_camera_coordinate);
            holder.selected_tag.setVisibility(View.GONE);
            holder.image.setOnClickListener(v -> {
                if (this.onPickCameraListener != null) {
                    this.onPickCameraListener.onPickCamera();
                }
            });
        } else {
            holder.selected_tag.setVisibility(View.VISIBLE);
            String uri = ImageDownloader.Scheme.FILE.wrap(path);
            imageLoader.displayImage(uri, holder.image, OPTIONS_F);
            if (newSelectedData.contains(path)) {
                holder.selected_tag.setImageResource(R.drawable.image_selected_small_s);
            } else {
                holder.selected_tag.setImageResource(R.drawable.image_selected_small_n);
            }
            holder.image.setOnClickListener(v -> {
                if (!newSelectedData.contains(path)) {
                    if (newSelectedData.size() >= max_select) {
                        Snackbar.make(v, String.format("最多选择%s张照片", max_select), Snackbar.LENGTH_SHORT).show();
                        return;
                    } else {
                        newSelectedData.add(path);
                    }
                } else if (newSelectedData.contains(path)) {
                    newSelectedData.remove(path);
                }
                this.notifyDataSetChanged();
                int totalSize = newSelectedData.size();
                if (this.onImageSelectedListener != null) {
                    onImageSelectedListener.onImageSelected(totalSize);
                }
            });

//            if (selectedData.indexOfKey(position) >= 0) {
//                holder.selected_tag.setImageResource(R.drawable.image_selected_small_s);
//            } else {
//                holder.selected_tag.setImageResource(R.drawable.image_selected_small_n);
//            }
//            holder.image.setOnClickListener(v -> {
//                if (selectedData.indexOfKey(position) < 0) {
//                    if (selectedData.size() >= max_select) {
//                        Snackbar.make(v, String.format("最多选择%s张照片", max_select), Snackbar.LENGTH_SHORT).show();
//                        return;
//                    }
//                }
//                if (selectedData.indexOfKey(position) >= 0) {
//                    selectedData.remove(position);
//                } else {
//                    selectedData.put(position, path);
//                }
//                notifyItemChanged(position);
//                if (this.onImageSelectedListener != null) {
//                    this.onImageSelectedListener.onImageSelected(selectedData.size());
//                }
//            });
        }
    }

//    public List<String> getSelectedItems() {
//        List<String> rst = new ArrayList<>();
//        for (int i = 0, z = mList.size(); i < z; i++) {
//            String path = selectedData.get(i);
//            if (path != null) {
//                rst.add(path);
//            }
//        }
//        return rst;
//    }

    public List<String> getSelectedItems() {
        ArrayList<String> rst = new ArrayList<>();
        if (newSelectedData != null)
            rst = newSelectedData;
        return rst;
    }


    @Override
    public int getItemCount() {
        return mList.size();
    }

    static class SectionViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.image)
        ImageView image;
        @Bind(R.id.selected_tag)
        ImageView selected_tag;

        public SectionViewHolder(View itemView, Context context) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            //ViewGroup.LayoutParams params = image.getLayoutParams();
            //params.height = (PWUtils.getWindowWidth(context) - PWUtils.getPXbyDP(context, 6) * 8) / 4;
            //image.setLayoutParams(params);
        }
    }

    private OnImageSelected onImageSelectedListener;

    public void setonImageSelectedListener(OnImageSelected onImageSelectedListener) {
        this.onImageSelectedListener = onImageSelectedListener;
    }

    public interface OnImageSelected {
        void onImageSelected(int total_size);
    }

    private OnPickCameraListener onPickCameraListener;

    public void setOnPickCameraListener(OnPickCameraListener onPickCameraListener) {
        this.onPickCameraListener = onPickCameraListener;
    }


    public interface OnPickCameraListener {
        void onPickCamera();
    }
}
