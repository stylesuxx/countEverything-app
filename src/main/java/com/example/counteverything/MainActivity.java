package com.example.counteverything;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.app.Activity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private final static String TAG = "Main";
    private DataAdapter mDbHelper;
    private Button btnAdd, btnDelete;
    private Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*
        // TODO do not execute networking in the main thread
        StrictMode.ThreadPolicy policy = new StrictMode.
        ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);*/

        mDbHelper = new DataAdapter(this);
        mDbHelper.createDatabase();
        mDbHelper.open();

        //mDbHelper.close();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TabHost tabhost = (TabHost) findViewById(R.id.tabHost);
        tabhost.setup();

        TabHost.TabSpec tabSpec = tabhost.newTabSpec("tabMain");
        tabSpec.setContent(R.id.tabMain);
        tabSpec.setIndicator("Main");
        tabhost.addTab(tabSpec);

        /*
        // TODO
        tabSpec = tabhost.newTabSpec("tabStats");
        tabSpec.setContent(R.id.tabStats);
        tabSpec.setIndicator("Stats");
        tabhost.addTab(tabSpec);
        */

        tabSpec = tabhost.newTabSpec("tabManage");
        tabSpec.setContent(R.id.tabManage);
        tabSpec.setIndicator("Manage");
        tabhost.addTab(tabSpec);

        addItemsDeleteable();
        addItemButtons();

        btnAdd = (Button) findViewById(R.id.btn_settings);
        btnAdd.setOnClickListener(addNewItem);

        btnDelete = (Button) findViewById(R.id.btn_item_delete);
        btnDelete.setOnClickListener(deleteItem);

    }

    /**
     * When an item submit button is pressed get the data from the db and send to webserver.
     */
    View.OnClickListener addItem = new View.OnClickListener() {
        public void onClick(View v) {
            try{
                int id = ((Button)v).getId();
                String name = mDbHelper.getItemName(id);
                float amount = mDbHelper.getItemAmount(id);
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(v.getContext());
                String token = prefs.getString("api_token", "");
                String params = "?action=add&token=" + token + "&item=" + URLEncoder.encode(name, "utf-8") + "&amount=" + amount;
                String url = prefs.getString("api_url", "") + params;

                Log.v(TAG, name);

                new Api(v.getContext()).execute(url);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    };

    View.OnClickListener addNewItem = new View.OnClickListener() {
        public void onClick(View v) {
            float amount;
            EditText field = (EditText)findViewById(R.id.edit_manage_name);
            String name = field.getText().toString();
            if(name.isEmpty()){
                Toast.makeText(getApplicationContext(), "Enter name", Toast.LENGTH_SHORT).show();
                return;
            }

            try{
                field = (EditText)findViewById(R.id.edit_manage_amount);
                amount = Float.valueOf(field.getText().toString());
            } catch(Exception ex){
                Toast.makeText(getApplicationContext(), "Enter amount", Toast.LENGTH_SHORT).show();
                return;
            }

            String text = "Error while adding new item to the database.";
            if(mDbHelper.addNewBeverage(name, amount)){
                text = "Added new item to your database.";
                field = (EditText)findViewById(R.id.edit_manage_amount);
                field.setText("");
                field = (EditText)findViewById(R.id.edit_manage_name);
                field.setText("");
                addItemsDeleteable();
                addItemButtons();
            }
            Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
        }
    };

    View.OnClickListener deleteItem = new View.OnClickListener() {
        public void onClick(View v) {
            String text = "";
            if (( (SpinnerObject) spinner.getSelectedItem () ) != null) {
                int databaseId = Integer.parseInt (String.valueOf(( (SpinnerObject) spinner.getSelectedItem () ).getId ()));
                mDbHelper.removeItem(databaseId);
                text = "Deleted item " + databaseId + " from your database.";
            }
            else {
                text = "Error while removing item from the database.";
            }
            addItemsDeleteable();
            addItemButtons();
            Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_settings:
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                break;
            default:
                Toast.makeText(getApplicationContext(), "Unknown...", Toast.LENGTH_SHORT).show();
                break;
        }

        //Return false to allow normal menu processing to proceed,
        //true to consume it here.
        return false;
    }

    public void addItemButtons(){
        Cursor cursor = mDbHelper.getItems();

        if (cursor.moveToFirst()){
            LinearLayout layout = (LinearLayout) findViewById(R.id.tab_main_content);
            if(((LinearLayout) layout).getChildCount() > 0)
                ((LinearLayout) layout).removeAllViews();
            do{
                int id = cursor.getInt(cursor.getColumnIndex("_id"));
                String name = cursor.getString(cursor.getColumnIndex("name"));
                String amount = cursor.getString(cursor.getColumnIndex("amount"));

                //set the properties for button
                Button btnTag = new Button(this);
                btnTag.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                btnTag.setText(name + "/" + amount);
                btnTag.setId(id);
                btnTag.setOnClickListener(addItem);

                //add button to the layout
                layout.addView(btnTag);
            }while(cursor.moveToNext());
        }
        cursor.close();
    }

    public void addItemsDeleteable() {
        spinner = (Spinner) findViewById(R.id.spinner);
        List<SpinnerObject> list = new ArrayList<SpinnerObject>();
        Cursor cursor = mDbHelper.getItems();

        if (cursor.moveToFirst()){
            do{
                int id = cursor.getInt(cursor.getColumnIndex("_id"));
                String name = cursor.getString(cursor.getColumnIndex("name"));
                String amount = cursor.getString(cursor.getColumnIndex("amount"));
                SpinnerObject so = new SpinnerObject(id, name + "/" + amount);
                list.add(so);
            }while(cursor.moveToNext());
        }
        cursor.close();

        ArrayAdapter<SpinnerObject> dataAdapter = new ArrayAdapter<SpinnerObject>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
    }
}