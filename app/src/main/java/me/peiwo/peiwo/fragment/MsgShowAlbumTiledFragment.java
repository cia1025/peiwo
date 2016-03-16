package me.peiwo.peiwo.fragment;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.activity.MsgAlbumFullScreenActivity;
import me.peiwo.peiwo.activity.MsgShowAlbumActvity;
import me.peiwo.peiwo.adapter.AlbumTiledAdapter;
import me.peiwo.peiwo.util.MsgImageKeeper;

/**
 * A simple {@link Fragment} subclass.
 */
public class MsgShowAlbumTiledFragment extends Fragment {

    private ArrayList<String> mImageUrlList = new ArrayList<>();
    private int GRID_COUNT = 4;

    @Bind(R.id.album_tiled_container)
    RecyclerView album_tiled_container;
    @Bind(R.id.msg_img_send)
    TextView msg_img_send;
    @Bind(R.id.img_selected_count)
    TextView img_selected_count;
    private AlbumTiledAdapter albumTiledAdapter;

    public MsgShowAlbumTiledFragment() {
        // Required empty public constructor
    }

    public static MsgShowAlbumTiledFragment getInstance() {
        MsgShowAlbumTiledFragment msgShowAlbumTiledFragment = new MsgShowAlbumTiledFragment();
        return msgShowAlbumTiledFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View containerView = inflater.inflate(R.layout.fragment_msg_show_album_tiled, container, false);
        ButterKnife.bind(this, containerView);
        init();
        return containerView;
    }

    private void init() {
        album_tiled_container.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), GRID_COUNT);
        album_tiled_container.setLayoutManager(gridLayoutManager);
        albumTiledAdapter = new AlbumTiledAdapter(getActivity(), mImageUrlList);
        album_tiled_container.setAdapter(albumTiledAdapter);
        albumTiledAdapter.setOnImageTagClickListener(this::setSelectedCount);


        albumTiledAdapter.setOnImageClickListener((position, imageUrlList) -> {
            Intent intent = new Intent(getActivity(), MsgAlbumFullScreenActivity.class);
            intent.putStringArrayListExtra(MsgAlbumFullScreenActivity.IMG_URL_LIST, (ArrayList<String>) imageUrlList);
            intent.putExtra(MsgAlbumFullScreenActivity.INIT_POSITION, position);
            startActivity(intent);
        });

        albumTiledAdapter.setOnTakePictureListener(this::takePicture);

        msg_img_send.setOnClickListener(v -> sendImgMsg());

        showMsgCount();

    }

    private void showMsgCount() {
        int size = MsgImageKeeper.getInstance().getImgList().size();
        if (size > 0) {
            img_selected_count.setVisibility(View.VISIBLE);
            img_selected_count.setText(String.valueOf(size));
        }
    }

    private void sendImgMsg() {
        Activity activity = getActivity();
        if (activity instanceof MsgShowAlbumActvity) {
            MsgShowAlbumActvity msgShowAlbumActvity = (MsgShowAlbumActvity) activity;
            msgShowAlbumActvity.sendImgMsg();
        }
    }

    private void takePicture() {
        FragmentActivity activity = getActivity();
        if (activity instanceof MsgShowAlbumActvity) {
            MsgShowAlbumActvity msgShowAlbumActvity = (MsgShowAlbumActvity) activity;
            msgShowAlbumActvity.takePicture();
        }
    }

    private void setSelectedCount(int selectedCount) {
        if (selectedCount > 0) {
            img_selected_count.setVisibility(View.VISIBLE);
            img_selected_count.setText(String.valueOf(selectedCount));
        } else {
            img_selected_count.setVisibility(View.GONE);
        }
    }

    public void updateImgUrlList(List<String> imgUrlList) {
        new Handler().postDelayed(() -> albumTiledAdapter.setImgUrlList(imgUrlList), 500);
        showMsgCount();
    }

    public List<String> getImgUrlList() {
        return albumTiledAdapter.getImgUrlList();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }
}
