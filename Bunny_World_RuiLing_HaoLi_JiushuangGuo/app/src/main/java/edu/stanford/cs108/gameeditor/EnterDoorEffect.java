package edu.stanford.cs108.gameeditor;

import android.graphics.Canvas;
import android.graphics.Region;

public class EnterDoorEffect extends Animation {
    public EnterDoorEffect(EnterAnimLayout view) {
        super(view);
    }

    @Override
    public void handleCanvas(Canvas canvas, float rate) {
        float rectLeft =  (w/2 * rate);
        float rectRight = w - rectLeft;
        float rectTop = (h/2 * rate);
        float rectBottom = h - rectTop;
        //The area to show the effect
        canvas.clipRect(rectLeft, rectTop,rectRight , rectBottom, Region.Op.DIFFERENCE);
        canvas.save();
    }
}