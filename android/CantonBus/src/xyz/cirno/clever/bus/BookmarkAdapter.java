package xyz.cirno.clever.bus;

import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class BookmarkAdapter extends ArrayAdapter {
	private final int resourceId;
	public BookmarkAdapter(Context context, int textViewResourceId, List objects) {
		super(context, textViewResourceId, objects);
		resourceId = textViewResourceId;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = LayoutInflater.from(getContext()).inflate(resourceId, null);
		TextView name_field = (TextView) v.findViewById(R.id.name);
		JSONObject item = (JSONObject) getItem(position);
		name_field.setText(item.optString("name"));
		return v;
	}
}
