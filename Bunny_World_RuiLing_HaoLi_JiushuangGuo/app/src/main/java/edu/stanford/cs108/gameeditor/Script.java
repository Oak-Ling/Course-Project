package edu.stanford.cs108.gameeditor;

import android.media.MediaPlayer;
import android.util.Log;
import java.util.HashMap;
import java.util.Map;


public class Script {
    static final String GOTO = "goto";
    static final String PLAY = "play";
    static final String HIDE = "hide";
    static final String SHOW = "show";

    static void beginActions(String[] actionList, PageView page) {
        if (actionList == null && actionList.length == 0) {
            return;
        } else {
            for (String s : actionList) {
                //show Page1@shapeName
                String actionName = s.substring(0, s.indexOf(" ")); //show
                String actionObject = s.substring(s.indexOf(" ") + 1); //Page1@shapeName
                if (actionName.equals(GOTO)) {
                    goTo(actionObject, page);
                } else if (actionName.equals(PLAY)) {
                    play(actionObject, page);
                } else if (actionName.equals(HIDE)) {
                    hide(actionObject, page);
                } else if (actionName.equals(SHOW)) {
                    show(actionObject, page);
                }
            }
        }
    }
    static void goTo(String pageName, PageView page) {
        page.toGo(pageName);
    }

    static void play(String musicName, PageView page) {
        MediaPlayer mp = page.getMusic(musicName);
        if (mp == null) return;
        mp.start();
    }

    //remove item or hide item
    static void hide(String shapeName, PageView page) {
        page.toHide(shapeName);
    }

    static void show(String shapeName, PageView page) {
        page.toShow(shapeName);
    }

    //onClick: {play evilLaugh,show exit}
    static Map<String, String[]> parseScript(String S) {
        Map<String, String[]> actionMap = new HashMap<>();
        if (S != null) {
            String[] actionTrigger = S.split(";");
            for (String s : actionTrigger) {
                if (s == null || s.equals("null") || s.equals("")){
                    continue;
                }
                String[] action = s.split("\\|");
                String[] actions = new String[action.length - 1];
                System.arraycopy(action,1,actions,0, action.length - 1);
                actionMap.put(action[0],actions);
            }
        }
        return actionMap;
    }
}