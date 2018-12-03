package xyz.cirno.clever.bus;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

public class BookmarkListActivity extends Activity implements OnItemClickListener {
	List<JSONObject> list = new ArrayList<>();
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bookmark_page);
        ActionBar action_bar = getActionBar();
        action_bar.setDisplayHomeAsUpEnabled(true);
        ListView list_view = (ListView) findViewById(R.id.bookmark_list);
        list_view.setOnItemClickListener(this);
        try {
        	JSONObject b = (new Bookmarks(getApplicationContext())).read();
        	JSONObject r = b.getJSONObject("route");
        	JSONObject s = b.getJSONObject("station");
        	list = new ArrayList<>();
        	Iterator<String> it = r.keys();
        	while (it.hasNext()) {
        		String key = it.next();
        		String name = r.getJSONObject(key).getString("name");
        		JSONObject item = new JSONObject();
        		item.put("type", "route");
        		item.put("id", key);
        		item.put("name", name);
        		list.add(item);
        	}
        	it = s.keys();
        	while (it.hasNext()) {
        		String key = it.next();
        		String name = r.getJSONObject(key).getString("name");
        		JSONObject item = new JSONObject();
        		item.put("type", "station");
        		item.put("id", key);
        		item.put("name", name);
        		list.add(item);
        	}
        	Collections.sort(list, new Comparator<JSONObject>() {
        		@Override
        		public int compare(JSONObject l, JSONObject r) {
        			try {
        				return l.getString("name").compareToIgnoreCase(r.getString("name"));
        			} catch (Exception err) {
        				return 0;
        			}
        		}
        	});
        	list_view.setAdapter(new BookmarkAdapter(
        			getApplicationContext(),
        			R.layout.bookmark_item,
        			list
        	));
        } catch (Exception err) {
	    	just_show_error("Error loading bookmarks");
        }
	}
	
	@Override
	public void onItemClick(AdapterView<?> adapter, View v, int index, long _) {
		JSONObject item = list.get(index);
		try {
			String type = item.getString("type");
			if ( type == "station" ) {
				Intent intent = new Intent(this, StationActivity.class);
				intent.putExtra("編號", item.getString("id"));
				intent.putExtra("車站名", item.getString("name"));
				startActivity(intent);
			} else if ( type == "route" ) {
				Intent intent = new Intent(this, RouteActivity.class);
				intent.putExtra("編號", item.getString("id"));
				intent.putExtra("線路名", item.getString("name"));
				startActivity(intent);
			}
		} catch (Exception err) {
			just_show_error("Error reading list");
		}
	}
	
	public void just_show_error(String message) {
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if ( id == android.R.id.home ) {
			finish();
		}
		return true;
	}
}
