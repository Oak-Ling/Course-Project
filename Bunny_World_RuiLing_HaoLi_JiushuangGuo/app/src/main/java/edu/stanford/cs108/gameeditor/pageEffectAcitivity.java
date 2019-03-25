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
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class pageEffectAcitivity extends AppCompatActivity {
    ArrayList<String> pageArray = new ArrayList<String>();
    ArrayList<String> pageEffectArray = new ArrayList<String>();
    HashSet<String> pageEffectSet = new HashSet<String>();
    HashMap<String, ArrayList<String>> pageShapeMap = new HashMap<String, ArrayList<String>>();
    final String[] effectStr = new String[1];
    final String[] pageStr = new String[1];

    final String[] itemStr = new String[1];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_effect_acitivity);
        // get the info from last activity
        Intent intent = getIntent();
        pageEffectArray = intent.getStringArrayListExtra("CURRENT_EFFECT_ARRAY");
        pageShapeMap = (HashMap<String, ArrayList<String>>) intent.getSerializableExtra("SHAPE_PAGE_MAP");
        pageEffectSet = stringArrayToSet(pageEffectArray);
        pageArray = new ArrayList<String>(pageShapeMap.keySet());
        onEnterSpinner();
        addPageEffectTo();
        saveEffect();


        // set the list view
        ListView listView = findViewById(R.id.effectListView);
        MyCustomAdapter adapter = new MyCustomAdapter(pageEffectArray, pageEffectAcitivity.this, pageEffectSet);
        listView.setAdapter(adapter);
    }

    public void onEnterSpinner(){
        // FIRST SPINNER
        // Array of the first Spinner
        ArrayList<String> onEnterEffectArray = new ArrayList<String>();
        onEnterEffectArray.add("play");
        onEnterEffectArray.add("show");
        onEnterEffectArray.add("hide");

        // FIRST Spinner
        final Spinner onEnterSpinner = findViewById(R.id.onEnterSpinner);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_item,
                        onEnterEffectArray);
        adapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        onEnterSpinner.setAdapter(adapter);
//        // str that stores the select effect
//        final String[] effectStr = new String[1];


        // SECOND SPINNER
        // array of the second spinner
        final ArrayList<String> musicFiles =  new ArrayList<String>();
        final ArrayList<String> shapes =  new ArrayList<String>();
        final Spinner PageSpinner = findViewById(R.id.onEnterPageSpinner);
        shapes.add("test shape");
        Field[] fields=R.raw.class.getFields();
        for(int count=0; count < fields.length; count++){
            musicFiles.add(fields[count].getName());
        }

        //THIRD SPINNER
        final Spinner itemSpinner = findViewById(R.id.onEnterItemSpinner);

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
                            (pageEffectAcitivity.this, android.R.layout.simple_spinner_item,
                                    musicFiles);

                    adapter3 =  new ArrayAdapter<String>
                            (pageEffectAcitivity.this, android.R.layout.simple_spinner_item,
                                    new ArrayList<String>() );
                    adapter3.setDropDownViewResource(android.R.layout
                            .simple_spinner_dropdown_item);
                    itemSpinner.setAdapter(adapter3);

                }else{
                    adapter2 = new ArrayAdapter<String>
                            (pageEffectAcitivity.this, android.R.layout.simple_spinner_item,
                                    pageArray);
                }

                adapter2.setDropDownViewResource(android.R.layout
                        .simple_spinner_dropdown_item);
                PageSpinner.setAdapter(adapter2);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
        });

        PageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            // String effectStr;
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                pageStr[0]= PageSpinner.getSelectedItem().toString();
                ArrayAdapter<String> adapter3;
                if(pageShapeMap.get(pageStr[0]) != null && !effectStr[0].equals("play")){
                    adapter3 = new ArrayAdapter<String>
                            (pageEffectAcitivity.this, android.R.layout.simple_spinner_item,
                                    pageShapeMap.get(pageStr[0]));
                    adapter3.setDropDownViewResource(android.R.layout
                            .simple_spinner_dropdown_item);
                    itemSpinner.setAdapter(adapter3);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
        });

        itemSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            // String effectStr;
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if(itemSpinner.getSelectedItem()!=null){
                    itemStr[0]= itemSpinner.getSelectedItem().toString();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
        });
    }

    public void addPageEffectTo(){
        final Button  button = findViewById(R.id.addPageEffectButton2);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String currentEffect;

                if( (effectStr[0].equals("show") || effectStr[0].equals("hide")) && itemStr[0] == null ){
                    Toast toast = Toast.makeText(pageEffectAcitivity.this,"ADD SHAPE TO THIS PAGE FIRST!" ,Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, -100, 20);
                    toast.show();
                }else{

                    if(itemStr[0] == null){
                        currentEffect = effectStr[0] + " "+ pageStr[0];
                    }else{
                        currentEffect = effectStr[0] + " "+ pageStr[0]+"@" +itemStr[0];
                    }
                    // the listView and adapter
                    ListView listView = findViewById(R.id.effectListView);
                    MyCustomAdapter adapter = new MyCustomAdapter(pageEffectArray, pageEffectAcitivity.this, pageEffectSet);
                    pageEffectSet = adapter.getPageEffectHash();

                    if(!pageEffectSet.contains(currentEffect)){
                        pageEffectArray.add(currentEffect);
                        listView.setAdapter(adapter);
                        pageEffectSet.add(currentEffect);
                    }else{
                        Toast toast = Toast.makeText(pageEffectAcitivity.this,"Effect Already Exist" ,Toast.LENGTH_SHORT);
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
        Button saveEffectButton = findViewById(R.id.saveEffectButton_page_effect);
        saveEffectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent data = new Intent();

                data.putExtra("PAGE_EFFECT_ARR", pageEffectArray);
                // Activity finished ok, return the data
                setResult(RESULT_OK, data);
                finish();
            }
        });
    }

    public String hashsetToString(HashSet<String> hashSet){
        String result = "";
        if(!hashSet.isEmpty()){
            for(String s:hashSet){
                result += "|" + s;
            }
            //result = result.substring(1);
        }
        return result;
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

