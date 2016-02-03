package me.peiwo.peiwo.widget;

import me.peiwo.peiwo.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;

import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.internal.FlipLoadingLayout;
import com.handmark.pulltorefresh.library.internal.LoadingLayout;

public class PWPullToRefreshListView extends PullToRefreshListView {

    private boolean isEnd = false;

    public PWPullToRefreshListView(Context context) {
        super(context);
    }

    public PWPullToRefreshListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void end(boolean isEnd) {
        this.isEnd = isEnd;
    }

    @Override
    protected void onRefreshing(boolean doScroll) {
        if (getCurrentMode() == Mode.PULL_FROM_END && isEnd) {
            super.onRefreshing(false);
            if (doScroll) {
                int selection = getRefreshableView().getCount() - 1;
                int scrollToY = getScrollY() - getFooterSize();

                disableLoadingLayoutVisibilityChanges();
                setHeaderScroll(scrollToY);
                getRefreshableView().setSelection(selection);
                smoothScrollTo(0);
                onRefreshComplete();
            }
        } else {
            super.onRefreshing(doScroll);
        }
    }

    protected void callRefreshListener() {
        if (getCurrentMode() == Mode.PULL_FROM_END && isEnd) {
            return;
        }
        super.callRefreshListener();
    }

    protected LoadingLayout createLoadingLayout(Context context, Mode mode, TypedArray attrs) {
        LoadingLayout layout = createLoadingLayout(context, mode, getPullToRefreshScrollDirection(), attrs);
        layout.setVisibility(View.INVISIBLE);
        return layout;
    }

    private LoadingLayout createLoadingLayout(Context context, final Mode mode, final Orientation scrollDirection, TypedArray attrs) {
        return new GLoadingLayout(context, mode, scrollDirection, attrs);
    }


    private class GLoadingLayout extends FlipLoadingLayout {

        private CharSequence mEndText;

        public GLoadingLayout(Context context, final Mode mode, final Orientation scrollDirection, TypedArray attrs) {
            super(context, mode, scrollDirection, attrs);
            mEndText = context.getText(R.string.pull_to_refresh_from_bottom_end_label);
        }

        @Override
        protected void pullToRefreshImpl() {
            if (mMode == Mode.PULL_FROM_END && isEnd) {
                mHeaderText.setText(mEndText);
                mHeaderImage.setVisibility(View.INVISIBLE);
                mSubHeaderText.setVisibility(View.INVISIBLE);
            } else {
                super.pullToRefreshImpl();
            }
        }

        @Override
        protected void releaseToRefreshImpl() {
            if (mMode == Mode.PULL_FROM_END && isEnd) {
                mHeaderText.setText(mEndText);
                mHeaderImage.setVisibility(View.INVISIBLE);
                mSubHeaderText.setVisibility(View.INVISIBLE);
            } else {
                super.releaseToRefreshImpl();
            }
        }

        @Override
        protected void refreshingImpl() {
            if (mMode == Mode.PULL_FROM_END && isEnd) {
                mHeaderText.setText(mEndText);
                mHeaderImage.clearAnimation();
                mHeaderImage.setVisibility(View.INVISIBLE);
                mHeaderProgress.setVisibility(View.INVISIBLE);
                mSubHeaderText.setVisibility(View.INVISIBLE);
            } else {
                super.refreshingImpl();
            }
        }

        @Override
        protected void resetImpl() {
            if (mMode == Mode.PULL_FROM_END && isEnd) {
                mHeaderText.setText(mEndText);
                mHeaderImage.clearAnimation();
                mHeaderImage.setVisibility(View.INVISIBLE);
                mHeaderProgress.setVisibility(View.INVISIBLE);
                mSubHeaderText.setVisibility(View.INVISIBLE);
            } else {
                super.resetImpl();
            }
        }

//		@Override
//		protected int getDefaultDrawableResId() {
//			return R.drawable.arrow_down;
//		}
    }
}
