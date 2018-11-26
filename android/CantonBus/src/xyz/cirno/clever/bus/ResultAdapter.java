package xyz.cirno.clever.bus;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import xyz.cirno.clever.bus.Parser.*;

class ResultAdapter extends ArrayAdapter {
	private final int resourceId;
	public ResultAdapter(Context context, int textViewResourceId, List objects) {
		super(context, textViewResourceId, objects);
		resourceId = textViewResourceId;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = LayoutInflater.from(getContext()).inflate(resourceId, null);
		TextView title_field = (TextView) v.findViewById(R.id.title);
		TextView detail_field = (TextView) v.findViewById(R.id.detail);
		Object item = getItem(position);
		if ( item instanceof 線路 ) {
			線路 r = (線路) item;
			title_field.setText(r.線路名);
			detail_field.setText(String.format("%s -> %s", r.始發, r.終到));
		} else if ( item instanceof 車站 ) {
			車站 s = (車站) item;
			title_field.setText(s.車站名);
			detail_field.setText("");
		}
		return v;
	}
}
