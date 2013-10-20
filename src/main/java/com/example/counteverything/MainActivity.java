package com.example.counteverything;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Activity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private final static String TAG = "Main";
    private DataAdapter mDbHelper;
    private Button btnAdd, btnDelete;
    private Spinner spinner;
    private WebView webView;
    Activity activity ;
    private ProgressDialog progDailog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        tabSpec = tabhost.newTabSpec("tabStats");
        tabSpec.setContent(R.id.tabStats);
        tabSpec.setIndicator("Stats");
        tabhost.addTab(tabSpec);

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

        activity = this;
        webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().supportZoom();
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.setWebViewClient(new WebViewClient(){

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            //progDailog.show();
            view.loadUrl(url);
            return true;
        }

        @Override
            public void onPageFinished(WebView view, final String url) {
                //progDailog.dismiss();
            }
        });

        webView.loadUrl("http://beer.1337.af");
    }

    /**
     * When an item submit button is pressed get the data from the db and send to webserver.
     */
    View.OnClickListener addItem = new View.OnClickListener() {
        public void onClick(View v) {
            int id = ((Button)v).getId();
            String name = mDbHelper.getItemName(id);
            float amount = mDbHelper.getItemAmount(id);

            JSONObject json = new JSONObject();
            try {
               json.put("action", "add");
               json.put("item", name);
               json.put("amount", amount);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            new Api(v.getContext()).execute(json);
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

                LinearLayout ll1 = new LinearLayout(this);
                ll1.setPadding(0, 0, 0, 10);

                RelativeLayout ll = new RelativeLayout(this);
                //ll.setOrientation(LinearLayout.HORIZONTAL);
                ll.setPadding(0, 0, 0, 30);
                ll.setBackgroundResource(R.drawable.borderframe);
                ll1.addView(ll);
                layout.addView(ll1);
                TextView tv = new TextView(this);
                tv.setText(name);
                tv.setTextSize(20);
                ll.addView(tv);

                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);

                // the add button
                Button btnTag = new Button(this);
                btnTag.setLayoutParams(params);
                btnTag.setText("+");
                btnTag.setId(id);
                btnTag.setTextSize(30);
                btnTag.setBackgroundColor(Color.GREEN);
                btnTag.setOnClickListener(addItem);

                ll.addView(btnTag);
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