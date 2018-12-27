package xyz.cirno.clever.bus;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import xyz.cirno.clever.bus.Parser.*;
import android.content.Intent;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

public class StationActivity extends Activity implements View.OnClickListener, OnItemClickListener {
	static final String API_URL = "https://rycxapi.gci-china.com/xxt-min-api/bus/";
	static final String ACTION_ROUTESTATION = "routeStation/getByStation.do?";
	static final String ACTION_RUNBUS = "runbus/getByStation.do?";
    
	String 車站編號;
	String 車站名稱;
	String step1_response;
	ListView distance_list;
	View loading;
	View retry;
	Button retry_btn;
	Bookmarks bookmarks;
	Menu top_right_menu;
	List<線路狀態> 站距表;
	Boolean ready = false;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.station);
        ActionBar action_bar = getActionBar();
        action_bar.setDisplayHomeAsUpEnabled(true);
        loading = findViewById(R.id.loading);
        distance_list = (ListView) findViewById(R.id.distance_list);
        distance_list.setOnItemClickListener(this);
        retry = findViewById(R.id.retry);
        retry_btn = (Button) findViewById(R.id.retry_btn);
        retry_btn.setOnClickListener(this);
        bookmarks = new Bookmarks(getApplicationContext());
        Intent intent = getIntent();
        String 編號 = intent.getStringExtra("編號");
        String 車站名 = intent.getStringExtra("車站名");
        setTitle(車站名);
        車站編號 = 編號;
        車站名稱 = 車站名;
        send_request();
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		try {
			JSONObject b = bookmarks.read().getJSONObject("station");
			if (!b.has(車站編號)) {
				getMenuInflater().inflate(R.menu.top_right, menu);
				//menu.getItem(0).setIcon(R.drawable.refresh);
				//menu.getItem(1).setIcon(R.drawable.star);
			} else {
				getMenuInflater().inflate(R.menu.top_right_single, menu);
				//menu.getItem(0).setIcon(R.drawable.refresh);
			}
		} catch (Exception err) {
			just_show_error("Error loading bookmarks");
		}
		top_right_menu = menu;
		return true;
	}
	
	
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if ( id == android.R.id.home ) {
			finish();
		} else if ( id == R.id.reload ) {
			if (ready) {
				send_request();
			}
		} else if ( id == R.id.add_bookmark ) {
			try {
				JSONObject b = bookmarks.read();
				JSONObject r = new JSONObject();
				r.put("name", 車站名稱);
				b.getJSONObject("station").put(車站編號, r);
				bookmarks.write(b);
				top_right_menu.findItem(R.id.add_bookmark).setVisible(false);
				invalidateOptionsMenu();
			} catch (Exception err) {
				just_show_error("Error saving bookmarks");
			}
		}
		return true;
	}
	
    public void show_error(String message) {
    	loading.setVisibility(View.GONE);
    	retry.setVisibility(View.VISIBLE);
    	Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }
    
    public void just_show_error(String message) {
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
	}
    
    public Handler req_handler = new Handler() {
    	@Override
    	public void handleMessage(Message msg) {
    		Bundle data = msg.getData();
    		int status = data.getInt("status");
    		String response = msg.getData().getString("response");
    		String traceback = msg.getData().getString("traceback");
    		if (status == 200) {
    			try {
    				車站資訊 t = Parser.解析車站資訊(step1_response, response);
    				站距表 = t.站距表;
    				distance_list.setAdapter(new DistanceAdapter(
    						getBaseContext(),
    						R.layout.distance_item,
    						t.站距表
    				));
    				ready = true;
    				loading.setVisibility(View.GONE);
    				distance_list.setVisibility(View.VISIBLE);
    			} catch (Exception e) {
    				show_error("JSON Parsing Error");
    			}
    		} else if (status == -1) {
    			show_error(traceback.substring(0, 100));
    		} else {
    			show_error(String.valueOf(status));
    		}
    	}
    };
    
    public void send_request () {
		retry.setVisibility(View.GONE);
		distance_list.setVisibility(View.GONE);
		loading.setVisibility(View.VISIBLE);
    	List<NameValuePair> params = new LinkedList<NameValuePair>();
    	params.add(new BasicNameValuePair("stationNameId", 車站編號));
    	final String params_str = URLEncodedUtils.format(params, "utf-8");
        Request.send(API_URL+ACTION_ROUTESTATION+params_str,
        			new Handler() {
        	@Override
        	public void handleMessage(Message msg) {
        		Bundle data = msg.getData();
        		int status = data.getInt("status");
        		String response = msg.getData().getString("response");
        		String traceback = msg.getData().getString("traceback");
        		if (status == 200) {
        			step1_response = response;
        			Request.send(API_URL+ACTION_RUNBUS+params_str, req_handler);
        		} else if (status == -1) {
        			show_error(traceback.substring(0, 100));
        		} else {
        			show_error(String.valueOf(status));
        		}
        	}
        });
    }

	@Override
	public void onClick(View v) {
		send_request();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int index, long id) {
		if (index < 站距表.size()) {
			Intent intent = new Intent(this, RouteActivity.class);
			線路 r = 站距表.get(index).線路物件;
			intent.putExtra("編號", r.編號);
    		intent.putExtra("線路名", r.線路名);
    		startActivity(intent);
		}
	}
}
