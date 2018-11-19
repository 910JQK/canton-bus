package xyz.cirno.clever.bus;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import xyz.cirno.clever.bus.Parser.*;

public class DistanceAdapter extends ArrayAdapter {
	private final int resourceId;
	public DistanceAdapter(Context context, int textViewResourceId, List objects) {
		super(context, textViewResourceId, objects);
		resourceId = textViewResourceId;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = LayoutInflater.from(getContext()).inflate(resourceId, null);
		TextView name_field = (TextView) v.findViewById(R.id.name);
		TextView up_field = (TextView) v.findViewById(R.id.up);
		TextView down_field = (TextView) v.findViewById(R.id.down);
		線路狀態 s = (線路狀態) getItem(position);
		name_field.setText(s.線路物件.線路名);
		up_field.setText(String.format("上行 %s", dist2str(s.線路物件.終到, s.次車距離.上行距離)));
		down_field.setText(String.format("下行 %s", dist2str(s.線路物件.始發, s.次車距離.下行距離)));
		return v;
	}
	static String dist2str(String 終點站, int 次車距離) {
		if ( 次車距離 == -2 ) {
			return "不过站";
		} else if ( 次車距離 == -1 ) {
			return String.format("%s 方向 尚未发班", 終點站);
		} else {
			return String.format("%s 方向 %d 站", 終點站, 次車距離);
		}
	}
}
