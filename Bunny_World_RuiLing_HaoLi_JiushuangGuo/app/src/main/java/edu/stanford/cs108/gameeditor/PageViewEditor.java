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
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PageViewEditor extends View {
    private Paint defaultPaint;
    private Paint defaultOutline;
    private Paint defaultDroppable;
    private TextPaint defaultPen;

    private ArrayList<Shape> pageShapeList;
    private Map<String, Page> pageMap;
    private String pageName = null;
    private ArrayList<Shape> possessionList;

    float x1,y1,x2,y2;
    private int currSelected = -1;
    float viewWidth, viewHeight;

    public PageViewEditor(Context context, AttributeSet attrs) {
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
                    setField();
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
                setField();

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
                        reSize(item,item.getBmp(),0.25f);
                        relocate(item);
                        item.setTop(viewHeight * 3 / 4 + 2.5f);
                        item.setBottom(item.getTop() + item.getHeight());

                    } else {
                        backToOriginalSize(item);
                        reSize(item,item.getBmp(),0.75f);
                        relocate(item);
                        item.setBottom(viewHeight * 3 / 4 - 2.5f);
                        item.setTop(item.getBottom() - item.getHeight());
                    }
                }else{
                    backToOriginalSize(item);
                    reSize(item,item.getBmp(),0.75f);
                    relocate(item);
                    if (item.getBottom() - 0.75f * viewHeight >= 0 && item.getTop() - 0.75f * viewHeight <= 0) {
                        item.setBottom(viewHeight * 3 / 4 - 2.5f);
                        item.setTop(item.getBottom() - item.getHeight());
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
                reSetText(item);
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

    private boolean checkDrop(Shape shape) {
        if (shape.getActionMap().containsKey("onDrop")) {
            return true;
        }
        return false;
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
        Page newPage = pageMap.get(pageName);
        if (newPage == null) {
            return;
        }
        this.pageName = pageName;
        List<Shape> newShapeList = newPage.getShapeList();
        if (!newShapeList.isEmpty()){
            addShapeToDraw(newShapeList); //shallow copy
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

    public void setField() {
        if(currSelected != -1){
            Shape currentShape = pageShapeList.get(currSelected);
            String shapeName = currentShape.getName();

            relocate(currentShape);

            float leftPoint = currentShape.getLeft();
            float topPoint = currentShape.getTop();
            String imageName = currentShape.getImageName();
            String shapeText = currentShape.getText();
            Boolean movable = currentShape.getMovable();
            Boolean visible = currentShape.getVisible();
            String script = currentShape.getScript();
            String completeScript =  "addShape_" + leftPoint + "_" + topPoint + "_"+ shapeName + "_"
                    + movable + "_" + visible + "_" + imageName + "_" + shapeText +"_"+ script +"/";

            //((Activity) getContext()).findViewById(R.id.*);
            Spinner shapeSpinner =  ((Activity) getContext()).findViewById(R.id.shapeSpinner);
            Spinner shapeImageSpinner =  ((Activity) getContext()).findViewById(R.id.imageSpinner);
            EditText shapeTextEditText =  ((Activity) getContext()).findViewById(R.id.shapeTextInput);
            CheckBox movableCheckBox =  ((Activity) getContext()).findViewById(R.id.movableCheck);
            CheckBox visibleCheckBox =  ((Activity) getContext()).findViewById(R.id.visibleCheck);
            TextView textViewShapeEffect =  ((Activity) getContext()).findViewById(R.id.currentShapeEffect_Editor);

            ArrayAdapter<String> adapter = (ArrayAdapter<String>) shapeSpinner.getAdapter();
            int n = adapter.getCount();
            int selectedShape = -1;
            for (int i = 0; i < n; i++) {
                if (shapeName.equals(adapter.getItem(i))) {
                    selectedShape = i;
                    break;
                }
            }
            if (selectedShape != -1) {
                shapeSpinner.setSelection(selectedShape); // shapeName
            }

            reSetText(currentShape);

            shapeTextEditText.setText(shapeText);
            movableCheckBox.setChecked(movable);
            visibleCheckBox.setChecked(visible);
            textViewShapeEffect.setText(completeScript);

            shapeImageSpinner.setSelection(findImagedPosition(imageName) + 1);
        }
    }

    public void reSetText(Shape curShape){
        EditText leftPosition =  ((Activity) getContext()).findViewById(R.id.left_position);
        EditText topPosition =  ((Activity) getContext()).findViewById(R.id.top_position);
        EditText rightPosition = ((Activity) getContext()).findViewById(R.id.right_position);
        EditText bottomPosition = ((Activity) getContext()).findViewById(R.id.bottom_position);
        float leftPoint = curShape.getLeft();
        float topPoint = curShape.getTop();

        leftPosition.setText(Integer.toString((int)leftPoint));// left position
        topPosition.setText(Integer.toString((int)(topPoint>=0?topPoint:0)));// top position

        rightPosition.setText(Integer.toString((int) (leftPoint + curShape.getWidth())));
        bottomPosition.setText(Integer.toString((int) (topPoint + curShape.getHeight())));
    }

    public int findImagedPosition(String imageName){
        ArrayList<String> imageFiles =  new ArrayList<String>();
        Field[] arr = R.drawable.class.getDeclaredFields();
        for (Field a:arr) {
            boolean print = true;
            for (int i=0;i<a.getName().length();i++){
                if (a.getName().charAt(i) == '_'){
                    print = false;
                    break;
                }
            }
            if (print){
                imageFiles.add(a.getName());
            }
        }
        int imagePosition = -1;
        for(int i = 0; i < imageFiles.size(); i++){
            if(imageFiles.get(i).equals(imageName)){
                imagePosition = i;
            }
        }
        return imagePosition;
    }

    public void relocate(Shape currentShape){
        if (currentShape.getLeft()<0 || currentShape.getTop()<0){
            float leftPoint = currentShape.getLeft()>=0?currentShape.getLeft():0;
            float topPoint = currentShape.getTop()>=0?currentShape.getTop():0;
            float rightPoint = leftPoint + currentShape.getWidth();
            float bottomPoint = topPoint + currentShape.getHeight();
            if (rightPoint >= viewWidth){
                rightPoint = viewWidth;
                leftPoint = rightPoint - currentShape.getWidth();
            }
            if (bottomPoint >= viewHeight){
                bottomPoint = viewHeight;
                topPoint = bottomPoint - currentShape.getHeight();
            }
            currentShape.setLeft(leftPoint);
            currentShape.setTop(topPoint);
            currentShape.setRight(rightPoint);
            currentShape.setBottom(bottomPoint);
        }else {
            float leftPoint = currentShape.getLeft();
            float topPoint = currentShape.getTop();

            float rightPoint = leftPoint + currentShape.getWidth();
            float bottomPoint = topPoint + currentShape.getHeight();

            rightPoint = rightPoint >= viewWidth ? viewWidth : rightPoint;
            bottomPoint = bottomPoint >= viewHeight ? viewHeight : bottomPoint;
            leftPoint = rightPoint - currentShape.getWidth();
            topPoint = bottomPoint - currentShape.getHeight();

            currentShape.setLeft(leftPoint);
            currentShape.setTop(topPoint);
            currentShape.setRight(rightPoint);
            currentShape.setBottom(bottomPoint);
        }
    }

    public void reSize(Shape item,Bitmap img,float ratio){
        float width = -1f;
        float height = -1f;
        if(img!=null) {
            float imgHeight = item.getHeight();
            float imgWidth = item.getWidth();
            if (imgHeight > viewHeight*ratio){
                imgWidth = imgWidth * (viewHeight*ratio) / imgHeight;
                imgHeight = viewHeight*ratio;
            }
            if (imgWidth > viewWidth){
                imgHeight = imgHeight*viewWidth/imgWidth;
                imgWidth = viewWidth;
            }
            width = imgWidth;
            height = imgHeight;
        }
        if (width != -1 && height!= -1){
            item.setWidth(width);
            item.setHeight(height);
        }
    }

    public void backToOriginalSize(Shape item){
        Bitmap img = item.getBmp();
        if (img!=null){
            float originalHeight = img.getHeight();
            float originalWidth = img.getWidth();
            item.setHeight(originalHeight);
            item.setWidth(originalWidth);
        }
    }
}

