package xyz.cirno.clever.bus;

import org.json.*;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Bookmarks {
	Context context;
	
	public Bookmarks (Context app_context) {
		context = app_context;
	}
	
	public JSONObject read () throws Exception {
		SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
		return new JSONObject(p.getString("bookmarks", "{route:{}, station:{}}"));
	}
	
	public void write (JSONObject value) {
		SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = p.edit();
		editor.putString("bookmarks", value.toString());
		editor.commit();
	}
}
