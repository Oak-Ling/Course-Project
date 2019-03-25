package edu.stanford.cs108.gameeditor;

import android.graphics.Canvas;

public abstract class Animation {
    protected EnterAnimLayout view;
    protected float w;
    protected float h;
    public float totalPaintTime;//Changing time

    public Animation(EnterAnimLayout view) {
        this(view,500);
    }

    public Animation(EnterAnimLayout view, float totalPaintTime) {
        this.totalPaintTime = totalPaintTime;
        this.view = view;
        this.view.setAnim(this);
        w = view.getWidth();
        h = view.getHeight();
    }

    public void startAnimation() {
        view.setmIsAnimaionRun(true);
        view.setStartTime(System.currentTimeMillis());
        view.invalidate();
    }
    public abstract void handleCanvas(Canvas canvas, float rate);
}