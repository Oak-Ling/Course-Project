package edu.stanford.cs108.gameeditor;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class EditorActivity extends AppCompatActivity {
    // SQL
    SQLiteDatabase db;
    // PAGE
    List<String> pageArray = new ArrayList<String>(); // page names array save the pages
    Set<String> pageSet = new HashSet<String>(); // page set save the pages names
    ArrayList<String> currentPageEffectArray = new ArrayList<String>(); // page effect array
    int totalPage = 0; // total num of page
    HashMap<String, ArrayList<String>> pageShapeMap = new HashMap<String, ArrayList<String>>();
    String currentPage = "Page1";
    String currentPageEffect;
    HashMap<String, String> pageScriptMap = new HashMap<String, String>(); //full script including addPage
    HashMap<String, ArrayList<String>> pageScriptArrayMap = new HashMap<String, ArrayList<String>>();

    // SHAPE
    ArrayList<String> shapeArray = new ArrayList<String>();
    Set<String> shapeSet = new HashSet<String>();
    int totalShape = 0; // total num of shape
    ArrayList<String> currentOnDropEffectArray = new ArrayList<String>();
    ArrayList<String> currentOnClickEffectArray = new ArrayList<String>();
    String currentShape = "Shape1";
    String currentShapeEffect;
    Map<String, Page> pageContentMap = new HashMap<>();
    PageViewEditor currPage;
    HashMap<String, String> shapeScriptMap = new HashMap<String, String>();  //full script including addShape
    HashMap<String, ArrayList<String>> shapeOnDropArrayMap = new HashMap<String, ArrayList<String>>();
    HashMap<String, ArrayList<String>> shapeOnClickArrayMap = new HashMap<String, ArrayList<String>>();

    ArrayList<String> imageFiles =  new ArrayList<String>();
    HashMap<String, String> gameHash = new HashMap<String, String>();

    //backup
    boolean saveShapeTrigger = false;
    ArrayList<String> backupShapeArray = new ArrayList<String>();
    String backupShapeName;
    int backupShapePosition;
    float backupTop;
    float backupLeft;
    String backupText;
    int backupImagePosition;
    boolean backMovable;
    boolean backVisible;
    boolean backOnDropable;
    boolean backOnClickable;
    ArrayList<String> backupOnDropEffect = new ArrayList<>();
    ArrayList<String> backupOnClickEffect = new ArrayList<>();
    String backupTextViewShapeEffect;
    HashMap<String, ArrayList<String>> backupPageShapeMap = new HashMap<>();
    Set<String> backupShapeSet= new HashSet<>();
    int backupTotalShape;
    HashMap<String, String> backupShapeScriptMap = new HashMap<>();
    HashMap<String, ArrayList<String>> backupShapeOnDropArrayMap = new HashMap<>();
    HashMap<String, ArrayList<String>> backupShapeOnClickArrayMap = new HashMap<>();
    Map<String, Page> backupPageContentMap = new HashMap<>();
    String backupPage;
    int backupPagePosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        // initial setup of the current activity
        db = openOrCreateDatabase("gamesTable",MODE_PRIVATE,null);
        currPage = findViewById(R.id.page_view_editor);
        Intent intent = getIntent();
        String currentGame = intent.getStringExtra("M");
        if (!currentGame.equals("")) {
            sqlLoadGame(currentGame);
        } else {
            onCreateSetUp();
        }
        imageFiles = imageFilesAdd();
        setUpPage();
        setUpShape();
        saveGame();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Button saveShapeBtn = findViewById(R.id.saveShape);
                saveShapeBtn.performClick();
            }
        }, 900);
    }

    /*
    the function to do initial setup of the current activity
    add Page 1, so always has Page1
    add Shape 1, so so always has Shape1
     */
    public void onCreateSetUp(){
        totalPage = 1;
        pageArray.add("Page1"); // default page
        pageSet.add("Page1");

        totalShape = 1;
        shapeArray.add("Shape1"); // default page
        shapeSet.add("Shape1");
        shapeOnDropArrayMap.put("Shape1", new ArrayList<String>());
        shapeOnClickArrayMap.put("Shape1", new ArrayList<String>());

        pageShapeMap.put("Page1", new ArrayList<String>());
        pageShapeMap.get("Page1").add("Shape1");
        pageScriptMap.put("Page1","addPage_Page1_null/");
        pageScriptArrayMap.put("Page1", new ArrayList<String>());

        Page startPage1 = new Page("Page1",null);
        Shape currShape = new Shape(0, 0, null, "Shape1" , false, false, null, null, null);
        startPage1.getShapeList().add(currShape);
        pageContentMap.put(startPage1.getPageName(),startPage1);
        currPage.setPageMap(pageContentMap);
    }

    public void sqlLoadGame(String currentGame){
        Cursor cursor = db.rawQuery("SELECT * FROM [" + currentGame + "]",null);
        while(cursor.moveToNext()) {
            int index;
            index = cursor.getColumnIndexOrThrow("pageName");
            String page = cursor.getString(index);
            index = cursor.getColumnIndexOrThrow("script");
            String script = cursor.getString(index);
            pageArray.add(page);
            gameHash.put(page, script);
        }
        for (String key : gameHash.keySet()) {
            Page pageNew = parsePageContent(gameHash.get(key), currPage);
            pageContentMap.put(key, pageNew);
        }
        currPage.setPageMap(pageContentMap);

        // setting all showUp containers
        currentPage = "Page1";
        pageSet = setCopy(pageContentMap.keySet());
        totalPage = pageSet.size();
        ArrayList<String> stringList = new ArrayList<String>();
        if (pageContentMap.get("Page1").getActionMap().containsKey("onEnter")) {
            stringList = new ArrayList<String>(Arrays.asList(pageContentMap.get("Page1").getActionMap().get("onEnter")));
        }
        currentPageEffectArray = stringList;
        totalShape = 0;

        // for each page in the pageContentMap, get the shapeList, then loop shapeList, out the names into an array, then put page, arr into pageShapeMap
        for(String page: pageContentMap.keySet()){
            Page tempPage = pageContentMap.get(page);
            List<Shape> tempShapeList = tempPage.getShapeList();
            ArrayList<String> shapeNameArr = new ArrayList<>();
            ArrayList<String> tempStringList = new ArrayList<>();
            if (pageContentMap.get(page).getActionMap().containsKey("onEnter")){
                tempStringList = new ArrayList<>(Arrays.asList(pageContentMap.get(page).getActionMap().get("onEnter")));
            }
            pageScriptArrayMap.put(page,tempStringList);
            for (Shape s : tempShapeList) {
                String tempShapeName = s.getName();
                shapeNameArr.add(tempShapeName);
                shapeSet.add(tempShapeName);
                totalShape++;
                ArrayList<String> onDropArrList = new ArrayList<String>();;
                if (s.getActionMap().containsKey("onDrop")) {
                    onDropArrList = new ArrayList<String>(Arrays.asList(s.getActionMap().get("onDrop")));
                }
                ArrayList<String> onClickArrList = new ArrayList<String>();
                if (s.getActionMap().containsKey("onClick")) {
                    onClickArrList = new ArrayList<String>(Arrays.asList(s.getActionMap().get("onClick")));
                }
                shapeOnDropArrayMap.put(tempShapeName,onDropArrList);
                shapeOnClickArrayMap.put(tempShapeName,onClickArrList);
            }
            pageShapeMap.put(page,shapeNameArr);
        }
        currentPageEffect = pageScriptMap.get("Page1");

        //PAGE SETTING
        List<Shape> currShapeList = pageContentMap.get("Page1").getShapeList();
        if (!currShapeList.isEmpty()) {
            currentShape = currShapeList.get(0).getName();
            shapeArray = pageShapeMap.get("Page1");
            currentOnDropEffectArray = shapeOnDropArrayMap.get(currentShape);
            currentOnClickEffectArray = shapeOnClickArrayMap.get(currentShape);
            currentShapeEffect = shapeScriptMap.get(currentShape);
        }
    }

    public Page parsePageContent(String content, PageViewEditor inputPage){
        Page page = new Page();
        String[] object = content.split("/");
        for (String s : object) {
            String[] item = s.split("_");
            if (item[0].equals("addPage")) {
                page = new Page(item[1], item[2]);
                pageScriptMap.put(item[1], s + "/");
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
                shapeScriptMap.put(item[3], s + "/");
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
     * the function that sets up the page spinner, add page, remove page, rename page button
     **/
    public void setUpPage(){
        final Spinner pageSpinner = findViewById(R.id.pageSpinner);
        final ArrayAdapter<String> pageAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, pageArray);
        pageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        pageSpinner.setAdapter(pageAdapter);
        for (int i = 0; i < pageArray.size(); i++) {
            if (pageArray.get(i).equals("Page1")) {
                pageSpinner.setSelection(i);
                break;
            }
        }
        currentPage = pageSpinner.getSelectedItem().toString();

        // REMOVE PAGE BUTTON
        Button removePageBtn = findViewById(R.id.removePageId);
        removePageBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // if user want to remove Page 1
                if (pageSpinner.getSelectedItem().toString().equals("Page1")) { // Page1 can't be removed
                    Toast toast = Toast.makeText(EditorActivity.this,"You must have a default enter page!",Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP|Gravity.LEFT, 70, 10);
                    toast.show();
                }else{
                    // Dialog that alert user when remove page
                    AlertDialog alertDialog = new AlertDialog.Builder(EditorActivity.this).create();
                    alertDialog.setTitle("Page Remove Alert");
                    alertDialog.setMessage("ALL SHAPES will be removed! \nDo you still want to remove this page?");
                    alertDialog.setIcon(R.drawable.carrot);
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "YES", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            ArrayList<String> currentPageShape = pageShapeMap.get(pageSpinner.getSelectedItem().toString());
                            for(String s: currentPageShape){
                                shapeSet.remove(s);
                            }
                            String selected = pageSpinner.getSelectedItem().toString();
                            pageArray.remove(selected);
                            pageSet.remove(selected);
                            pageShapeMap.remove(selected);
                            pageScriptArrayMap.remove(selected);
                            pageAdapter.notifyDataSetChanged();

                            pageSpinner.setAdapter(pageAdapter);
                            pageContentMap.remove(selected);
                            currPage.setPageMap(pageContentMap);
                            pageSpinner.setSelection(pageArray.size() - 1);

                            // Refer to the STACK OVERFLOW: How to automatically Click a Button in Android after a 5 second delay
                            // https://stackoverflow.com/questions/35708453/how-to-automatically-click-a-button-in-android-after-a-5-second-delay
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Button saveShapeBtn = findViewById(R.id.saveShape);
                                    saveShapeBtn.performClick();
                                }
                            }, 900);
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

        // ADD PAGE BUTTON
        Button addPageBtn = findViewById(R.id.addPageId);
        addPageBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                while (true){
                    boolean out = true;
                    for (String page : pageArray){
                        if (page.length() <= 4 || !page.substring(0,4).equals("Page")){
                            continue;
                        }
                        String regEx = "[^0-9]";
                        Pattern p = Pattern.compile(regEx);
                        Matcher m = p.matcher(page);
                        String string = m.replaceAll("").trim();
                        if (string.equals("")){
                            continue;
                        }
                        if (Integer.parseInt(string) == totalPage){
                            totalPage++;
                            out = false;
                        }
                    }
                    if (out){
                        break;
                    }
                }
                String tempPageName = "Page"+totalPage;
                pageArray.add(tempPageName);
                pageSet.add(tempPageName);
                pageScriptMap.put(tempPageName,"addPage_"+tempPageName+"_null/");
                pageAdapter.notifyDataSetChanged();
                ArrayList<String> shape = new ArrayList<String>();
                totalShape++;
                String tempShapeName = "Shape" + totalShape;
                shape.add(tempShapeName);
                shapeSet.add(tempShapeName);
                shapeOnDropArrayMap.put(tempShapeName, new ArrayList<String>());
                shapeOnClickArrayMap.put(tempShapeName, new ArrayList<String>());
                pageShapeMap.put(tempPageName, shape );
                pageScriptArrayMap.put(tempPageName, new ArrayList<String>());
                Toast toast = Toast.makeText(EditorActivity.this, tempPageName +" ADDED" ,Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP|Gravity.LEFT, 70, 10);
                toast.show();

                //add a new page and a shape
                Page newPage = new Page(tempPageName,"null");
                Shape currShape = new Shape(0, 0, null, "Shape" + totalShape, false, false, null, null, null);
                newPage.getShapeList().add(currShape);
                pageContentMap.put(newPage.getPageName(),newPage);
                currPage.setPageMap(pageContentMap);
                currentPage = tempPageName;

                //set current page to new added page
                pageSpinner.setSelection(pageArray.size() - 1);
            }
        });

        // RENAME PAGE BUTTON
        Button renamePageBtn = findViewById(R.id.renamePageButton);
        renamePageBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // if user want to remove Page 1
                EditText editText = findViewById(R.id.renamePageText);
                String newName = editText.getText().toString().trim();
                if(pageSpinner.getSelectedItem().toString().equals("Page1")){
                    Toast toast = Toast.makeText(EditorActivity.this,"Default page -Page1 cannot be renamed!",Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP|Gravity.LEFT, 70, 10);
                    toast.show();
                }else if (newName.equals("")){
                    Toast toast = Toast.makeText(EditorActivity.this,"Page Name cannot be Empty!",Toast.LENGTH_SHORT);
                    toast.show();
                }else if(!pageSet.contains(newName)){
                    // change map
                    String selected = pageSpinner.getSelectedItem().toString();
                    currentPage = selected;
                    ArrayList<String> tempShapeArray = pageShapeMap.get(selected);
                    pageShapeMap.remove(selected);

                    ArrayList<String> tempScriptArray = pageScriptArrayMap.get(selected);
                    pageScriptArrayMap.remove(selected);
                    pageScriptArrayMap.put(newName,tempScriptArray);

                    TextView currentPageEffectStr = findViewById(R.id.currentPageEffect_Editor);
                    String [] pageStrArr = pageScriptMap.get(selected).split("_");
                    pageScriptMap.remove(selected);
                    String newNameEffect = pageStrArr[0] + "_" + newName + "_" + pageStrArr[2];
                    pageScriptMap.put(newName,newNameEffect);
                    currentPageEffect = newNameEffect;
                    currentPageEffectStr.setText(newNameEffect);

                    // change array
                    pageShapeMap.put(newName,tempShapeArray);
                    pageArray.set(pageSpinner.getSelectedItemPosition(), newName);
                    // change set
                    pageSet.remove(selected);
                    pageSet.add(newName);
                    // change the page name in the pageContentMap
                    try {
                        Page currentPageObj = pageContentMap.get(currentPage);
                        pageContentMap.remove(currentPage);
                        currentPageObj.setPageName(newName);
                        pageContentMap.put(newName,currentPageObj);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    currentPage = newName;
                    // change adapter
                    pageAdapter.notifyDataSetChanged();
                }else{
                    Toast toast = Toast.makeText(EditorActivity.this,"Page Name already Exist!",Toast.LENGTH_SHORT);
                    toast.show();
                }
            }

        });
    }

    /**
     * the function that reset the effect of the shape of the view at EditorActivity with all null
     */
    public void resetShapeEffect(){
        Spinner shapeImageSpinner = findViewById(R.id.imageSpinner);
        EditText shapeTextEditText = findViewById(R.id.shapeTextInput);
        CheckBox movableCheckBox = findViewById(R.id.movableCheck);
        CheckBox visibleCheckBox = findViewById(R.id.visibleCheck);
        EditText leftPosition = findViewById(R.id.left_position);
        EditText topPosition = findViewById(R.id.top_position);
        TextView textViewShapeEffect = findViewById(R.id.currentShapeEffect_Editor);

        shapeImageSpinner.setSelection(0);
        shapeTextEditText.setText(null);
        movableCheckBox.setChecked(false);
        visibleCheckBox.setChecked(false);
        leftPosition.setText(null);
        topPosition.setText(null);
        textViewShapeEffect.setText("");
        ArrayList<String> onClickArrayList = new ArrayList<>();
        currentOnClickEffectArray = onClickArrayList;
        ArrayList<String> onDropArrayList = new ArrayList<>();
        currentOnClickEffectArray = onDropArrayList;
    }

    /**
     * the function that set up the shape spinner, shape image spinner, add shape button, remove shape button, rename shape button
     */
    public void setUpShape(){
        // set up shape image spinner
        shapeImageSpinner();
        final Spinner pageSpinner =  findViewById(R.id.pageSpinner);
        shapeArray = pageShapeMap.get(currentPage);

        // Spinner for the shape
        final Spinner shapeSpinner = findViewById(R.id.shapeSpinner);
        currentPage = pageSpinner.getSelectedItem().toString();
        final TextView PageScriptTextView = findViewById(R.id.currentPageEffect_Editor);

        // set spinner connection between page spinner and shape spinner
        pageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                currentPage = pageSpinner.getSelectedItem().toString();
                shapeArray = pageShapeMap.get(currentPage);

                currPage.toGo(currentPage);
                currPage.invalidate();

                // Spinner for the shape
                updateShapeSpinner(shapeSpinner);
                if (shapeSpinner.getSelectedItem() != null) {
                    currentShape = shapeSpinner.getSelectedItem().toString();
                }
                PageScriptTextView.setText(pageScriptMap.get(currentPage));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        //override shape spinner onSelection to capture currentShape
        shapeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                Spinner shapeImageSpinner = findViewById(R.id.imageSpinner);
                EditText shapeTextEditText = findViewById(R.id.shapeTextInput);
                CheckBox movableCheckBox = findViewById(R.id.movableCheck);
                CheckBox visibleCheckBox = findViewById(R.id.visibleCheck);
                CheckBox dropableCheckbox = findViewById(R.id.dropableCheck);
                CheckBox clickableCheckbox = findViewById(R.id.clickableCheck);
                TextView textViewShapeEffect = findViewById(R.id.currentShapeEffect_Editor);
                Shape currentShapeObj = null;
                if (shapeSpinner.getSelectedItem() != null) {
                    currentShape = shapeSpinner.getSelectedItem().toString();
                    Page currentPageObj = pageContentMap.get(currentPage);
                    List<Shape> shapeList = currentPageObj.getShapeList();
                    for(Shape shape : shapeList){
                        if(shape.getName().equals(currentShape)){
                            currentShapeObj = shape;
                        }
                    }
                    if (currentShapeObj == null) {
                        resetShapeEffect();
                    } else {
                        shapeImageSpinner.setSelection(findImagedPosition(currentShapeObj.getImageName()));
                        shapeTextEditText.setText(currentShapeObj.getText());
                        movableCheckBox.setChecked(currentShapeObj.getMovable());
                        visibleCheckBox.setChecked(currentShapeObj.getVisible());
                        if (!shapeOnDropArrayMap.get(currentShapeObj.getName()).isEmpty()) {
                            dropableCheckbox.setChecked(true);
                        }else{
                            dropableCheckbox.setChecked(false);
                        }
                        if (!shapeOnClickArrayMap.get(currentShapeObj.getName()).isEmpty()) {
                            clickableCheckbox.setChecked(true);
                        } else {
                            clickableCheckbox.setChecked(false);
                        }
                        currPage.reSetText(currentShapeObj);
                        textViewShapeEffect.setText(shapeScriptMap.get(currentShapeObj.getName()));
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                currentShape = "Shape1";
            }
        });

        // REMOVE SHAPE BUTTON
        Button removeShapeBtn = findViewById(R.id.removeShapeBtn);
        removeShapeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // if user want to remove Page 1
                shapeArray = pageShapeMap.get(currentPage);
                // Spinner for the shape
                if (shapeArray.size() == 0) {
                    return;
                }
                String selected = shapeSpinner.getSelectedItem().toString();
                shapeArray.remove(selected);
                shapeSet.remove(selected);
                shapeOnDropArrayMap.remove(selected);
                shapeOnClickArrayMap.remove(selected);

                List<Shape> modify = pageContentMap.get(currentPage).getShapeList();
                Iterator<Shape> it = modify.iterator();
                while (it.hasNext()){
                    Shape cur = it.next();
                    if (cur.getName().equals(selected)){
                        it.remove();
                    }
                }
                currPage.addShapeToDraw(pageContentMap.get(currentPage).getShapeList());
                currPage.invalidate();

                // update spinner for the shape
                updateShapeSpinner(shapeSpinner);
            }
        });

        // ADD SHAPE BUTTON
        Button addShapeBtn = findViewById(R.id.addShapeBtn);
        addShapeBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                while (true){
                    boolean out = true;
                    for (String shape : shapeSet){
                        if (shape.length() <= 5 || !shape.substring(0,5).equals("Shape")){
                            continue;
                        }
                        String regEx = "[^0-9]";
                        Pattern p = Pattern.compile(regEx);
                        Matcher m = p.matcher(shape);
                        String string = m.replaceAll("").trim();

                        if (string.equals("")){
                            continue;
                        }
                        if (Integer.parseInt(string) == totalShape){
                            totalShape++;
                            out = false;
                        }
                    }
                    if (out){
                        break;
                    }
                }

                //duplicate names
                String addShapeName = "Shape"+totalShape;
                shapeArray = pageShapeMap.get(currentPage);
                shapeArray.add(addShapeName);
                pageShapeMap.put(currentPage,shapeArray);
                shapeSet.add(addShapeName);
                shapeOnDropArrayMap.put(addShapeName, new ArrayList<String>());
                shapeOnClickArrayMap.put(addShapeName, new ArrayList<String>());
                updateShapeSpinner(shapeSpinner);
                Toast toast = Toast.makeText(EditorActivity.this, addShapeName +" ADDED" ,Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP|Gravity.RIGHT, 50, 10);
                toast.show();

                Shape shape = new Shape(0, 0, null, "Shape"+totalShape, false, false, null, null, null);
                pageContentMap.get(currentPage).getShapeList().add(shape);
                currPage.addShapeToDraw(pageContentMap.get(currentPage).getShapeList());
                currPage.invalidate();

                // change to the newly added shape
                shapeSpinner.setSelection(shapeArray.size()-1);
            }
        });

        // RENAME SHAPE BUTTON
        Button renameShapeBtn = findViewById(R.id.renameShapeBtn);
        renameShapeBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText editText = findViewById(R.id.renameShapeEdit);
                String newName = editText.getText().toString().trim();
                if(!newName.equals("")){
                    if(!shapeSet.contains(newName)){
                        // Spinner for the shape
                        final Spinner spinnerShape  = findViewById(R.id.shapeSpinner);
                        currentShape = spinnerShape.getSelectedItem().toString();
                        if(spinnerShape.getSelectedItem()!=null&&!shapeArray.isEmpty()){
                            int i = 0;
                            for (int j = 0;j < shapeArray.size();j++){
                                String s = shapeArray.get(j);
                                if (s.equals(currentShape)){
                                    i = j;
                                }
                            }
                            shapeArray.set(i,newName) ;
                            shapeSet.remove(currentShape);
                            shapeSet.add(newName);
                            ArrayList<String> tempOnDrop = shapeOnDropArrayMap.get(currentShape);
                            ArrayList<String> tempOnClick = shapeOnClickArrayMap.get(currentShape);
                            shapeOnClickArrayMap.remove(currentShape);
                            shapeOnDropArrayMap.remove(currentShape);
                            shapeOnDropArrayMap.put(newName,tempOnDrop);
                            shapeOnClickArrayMap.put(newName,tempOnClick);
                            List<Shape> modify = pageContentMap.get(currentPage).getShapeList();
                            try {
                                for (Shape s: modify){
                                    if (s.getName().equals(currentShape)){
                                        s.setName(newName);
                                    }
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                            currentShape = newName;
                            updateShapeSpinner(shapeSpinner);
                            shapeSpinner.setSelection(i);
                        }
                    }else{
                        Toast toast = Toast.makeText(EditorActivity.this,"Shape Name already Exist!",Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.TOP|Gravity.RIGHT, 20, 150);
                        toast.show();
                    }
                }
            }
        });
    }

    /**
     * function that setup the shape image spinner
     */
    public void shapeImageSpinner(){
        imageFiles.add(0,"no image");
        Spinner imageSpinner = findViewById(R.id.imageSpinner);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_item,
                        imageFiles);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        imageSpinner.setAdapter(spinnerArrayAdapter);
    }

    /**
     * function to save the shape script when click save shape button
     * save the current shape effect to string currentShapeEffect
     */
    public void saveShape(View view){
        saveShapeTrigger = true;
        Spinner pageSpinner = findViewById(R.id.pageSpinner);
        Spinner shapeImageSpinner = findViewById(R.id.imageSpinner);
        EditText shapeTextEditText = findViewById(R.id.shapeTextInput);
        CheckBox movableCheckBox = findViewById(R.id.movableCheck);
        CheckBox visibleCheckBox = findViewById(R.id.visibleCheck);
        EditText leftPosition = findViewById(R.id.left_position);
        EditText topPosition = findViewById(R.id.top_position);
        TextView textViewShapeEffect = findViewById(R.id.currentShapeEffect_Editor);
        Spinner spinnerShape = findViewById(R.id.shapeSpinner);
        if(spinnerShape.getSelectedItem() == null){
            currentShapeEffect = "";
            textViewShapeEffect.setText(currentShapeEffect);
            return;
        }
        String shapeName = currentShape;
        String imageName = shapeImageSpinner.getSelectedItem().toString();
        String shapeText = shapeTextEditText.getText().toString().trim();
        float left = 0;
        float top = 0;
        if(leftPosition.getText().toString().matches("")){
            left = 0;
        }else{
            left = Float.parseFloat(leftPosition.getText().toString());
            if (left > 1962){
                Toast toast = Toast.makeText(EditorActivity.this,"The Coordinates of Shape are out of Frame!",Toast.LENGTH_SHORT);
                toast.show();
                left = 1962;
            }
        }
        if(topPosition.getText().toString().matches("")){
            top = 0;
        }else{
            top = Float.parseFloat(topPosition.getText().toString());
            if (top > 1130){
                Toast toast = Toast.makeText(EditorActivity.this,"The Coordinates of Shape are out of Frame!",Toast.LENGTH_SHORT);
                toast.show();
                top = 1130;
            }
        }
        boolean movable = movableCheckBox.isChecked();
        boolean visiable = visibleCheckBox.isChecked();
        boolean dropable;
        boolean clickable;

        if(imageName.equals("no image")){
            imageName = "null";
        }
        if(shapeText == null || shapeText.trim().matches("")){
            shapeText = "null";
        }
        if(currentOnDropEffectArray.isEmpty()){
            dropable = false;
        }else{
            dropable = true;
        }
        if(currentOnClickEffectArray.isEmpty()){
            clickable = false;
        }else{
            clickable = true;
        }
        currentShapeEffect = "addShape_" + left+ "_" + top + "_"+ shapeName + "_"
                + movable + "_" + visiable + "_" + imageName + "_" + shapeText;
        if(!dropable && !clickable){
            currentShapeEffect = currentShapeEffect + "_null/";
        }else if(!dropable && clickable){
            currentShapeEffect = currentShapeEffect + "_onClick|" + TextUtils.join("|", currentOnClickEffectArray) + ";/";
        }else if(dropable && !clickable){
            currentShapeEffect = currentShapeEffect + "_onDrop|" + TextUtils.join("|", currentOnDropEffectArray) + ";/";
        }else{
            currentShapeEffect = currentShapeEffect + "_onDrop|" + TextUtils.join("|", currentOnDropEffectArray) + ";"
                    + "onClick|" + TextUtils.join(" | ", currentOnClickEffectArray) + ";/";
        }
        textViewShapeEffect.setText(currentShapeEffect);
        // add shape,script into the shapeScriptMap
        shapeScriptMap.put(shapeName,currentShapeEffect);

        String[] item = currentShapeEffect.split("_");
        float leftPoint = Float.parseFloat(item[1]);
        float topPoint = Float.parseFloat(item[2]);
        String graphName = item[6].equals("null") ? null : item[6];
        String T = item[7].equals("null") ? null : item[7];
        String str = item[8].split("/")[0];
        String S = str.equals("null") ? null : str;
        Bitmap img;
        if (graphName == null) {
            img = null;
        } else {
            img = currPage.getImage(graphName);
        }
        Page currentPageObj = pageContentMap.get(currentPage);
        List<Shape> shapeList = currentPageObj.getShapeList();
        for(Shape shape : shapeList){
            if (shape.getName().equals(currentShape)) {
                resetShape(shape, leftPoint, topPoint, img, item[3], movable, visiable, item[6], T, S);
                if(img!=null && shape.getText() == null) {
                    currPage.reSize(shape,img,0.75f);
                    currPage.relocate(shape);
                    if (shape.getBottom() - 0.75f * currPage.viewHeight >= 0 && shape.getTop() - 0.75f * currPage.viewHeight <= 0) {
                        if (shape.getBottom() - 0.75f * currPage.viewHeight < 0.5f * shape.getHeight()) {
                            shape.setBottom(currPage.viewHeight * 3 / 4 - 2.5f);
                            shape.setTop(shape.getBottom() - shape.getHeight());
                        }
                        else{
                            currPage.reSize(shape,img,0.25f);
                            currPage.relocate(shape);
                            shape.setTop(currPage.viewHeight * 3 / 4 + 2.5f);
                            shape.setBottom(shape.getTop() + shape.getHeight());
                        }
                    }
                }else{
                    shape.setBmp(null);
                }
                currPage.reSetText(shape);
                currPage.addShapeToDraw(pageContentMap.get(currentPage).getShapeList());
                break;
            }
        }
        currPage.invalidate();

        //backup
        backupShapeArray = arrayCopy(pageShapeMap.get(currentPage)); // should be no problem, but double check with them
        backupShapeName = spinnerShape.getSelectedItem().toString();
        backupShapePosition = spinnerShape.getSelectedItemPosition();
        backupTop = top;
        backupLeft = left;
        backupText = shapeText;
        backupImagePosition = shapeImageSpinner.getSelectedItemPosition();
        backMovable = movable;
        backVisible = visiable;
        backOnDropable = dropable;
        backOnClickable = clickable;
        backupTotalShape = totalShape;
        backupOnDropEffect = arrayCopy(currentOnDropEffectArray);
        backupOnClickEffect = arrayCopy(backupOnClickEffect);
        backupTextViewShapeEffect = currentShapeEffect;
        backupPageShapeMap = hashMapCopy(pageShapeMap);
        backupShapeSet = setCopy(shapeSet);
        backupTotalShape = totalShape;
        backupShapeScriptMap = hashMapCopyString(shapeScriptMap);
        backupShapeOnDropArrayMap = hashMapCopy(shapeOnDropArrayMap);
        backupShapeOnClickArrayMap = hashMapCopy(shapeOnClickArrayMap);
        backupPageContentMap = PageMapCopy(pageContentMap);
        backupPage = currentPage;
        backupPagePosition = pageSpinner.getSelectedItemPosition();
    }

    public void undoShape(View view){
        if(!saveShapeTrigger){
            return;
        }
        Spinner spinnerPage = findViewById(R.id.pageSpinner);
        Spinner spinnerShape = findViewById(R.id.shapeSpinner);
        Spinner shapeImageSpinner = findViewById(R.id.imageSpinner);
        EditText shapeTextEditText = findViewById(R.id.shapeTextInput);
        CheckBox movableCheckBox = findViewById(R.id.movableCheck);
        CheckBox visibleCheckBox = findViewById(R.id.visibleCheck);
        EditText leftPosition = findViewById(R.id.left_position);
        EditText topPosition = findViewById(R.id.top_position);
        TextView textViewShapeEffect = findViewById(R.id.currentShapeEffect_Editor);

        currentPage = backupPage;
        spinnerPage.setSelection(backupPagePosition);
        shapeArray = arrayCopy(backupShapeArray);
        currentOnDropEffectArray = arrayCopy(backupOnDropEffect);
        currentOnClickEffectArray = arrayCopy(backupOnClickEffect);
        shapeSet = setCopy(backupShapeSet);
        shapeScriptMap = hashMapCopyString(backupShapeScriptMap);
        shapeOnClickArrayMap = hashMapCopy(backupShapeOnClickArrayMap);
        shapeOnDropArrayMap = hashMapCopy(backupShapeOnDropArrayMap);
        pageShapeMap = hashMapCopy(backupPageShapeMap);
        totalShape = backupTotalShape;
        currentShape = backupShapeName;
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, backupShapeArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerShape.setAdapter(adapter);
        spinnerShape.setSelection(backupShapePosition);
        shapeImageSpinner.setSelection(backupImagePosition);
        shapeTextEditText.setText(backupText);
        movableCheckBox.setChecked(backMovable);
        visibleCheckBox.setChecked(backVisible);
        leftPosition.setText(Float.toString(backupLeft));
        topPosition.setText(Float.toString(backupTop));
        textViewShapeEffect.setText(backupTextViewShapeEffect);
        pageContentMap = PageMapCopy(backupPageContentMap);
    }

    /**
     * Function that go to a new activity to set the page effect
     */
    public void addPageEffect(View view){
        Intent intent = new Intent(this, pageEffectAcitivity.class);
        intent.putExtra("CURRENT_EFFECT_ARRAY", pageScriptArrayMap.get(currentPage));
        intent.putExtra("CURRENT_PAGE_ARRAY",currentPage);
        intent.putExtra("SHAPE_PAGE_MAP",pageShapeMap);
        startActivityForResult(intent, 1);
    }

    /**
     * Function that go to a new activity to set the on Click effect
     */
    public void onClickEffect(View view){
        Spinner spinnerShape = findViewById(R.id.shapeSpinner);
        if(spinnerShape.getSelectedItem() != null){
            CheckBox click = findViewById(R.id.clickableCheck);
            currentShape = spinnerShape.getSelectedItem().toString();
            if(click.isChecked()){
            }else{
                Toast toast = Toast.makeText(EditorActivity.this,"Setting Clickable True" ,Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.RIGHT|Gravity.CENTER, 460, 190);
                toast.show();
                click.setChecked(true);
            }
            Intent intent = new Intent(this,onClickEffectActivity.class);
            intent.putExtra("CURRENT_EFFECT_ARRAY",shapeOnClickArrayMap.get(currentShape));
            intent.putExtra("CURRENT_PAGE_ARRAY",currentPage);
            intent.putExtra("SHAPE_PAGE_MAP",pageShapeMap);
            intent.putExtra("CURRENT_SHAPE",currentShape);
            startActivityForResult(intent, 2);
        }else{
            Toast toast = Toast.makeText(EditorActivity.this,"Add shape first" ,Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.RIGHT|Gravity.CENTER, 460, 190);
            toast.show();
        }
    }

    /**
     * Function that go to a new activity to set the on Drop effect
     */
    public void onDropEffect(View view){
        Spinner spinnerShape = findViewById(R.id.shapeSpinner);
        if(spinnerShape.getSelectedItem() != null){
            CheckBox drop = findViewById(R.id.dropableCheck);
            currentShape = spinnerShape.getSelectedItem().toString();
            if(drop.isChecked()){
            }else{
                Toast toast = Toast.makeText(EditorActivity.this,"Setting Dropable True" ,Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.RIGHT|Gravity.CENTER, 360, 190);
                toast.show();
                drop.setChecked(true);
            }
            Intent intent = new Intent(this,onDropEffectActivity.class);
            intent.putExtra("CURRENT_EFFECT_ARRAY",shapeOnDropArrayMap.get(currentShape));
            intent.putExtra("CURRENT_PAGE_ARRAY",currentPage);
            intent.putExtra("SHAPE_PAGE_MAP",pageShapeMap);
            intent.putExtra("CURRENT_SHAPE",currentShape);
            startActivityForResult(intent, 3);
        }else{
            Toast toast = Toast.makeText(EditorActivity.this,"Add shape first" ,Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.RIGHT|Gravity.CENTER, 460, 190);
            toast.show();
        }
    }

    /**
     * parent activity data receiver from child page
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        TextView textView = findViewById(R.id.currentPageEffect_Editor);
        if (resultCode == RESULT_OK && requestCode ==  1) {
            if (data.hasExtra("PAGE_EFFECT_ARR" )) {
                currentPageEffectArray = data.getStringArrayListExtra("PAGE_EFFECT_ARR");
                String joined = TextUtils.join(" | ", currentPageEffectArray);
                if(!joined.isEmpty()){
                    joined = "addPage_" + currentPage + "_onEnter|" + joined +";/";
                    currentPageEffect = joined;
                    textView.setText(currentPageEffect);
                    pageScriptMap.put(currentPage,currentPageEffect);
                    pageScriptArrayMap.put(currentPage, currentPageEffectArray);
                    Page tempPage = pageContentMap.get(currentPage);
                    tempPage.setScript(currentPageEffect);
                    tempPage.setActionMap(Script.parseScript(currentPageEffect));
                    pageContentMap.put(tempPage.getPageName(),tempPage);
                    currPage.setPageMap(pageContentMap);
                }
                else{
                    joined = "addPage_" + currentPage + "_null;/";
                    currentPageEffect = joined;
                    textView.setText(currentPageEffect);
                }
            }
        }
        if (resultCode == RESULT_OK && requestCode ==  2) {
            if (data.hasExtra("ON_CLICK_EFFECT_ARR" )) {
                currentOnClickEffectArray = data.getStringArrayListExtra("ON_CLICK_EFFECT_ARR");
                shapeOnClickArrayMap.put(currentShape,currentOnClickEffectArray);
            }
        }
        if (resultCode == RESULT_OK && requestCode ==  3) {
            if (data.hasExtra("ON_DROP_EFFECT_ARR" )) {
                currentOnDropEffectArray = data.getStringArrayListExtra("ON_DROP_EFFECT_ARR");
                shapeOnDropArrayMap.put(currentShape,currentOnDropEffectArray);
            }
        }
    }

    public void sqlResetAndRecreate(String tableName){
        String resetStr = "DROP TABLE IF EXISTS [" + tableName +"];";
        db.execSQL(resetStr);
        String setupStr = "CREATE TABLE [" +
                tableName +
                "] ("
                + "pageName TEXT,"
                + "script TEXT,"
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT"
                + ");";
        db.execSQL(setupStr);
    }

    public void sqlInsertPageScript(String gameName,Map<String, Page> pageContentMap){
        for(String key: pageContentMap.keySet()){
            Page currPageInsert = pageContentMap.get(key);
            List<Shape> pageShapeListInsert = currPageInsert.getShapeList();
            String currPageInsertScript = pageScriptMap.get(currPageInsert.getPageName());
            for(Shape s : pageShapeListInsert){
                String shapeName = s.getName();
                if (shapeScriptMap.containsKey(shapeName)) {
                    String tempStr = shapeScriptMap.get(shapeName);
                    currPageInsertScript = currPageInsertScript + tempStr;
                }
            }
            String insertStr = "INSERT INTO [" +
                    gameName +
                    "] VALUES "
                    + "('" +
                    key +
                    "','" +
                    currPageInsertScript+
                    "',NULL)"
                    + ";";
            db.execSQL(insertStr);
        }
    }

    public void resetShape(Shape currShape, float leftPoint, float topPoint, Bitmap img, String shapeName, boolean Move, boolean visibility, String graphName, String T, String S) {
        currShape.setTop(topPoint);
        currShape.setLeft(leftPoint);
        currShape.setName(shapeName);
        currShape.setMovable(Move);
        currShape.setVisible(visibility);
        currShape.setImageName(graphName);
        currShape.setText(T);
        currShape.setScript(S);
        if (img != null){
            currShape.setBmp(img);
            currShape.setRight(leftPoint + img.getWidth());
            currShape.setBottom(topPoint + img.getHeight());
        }else {
            currShape.setRight(leftPoint + 200);
            currShape.setBottom(topPoint + 200);
        }
        if (T != null){
            currShape.setRight(leftPoint + 500);
            currShape.setBottom(topPoint + 400);
            currShape.setBmp(null);
        }
        currShape.setWidth(Math.abs(currShape.getLeft()-currShape.getRight()));
        currShape.setHeight(Math.abs(currShape.getTop()-currShape.getBottom()));
        currShape.setActionMap(Script.parseScript(S));
    }

    public ArrayList<String> imageFilesAdd(){
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
        return imageFiles;
    }

    /**
     * find the image position in the array of image by passing in a imageName
     */
    public int findImagedPosition(String imageName){
        int imagePosition = 0;
        int position = -1;
        for(String s : imageFiles){
            position++;
            if(s.equals(imageName)){
                imagePosition = position;
            }
        }
        return  imagePosition;
    }

    public void updateShapeSpinner(final Spinner shapeSpinner) {
        ArrayAdapter<String> adapterShape = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, shapeArray);
        adapterShape.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        shapeSpinner.setAdapter(adapterShape);
    }

    public void saveGame(){
        Button saveGameBtn = findViewById(R.id.saveGame);
        saveGameBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // get the gameName
                EditText editTextGameName = findViewById(R.id.gameName);
                final String gameName = editTextGameName.getText().toString().trim();
                if(gameName.equals("")){
                    Toast toast = Toast.makeText(EditorActivity.this, "Game Name Can't be Empty String" ,Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.BOTTOM|Gravity.RIGHT, 70, 10);
                    toast.show();
                    return;
                }
                final String setupStr = "CREATE TABLE [" +
                        gameName +
                        "] ("
                        + "pageName TEXT,"
                        + "script TEXT,"
                        + "_id INTEGER PRIMARY KEY AUTOINCREMENT"
                        + ");";

                // get the cursor the test whether the gameName already exist
                Cursor Cursor = db.rawQuery(
                        "SELECT * FROM sqlite_master WHERE type='table' AND name='" +
                                gameName +
                                "';",null);

                if(Cursor.getCount() !=0){ // if the gameName exist in the games table
                    AlertDialog alertDialog = new AlertDialog.Builder(EditorActivity.this).create();
                    alertDialog.setTitle("Game Name Exist Alert");
                    alertDialog.setMessage("Game Already Exist! Do you want to rewrite the existing game?");
                    alertDialog.setIcon(R.drawable.carrot);
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "YES", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            sqlResetAndRecreate(gameName);
                            sqlInsertPageScript(gameName,pageContentMap);
                            Toast toast = Toast.makeText(EditorActivity.this,"Game Saved!",Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER|Gravity.CENTER, 0, 0);
                            toast.show();
                            finish();
                        }
                    });
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NO", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    alertDialog.show();
                }else{ // not exist yet
                    // insert the gameName into games table; the table that store all the game names if the name not already exist
                    String saveGameNameIntoGamesStr = "INSERT INTO games VALUES "
                            + "('" +
                            gameName +
                            "',NULL)"
                            + ";";
                    db.execSQL(saveGameNameIntoGamesStr);
                    db.execSQL(setupStr);
                    sqlInsertPageScript(gameName, pageContentMap);
                    Intent returnIntent = new Intent();
                    setResult(RESULT_CANCELED, returnIntent);
                    Toast toast = Toast.makeText(EditorActivity.this,"Game Saved!",Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER|Gravity.CENTER, 0, 0);
                    toast.show();
                    finish();
                }
            }
        });
    }

    public HashMap<String, ArrayList<String>> hashMapCopy(HashMap<String, ArrayList<String>> original){
        HashMap<String, ArrayList<String>> copy = new HashMap<String, ArrayList<String>>();
        for (Map.Entry<String, ArrayList<String>> entry : original.entrySet())
        {
            copy.put(entry.getKey(), new ArrayList<String>(entry.getValue()));
        }
        return copy;
    }

    public HashMap<String, String> hashMapCopyString(HashMap<String, String> original){
        HashMap<String, String> copy = new HashMap<String, String>();
        copy.putAll(original);
        return copy;
    }

    public ArrayList<String> arrayCopy(ArrayList<String> original){
        ArrayList<String> copy = new ArrayList<String>();
        for(String p : original) {
            copy.add(p);
        }
        return copy;
    }

    public Set<String> setCopy(Set<String> original) {
        Set<String> copy = new HashSet<>();
        copy.addAll(original);
        return copy;
    }

    public Map<String, Page> PageMapCopy(Map<String, Page> original){
        Map<String, Page> copy = new HashMap<>();
        for(String newKey : original.keySet()){
            Page newPage = new Page(original.get(newKey));
            copy.put(newKey, newPage);
        }
        return copy;
    }
}

