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
		TextView up_tip = (TextView) v.findViewById(R.id.up_t);
		TextView down_tip = (TextView) v.findViewById(R.id.down_t);
		TextView up_name = (TextView) v.findViewById(R.id.up_n);
		TextView down_name = (TextView) v.findViewById(R.id.down_n);
		TextView up_label = (TextView) v.findViewById(R.id.up_l);
		TextView down_label = (TextView) v.findViewById(R.id.down_l);
		TextView up_dist = (TextView) v.findViewById(R.id.up_d);
		TextView down_dist = (TextView) v.findViewById(R.id.down_d);
		線路狀態 s = (線路狀態) getItem(position);
		name_field.setText(Parser.簡化線路名(s.線路物件.線路名));
		if (s.次車距離.上行距離 == -2) {
			up_tip.setText("上行不过站");
		} else if (s.次車距離.上行距離 == -1) {
			up_name.setText(Parser.簡化總站名(s.線路物件.終到));
			up_label.setText("方向");
			up_dist.setText("尚未发班");
		} else {
			up_name.setText(Parser.簡化總站名(s.線路物件.終到));
			up_label.setText("方向");
			up_dist.setText(String.format("%s 站", s.次車距離.上行距離));
		}
		if (s.次車距離.下行距離 == -2) {
			down_tip.setText("下行不过站");
		} else if (s.次車距離.下行距離 == -1) {
			down_name.setText(Parser.簡化總站名(s.線路物件.始發));
			down_label.setText("方向");
			down_dist.setText("尚未发班");
		} else {
			down_name.setText(Parser.簡化總站名(s.線路物件.始發));
			down_label.setText("方向");
			down_dist.setText(String.format("%s 站", s.次車距離.下行距離));
		}
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
