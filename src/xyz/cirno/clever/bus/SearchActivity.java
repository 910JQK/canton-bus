package xyz.cirno.clever.bus;

import xyz.cirno.clever.bus.ResultAdapter;
import xyz.cirno.clever.bus.Parser.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import android.os.Handler;
import android.os.Message;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.view.MenuItem;
import android.view.View;


public class SearchActivity extends Activity implements View.OnClickListener, OnItemClickListener  {
	
	static final String API_URL = "https://rycxapi.gci-china.com/xxt-min-api/bus/";
	static final String ACTION_SEARCH = "getByName.do?";
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);
        ActionBar action_bar = getActionBar();
        action_bar.setDisplayHomeAsUpEnabled(true);
        btn = (Button) this.findViewById(R.id.btn);
        btn.setOnClickListener(this);
        /*
        bookmark_btn = (Button) this.findViewById(R.id.bookmark_btn);
        bookmark_btn.setOnClickListener(this);
        */
        field = (EditText) this.findViewById(R.id.field);
        result_list = (ListView) this.findViewById(R.id.result_list);
        result_list.setOnItemClickListener(this);
    }
    
    Button btn;
    Button bookmark_btn;
    String html;
    ListView result_list;
    EditText field;
    List 總列表 = new ArrayList();
    
    @Override
    public void onItemClick(AdapterView<?> adapter, View v, int index, long _) {
    	Intent intent = null;
    	if ( 總列表.get(index) instanceof 車站 ) {
    		intent = new Intent(this, StationActivity.class);
    		車站 s = (車站) 總列表.get(index);
    		intent.putExtra("編號", s.編號);
    		intent.putExtra("車站名", s.車站名);
    	} else if ( 總列表.get(index) instanceof 線路 ) {
    		intent = new Intent(this, RouteActivity.class);
    		線路 r = (線路) 總列表.get(index);
    		intent.putExtra("編號", r.編號);
    		intent.putExtra("線路名", r.線路名);
    	}
    	if (intent != null) {
    		startActivity(intent);
    	}
    }
    
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if ( id == android.R.id.home ) {
			finish();
		}
		return true;
	}
    
    public void show_error(String message) {
    	Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }
    
    public Handler req_handler = new Handler() {
    	@Override
    	public void handleMessage(Message msg) {
    		btn.setEnabled(true);
    		Bundle data = msg.getData();
    		int status = data.getInt("status");
    		String response = msg.getData().getString("response");
    		String traceback = msg.getData().getString("traceback");
    		if (status == 200) {
    			try {
    				搜尋結果 結果 = Parser.解析搜尋結果(response);
    				總列表 = new ArrayList();
    				總列表.addAll(結果.線路列表);
    				總列表.addAll(結果.車站列表);
    				result_list.setAdapter(new ResultAdapter(
    						getBaseContext(),
    						R.layout.result_item, 總列表
    				));
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
    
    @Override
    public void onClick(View view) {
    	int id = view.getId();
    	if ( id == R.id.btn ) {
    		btn.setEnabled(false);
    		String name = field.getText().toString();
    		List<NameValuePair> params = new LinkedList<NameValuePair>();
    		params.add(new BasicNameValuePair("name", name));
    		Request.send(API_URL+ACTION_SEARCH+URLEncodedUtils.format(params, "utf-8"), req_handler);
    	}/* else if ( id == R.id.bookmark_btn ) {
    		Intent intent = new Intent(this, BookmarkListActivity.class);
    		startActivity(intent);
    	} */
    }
}