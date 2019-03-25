package edu.stanford.cs108.gameeditor;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    SQLiteDatabase db;
    String currentGame = "Bunny World";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // the database that stores info of the games
        db = openOrCreateDatabase("gamesTable",MODE_PRIVATE,null);

        // sql
        sqlDefaultGame();
        sqlChooseGame();
        deleteGame();
        resetDatabaseListener();
    }

    public void  gotoEditor(View view){
        Intent intent = new Intent(this,EditorActivity.class);
        intent.putExtra("M", "");
        startActivityForResult(intent, 4);
    }
    public void  gotoExistEditor(View view){
        Intent intent = new Intent(this,EditorActivity.class);
        intent.putExtra("M", currentGame);
        startActivityForResult(intent, 4);
    }

    public void  gotoPlay(View view){
        Intent intent = new Intent(this,PlayActivity.class);
        intent.putExtra("M", currentGame);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == 4) {
            // Make sure the request was successful
            if (resultCode == RESULT_CANCELED) {
                setUpSpinner();
            }
        }
    }

    @Override
    public void onRestart() {
        super.onRestart();
        //When BACK BUTTON is pressed, the activity on the stack is restarted
        setUpSpinner();
    }
    /**
     * The function to get names of the existing games; Show existing games in the Spinner; record the choosen game; Bunny World as the default game.
     */
    public void sqlChooseGame()  {
        // if no game table that stores game names exist, create one
        String setupStr = "CREATE TABLE games ("
                + "name TEXT,"
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT"
                + ");";
        // insert bunny world as the default game
        String dataStr = "INSERT INTO games VALUES "
                + "('Bunny World',NULL)"
                + ";";

        // if no game table that stores game names exist, create one  and insert bunny world as the default game
        Cursor tablesCursor = db.rawQuery(
                "SELECT * FROM sqlite_master WHERE type='table' AND name='games';",null);
        if (tablesCursor.getCount() == 0){
            db.execSQL(setupStr);
            db.execSQL(dataStr);
        }
        setUpSpinner();
    }


    public void setUpSpinner(){
        final Spinner gameSpinner = findViewById(R.id.gameSpinner);

        ArrayList<String> labels = new ArrayList<String>();
        String selectQuery = " SELECT name FROM games";
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                labels.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_item,
                        labels);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        gameSpinner.setAdapter(spinnerArrayAdapter);

        gameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                currentGame = gameSpinner.getSelectedItem().toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
    }

    /**
     * Function that setUp Default Game - Bunny World
     */
    public void sqlDefaultGame() {
        ContentValues values = new ContentValues();
        // SQL: create currentGame sql table in the db gameTable (if no sql table exist for the current game)
        String setupStr = "CREATE TABLE [Bunny World] ("
                + "pageName TEXT,"
                + "script TEXT,"
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT"
                + ");";

        String content1 = "addPage_Page1_onEnter|play woof;/" +
                "addShape_1100_100_bunnyWorldTitle_false_true_null_Bunny World!_null/" +
                "addShape_1000_300_bunnyWorldDescription_false_true_null_You are in a maze of twisty little passages, all alike_null/" +
                "addShape_400_800_door1_false_true_null_null_onClick|goto pageMystic;/" +
                "addShape_1200_800_door2_false_false_null_null_onClick|goto pageFire;/" +
                "addShape_2000_800_door3_false_true_null_null_onClick|goto pageDeath;/";

        String content2 = "addPage_pageMystic_onEnter|show Page1@door2;/" +
                "addShape_400_800_mysticDoor_false_true_null_null_onClick|goto Page1;/" +
                "addShape_1100_200_mysticBunny_false_true_mystic_null_onClick|hide possession@carrot|play munch;/" +
                "addShape_1000_600_BunnyText_false_true_null_Mystic Bunny-Rub my tummy for a big surprise_null/";

        String content3 = "addPage_pageFire_onEnter|play fire;/" +
                "addShape_400_800_fireDoor_false_true_null_null_onClick|goto pageMystic/" +
                "addShape_1000_200_fireImage_false_true_fire_null_null/" +
                "addShape_1050_800_fireText_false_true_null_Eek! Fire-Room. Run away!_null/" +
                "addShape_1800_800_carrotImage1_true_true_carrot_null_null/";

        String content4 = "addPage_pageDeath_onEnter|play evillaugh;/" +
                "addShape_1000_200_deadBunny_false_true_death_null_onDrop|hide dropped@carrot|play carrotcarrotcarrot|hide pageDeath@deadBunny|hide pageDeath@deadText|show pageDeath@exit;onClick|play evillaugh;/" +
                "addShape_2000_800_exit_false_false_null_null_onClick|goto pageExit;/" + //原本这里exit的门和其他是重叠的，hide其他之后，点不到这个门。需要重新弄一下
                "addShape_1050_800_deadText_false_true_null_You must appease Bunny of Death!_null/";

        String content5 = "addPage_pageExit_onEnter|play hooray;/" + //需要handle一下音乐不存在的情况
                "addShape_1100_500_carrot1_false_true_carrot_null_null/" +
                "addShape_1200_400_carrot2_false_true_carrot_null_null/" +
                "addShape_1300_500_carrot3_false_true_carrot_null_null/" +
                "addShape_1150_800_winText_false_true_null_You Win Yay!_null/";

        sqlReset("Bunny World");

        Cursor tablesCursor = db.rawQuery(
                "SELECT * FROM sqlite_master WHERE type='table' AND name= 'Bunny World';",null);
        if (tablesCursor.getCount() == 0){
            db.execSQL(setupStr);
            values.put("pageName","Page1");
            values.put("script",content1);
            db.insert("[Bunny World]",null,values);
            values.clear();
            values.put("pageName","pageMystic");
            values.put("script",content2);
            db.insert("[Bunny World]",null,values);
            values.clear();
            values.put("pageName","pageFire");
            values.put("script",content3);
            db.insert("[Bunny World]",null,values);
            values.clear();
            values.put("pageName","pageDeath");
            values.put("script",content4);
            db.insert("[Bunny World]",null,values);
            values.clear();
            values.put("pageName","pageExit");
            values.put("script",content5);
            db.insert("[Bunny World]",null,values);
        }
    }

    public void deleteGame(){
        Button deleteGameBtn = findViewById(R.id.deleteGame);
        deleteGameBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(currentGame.equals("Bunny World")){
                    Toast toast = Toast.makeText(MainActivity.this,"Default Game Cannot Be Deleted!" ,Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.BOTTOM|Gravity.RIGHT, 520, 220);
                    toast.show();
                }else{
                    // Dialog that alert user when remove page
                    AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                    alertDialog.setTitle("GAME DELETE ALERT");
                    alertDialog.setMessage("Do You Really Want To Delete This Game?");
                    alertDialog.setIcon(R.drawable.carrot);
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "YES", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            sqlReset(currentGame);
                            deleteGameName(currentGame);
                            setUpSpinner();
                        }
                    });
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NO", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    alertDialog.show();
                }
            }
        });
    }

    public void resetDatabaseListener() {
        Button resetDatabaseBtn = findViewById(R.id.resetDatabase);
        resetDatabaseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Dialog that alert user when remove page
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle("Reset Database Alert");
                alertDialog.setMessage("Do You Really Want To Reset The Database Except For Default Game?");
                alertDialog.setIcon(R.drawable.carrot);
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        resetDatabase();
                    }
                });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NO", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                alertDialog.show();
            }
        });
    }

    /**
     * Function that reset the gameTable
     */
    public void sqlReset(String tableName){
        String resetStr = "DROP TABLE IF EXISTS [" + tableName +"];";
        db.execSQL(resetStr);
    }

    public void deleteGameName(String id) {
        db.execSQL("delete from games where name='"+id+"'");
    }

    public void resetDatabase() {
        // query to obtain the names of all tables in your database except for sqlite_sequence and games list
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type = 'table' AND name != 'sqlite_sequence' AND name != 'games' AND name != 'Bunny World'", null);
        List<String> tables = new ArrayList<>();

        // iterate over the result set, adding every table name to a list
        while (c.moveToNext()) {
            tables.add(c.getString(0));
        }

        // call DROP TABLE on every table name
        for (String table : tables) {
            String dropQuery = "DROP TABLE IF EXISTS [" + table + "];";
            db.execSQL(dropQuery);
        }

        //delete data from the table 'games' except for default game
        String deleteGamesTable = "DELETE FROM games WHERE name != 'Bunny World';";
        db.execSQL(deleteGamesTable);
        setUpSpinner();
    }
}


