package me.peiwo.peiwo.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.adapter.EmotionFragmentAdapter;
import me.peiwo.peiwo.adapter.GIFFragmentAdapter;
import me.peiwo.peiwo.callback.ExpressionItemClickListener;
import me.peiwo.peiwo.model.ExpressionBaseModel;
import me.peiwo.peiwo.util.group.ExpressionData;

/**
 * Created by fuhaidong on 15/11/18.
 */
public class ExpressionPanelFragment extends Fragment implements ExpressionItemClickListener {

    public static final int EXPRESSION_TYPE_EMOTION = 1;
    public static final int EXPRESSION_TYPE_GIF = 2;
    private ExpressionItemClickListener listener;


    public static ExpressionPanelFragment newInstance(int expression_type) {
        ExpressionPanelFragment fragment = new ExpressionPanelFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("expression_type", expression_type);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_expression_panel, container, false);
        init(v);
        return v;
    }

    private void init(View parent) {
        int expression_type = getArguments().getInt("expression_type");
        RecyclerView v_recycler_expression = (RecyclerView) parent.findViewById(R.id.v_recycler_expression);
        v_recycler_expression.setHasFixedSize(true);
        //long start = SystemClock.currentThreadTimeMillis();
        if (expression_type == EXPRESSION_TYPE_EMOTION) {
            GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 7);
            v_recycler_expression.setLayoutManager(layoutManager);
            EmotionFragmentAdapter emotionFragmentAdapter = new EmotionFragmentAdapter(getActivity(), ExpressionData.getInstance(getActivity()).getEmotionModels());
            v_recycler_expression.setAdapter(emotionFragmentAdapter);
            emotionFragmentAdapter.setOnExpressionItemClickListener(this);
        } else if (expression_type == EXPRESSION_TYPE_GIF) {
            GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 4);
            v_recycler_expression.setLayoutManager(layoutManager);
            GIFFragmentAdapter gifFragmentAdapter = new GIFFragmentAdapter(getActivity(), ExpressionData.getInstance(getActivity()).getGifModels());
            v_recycler_expression.setAdapter(gifFragmentAdapter);
            gifFragmentAdapter.setOnExpressionItemClickListener(this);
        }
        //Log.i("panel", "temp == " + (SystemClock.currentThreadTimeMillis() - start));
    }


    public void setOnExpressionItemClickListener(ExpressionItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onExpressionItemClick(ExpressionBaseModel model) {
        if (this.listener != null) {
            this.listener.onExpressionItemClick(model);
        }
    }
}
