package xyz.cirno.clever.bus;

import xyz.cirno.clever.bus.Parser.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

public class RouteActivity extends Activity implements View.OnClickListener {
	
	static final String API_URL = "https://rycxapi.gci-china.com/xxt-min-api/bus/";
	static final String ACTION_ROUTE = "route/getByRouteIdAndDirection.do?";
	static final String ACTION_BUS = "routeSub/getBySubId.do?";
	
	View loading;
	View retry;
	View result;
	TextView progress;
	TextView top;
	TextView bottom;
	ListView route_map;
	Button retry_btn;
	
	String 線路編號;
	String response_meta_up;
	String response_meta_down;
	List<車輛標識> id_up = new ArrayList();
	List<車輛標識> id_down = new ArrayList();
	List<String> response_bus_up = new ArrayList<>();
	List<String> response_bus_down = new ArrayList<>();
	線路全資訊 info;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.route);
        ActionBar action_bar = getActionBar();
        action_bar.setDisplayHomeAsUpEnabled(true);
        loading = findViewById(R.id.loading);
        retry = findViewById(R.id.retry);
        result = findViewById(R.id.result);
        progress = (TextView) findViewById(R.id.progress);
        top = (TextView) findViewById(R.id.top);
        bottom = (TextView) findViewById(R.id.bottom);
        route_map = (ListView) findViewById(R.id.route_map);
        retry_btn = (Button) findViewById(R.id.retry_btn);
        retry_btn.setOnClickListener(this);
        Intent intent = getIntent();
        String 編號 = intent.getStringExtra("編號");
        String 線路名 = intent.getStringExtra("線路名");
        setTitle(線路名);
        線路編號 = 編號;
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
    
    public void send_request () {
    	retry.setVisibility(View.GONE);
    	loading.setVisibility(View.VISIBLE);
    	response_bus_up.clear();
    	response_bus_down.clear();
    	request_meta("0");
    }
	
	public void request_meta (final String direction) {
		if ( direction.equals("0") ) {
    		progress.setText("正在获取上行路线图...");
    	} else {
    		progress.setText("正在获取下行路线图...");
    	}
		List<NameValuePair> params = new LinkedList<NameValuePair>();
		params.add(new BasicNameValuePair("routeId", 線路編號));
		params.add(new BasicNameValuePair("direction", direction));
    	final String params_str = URLEncodedUtils.format(params, "utf-8");
        Request.send(API_URL+ACTION_ROUTE+params_str, new Handler() {
        	@Override
        	public void handleMessage(Message msg) {
        		Bundle data = msg.getData();
        		int status = data.getInt("status");
        		String response = msg.getData().getString("response");
        		String traceback = msg.getData().getString("traceback");
        		if (status == 200) {
        			if ( direction.equals("0") ) {
        				response_meta_up = response;
        				request_meta("1");
        			} else if ( direction.equals("1") ) {
        				response_meta_down = response;
        				try {
        					id_up = Parser.取得車輛標識表(response_meta_up);
        					id_down = Parser.取得車輛標識表(response_meta_down);
        					request_bus("0", 0);
        				} catch (Exception err) {
        					show_error("JSON Parsing Error");
        				}
        			}
        		} else if (status == -1) {
        			show_error(traceback.substring(0, 100));
        		} else {
        			show_error(String.valueOf(status));
        		}
        	}
        });
	}
	
	public void request_bus (final String direction, final int index) {
		if ( direction.equals("0") ) {
			progress.setText(String.format("正在获取上行车辆信息 (%d/%d)", index, id_up.size()) );
		} else {
			progress.setText(String.format("正在获取下行车辆信息 (%d/%d)", index, id_down.size()) );
		}
		List<NameValuePair> params = new LinkedList<NameValuePair>();
		if ( direction.equals("0") ) {
			if ( id_up.size() == 0 ) {
				request_bus("1", 0);
				return;
			}
			params.add(new BasicNameValuePair("busId", id_up.get(index).BusID));
			params.add(new BasicNameValuePair("subId", id_up.get(index).SubID));
		} else {
			if ( id_down.size() == 0 ) {
				parse_response();
				return;
			}
			params.add(new BasicNameValuePair("busId", id_down.get(index).BusID));
			params.add(new BasicNameValuePair("subId", id_down.get(index).SubID));
		}
    	final String params_str = URLEncodedUtils.format(params, "utf-8");
        Request.send(API_URL+ACTION_BUS+params_str, new Handler() {
        	@Override
        	public void handleMessage(Message msg) {
        		Bundle data = msg.getData();
        		int status = data.getInt("status");
        		String response = msg.getData().getString("response");
        		String traceback = msg.getData().getString("traceback");
        		if (status == 200) {
        			if ( direction.equals("0") ) {
        				response_bus_up.add(response);
        				if ( index+1 < id_up.size() ) {
        					request_bus("0", index+1);
        				} else {
        					request_bus("1", 0);
        				}
        			} else {
        				response_bus_down.add(response);
        				if ( index+1 < id_down.size() ) {
        					request_bus("1", index+1);
        				} else {
        					parse_response();
        				}
        			}
        		} else if (status == -1) {
        			show_error(traceback.substring(0, 100));
        		} else {
        			show_error(String.valueOf(status));
        		}
        	}
        });
	}
	
	public void parse_response () {
		try {
			info = Parser.解析線路全資訊(response_meta_up, response_meta_down, response_bus_up, response_bus_down);
			top.setText(String.format(
				"上行: %s → %s",
				info.基本資訊.上行資訊.首班時間,
				info.基本資訊.上行資訊.尾班時間
			));
			bottom.setText(String.format(
				"下行: %s → %s",
				info.基本資訊.下行資訊.首班時間,
				info.基本資訊.下行資訊.尾班時間
			));
			route_map.setAdapter(new RouteMapAdapter(
					getApplicationContext(),
					R.layout.route_map_item,
					info.線路圖
			));
			loading.setVisibility(View.GONE);
			result.setVisibility(View.VISIBLE);
		} catch (Exception err) {
			show_error("JSON Parsing Error");
		}
	}
    
    @Override
    public void onClick(View v) {
    	retry.setVisibility(View.GONE);
    	loading.setVisibility(View.VISIBLE);
    	send_request();
    }
}
