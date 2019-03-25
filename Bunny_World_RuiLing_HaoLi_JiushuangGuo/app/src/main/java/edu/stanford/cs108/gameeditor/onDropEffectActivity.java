package edu.stanford.cs108.gameeditor;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class onDropEffectActivity extends AppCompatActivity {

    ArrayList<String> pageArray = new ArrayList<String>();
    ArrayList<String> onDropEffectArray = new ArrayList<String>();
    HashSet<String> onDropEffectSet= new HashSet<String>();
    HashMap<String, ArrayList<String>> pageShapeMap = new HashMap<String, ArrayList<String>>();
    final String[] effectStr = new String[1];
    final String[] pageStr = new String[1];
    final String[] itemStr = new String[1];
    String currentShape = "Shape1";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_drop_effect);

        // get the info from last activity
        Intent intent = getIntent();
        onDropEffectArray = intent.getStringArrayListExtra("CURRENT_EFFECT_ARRAY");
        pageShapeMap = (HashMap<String, ArrayList<String>>) intent.getSerializableExtra("SHAPE_PAGE_MAP");
        currentShape = intent.getStringExtra("CURRENT_SHAPE");

        onDropEffectSet = stringArrayToSet(onDropEffectArray);
        pageArray = new ArrayList<String>(pageShapeMap.keySet());
        //setupAddSpinner();
        addDropEffectTo();
        saveEffect();
        onEnterSpinner();

        // set the list view
        ListView listView = findViewById(R.id.onDropEffectListView);
        MyCustomAdapter adapter = new MyCustomAdapter(onDropEffectArray, onDropEffectActivity.this, onDropEffectSet);
        listView.setAdapter(adapter);

        // set the EditText
        TextView textView = findViewById(R.id.onDropCurrentShapeName);
        textView.setText("Setting On Drop effect for  " + currentShape);
    }

    public void onEnterSpinner(){
        // FIRST SPINNER
        // Array of the first Spinner
        ArrayList<String> onEnterEffectArray = new ArrayList<String>();
        onEnterEffectArray.add("play");
        onEnterEffectArray.add("show");
        onEnterEffectArray.add("hide");
        onEnterEffectArray.add("goto");

        final Spinner onEnterSpinner = findViewById(R.id.onDropEffectSpinner);
        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_item,
                        onEnterEffectArray);
        adapter1.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        onEnterSpinner.setAdapter(adapter1);

        // SECOND SPINNER
        // array of the second spinner
        final ArrayList<String> musicFiles =  new ArrayList<String>();
        final Spinner PageSpinner = findViewById(R.id.onDropPageSpinner);
        Field[] fields=R.raw.class.getFields();
        for(int count=0; count < fields.length; count++){
            musicFiles.add(fields[count].getName());
        }

        //THIRD SPINNER
        final Spinner itemSpinner = findViewById(R.id.onDropItemSpinner);
        // on Select capture the dynamic select and set the second spinner
        onEnterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            // String effectStr;
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                effectStr[0] = onEnterSpinner.getSelectedItem().toString();
                ArrayAdapter<String> adapter2;
                ArrayAdapter<String> adapter3;
                if(effectStr[0].equals("play")){
                    adapter2 = new ArrayAdapter<String>
                            (onDropEffectActivity.this, android.R.layout.simple_spinner_item,
                                    musicFiles);
                    adapter3 = new ArrayAdapter<String>
                            (onDropEffectActivity.this, android.R.layout.simple_spinner_item,
                                    new ArrayList<String>());
                    adapter3.setDropDownViewResource(android.R.layout
                            .simple_spinner_dropdown_item);
                    itemSpinner.setAdapter(adapter3 );
                }else if( effectStr[0].equals("hide") ){
                    ArrayList<String> onDropPageArray = (ArrayList<String>) pageArray.clone();
                    onDropPageArray.add("dropped");
                    adapter2 = new ArrayAdapter<String>
                            (onDropEffectActivity.this, android.R.layout.simple_spinner_item,
                                    onDropPageArray);
                }else if(effectStr[0].equals("show")){
                    ArrayList<String> onDropPageArray = (ArrayList<String>) pageArray.clone();
                    adapter2 = new ArrayAdapter<String>
                            (onDropEffectActivity.this, android.R.layout.simple_spinner_item,
                                    onDropPageArray);
                }else{
                    adapter2 = new ArrayAdapter<String>
                            (onDropEffectActivity.this, android.R.layout.simple_spinner_item,
                                    pageArray);
                    adapter3 = new ArrayAdapter<String>
                            (onDropEffectActivity.this, android.R.layout.simple_spinner_item,
                                    new ArrayList<String>());
                    adapter3.setDropDownViewResource(android.R.layout
                            .simple_spinner_dropdown_item);
                    itemSpinner.setAdapter(adapter3 );
                }
                adapter2.setDropDownViewResource(android.R.layout
                        .simple_spinner_dropdown_item);
                PageSpinner.setAdapter(adapter2);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        PageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                pageStr[0]= PageSpinner.getSelectedItem().toString();
                ArrayAdapter<String> adapter3;
                if(pageShapeMap.get(pageStr[0])!=null){
                    if(!effectStr[0].equals("goto")){
                        adapter3 = new ArrayAdapter<String>
                                (onDropEffectActivity.this, android.R.layout.simple_spinner_item,
                                        pageShapeMap.get(pageStr[0]));
                        adapter3.setDropDownViewResource(android.R.layout
                                .simple_spinner_dropdown_item);
                        itemSpinner.setAdapter(adapter3);
                    }
                }else if(pageStr[0].equals("dropped")){
                    ArrayList<String> imageFiles =  new ArrayList<String>();
                    // available image spinner
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
                    imageFiles.add("everything");
                    adapter3 = new ArrayAdapter<String>
                            (onDropEffectActivity.this, android.R.layout.simple_spinner_item,
                                    imageFiles);
                    adapter3.setDropDownViewResource(android.R.layout
                            .simple_spinner_dropdown_item);
                    itemSpinner.setAdapter(adapter3);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        itemSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if(itemSpinner.getSelectedItem()!=null){
                    itemStr[0]= itemSpinner.getSelectedItem().toString();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
    }


    public void addDropEffectTo(){
        final Button  button = findViewById(R.id.addDropEffectButton2);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if( (effectStr[0].equals("show") || effectStr[0].equals("hide")) && itemStr[0] == null){
                    Toast toast = Toast.makeText(onDropEffectActivity.this,"ADD SHAPE TO THIS PAGE FIRST!" ,Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, -100, 20);
                    toast.show();
                }else{
//
//                    Spinner ondropItemPage = findViewById(R.id.onDropPage_ondrop);
//                    Spinner dondropItemName = findViewById(R.id.onDropItem_ondrop);

                    String currentEffect;
                    if(itemStr[0] == null){
                        currentEffect =   effectStr[0] + " "+ pageStr[0];
                    }else{
                        currentEffect = effectStr[0] + " "+ pageStr[0]+"@" +itemStr[0];
                    }
                    // the listView and adapter
                    ListView listView = findViewById(R.id.onDropEffectListView);
                    MyCustomAdapter adapter = new MyCustomAdapter(onDropEffectArray, onDropEffectActivity.this, onDropEffectSet);
                    onDropEffectSet = adapter.getPageEffectHash();
                    if(!onDropEffectSet.contains(currentEffect)){
                        onDropEffectArray.add(currentEffect);
                        listView.setAdapter(adapter);
                        onDropEffectSet.add(currentEffect);
                    }else{
                        Toast toast = Toast.makeText(onDropEffectActivity.this,"Effect Already Exist" ,Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, -100, 20);
                        toast.show();
                    }
                }
            }
        });
    }

    /*
    Function when click save effect button, return to parent page and pass the page Effect str and ArrayList Back
     */
    public void saveEffect(){
        Button saveOnDropButton = findViewById(R.id.saveDropButton_page_effect);
        saveOnDropButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent data = new Intent();
                data.putExtra("ON_DROP_EFFECT_ARR", onDropEffectArray);
                // Activity finished ok, return the data
                setResult(RESULT_OK, data);
                finish();
            }
        });
    }
    public HashSet<String> stringArrayToSet(ArrayList<String> list){
        HashSet<String> set = new HashSet<String>();
        if(!list.isEmpty()){
            for(String s: list){
                set.add(s);
            }
        }
        return set;
    }
}
