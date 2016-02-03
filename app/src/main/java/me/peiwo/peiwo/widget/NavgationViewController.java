package me.peiwo.peiwo.widget;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.sqlbrite.BriteDatabase;
import com.squareup.sqlbrite.SqlBrite;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.activity.MainActivity;
import me.peiwo.peiwo.constans.PWDBConfig;
import me.peiwo.peiwo.db.BriteDBHelperHolder;
import me.peiwo.peiwo.db.MsgDBCenterService;
import me.peiwo.peiwo.net.AsynHttpClient;
import me.peiwo.peiwo.util.PWUtils;
import rx.Observable;
import rx.Subscription;

/**
 * Created by fuhaidong on 15/10/14.
 * 新版首页导航条
 */
public class NavgationViewController extends FrameLayout implements ViewPager.OnPageChangeListener, View.OnClickListener {
    private ViewPager pager;
    private TextView tv_nav_title;
    private View iv_badge;
    private static final int INDEX_WILDCAT = 0;
    private static final int INDEX_ONLINE = 1;
    private static final int INDEX_MESSAGE = 2;
    private static final int INDEX_FRIENDS = 3;
    private Subscription subscription_friend;

    public NavgationViewController(Context context) {
        super(context);
        init();
    }

    public NavgationViewController(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NavgationViewController(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.layout_navgation_bar, this);
        tv_nav_title = (TextView) findViewById(R.id.tv_nav_title);
        tv_nav_title.setText("新声");
        tv_nav_title.setOnClickListener(this);
        View iv_wildcat = findViewById(R.id.iv_wildcat);
        View iv_online = findViewById(R.id.iv_online);
        View iv_message = findViewById(R.id.iv_message);
        View iv_friends = findViewById(R.id.iv_friends);
        iv_wildcat.setOnClickListener(this);
        iv_online.setOnClickListener(this);
        iv_message.setOnClickListener(this);
        iv_friends.setOnClickListener(this);
        findViewById(R.id.v_indicator_wildcat).setVisibility(VISIBLE);
        iv_wildcat.setSelected(true);
        iv_online.setSelected(false);
        iv_message.setSelected(false);
        iv_friends.setSelected(false);
        findViewById(R.id.v_indicator_online).setVisibility(INVISIBLE);
        findViewById(R.id.v_indicator_message).setVisibility(INVISIBLE);
        findViewById(R.id.v_indicator_friends).setVisibility(INVISIBLE);
        ImageView iv_nav_avatar = (ImageView) findViewById(R.id.iv_nav_avatar);
        iv_nav_avatar.setOnClickListener(this);
        setUserAvatar();
        iv_badge = findViewById(R.id.iv_message_bagde);
        int count = MsgDBCenterService.getInstance().getBadge();
        iv_badge.post(() -> setMessageBadge(count));
    }

    private void setFriendsCount() {
        BriteDatabase briteDatabase = BriteDBHelperHolder.getInstance().getBriteDatabase(getContext());
        String sql = String.format("select count(*) from %s where contact_state = 0", PWDBConfig.TB_PW_CONTACTS);
        Observable<SqlBrite.Query> observable = briteDatabase.createQuery(PWDBConfig.TB_PW_CONTACTS, sql);
        if (subscription_friend != null) {
            subscription_friend.unsubscribe();
            subscription_friend = null;
        }
        subscription_friend = observable.subscribe(query -> {
            int friends_count = 0;
            Cursor c = query.run();
            if (c != null) {
                if (c.moveToFirst()) {
                    friends_count = c.getInt(0);
                    c.close();
                }
            }
            if (friends_count > 0) {
                tv_nav_title.setText("友達(" + friends_count + ")");
            } else {
                tv_nav_title.setText("友達");
            }
        });
    }


    private void setUserAvatar() {
        //String avatar = UserManager.getAvatar_thumbnail(getContext());
        //ImageLoader.getInstance().displayImage(avatar, iv_nav_avatar);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        //EventBus.getDefault().register(this);
    }


