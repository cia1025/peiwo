package me.peiwo.peiwo.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.download.ImageDownloader;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.activity.AlbumCompatActivity;
import me.peiwo.peiwo.model.AblumImageModel;

import java.util.List;

/**
 * Created by fuhaidong on 15/12/14.
 */
public class AlbumFolderItemAdapter extends RecyclerView.Adapter<AlbumFolderItemAdapter.FolderViewHolder> {

    private List<AblumImageModel> mList;
    private AlbumCompatActivity context;
    private LayoutInflater inflater;
    private ImageLoader imageLoader;

    public AlbumFolderItemAdapter(AlbumCompatActivity context, List<AblumImageModel> mList) {
        inflater = LayoutInflater.from(context);
        this.mList = mList;
        imageLoader = ImageLoader.getInstance();
        this.context = context;
    }

    @Override
    public FolderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new FolderViewHolder(inflater.inflate(R.layout.lv_album_item, parent, false));
    }

    @Override
    public void onBindViewHolder(FolderViewHolder holder, int position) {
        AblumImageModel model = mList.get(position);
        String path = ImageDownloader.Scheme.FILE.wrap(model.topImagePath);
        imageLoader.displayImage(path, holder.iv_top_image);
        holder.tv_ablum_name.setText(model.folderName);
        holder.tv_images_count.setText(String.valueOf(model.childs.size()));
        holder.itemView.setOnClickListener(v -> context.setlectFolderItem(model));
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    static class FolderViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.iv_top_image)
        ImageView iv_top_image;
        @Bind(R.id.tv_ablum_name)
        TextView tv_ablum_name;
        @Bind(R.id.tv_images_count)
        TextView tv_images_count;

        private View itemView;

        public FolderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.itemView = itemView;
        }
    }
}
