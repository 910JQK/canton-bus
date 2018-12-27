package xyz.cirno.clever.bus;

import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class NavAdapter extends ArrayAdapter {
	private final int resourceId;
	public NavAdapter(Context context, int textViewResourceId, List objects) {
		super(context, textViewResourceId, objects);
		resourceId = textViewResourceId;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = LayoutInflater.from(getContext()).inflate(resourceId, null);
		TextView name_field = (TextView) v.findViewById(R.id.name);
		TextView desc_field = (TextView) v.findViewById(R.id.desc);
		ImageView icon_field = (ImageView) v.findViewById(R.id.icon);
		NavItem item = (NavItem) getItem(position);
		icon_field.setImageResource(item.icon);
		name_field.setText(item.name);
		desc_field.setText(item.desc);
		return v;
	}
}
