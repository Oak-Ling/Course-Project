package edu.stanford.cs108.gameeditor;


import android.graphics.Bitmap;
import android.opengl.Visibility;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;


public class Shape {
    private float left;
    private float right;
    private float top;
    private float bottom;
    private float width;
    private float height;

    private String name;
    private boolean movable = false;
    private boolean visible = false;
    private String imageName;
    private String text;
    private String script;

    private Bitmap bmp = null;
    private Map<String, String[]> actionMap;

    public Shape(Shape shapeCopy) {
//        Log.v("asdfasd", shapeCopy.getName());
        left = shapeCopy.getLeft();
        top = shapeCopy.getTop();
        right = shapeCopy.getRight();
        bottom = shapeCopy.getBottom();
        bmp =  shapeCopy.getBmp();
        name = shapeCopy.getName();
        movable = shapeCopy.getMovable();
        visible = shapeCopy.getVisible();
        imageName = shapeCopy.getImageName();
        text = shapeCopy.getText();
        script = shapeCopy.getScript();
        width = Math.abs(left-right);
        height = Math.abs(top-bottom);
        actionMap = Script.parseScript(script);

        //new Shape(shapeCopy.getLeft(), shapeCopy.getTop(), shapeCopy.getBmp(), shapeName,shapeCopy.getMovable(),shapeCopy.getVisible(),shapeCopy.getImageName(),shapeCopy.getText(),shapeCopy.getScript());

    }


    public Shape(float leftPoint, float topPoint,
                 Bitmap img, String shapeName, boolean Move, boolean visibility, String graphName, String T, String S){
        movable = Move;
        left = leftPoint;
        top = topPoint;
        name = shapeName;
        imageName = graphName;
        visible = visibility;
        text = T;
        script = S;

        if (img != null){
            bmp = img;
            right = leftPoint + bmp.getWidth();
            bottom = topPoint + bmp.getHeight();
        }else {
            right = leftPoint + 200;
            bottom = topPoint + 200;
        }

        if (text != null){
            right = leftPoint + 500;
            bottom = topPoint + 400;
            bmp = null;
        }

        width = Math.abs(left-right);
        height = Math.abs(top-bottom);

        actionMap = Script.parseScript(S);
    }

    public float getTop() {
        return top;
    }
    public float getBottom() {
        return bottom;
    }
    public float getLeft() {
        return left;
    }
    public float getRight() {
        return right;
    }
    public float getHeight() {
        return height;
    }
    public float getWidth() {
        return width;
    }

    public boolean getMovable() { return  movable; }
    public Bitmap getBmp() {
        return bmp;
    }
    public String getName() {
        return name;
    }

    public String getImageName() { return imageName; }
    public String getText() { return text; }
    public String getScript() { return script; }

    public Map<String, String[]> getActionMap() {
        return actionMap;
    }

    public void setBottom(float bottom) { this.bottom = bottom; }
    public void setTop(float top) { this.top = top; }
    public void setLeft(float left) { this.left = left; }
    public void setRight(float right) { this.right = right; }
    public void setMovable(boolean movable) { this.movable = movable; }
    public void setName(String name) {
        this.name = name;
    }
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public void setBmp(Bitmap bmp) {
        this.bmp = bmp;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public void setActionMap(Map<String, String[]> actionMap) {
        this.actionMap = actionMap;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public boolean getVisible() {return visible;}

}
