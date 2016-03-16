package me.peiwo.peiwo.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.download.ImageDownloader;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.activity.MsgShowAlbumActvity;
import me.peiwo.peiwo.model.AblumImageModel;

/**
 * Created by jiangxiaoqiang on 16/3/4.
 */
public class AlbumFolderAdapter extends RecyclerView.Adapter<AlbumFolderAdapter.AlbumFolderViewHolder> {

    private MsgShowAlbumActvity mContext;
    private List<AblumImageModel> mAlbumFolderList = new ArrayList<>();
    private ImageLoader mImageLoder;
    private final LayoutInflater layoutInflater;


    public AlbumFolderAdapter(MsgShowAlbumActvity actvity, List<AblumImageModel> albumFolderList) {
        this.mContext = actvity;
        if (!albumFolderList.isEmpty())
            this.mAlbumFolderList = albumFolderList;
        this.mImageLoder = ImageLoader.getInstance();
        layoutInflater = LayoutInflater.from(mContext);
    }

    @Override
    public AlbumFolderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AlbumFolderViewHolder(layoutInflater.inflate(R.layout.item_album_folder, parent, false));
    }

    @Override
    public void onBindViewHolder(AlbumFolderViewHolder holder, int position) {
        AblumImageModel albumFolder = mAlbumFolderList.get(position);
        String wrapUrl = ImageDownloader.Scheme.FILE.wrap(albumFolder.topImagePath);
        mImageLoder.displayImage(wrapUrl, holder.album_folder_img);
        holder.album_folder_name.setText(albumFolder.folderName);
        holder.album_folder_count.setText(String.valueOf(albumFolder.childs.size()));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.clickAlbumFolderItem(albumFolder);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mAlbumFolderList.size();
    }

    public class AlbumFolderViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.album_folder_img)
        ImageView album_folder_img;
        @Bind(R.id.album_folder_name)
        TextView album_folder_name;
        @Bind(R.id.album_folder_count)
        TextView album_folder_count;
        View itemView;

        public AlbumFolderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
            this.itemView = itemView;
        }
    }

    public void setAlbumFolderList(List<AblumImageModel> albumFolderList) {
        if (!albumFolderList.isEmpty()) {
            mAlbumFolderList.clear();
            mAlbumFolderList.addAll(albumFolderList);
        }
        notifyDataSetChanged();
    }

    public List<AblumImageModel> getAlbumFolderList() {
        return mAlbumFolderList;
    }


}
