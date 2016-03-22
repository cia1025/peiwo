package me.peiwo.peiwo.widget;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.adapter.ImageQuickSwitchAdapter;
import me.peiwo.peiwo.util.group.ChatImageWrapper;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fuhaidong on 15/10/12.
 */
public class ImageQuickSwitchView extends FrameLayout {
    private List<String> mList;
    private ImageQuickSwitchAdapter mAdapter;
    private Subscription subscription;


    public ImageQuickSwitchView(Context context) {
        super(context);
        init();
    }

    public ImageQuickSwitchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ImageQuickSwitchView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mList = new ArrayList<>();
        RecyclerView mRecyclerView = new RecyclerView(getContext());
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new ImageQuickSwitchAdapter(getContext(), mList);
        mRecyclerView.setAdapter(mAdapter);
        addView(mRecyclerView, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        ImageView iv_switch_all_images = new ImageView(getContext());
        iv_switch_all_images.setImageResource(R.drawable.ic_switch_all_images);
        iv_switch_all_images.setOnClickListener(v -> {
            if (this.listener != null) {
                this.listener.onMoreActionClick();
            }
        });
        LayoutParams img_all_switch_params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        img_all_switch_params.gravity = Gravity.LEFT | Gravity.BOTTOM;
        img_all_switch_params.setMargins(10, 0, 0, 10);
        addView(iv_switch_all_images, img_all_switch_params);
    }

    private void loadSectionImages() {
        unSubscribed();
        Observable<List<String>> query = ChatImageWrapper.scanExternalImages(getContext(), true);
        subscription = query.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<List<String>>() {
            @Override
            public void onCompleted() {
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(List<String> items) {
                mList.clear();
                mList.addAll(items);
            }
        });
    }

    private void unSubscribed() {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
        subscription = null;
    }

    @Override
    protected void onDetachedFromWindow() {
        unSubscribed();
        super.onDetachedFromWindow();
    }

    @Override
    public void setVisibility(int visibility) {
        if (visibility == getVisibility()) return;
        if (visibility == GONE) {
            removeData();
        }
        super.setVisibility(visibility);
        if (visibility == VISIBLE) {
            resetData();
        }

    }

    private void resetData() {
        post(this::loadSectionImages);
    }

    private void removeData() {
        mList.clear();
        mAdapter.notifyDataSetChanged();
        mAdapter.resetImageCount();
        unSubscribed();
    }

    //返回String path，让quickview关闭
    public List<String> getSelectedImages() {
        return mAdapter.getSelectedImages();
    }

    private OnMoreActionClickListener listener;

    public void setOnMoreActionClickListener(OnMoreActionClickListener listener) {
        this.listener = listener;
    }

    public interface OnMoreActionClickListener {
        void onMoreActionClick();
    }
}
