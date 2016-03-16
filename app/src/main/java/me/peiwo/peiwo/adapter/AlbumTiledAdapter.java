package me.peiwo.peiwo.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.download.ImageDownloader;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.util.MsgImageKeeper;

/**
 * Created by jiangxiaoqiang on 16/3/4.
 */
public class AlbumTiledAdapter extends RecyclerView.Adapter<AlbumTiledAdapter.AlbumTiledViewHodler> {

    private Context mContext;
    private List<String> mImgUrlList = new ArrayList<>();
    private ImageLoader mImageLoader;
    private static final DisplayImageOptions IMAGE_OPTIONS = new DisplayImageOptions.Builder().imageScaleType(ImageScaleType.EXACTLY).showImageOnLoading(R.drawable.ic_default_avatar).bitmapConfig(Bitmap.Config.RGB_565)
            .cacheInMemory(true).considerExifParams(true)
            .cacheOnDisk(false).build();
    private final LayoutInflater layoutInflater;
    private int IMG_MAX = 5;
    private OnImageTagClickListener mOnImageTagClickListener;
    private OnImageClickListener mOnImageClickListener;
    private OnTakePictureListener mOnTakePictureListener;

    public AlbumTiledAdapter(Context context, List<String> imgUrlList) {
        this.mContext = context;
        mImgUrlList.add("");
        if (!imgUrlList.isEmpty())
            this.mImgUrlList = imgUrlList;
        mImageLoader = ImageLoader.getInstance();
        layoutInflater = LayoutInflater.from(mContext);
    }

    @Override
    public AlbumTiledViewHodler onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AlbumTiledViewHodler(layoutInflater.inflate(R.layout.item_album_tiled, parent, false));
    }

    @Override
    public void onBindViewHolder(AlbumTiledViewHodler holder, int position) {
        String imgUrl = mImgUrlList.get(position);
        if (TextUtils.isEmpty(imgUrl)) {
            holder.img_selected_tag.setVisibility(View.GONE);
            holder.img_element.setImageResource(R.drawable.icon_camera_coordinate);
            holder.img_element.setOnClickListener(v -> {
                if (mOnTakePictureListener != null) {
                    mOnTakePictureListener.onTakePicture();
                }
            });
        } else {
            holder.img_selected_tag.setVisibility(View.VISIBLE);
            String wrapUrl = ImageDownloader.Scheme.FILE.wrap(imgUrl);
            mImageLoader.displayImage(wrapUrl, holder.img_element, IMAGE_OPTIONS);
            if (MsgImageKeeper.getInstance().contains(imgUrl)) {
                holder.img_selected_tag.setImageResource(R.drawable.image_selected_small_s);
            } else {
                holder.img_selected_tag.setImageResource(R.drawable.image_selected_small_n);
            }

            holder.img_selected_tag.setOnClickListener(v -> {
                if (!MsgImageKeeper.getInstance().contains(imgUrl)) {
                    if (MsgImageKeeper.getInstance().getImgList().size() >= IMG_MAX) {
                        Snackbar.make(v, "最多选择5张照片", Snackbar.LENGTH_SHORT).show();
                        return;
                    } else {
                        // mSelectedUrlList.add(imgUrl);
                        MsgImageKeeper.getInstance().add(imgUrl);
                    }
                } else {
                    MsgImageKeeper.getInstance().remove(imgUrl);
                }
                notifyDataSetChanged();
                if (mOnImageTagClickListener != null) {
                    mOnImageTagClickListener.onImageTagClick(MsgImageKeeper.getInstance().getImgList().size());
                }
            });

            holder.img_element.setOnClickListener(v -> {
                if (mOnImageClickListener != null) {
                    List<String> mAllUrlList = mImgUrlList;
                    mOnImageClickListener.onImageClick(position, mAllUrlList);
                }
            });

        }

    }

    @Override
    public int getItemCount() {
        return mImgUrlList.size();
    }

    public void setImgUrlList(List<String> imgUrlList) {
        if (!imgUrlList.isEmpty()) {
            mImgUrlList.clear();
            mImgUrlList.add("");
            mImgUrlList.addAll(imgUrlList);
        }
        notifyDataSetChanged();
    }

    public List<String> getImgUrlList() {
        return this.mImgUrlList;
    }

    public class AlbumTiledViewHodler extends RecyclerView.ViewHolder {

        @Bind(R.id.img_element)
        ImageView img_element;
        @Bind(R.id.img_selected_tag)
        ImageView img_selected_tag;

        public AlbumTiledViewHodler(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public void setOnImageTagClickListener(OnImageTagClickListener onImageClickTagListener) {
        this.mOnImageTagClickListener = onImageClickTagListener;
    }

    public interface OnImageTagClickListener {
        void onImageTagClick(int selectedCount);
    }

    public void setOnImageClickListener(OnImageClickListener onImageClickListener) {
        this.mOnImageClickListener = onImageClickListener;
    }

    public interface OnImageClickListener {
        void onImageClick(int position, List<String> imageUrlList);
    }

    public void setOnTakePictureListener(OnTakePictureListener onTakePictureListener) {
        this.mOnTakePictureListener = onTakePictureListener;
    }

    public interface OnTakePictureListener {
        void onTakePicture();
    }


}
