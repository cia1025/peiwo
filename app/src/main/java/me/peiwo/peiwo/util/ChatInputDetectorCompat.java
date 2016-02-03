package me.peiwo.peiwo.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import me.peiwo.peiwo.constans.Constans;
import me.peiwo.peiwo.widget.GroupBottomActionView;

/**
 * Created by dss886 on 15/9/26.
 */
public class ChatInputDetectorCompat {

    private static final String SHARE_PREFERENCE_NAME = Constans.SP_NAME;
    private static final String SHARE_PREFERENCE_TAG = "soft_input_height";

    private Activity mActivity;
    private InputMethodManager mInputManager;
    private SharedPreferences sp;
    private View mEmotionLayout;
    private EditText mEditText;
    private View mContentView;

    private ChatInputDetectorCompat() {
    }

    public static ChatInputDetectorCompat with(Activity activity) {
        ChatInputDetectorCompat emotionInputDetector = new ChatInputDetectorCompat();
        emotionInputDetector.mActivity = activity;
        emotionInputDetector.mInputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        emotionInputDetector.sp = activity.getSharedPreferences(SHARE_PREFERENCE_NAME, Context.MODE_PRIVATE);
        return emotionInputDetector;
    }

    public ChatInputDetectorCompat bindToContent(View contentView) {
        mContentView = contentView;
        return this;
    }

