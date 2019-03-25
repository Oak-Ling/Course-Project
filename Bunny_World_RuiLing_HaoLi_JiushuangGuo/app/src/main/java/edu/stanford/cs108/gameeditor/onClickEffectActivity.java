package edu.stanford.cs108.gameeditor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
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


public class onClickEffectActivity extends AppCompatActivity {
    ArrayList<String> pageArray = new ArrayList<String>();
    ArrayList<String> onClickEffectArray = new ArrayList<String>();
    HashSet<String> onClickEffectSet= new HashSet<String>();
    HashMap<String, ArrayList<String>> pageShapeMap = new HashMap<String, ArrayList<String>>();
    final String[] effectStr = new String[1];
    final String[] pageStr = new String[1];
    final String[] itemStr = new String[1];
    String currentShape;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_click_effect);

        // get the info from last activity
        Intent intent = getIntent();
        onClickEffectArray = intent.getStringArrayListExtra("CURRENT_EFFECT_ARRAY");
        pageShapeMap = (HashMap<String, ArrayList<String>>) intent.getSerializableExtra("SHAPE_PAGE_MAP");
        currentShape = intent.getStringExtra("CURRENT_SHAPE");
        onClickEffectSet = stringArrayToSet(onClickEffectArray);
        pageArray = new ArrayList<String>(pageShapeMap.keySet());
        //setupAddSpinner();
        addClickEffectTo();
        saveEffect();
        onEnterSpinner();

        // set the list view
        ListView listView = findViewById(R.id.onClickEffectListView);
        MyCustomAdapter adapter = new MyCustomAdapter(onClickEffectArray, onClickEffectActivity.this, onClickEffectSet);
        listView.setAdapter(adapter);

        // set the current shape name
        TextView textView = findViewById(R.id.onClickCurrentShapeName);
        textView.setText("Setting On Click effect for " + currentShape);

    }

    public void onEnterSpinner(){
        // FIRST SPINNER
        // Array of the first Spinner
        ArrayList<String> onEnterEffectArray = new ArrayList<String>();
        onEnterEffectArray.add("play");
        onEnterEffectArray.add("show");
        onEnterEffectArray.add("hide");
        onEnterEffectArray.add("goto");

        final Spinner onEnterSpinner = findViewById(R.id.onClickEffectSpinner);
        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_item,
                        onEnterEffectArray);
        adapter1.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        onEnterSpinner.setAdapter(adapter1);

        // SECOND SPINNER
        // array of the second spinner
        final ArrayList<String> musicFiles =  new ArrayList<String>();
        final Spinner PageSpinner = findViewById(R.id.onClickPageSpinner);
        Field[] fields=R.raw.class.getFields();
        for(int count=0; count < fields.length; count++){
            musicFiles.add(fields[count].getName());
        }

        //THIRD SPINNER
        final Spinner itemSpinner = findViewById(R.id.onClickItemSpinner);

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
                            (onClickEffectActivity.this, android.R.layout.simple_spinner_item,
                                    musicFiles);
                    adapter3 = new ArrayAdapter<String>
                            (onClickEffectActivity.this, android.R.layout.simple_spinner_item,
                                    new ArrayList<String>());
                    adapter3.setDropDownViewResource(android.R.layout
                            .simple_spinner_dropdown_item);
                    itemSpinner.setAdapter(adapter3 );

                }else if(effectStr[0].equals("show") ){
                    ArrayList<String> onClickPageArrayShow = (ArrayList<String>) pageArray.clone();
                    adapter2 = new ArrayAdapter<String>
                            (onClickEffectActivity.this, android.R.layout.simple_spinner_item,
                                    onClickPageArrayShow);
                }else if(effectStr[0].equals("hide") ){
                    ArrayList<String> onClickPageArrayHide = (ArrayList<String>) pageArray.clone();
                    onClickPageArrayHide.add("possession");
                    adapter2 = new ArrayAdapter<String>
                            (onClickEffectActivity.this, android.R.layout.simple_spinner_item,
                                    onClickPageArrayHide);
                }else{
                    adapter2 = new ArrayAdapter<String>
                            (onClickEffectActivity.this, android.R.layout.simple_spinner_item,
                                    pageArray);
                    adapter3 = new ArrayAdapter<String>
                            (onClickEffectActivity.this, android.R.layout.simple_spinner_item,
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
                                (onClickEffectActivity.this, android.R.layout.simple_spinner_item,
                                        pageShapeMap.get(pageStr[0]));
                        adapter3.setDropDownViewResource(android.R.layout
                                .simple_spinner_dropdown_item);
                        itemSpinner.setAdapter(adapter3);
                    }
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

    public void addClickEffectTo(){
        final Button  button = findViewById(R.id.addClickEffectButton2);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if( (effectStr[0].equals("show") || effectStr[0].equals("hide")) && itemStr[0] == null ){
                    Toast toast = Toast.makeText(onClickEffectActivity.this,"ADD SHAPE TO THIS PAGE FIRST!" ,Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, -100, 20);
                    toast.show();
                }else{
                    String currentEffect;
                    if(effectStr[0].equals("goto") || effectStr[0].equals("play")){
                        currentEffect = effectStr[0] + " "+ pageStr[0];
                    }else{
                        currentEffect = effectStr[0] + " "+ pageStr[0]+"@" +itemStr[0];
                    }
                    // the listView and adapter
                    ListView listView = findViewById(R.id.onClickEffectListView);
                    MyCustomAdapter adapter = new MyCustomAdapter(onClickEffectArray, onClickEffectActivity.this, onClickEffectSet);
                    onClickEffectSet = adapter.getPageEffectHash();
                    if(!onClickEffectSet.contains(currentEffect)){
                        onClickEffectArray.add(currentEffect);
                        listView.setAdapter(adapter);
                        onClickEffectSet.add(currentEffect);
                    }else{
                        Toast toast = Toast.makeText(onClickEffectActivity.this,"Effect Already Exist" ,Toast.LENGTH_SHORT);
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
        Button saveClickButton = findViewById(R.id.saveClickButton_page_effect);
        saveClickButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent data = new Intent();
                data.putExtra("ON_CLICK_EFFECT_ARR", onClickEffectArray);
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
