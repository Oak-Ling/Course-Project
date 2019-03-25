package edu.stanford.cs108.gameeditor;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;

import android.widget.Toast;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayActivity extends AppCompatActivity {

    private static EnterAnimLayout enterAnimLayout;

    SQLiteDatabase db;

    // hashMap of the currentGame Cursor to store game info {Key: page, Value: script}
    HashMap<String, String> gameHash = new HashMap<String, String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        enterAnimLayout = (EnterAnimLayout)  findViewById(R.id.anim_layout);

        // the database that stores info of the games
        db = openOrCreateDatabase("gamesTable",MODE_PRIVATE,null);
        // get intent from the mainActivity
        Intent intent = getIntent();
        // get the currentGame from MainActivity
        String currentGame = intent.getStringExtra("M");

        // DELETE LATER: optional toast to see the currentName is good
        Toast toast = Toast.makeText(PlayActivity.this,"Begin Playing " + currentGame +"!" ,Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 50);
        toast.show();

        // when switch to the play activity, load the currentGame
        sqlLoadGame(currentGame);

        PageView currPage = findViewById(R.id.page_view);
        Map<String, Page> pageContentMap = new HashMap<>();

        for (String key : gameHash.keySet()) {
            Page pageNew = parsePageContent(gameHash.get(key), currPage);
            pageContentMap.put(key, pageNew);
        }
        currPage.setPageMap(pageContentMap);
        currPage.toGo("Page1");
    }

    //EnterDoorEffect Function
    public static void change(){
        //Create a new thread to show the effect
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Animation anim = new EnterDoorEffect(enterAnimLayout);
                anim.startAnimation();
            }
        }, 0);
    }

    public Page parsePageContent(String content, PageView inputPage){
        Page page = new Page();
        Log.v("parseContent", content);
        String[] object = content.split("/");
        for (String s : object) {
            String[] item = s.split("_");
            if (item[0].equals("addPage")) {
                page = new Page(item[1], item[2]);
            } else {
                float leftPoint = Float.parseFloat(item[1]);
                float topPoint = Float.parseFloat(item[2]);
                boolean movable = Boolean.parseBoolean(item[4]);
                boolean visible = Boolean.parseBoolean(item[5]);
                String graphName = item[6].equals("null") ? null : item[6];
                String T = item[7].equals("null") ? null : item[7];
                String S = item[8].equals("null") ? null : item[8];
                Bitmap img;
                if (graphName == null) {
                    img = null;
                } else {
                    img = inputPage.getImage(graphName);
                }
                Shape shape = new Shape(leftPoint, topPoint, img, item[3], movable, visible, item[6], T, S);

                if (page.getPageName() == null) {
                    try {
                        throw new Exception("wrong input");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                page.getShapeList().add(shape);
            }
        }
        return page;
    }

    /**
     * Function that Load the current Game from sql table with the table name-- currentGame
     */
    public void sqlLoadGame(String currentGame){
        // SQL: create currentGame sql table in the db gameTable (if no sql table exist for the current game)
        String setupStr = "CREATE TABLE ["
                + currentGame
                + "] ("
                + "pageName TEXT,"
                + "script TEXT,"
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT"
                + ");";

        // SQL: get the cursor
        Cursor tablesCursor = db.rawQuery(
                "SELECT * FROM sqlite_master WHERE type='table' AND name= '"+currentGame+"' ;",null);
        // SQL: if currentGame table not exist setup
        if (tablesCursor.getCount() == 0){
            db.execSQL(setupStr);
        }

        // cursor of the currentGame
        Cursor cursor = db.rawQuery("SELECT * FROM [" + currentGame + "]",null);
        // loop the cursor to load page-script info into hashMap
        while(cursor.moveToNext()) {
            int index;
            index = cursor.getColumnIndexOrThrow("pageName");
            String page = cursor.getString(index);
            index = cursor.getColumnIndexOrThrow("script");
            String script = cursor.getString(index);
            gameHash.put(page, script);
        }
    }
}