    public void setViewPager(ViewPager pager) {
        if (pager != null) {
            this.pager = pager;
            pager.addOnPageChangeListener(this);
        }
    }

    @Override
    public void onPageScrolled(int i, float v, int i1) {
        //Log.i("pager", "i1 == "+i1);
    }

    @Override
    public void onPageSelected(int i) {
        changeDividerStatus(i);
        setNavTitle(i);
        switch (i) {
            case INDEX_WILDCAT:
                findViewById(R.id.v_indicator_wildcat).setVisibility(VISIBLE);
                findViewById(R.id.iv_wildcat).setSelected(true);
                findViewById(R.id.iv_online).setSelected(false);
                findViewById(R.id.iv_message).setSelected(false);
                findViewById(R.id.iv_friends).setSelected(false);
                findViewById(R.id.v_indicator_online).setVisibility(INVISIBLE);
                findViewById(R.id.v_indicator_message).setVisibility(INVISIBLE);
                findViewById(R.id.v_indicator_friends).setVisibility(INVISIBLE);
                break;
            case INDEX_ONLINE:
                findViewById(R.id.v_indicator_online).setVisibility(VISIBLE);
                findViewById(R.id.iv_online).setSelected(true);
                findViewById(R.id.iv_wildcat).setSelected(false);
                findViewById(R.id.iv_message).setSelected(false);
                findViewById(R.id.iv_friends).setSelected(false);
                findViewById(R.id.v_indicator_wildcat).setVisibility(INVISIBLE);
                findViewById(R.id.v_indicator_message).setVisibility(INVISIBLE);
                findViewById(R.id.v_indicator_friends).setVisibility(INVISIBLE);
                break;
            case INDEX_MESSAGE:
                findViewById(R.id.v_indicator_message).setVisibility(VISIBLE);
                findViewById(R.id.iv_message).setSelected(true);
                findViewById(R.id.iv_wildcat).setSelected(false);
                findViewById(R.id.iv_online).setSelected(false);
                findViewById(R.id.iv_friends).setSelected(false);
                findViewById(R.id.v_indicator_wildcat).setVisibility(INVISIBLE);
                findViewById(R.id.v_indicator_online).setVisibility(INVISIBLE);
                findViewById(R.id.v_indicator_friends).setVisibility(INVISIBLE);
                break;
            case INDEX_FRIENDS:
                findViewById(R.id.v_indicator_friends).setVisibility(VISIBLE);
                findViewById(R.id.iv_wildcat).setSelected(false);
                findViewById(R.id.iv_online).setSelected(false);
                findViewById(R.id.iv_message).setSelected(false);
                findViewById(R.id.iv_friends).setSelected(true);
                findViewById(R.id.v_indicator_wildcat).setVisibility(INVISIBLE);
                findViewById(R.id.v_indicator_online).setVisibility(INVISIBLE);
                findViewById(R.id.v_indicator_message).setVisibility(INVISIBLE);
                break;
        }
    }

    private void changeDividerStatus(int i) {
        if (i == INDEX_WILDCAT) {
            findViewById(R.id.v_line_left).setBackgroundColor(getResources().getColor(R.color.c_de2_light));
            findViewById(R.id.v_line_bottom).setBackgroundColor(getResources().getColor(R.color.c_de2_light));
            tv_nav_title.setTextColor(getResources().getColor(R.color.c_white));
        } else {
            findViewById(R.id.v_line_left).setBackgroundColor(getResources().getColor(R.color.c_de2));
            findViewById(R.id.v_line_bottom).setBackgroundColor(getResources().getColor(R.color.c_de2));
            tv_nav_title.setTextColor(getResources().getColor(R.color.text_dim_color));
        }
    }

