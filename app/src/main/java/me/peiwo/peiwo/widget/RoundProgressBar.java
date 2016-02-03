package me.peiwo.peiwo.widget;

import me.peiwo.peiwo.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by chenhao on 2014-10-21 下午6:52.
 */

public class RoundProgressBar extends View {
    private Paint paint;
    private int roundColor;
    private int roundProgressColor;
    private float roundWidth;
    private int max;
    private int progress;

    public RoundProgressBar(Context context) {
        this(context, null);
    }

    public RoundProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }


    public RoundProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        paint = new Paint();
        TypedArray mTypedArray = context.obtainStyledAttributes(attrs,
                R.styleable.RoundProgressBar);
        //获取自定义属性和默认值
        roundProgressColor = mTypedArray.getColor(R.styleable.RoundProgressBar_roundProgressColor, R.color.progress);
        roundWidth = mTypedArray.getDimension(R.styleable.RoundProgressBar_roundWidth, 20);
        max = mTypedArray.getInteger(R.styleable.RoundProgressBar_max, 100);
        mTypedArray.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int centre = getWidth() / 2; //获取圆心的x坐标
        int radius = (int) (centre - 20 - roundWidth / 2); //圆环的半径
        paint.setAntiAlias(true);  //消除锯齿
        paint.setColor(roundColor);
        canvas.drawCircle(centre, centre, radius, paint); //画出圆环
        paint.setStrokeWidth(0);
        paint.setStrokeWidth(roundWidth); //设置圆环的宽度
        paint.setColor(roundProgressColor);  //设置进度的颜色
        RectF oval = new RectF(centre - radius, centre - radius, centre
                + radius, centre+radius); //用于定义的圆弧的形状和大小的界限
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawArc(oval, 270, 360 * progress / max, false, paint);  //根据进度画圆弧
    }

    public synchronized int getMax() {
        return max;
    }

    /**
     * 设置进度的最大值
     *
     * @param max
     */
    public synchronized void setMax(int max) {
        if (max < 0) {
            throw new IllegalArgumentException("max not less than 0");
        }
        this.max = max;
    }


    public synchronized void setProgress(int progress) {
        if (progress < 0) {
            throw new IllegalArgumentException("progress not less than 0");
        }
        if (progress > max) {
            progress = max;
        }
        if (progress <= max) {
            this.progress = progress;
            postInvalidate();
        }
    }

}