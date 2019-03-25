package edu.stanford.cs108.gameeditor;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Page {
    private String pageName;
    private String script;
    private List<Shape> shapeList;
    private Map<String, String[]> actionMap;

    public Page() {
        new Page(null, null);
    }

    public Page(Page pageCopy) {
        pageName = pageCopy.getPageName();
        script = pageCopy.getScript();
        shapeList = new ArrayList<>();
        List<Shape> shapeListCopy = pageCopy.getShapeList();
        for (Shape s : shapeListCopy) {
            Shape newShape = new Shape(s);
//            Log.v("asdfasdf", s.getName());
//            Log.v("asdfasdfasd", newShape.getName());
            shapeList.add(newShape);
        }
        actionMap = Script.parseScript(script);
    }

    public Page(String name,String script) {
        this.pageName = name;
        shapeList= new ArrayList<>();
        this.script = script;
        actionMap = Script.parseScript(script);
    }

    public void setPageName(String pageName) {
        this.pageName = pageName;
    }

    public String getPageName() {
        return pageName;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public void setActionMap(Map<String, String[]> actionMap) {
        this.actionMap = actionMap;
    }

    public Map<String, String[]> getActionMap() {
        return actionMap;
    }

    public List<Shape> getShapeList(){return this.shapeList;}
}

