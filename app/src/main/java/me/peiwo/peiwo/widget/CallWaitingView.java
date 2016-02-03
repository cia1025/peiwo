package me.peiwo.peiwo.widget;

import me.peiwo.peiwo.R;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Animatable;
import android.os.Bundle;
import android.widget.ImageView;

/**
 * Created by fuhaidong on 14/11/25.
 */
public class CallWaitingView extends Dialog {

    private ImageView iv_calling;

    public CallWaitingView(Context context) {
        super(context, R.style.AnimDialogLoading);
    }

    public CallWaitingView(Context context, int theme) {
        super(context, R.style.AnimDialogLoading);
    }

    protected CallWaitingView(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_call_waiting);
        iv_calling = (ImageView) findViewById(R.id.iv_calling);
        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                try {
                    if (iv_calling != null) {
                        Animatable animatable = (Animatable) iv_calling.getDrawable();
                        animatable.stop();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void show() {
        super.show();
        try {
            if (iv_calling != null) {
                Animatable animatable = (Animatable) iv_calling.getDrawable();
                if (animatable.isRunning())
                    return;
                animatable.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
