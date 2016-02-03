package me.peiwo.peiwo.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
  
/** 
 * @author huanglong 2013-5-28 自定义自动换行LinearLayout 
 */  
public class FixGridLayout extends ViewGroup {
	private final static int SPACE = 10;
	private boolean isFirst = true;
    public FixGridLayout(Context context) {  
        super(context);  
    }  
  
    public FixGridLayout(Context context, AttributeSet attrs) {  
        super(context, attrs);  
    }  
  
    public FixGridLayout(Context context, AttributeSet attrs, int defStyle) {  
        super(context, attrs, defStyle);  
    }  
  
    public void setWidth(int w) {
    	LayoutParams params = getLayoutParams();
    	params.width = w;
    	params.height = LayoutParams.WRAP_CONTENT;
    	setLayoutParams(params);
    }
    /** 
     * 控制子控件的换行 
     */  
    @Override  
    protected void onLayout(boolean changed, int l, int t, int r, int b) {  
        int x = 0;  
        int y = 0;  
        int height = 0;  
        int count = getChildCount();  
        for (int j = 0; j < count; j++) {
            final View childView = getChildAt(j);  
            // 获取子控件Child的宽高  
            int w = childView.getMeasuredWidth();  
            int h = childView.getMeasuredHeight();  
            // 计算子控件的顶点坐标  
            if (x + w > getWidth()) {
            	x = 0;
            	y = height;
            }
            int left = x;
            int top = y;
            int right = left + w;
            int bottom = top + h;
            
            x = right + SPACE;
            height = bottom + SPACE;
            
            childView.layout(left, top, right, bottom);
        }  
    }  
  
    /** 
     * 计算控件的大小 
     */  
    @Override  
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);  
        int measureWidth = measureWidth(widthMeasureSpec);  
        
        int x = 0;  
        int y = 0;  
        int height = 0;
        int line = 1;
        int count = getChildCount();  
        for (int j = 0; j < count; j++) {
            final View childView = getChildAt(j);  
            // 获取子控件Child的宽高  
            int w = childView.getMeasuredWidth();  
            int h = childView.getMeasuredHeight();  
            // 计算子控件的顶点坐标  
            if (x + w > getWidth()) {
            	x = 0;
            	y = height;
            	line++;
            	if (line == 3) {
            		break;
            	}
            }
            int left = x;
            int top = y;
            int right = left + w;
            int bottom = top + h;
            
            x = right + SPACE;
            height = bottom + SPACE;
        }
        if (isFirst && height >= 0) {
        	isFirst = false;
        	height += 100;
        }
        
        // 计算自定义的ViewGroup中所有子控件的大小  
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        
        // 设置自定义的控件MyViewGroup的大小  
        setMeasuredDimension(measureWidth, height);
    }  
  
    private int measureWidth(int pWidthMeasureSpec) {  
        int result = 0;  
        int widthMode = MeasureSpec.getMode(pWidthMeasureSpec);// 得到模式  
        int widthSize = MeasureSpec.getSize(pWidthMeasureSpec);// 得到尺寸  
  
        switch (widthMode) {  
        case MeasureSpec.AT_MOST:  
        case MeasureSpec.EXACTLY:  
            result = widthSize;  
            break;  
        }  
        return result;  
    }  
}  