    private void setNavTitle(int i) {
        switch (i) {
            case INDEX_WILDCAT:
                tv_nav_title.setCompoundDrawables(null, null, null, null);
                tv_nav_title.setText("新声");
                tv_nav_title.setTextColor(getResources().getColor(R.color.c_white));
                break;
            case INDEX_ONLINE:
                if (tv_nav_title.getCompoundDrawablePadding() <= 0) {
                    tv_nav_title.setCompoundDrawablePadding(PWUtils.getPXbyDP(getContext(), 2));
                }
                tv_nav_title.setTextColor(getResources().getColor(R.color.text_normal_color));
                Drawable compoundDrawable = PWUtils.getCompoundDrawable(R.drawable.arrow_down_task, getContext());
                tv_nav_title.setCompoundDrawables(null, null, compoundDrawable, null);
                updateUI(this.gender_mask, this.price_on);
                break;
            case INDEX_MESSAGE:
                tv_nav_title.setTextColor(getResources().getColor(R.color.text_normal_color));
                tv_nav_title.setCompoundDrawables(null, null, null, null);
                int unreadCount = MsgDBCenterService.getInstance().getBadge();
                if (unreadCount > 0) {
                    tv_nav_title.setText("音信(" + unreadCount + ")");
                } else {
                    tv_nav_title.setText("音信");
                }
                break;
            case INDEX_FRIENDS:
                tv_nav_title.setTextColor(getResources().getColor(R.color.text_normal_color));
                tv_nav_title.setCompoundDrawables(null, null, null, null);
                setFriendsCount();
                break;
        }
    }


    @Override
    public void onPageScrollStateChanged(int i) {
    }

    public void updateUI() {
        setNavTitle(pager.getCurrentItem());
    }

    private int gender_mask;
    private boolean price_on = false;

    public void updateUI(int mask, boolean price_on) {
        gender_mask = mask;
        this.price_on = price_on;
        if (pager.getCurrentItem() != INDEX_ONLINE) {
            return;
        }
        if (price_on) {
            tv_nav_title.setText("在线(收费)");
        } else {
            if (mask == AsynHttpClient.GENDER_MASK_FEMALE) {
                tv_nav_title.setText("在线(女)");
            } else if (mask == AsynHttpClient.GENDER_MASK_MALE) {
                tv_nav_title.setText("在线(男)");
            } else {
                tv_nav_title.setText("在线(全部)");
            }
        }
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.iv_wildcat:
                if (pager != null)
                    pager.setCurrentItem(INDEX_WILDCAT);
                break;
            case R.id.iv_online:
                if (pager != null)
                    pager.setCurrentItem(INDEX_ONLINE);
                break;
            case R.id.iv_message:
                if (pager != null)
                    pager.setCurrentItem(INDEX_MESSAGE);
                break;
            case R.id.iv_friends:
                if (pager != null)
                    pager.setCurrentItem(INDEX_FRIENDS);
                break;
            case R.id.iv_nav_avatar:
                Context context = getContext();
                if (context instanceof MainActivity) {
                    ((MainActivity) context).openDrawer();
                }
                break;
            case R.id.tv_nav_title:
                if (pager.getCurrentItem() == INDEX_ONLINE) {
                    if (getContext() instanceof MainActivity) {
                        ((MainActivity) getContext()).startRecommentFilter();
                    }
                }
                break;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (subscription_friend != null) {
            subscription_friend.unsubscribe();
        }

        //EventBus.getDefault().unregister(this);
        super.onDetachedFromWindow();
    }


    public void setMessageBadge(int count) {
        if (count > 0) {
            iv_badge.setVisibility(VISIBLE);
            if (pager.getCurrentItem() == INDEX_MESSAGE)
                tv_nav_title.setText("音信(" + count + ")");
        } else {
            iv_badge.setVisibility(GONE);
            if (pager.getCurrentItem() == INDEX_MESSAGE)
                tv_nav_title.setText("音信");
        }
    }

//    public void onEventMainThread(Intent intent) {
//        if (PWActionConfig.ACTION_USER_AVATAR_CHANGED.equals(intent.getAction())) {
//            setUserAvatar();
//        }
//    }
}
