package me.peiwo.peiwo.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.activity.MsgShowAlbumActvity;
import me.peiwo.peiwo.adapter.AlbumFolderAdapter;
import me.peiwo.peiwo.model.AblumImageModel;

/**
 * A simple {@link Fragment} subclass.
 */
public class MsgShowAlbumFolderFragment extends Fragment {

    private ArrayList<AblumImageModel> mAlbumFolderList = new ArrayList<>();

    @Bind(R.id.album_folder_container)
    RecyclerView album_folder_container;
    private AlbumFolderAdapter albumFolderAdapter;

    public MsgShowAlbumFolderFragment() {
        // Required empty public constructor
    }

    public static MsgShowAlbumFolderFragment getInstance() {
        MsgShowAlbumFolderFragment msgShowAlbumFolderFragment = new MsgShowAlbumFolderFragment();
        return msgShowAlbumFolderFragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View containerView = inflater.inflate(R.layout.fragment_msg_show_album_folder, container, false);
        ButterKnife.bind(this, containerView);
        init();
        return containerView;
    }

    private void init() {
        album_folder_container.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        album_folder_container.setLayoutManager(linearLayoutManager);
        albumFolderAdapter = new AlbumFolderAdapter((MsgShowAlbumActvity) getActivity(), mAlbumFolderList);
        album_folder_container.setAdapter(albumFolderAdapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }

    public void updateAlbumFolderList(List<AblumImageModel> albumFolderList) {
        albumFolderAdapter.setAlbumFolderList(albumFolderList);
    }


    public boolean needFlush() {
        return mAlbumFolderList.size() == 0;
    }

}
