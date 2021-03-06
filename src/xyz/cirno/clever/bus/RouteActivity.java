package xyz.cirno.clever.bus;

import xyz.cirno.clever.bus.Parser.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
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
import android.widget.TextView;
import android.widget.Toast;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

public class RouteActivity extends Activity implements View.OnClickListener, OnItemClickListener {
	
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
	Menu top_right_menu;
	
	Bookmarks bookmarks;
	String 線路編號;
	String 線路名稱;
	String response_meta_up;
	String response_meta_down;
	List<車輛標識> id_up = new ArrayList();
	List<車輛標識> id_down = new ArrayList();
	List<String> response_bus_up = new ArrayList<>();
	List<String> response_bus_down = new ArrayList<>();
	線路全資訊 info;
	List<線路圖結點> 線路圖;
	Boolean ready = false;
	
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
        route_map.setOnItemClickListener(this);
        retry_btn = (Button) findViewById(R.id.retry_btn);
        retry_btn.setOnClickListener(this);
        bookmarks = new Bookmarks(getApplicationContext());
        Intent intent = getIntent();
        String 編號 = intent.getStringExtra("編號");
        String 線路名 = intent.getStringExtra("線路名");
        setTitle(線路名);
        線路編號 = 編號;
        線路名稱 = 線路名;
        send_request();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		try {
			JSONObject b = bookmarks.read().getJSONObject("route");
			if (!b.has(線路編號)) {
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
			if(ready) {
				send_request();
			}
		} else if ( id == R.id.add_bookmark ) {
			try {
				JSONObject b = bookmarks.read();
				JSONObject r = new JSONObject();
				r.put("name", 線路名稱);
				b.getJSONObject("route").put(線路編號, r);
				bookmarks.write(b);
				top_right_menu.findItem(R.id.add_bookmark).setVisible(false);
				invalidateOptionsMenu();
				Toast.makeText(getApplicationContext(), "收藏成功", Toast.LENGTH_LONG).show();
			} catch (Exception err) {
				just_show_error("Error saving bookmarks");
			}
		}
		return true;
	}
		
	public void just_show_error(String message) {
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
	}
	
    public void show_error(String message) {
    	loading.setVisibility(View.GONE);
    	result.setVisibility(View.GONE);
    	retry.setVisibility(View.VISIBLE);
    	Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }
    
    public void send_request () {
    	retry.setVisibility(View.GONE);
    	result.setVisibility(View.GONE);
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
			線路圖 = info.線路圖;
			top.setText(String.format(
				"⇩ 上行  %s ~ %s",
				info.基本資訊.上行資訊.首班時間,
				info.基本資訊.上行資訊.尾班時間
			));
			bottom.setText(String.format(
				"%s ~ %s  下行 ⇧",
				info.基本資訊.下行資訊.首班時間,
				info.基本資訊.下行資訊.尾班時間
			));
			route_map.setAdapter(new RouteMapAdapter(
					getBaseContext(),
					R.layout.route_map_item,
					info.線路圖
			));
			loading.setVisibility(View.GONE);
			result.setVisibility(View.VISIBLE);
			ready = true;
		} catch (Exception err) {
			show_error("JSON Parsing Error");
			/*
			StringWriter sw = new StringWriter();
	    	PrintWriter pw = new PrintWriter(sw);
	    	err.printStackTrace(pw);
	    	show_error(sw.toString());
	    	*/
		}
	}
    
    @Override
    public void onClick(View v) {
    	send_request();
    }

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int index, long id) {
		if ( index < 線路圖.size() ) {
			Intent intent = new Intent(this, StationActivity.class);
			車站 s = 線路圖.get(index).對應車站;
			intent.putExtra("編號", s.編號);
    		intent.putExtra("車站名", s.車站名);
    		startActivity(intent);
		}
	}
}
