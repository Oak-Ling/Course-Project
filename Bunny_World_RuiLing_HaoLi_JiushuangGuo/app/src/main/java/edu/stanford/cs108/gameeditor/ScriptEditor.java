package edu.stanford.cs108.gameeditor;

import android.media.MediaPlayer;

public class ScriptEditor {
    static final String GOTO = "goto";
    static final String PLAY = "play";
    static final String HIDE = "hide";
    static final String SHOW = "show";

    static void beginActions(String[] actionList, PageViewEditor page) {
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
    static void goTo(String pageName, PageViewEditor page) {
        page.toGo(pageName);
    }

    static void play(String musicName, PageViewEditor page) {
        MediaPlayer mp = page.getMusic(musicName);
        if (mp == null) return;
        mp.start();
    }

    //remove item or hide item
    static void hide(String shapeName, PageViewEditor page) {
        page.toHide(shapeName);
    }

    static void show(String shapeName, PageViewEditor page) {
        page.toShow(shapeName);
    }
}