    public ChatInputDetectorCompat bindToEditText(EditText editText) {
        mEditText = editText;
        mEditText.requestFocus();
        mEditText.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP && mEmotionLayout.isShown()) {
                lockContentHeight();
                hideEmotionLayout(true);

                mEditText.postDelayed(this::unlockContentHeightDelayed, 200L);
            }
            if(event.getAction() == MotionEvent.ACTION_UP){
                if (this.textMessageButtonPerformListener != null) {
                    this.textMessageButtonPerformListener.onTextMessageButtonPerform();
                }
            }
            return false;
        });
        return this;
    }

    public ChatInputDetectorCompat bindToEmotionButton(View emotionButton) {
        emotionButton.setOnClickListener(v -> {
            if (mEmotionLayout instanceof GroupBottomActionView) {
                GroupBottomActionView groupBottomActionView = (GroupBottomActionView) mEmotionLayout;
                if (groupBottomActionView.isImageQuickSwitchShown()) {
                    if (this.emotionButtonPerformListener != null) {
                        this.emotionButtonPerformListener.onEmotionButtonPerform(true);
                    }
                    return;
                }
            }
            if (mEmotionLayout.isShown()) {
                lockContentHeight();
                hideEmotionLayout(true);
                unlockContentHeightDelayed();
                if (this.emotionButtonPerformListener != null) {
                    this.emotionButtonPerformListener.onEmotionButtonPerform(false);
                }
            } else {
                if (isSoftInputShown()) {
                    lockContentHeight();
                    showEmotionLayout();
                    unlockContentHeightDelayed();
                } else {
                    showEmotionLayout();
                }
                if (this.emotionButtonPerformListener != null) {
                    this.emotionButtonPerformListener.onEmotionButtonPerform(true);
                }
            }
        });
        return this;
    }

    public ChatInputDetectorCompat bindTextMessageButton(View textMessageButton) {
        textMessageButton.setOnClickListener(v -> {
            if (mEmotionLayout.isShown()) {
                lockContentHeight();
                hideEmotionLayout(true);

                mEditText.postDelayed(() -> mEditText.postDelayed(() -> {
                    ((LinearLayout.LayoutParams) mContentView.getLayoutParams()).weight = 1.0F;
                    showSoftInput();
                    if (this.textMessageButtonPerformListener != null) {
                        this.textMessageButtonPerformListener.onTextMessageButtonPerform();
                    }
                }, 200L), 200L);
            } else {
                showSoftInput();
                if (this.textMessageButtonPerformListener != null) {
                    this.textMessageButtonPerformListener.onTextMessageButtonPerform();
                }
            }
        });
        return this;
    }

    public ChatInputDetectorCompat bindImageQuickSwitchButton(View imageQuickSwitchButton) {
        imageQuickSwitchButton.setOnClickListener(v -> {
            if (mEmotionLayout instanceof GroupBottomActionView) {
                GroupBottomActionView groupBottomActionView = (GroupBottomActionView) mEmotionLayout;
                if (groupBottomActionView.isExpressionShown() || groupBottomActionView.consumingEvents()) {
                    if (this.imageButtonPerformListener != null) {
                        this.imageButtonPerformListener.onImageButtonPerform(true);
                    }
                    return;
                }
            }
            if (mEmotionLayout.isShown()) {
                lockContentHeight();
                hideEmotionLayout(true);
                unlockContentHeightDelayed();
                if (this.imageButtonPerformListener != null) {
                    this.imageButtonPerformListener.onImageButtonPerform(false);
                }
            } else {
                if (isSoftInputShown()) {
                    lockContentHeight();
                    showEmotionLayout();
                    unlockContentHeightDelayed();
                } else {
                    showEmotionLayout();
                }
                if (this.imageButtonPerformListener != null) {
                    this.imageButtonPerformListener.onImageButtonPerform(true);
                }
            }
        });
        return this;
    }

    public ChatInputDetectorCompat setEmotionView(View emotionView) {
        mEmotionLayout = emotionView;
        return this;
    }

    public ChatInputDetectorCompat build() {
        mActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN |
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        hideSoftInput();
        return this;
    }

    public boolean interceptBackPress() {
        if (mEmotionLayout.isShown()) {
            hideEmotionLayout(false);
            return true;
        }
        return false;
    }

    private void showEmotionLayout() {
        int softInputHeight = getSupportSoftInputHeight();
        if (softInputHeight == 0) {
            softInputHeight = sp.getInt(SHARE_PREFERENCE_TAG, 400);
        }
        hideSoftInput();
        mEmotionLayout.getLayoutParams().height = softInputHeight;
        mEmotionLayout.setVisibility(View.VISIBLE);
    }

    private void hideEmotionLayout(boolean showSoftInput) {
        if (mEmotionLayout.isShown()) {
            mEmotionLayout.setVisibility(View.GONE);
            if (showSoftInput) {
                showSoftInput();
            }
        }
    }

    private void lockContentHeight() {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mContentView.getLayoutParams();
        params.height = mContentView.getHeight();
        params.weight = 0.0F;
    }

    private void unlockContentHeightDelayed() {
        mEditText.postDelayed(() -> ((LinearLayout.LayoutParams) mContentView.getLayoutParams()).weight = 1.0F, 200L);
    }

    private void showSoftInput() {
        mEditText.requestFocus();
        mEditText.post(() -> mInputManager.showSoftInput(mEditText, 0));
    }

    public void hideSoftInput() {
        mInputManager.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
    }

    private boolean isSoftInputShown() {
        return getSupportSoftInputHeight() != 0;
    }

    private int getSupportSoftInputHeight() {
        Rect r = new Rect();
        mActivity.getWindow().getDecorView().getWindowVisibleDisplayFrame(r);
        int screenHeight = mActivity.getWindow().getDecorView().getRootView().getHeight();
        int softInputHeight = screenHeight - r.bottom;
        if (Build.VERSION.SDK_INT >= 20) {
            // When SDK Level >= 20 (Android L), the softInputHeight will contain the height of softButtonsBar (if has)
            softInputHeight = softInputHeight - getSoftButtonsBarHeight();
        }
        if (softInputHeight < 0) {
            Log.w("EmotionInputDetector", "Warning: value of softInputHeight is below zero!");
        }
        if (softInputHeight > 0) {
            sp.edit().putInt(SHARE_PREFERENCE_TAG, softInputHeight).apply();
        }
        return softInputHeight;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private int getSoftButtonsBarHeight() {
        DisplayMetrics metrics = new DisplayMetrics();
        mActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int usableHeight = metrics.heightPixels;
        mActivity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        int realHeight = metrics.heightPixels;
        if (realHeight > usableHeight) {
            return realHeight - usableHeight;
        } else {
            return 0;
        }
    }

    private TextMessageButtonPerformListener textMessageButtonPerformListener;

    public void setTextMessageButtonPerformListener(TextMessageButtonPerformListener textMessageButtonPerformListener) {
        this.textMessageButtonPerformListener = textMessageButtonPerformListener;
    }

    public interface TextMessageButtonPerformListener {
        void onTextMessageButtonPerform();
    }

    private EmotionButtonPerformListener emotionButtonPerformListener;

    public void setEmotionButtonPerformListener(EmotionButtonPerformListener emotionButtonPerformListener) {
        this.emotionButtonPerformListener = emotionButtonPerformListener;
    }

    public interface EmotionButtonPerformListener {
        void onEmotionButtonPerform(boolean isEmotionViewShown);
    }

    private ImageButtonPerformListener imageButtonPerformListener;

    public void setImageButtonPerformListener(ImageButtonPerformListener imageButtonPerformListener) {
        this.imageButtonPerformListener = imageButtonPerformListener;
    }

    public interface ImageButtonPerformListener {
        void onImageButtonPerform(boolean isEmotionViewShown);
    }

    public void showSoftInputIfNeed() {
        int height = sp.getInt(SHARE_PREFERENCE_TAG, 0);
        if (height == 0) {
            mEditText.postDelayed(() -> {
                mInputManager.showSoftInput(mEditText, 0);
                mEditText.postDelayed(this::getSupportSoftInputHeight, 600);
            }, 500);
        }
    }

}
