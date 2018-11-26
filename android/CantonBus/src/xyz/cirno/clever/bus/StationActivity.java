package xyz.cirno.clever.bus;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;
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

public class StationActivity extends Activity implements View.OnClickListener {
	static final String API_URL = "https://rycxapi.gci-china.com/xxt-min-api/bus/";
	static final String ACTION_ROUTESTATION = "routeStation/getByStation.do?";
	static final String ACTION_RUNBUS = "runbus/getByStation.do?";
    
	String 車站編號;
	String step1_response;
	ListView distance_list;
	View loading;
	View retry;
	Button retry_btn;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.station);
        ActionBar action_bar = getActionBar();
        action_bar.setDisplayHomeAsUpEnabled(true);
        loading = findViewById(R.id.loading);
        distance_list = (ListView) findViewById(R.id.distance_list);
        retry = findViewById(R.id.retry);
        retry_btn = (Button) findViewById(R.id.retry_btn);
        retry_btn.setOnClickListener(this);
        Intent intent = getIntent();
        String 編號 = intent.getStringExtra("編號");
        String 車站名 = intent.getStringExtra("車站名");
        setTitle(車站名);
        車站編號 = 編號;
        send_request();
    }
	
	public boolean onOptionsItemSelected(MenuItem item) {
		finish();
		return true;
	}
	
    public void show_error(String message) {
    	loading.setVisibility(View.GONE);
    	retry.setVisibility(View.VISIBLE);
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
    				distance_list.setAdapter(new DistanceAdapter(
    						getApplicationContext(),
    						R.layout.distance_item,
    						t.站距表
    				));
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
		retry.setVisibility(View.GONE);
		loading.setVisibility(View.VISIBLE);
		send_request();
	}
}
