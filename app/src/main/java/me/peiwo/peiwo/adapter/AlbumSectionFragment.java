package me.peiwo.peiwo.adapter;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.activity.AlbumCompatActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fuhaidong on 15/12/14.
 */
public class AlbumSectionFragment extends Fragment {

    public static AlbumSectionFragment newInstance(int max_count) {
        Bundle bundle = new Bundle();
        bundle.putInt("count", max_count);
        AlbumSectionFragment f = new AlbumSectionFragment();
        f.setArguments(bundle);
        return f;
    }


    @Bind(R.id.v_recycler_folder)
    RecyclerView v_recycler_folder;
    @Bind(R.id.btn_confirm)
    View btn_confirm;
    @Bind(R.id.tv_selected_count)
    TextView tv_selected_count;
    private List<String> mList = new ArrayList<>();
    private AlbumSectionItemAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View parent = inflater.inflate(R.layout.layout_album_section, container, false);
        ButterKnife.bind(this, parent);
        init();
        return parent;
    }

    private void init() {
        int max_count = getArguments().getInt("count");
        v_recycler_folder.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 4);
        v_recycler_folder.setLayoutManager(gridLayoutManager);
        adapter = new AlbumSectionItemAdapter(getActivity(), mList, max_count);
        v_recycler_folder.setAdapter(adapter);
        adapter.setonImageSelectedListener(this::setImageSelectedLable);
        adapter.setOnPickCameraListener(this::pickInCamera);
        btn_confirm.setOnClickListener(v -> {
            Activity activity = getActivity();
            if (activity instanceof AlbumCompatActivity) {
                AlbumCompatActivity albumCompatActivity = (AlbumCompatActivity) activity;
                List<String> rst = adapter.getSelectedItems();
                if (rst.isEmpty()) {
                    Toast.makeText(getActivity(), "请至少选择一张图片", Toast.LENGTH_SHORT).show();
                    return;
                }
                albumCompatActivity.resultAlbum((ArrayList<String>) rst);
            }
        });
    }

    private void setImageSelectedLable(int total_size) {
        if (total_size > 0) {
            tv_selected_count.setVisibility(View.VISIBLE);
            tv_selected_count.setText(String.valueOf(total_size));
        } else {
            tv_selected_count.setVisibility(View.GONE);
        }
    }


    private void pickInCamera() {
        Activity activity = getActivity();
        if (activity instanceof AlbumCompatActivity) {
            AlbumCompatActivity albumCompatActivity = (AlbumCompatActivity) activity;
            albumCompatActivity.pickInCamera();
        }
    }

    public void flushData(List<String> data) {
        new Handler().postDelayed(() -> {
            mList.clear();
            mList.add("");
            mList.addAll(data);
            adapter.notifyDataSetChanged();
        }, 500);
    }

    public List<String> getSectionData() {
        return mList;
    }
}
