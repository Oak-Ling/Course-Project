package edu.stanford.cs108.gameeditor;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ShareActionProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PageView extends View {
    private Paint defaultPaint;
    private Paint defaultOutline;
    private Paint defaultDroppable;
    private TextPaint defaultPen;

    private ArrayList<Shape> pageShapeList;
    private Map<String, Page> pageMap;
    private String pageName = null;
    private ArrayList<Shape> possessionList;

    private Bitmap background = getImage("bg");

    float x1,y1,x2,y2;
    private int currSelected = -1;
    float viewWidth, viewHeight;

    public PageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        defaultOutline = new Paint(Paint.ANTI_ALIAS_FLAG );
        defaultOutline.setColor(Color.BLACK);
        defaultOutline.setStyle(Paint.Style.STROKE);
        defaultOutline.setStrokeWidth(5.0f);

        defaultPen = new TextPaint();
        defaultPen.setTextSize(50);
        defaultPen.setAntiAlias(true);
        defaultPen.setFakeBoldText(true);
        defaultPen.setTextAlign(Paint.Align.LEFT);

        defaultPaint = new Paint();
        defaultPaint.setColor(Color.GRAY);
        defaultPaint.setStyle(Paint.Style.FILL);

        defaultDroppable = new Paint();
        defaultDroppable.setColor(Color.GREEN);
        defaultDroppable.setStyle(Paint.Style.STROKE);
        defaultDroppable.setStrokeWidth(10.0f);

        pageShapeList = new ArrayList<>();
        possessionList = new ArrayList<>();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = w;
        viewHeight = h;
    }

    @Override
    protected void onDraw(Canvas page) {
        super.onDraw(page);

        page.drawBitmap(background,null,new RectF(0, 0, viewWidth, viewHeight),null);

        page.drawLine(0,viewHeight*3/4,viewWidth,viewHeight*3/4,defaultOutline);
        for(Shape shape: pageShapeList){
            float left = shape.getLeft();
            float right = shape.getRight();
            float bottom = shape.getBottom();
            float top = shape.getTop();

            Bitmap pic = shape.getBmp();

            if (currSelected != -1){
                if (checkDrop(shape) && !shape.equals(pageShapeList.get(currSelected))){ //the current drop should not be itself
                    page.drawRect(left, top, right, bottom, defaultDroppable);
                }
            }

            if (pic == null && shape.getText() == null){
                page.drawRect(left, top, right, bottom, defaultPaint);
            }else if(pic != null && shape.getText() == null) {
                page.drawBitmap(pic,null,new RectF(left, top, right, bottom),null);
            }else{
                StaticLayout SL = new StaticLayout(shape.getText(),defaultPen,(int)shape.getWidth(), Layout.Alignment.ALIGN_NORMAL,1.0f,0.0f,true);
                page.save();
                page.translate((int)shape.getLeft(),(int)shape.getTop());
                SL.draw(page);
                page.restore();
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                y1 = event.getY();
                int size = pageShapeList.size();
                for (int j = size - 1; j >= 0; j--) {
                    Shape item = pageShapeList.get(j);
                    if (checkSelected(item, x1, y1)) {
                        currSelected = j;
                        break;
                    }
                }

                if (currSelected != -1) {
                    Shape shape = pageShapeList.get(currSelected);
                    if (checkClick(shape)) {
                        onClick(shape);
                    }
                    if (!shape.getMovable()) {
                        currSelected = -1;
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (currSelected == -1) break;
                x2 = event.getX();
                y2 = event.getY();
                Shape item = pageShapeList.get(currSelected);

                item.setLeft(item.getLeft() + x2 - x1);
                item.setRight(item.getRight() + x2 - x1);
                item.setTop(item.getTop() + y2 - y1);
                item.setBottom(item.getBottom() + y2 - y1);
                x1 = x2;
                y1 = y2;

                pageShapeList.remove(currSelected);
                pageShapeList.add(item);
                currSelected = pageShapeList.size()-1;

                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                if (currSelected == -1) {
                    break;
                }
                x2 = event.getX();
                y2 = event.getY();
                item = pageShapeList.get(currSelected);

                item.setLeft(item.getLeft() + x2 - x1);
                item.setRight(item.getRight() + x2 - x1);
                item.setTop(item.getTop() + y2 - y1);
                item.setBottom(item.getBottom() + y2 - y1);

                x1 = x2;
                y1 = y2;

                if (item.getBottom() - 0.75f * viewHeight >= 0 && item.getTop() - 0.75f * viewHeight <= 0) {
                    if (item.getBottom() - 0.75f * viewHeight >= 0.5f * item.getHeight()) { //move to the possession list
                        item.setTop(viewHeight * 3 / 4 + 2.5f);
                        item.setBottom(item.getTop() + item.getHeight());
                    } else {
                        item.setBottom(viewHeight * 3 / 4 - 2.5f);
                        item.setTop(item.getBottom() - item.getHeight());
                    }
                }

                for (int j = pageShapeList.size() - 1; j >= 0; j--) {
                    Shape droppableShape = pageShapeList.get(j);
                    if (!checkDrop(droppableShape) || j == currSelected){
                        continue;
                    }
                    Shape currSelectedShape = pageShapeList.get(currSelected);
                    float xCenter = (currSelectedShape.getLeft()+currSelectedShape.getRight())/2;
                    float yCenter = (currSelectedShape.getTop()+currSelectedShape.getBottom())/2;
                    if (checkSelected(droppableShape, xCenter, yCenter)) {
                        onDrop(droppableShape);
                        break;
                    }
                }

                if (!possessionList.contains(item)) {
                    if (item.getTop() - 0.75f * viewHeight >= 0) { //already at the possession view
                        //remove it from the current page
                        List<Shape> currShapeList =  pageMap.get(pageName).getShapeList();
                        for (int i = 0; i < currShapeList.size(); i++) {
                            if (currShapeList.get(i).getName().equals(item.getName())) {
                                currShapeList.remove(i);
                                break;
                            }
                        }
                        possessionList.add(item);
                    }
                } else {
                    if (item.getBottom() - 0.75f * viewHeight <= 0) { //move to the page view
                        List<Shape> currShapeList =  pageMap.get(pageName).getShapeList();
                        currShapeList.add(item);
                        possessionList.remove(item);
                    }
                }
                currSelected = -1;
                invalidate();
                break;
        }
        return true;
    }

    private boolean checkSelected(Shape item, float pointX, float pointY) {
        if (pointX >= item.getLeft() && pointX <= item.getRight() && pointY >= item.getTop() && pointY <= item.getBottom()) {
            return true;
        }
        return false;
    }

    public Bitmap getImage(String s) {
        int resID = getResources().getIdentifier(s, "drawable", getContext().getPackageName());
        BitmapDrawable bitmapDrawable = (BitmapDrawable) getResources().getDrawable(resID);
        Bitmap bitmap = bitmapDrawable.getBitmap();
        return bitmap;
    }

    public MediaPlayer getMusic(String s) {
        int resID = getResources().getIdentifier(s, "raw", getContext().getPackageName());
        if (resID == 0) {
            return null;
        }
        MediaPlayer mediaPlayer = MediaPlayer.create(getContext(),resID);
        return mediaPlayer;
    }

    private boolean checkClick(Shape shape) {
        if (shape.getActionMap().containsKey("onClick")) {
            return true;
        }
        return false;
    }

    private boolean checkDrop(Shape shape) {
        if (shape.getActionMap().containsKey("onDrop")) {
            return true;
        }
        return false;
    }

    private void onClick(Shape shape) {
        String[] actions = shape.getActionMap().get("onClick");
        PageView page = ((Activity) getContext()).findViewById(R.id.page_view);
        Script.beginActions(actions, page);
    }

    private void onDrop(Shape shape) {
        String[] actions = shape.getActionMap().get("onDrop");
        PageView page = ((Activity) getContext()).findViewById(R.id.page_view);
        Script.beginActions(actions,page);
    }

    public void onEnter(Page page) {
        String[] actions = page.getActionMap().get("onEnter");
        PageView pageView = ((Activity) getContext()).findViewById(R.id.page_view);
        Script.beginActions(actions, pageView);
    }

    public void toShow(String name) { //Page1@door
        String[] item = name.split("@");
        if (pageMap.containsKey(item[0])) {
            Page currPage = pageMap.get(item[0]);
            List<Shape> currShapeList = currPage.getShapeList();
            for (Shape curr : currShapeList) {
                if (curr.getName().equals(item[1])) {
                    curr.setVisible(true);
                }
            }
            if (currPage.getPageName().equals(pageName)) {
                addShapeToDraw(currShapeList);
            }
            invalidate();
        }
    }

    public void toHide(String name) {
        String[] item = name.split("@");
        if (pageMap.containsKey(item[0])) {
            Page currPage = pageMap.get(item[0]);
            List<Shape> currShapeList = currPage.getShapeList();
            for (Shape curr : currShapeList) {
                if (curr.getName().equals(item[1])) {
                    curr.setVisible(false);
                }
            }
            if (currPage.getPageName().equals(pageName)) {
                addShapeToDraw(currShapeList);
            }
        } else if (item[0].equals("possession")) {
            for (Shape possession : possessionList) {
                if (possession.getImageName().equals(item[1])) {
                    possessionList.remove(possession);
                    pageShapeList.remove(possession);
                }
            }
        } else if (item[0].equals("dropped")) {
            Shape droppedShape = pageShapeList.get(currSelected);
            if (droppedShape.getImageName().equals(item[1]) || item[1].equals("everything")) {
                if (possessionList.contains(droppedShape)) {
                    possessionList.remove(droppedShape);
                }
                droppedShape.setVisible(false);
                pageShapeList.remove(droppedShape);
            }
        }
        invalidate();
    }

    public void toGo(String pageName) {
        PlayActivity.change();
        Page newPage = pageMap.get(pageName);
        if (newPage == null) {
            return;
        }
        this.pageName = pageName;
        List<Shape> newShapeList = newPage.getShapeList();
        if (!newShapeList.isEmpty()){
            addShapeToDraw(newShapeList); //shallow copy
            if (newPage.getActionMap().containsKey("onEnter")) {
                onEnter(newPage);
            }
            invalidate();
        }
    }

    public void setPageMap(Map<String, Page> inputPageMap){
        this.pageMap = inputPageMap;
    }

    public void addShapeToDraw(List<Shape> list) {
        pageShapeList.clear(); //clear current shapeList
        for (Shape newPageShape : list){
            if (newPageShape.getVisible()) {
                pageShapeList.add(newPageShape);
            }
        }
        for (Shape possession : possessionList) {
            pageShapeList.add(possession);
        }
    }
}
