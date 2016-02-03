package me.peiwo.peiwo.adapter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.Bind;
import butterknife.ButterKnife;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.activity.AlbumCompatActivity;
import me.peiwo.peiwo.model.AblumImageModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fuhaidong on 15/12/14.
 */
public class AlbumFolderFragment extends Fragment {
    @Bind(R.id.v_recycler_folder)
    RecyclerView v_recycler_folder;

    private AlbumFolderItemAdapter adapter;
    private List<AblumImageModel> mList = new ArrayList<>();

    public static AlbumFolderFragment newInstance() {
        return new AlbumFolderFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View parent = inflater.inflate(R.layout.layout_album_folder, container, false);
        ButterKnife.bind(this, parent);
        init();
        return parent;
    }

    private void init() {
        v_recycler_folder.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        v_recycler_folder.setLayoutManager(linearLayoutManager);
        adapter = new AlbumFolderItemAdapter((AlbumCompatActivity) getActivity(), mList);
        v_recycler_folder.setAdapter(adapter);
    }

    public void flushData(List<AblumImageModel> data) {
        if (this.mList.size() == 0) {
            this.mList.addAll(data);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }

    public boolean needFlush() {
        return mList.size() == 0;
    }
}
