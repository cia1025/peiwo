package me.peiwo.peiwo.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import me.peiwo.peiwo.fragment.ExpressionPanelFragment;

/**
 * Created by fuhaidong on 15/11/18.
 */
public class ExpressionPanelAdapter extends FragmentPagerAdapter {
    private int max_page;

    public ExpressionPanelAdapter(FragmentManager fm, int max_page) {
        super(fm);
        this.max_page = max_page;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return ExpressionPanelFragment.newInstance(ExpressionPanelFragment.EXPRESSION_TYPE_EMOTION);

            case 1:
                return ExpressionPanelFragment.newInstance(ExpressionPanelFragment.EXPRESSION_TYPE_GIF);
        }
        return null;
    }

    @Override
    public int getCount() {
        return max_page;
    }
}
