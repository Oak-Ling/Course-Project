package edu.stanford.cs108.gameeditor;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;



public class EnterAnimLayout extends FrameLayout {
    private Animation anim ;
    private long startTime = 0;
    private boolean mIsAnimaionRun = false;//If set true， play after invalidate
    private boolean mIsVisibleAtFirst = true;//view

    public EnterAnimLayout(Context context) {
        super(context);
        initialize();
    }

    public EnterAnimLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray attribute = context.obtainStyledAttributes(attrs, R.styleable.EnterAnimLayout);
        mIsVisibleAtFirst = attribute.getBoolean(R.styleable.EnterAnimLayout_isVisibleAtFirst, true);

        attribute.recycle();
        initialize();
    }

    public EnterAnimLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray attribute = context.obtainStyledAttributes(attrs, R.styleable.EnterAnimLayout);
        mIsVisibleAtFirst = attribute.getBoolean(R.styleable.EnterAnimLayout_isVisibleAtFirst, false);

        attribute.recycle();
        initialize();
    }

    protected void initialize() {
    }
    public void setAnim(Animation anim) {
        this.anim = anim;
    }
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
    public void setmIsAnimaionRun(boolean mIsAnimaionRun) {
        this.mIsAnimaionRun = mIsAnimaionRun;
    }



    @Override
    protected void dispatchDraw(Canvas canvas) {

        if (mIsVisibleAtFirst && !mIsAnimaionRun) {
            super.dispatchDraw(canvas);
            return;
        }

        if (!mIsVisibleAtFirst && !mIsAnimaionRun) {
            return;
        }
        //rate=（currenttime-starttime）/total time
        long currentTime = System.currentTimeMillis();
        float rate = ((float)(currentTime - startTime) )/ anim.totalPaintTime;
        rate = rate > 1 ? 1 : rate;


        anim.handleCanvas(canvas,rate);
        super.dispatchDraw(canvas);

        if (rate < 1) {
            invalidate();
        } else {
            mIsAnimaionRun = false;
            mIsVisibleAtFirst = true;
        }
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN && mIsAnimaionRun == true) {
            mIsAnimaionRun = false;
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }
}