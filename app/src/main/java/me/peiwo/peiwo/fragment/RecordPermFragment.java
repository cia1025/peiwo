package me.peiwo.peiwo.fragment;

import me.peiwo.peiwo.R;
import me.peiwo.peiwo.activity.SoundRecordActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by chenhao on 2014-10-21 下午3:42.
 *
 * @modify:
 */
public class RecordPermFragment extends PPBaseFragment {
    public static RecordPermFragment newInstance() {
        return new RecordPermFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_record_permisstion, null);
        v.findViewById(R.id.btn_start_debug).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((SoundRecordActivity) getActivity()).setPage(1);
            }
        });
        return v;
    }

    @Override
    protected String getPageName() {
        return "通话调试";
    }
}
