package xyz.cirno.clever.bus;

import java.util.List;
import java.util.regex.Pattern;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import xyz.cirno.clever.bus.Parser.*;

public class RouteMapAdapter extends ArrayAdapter {
	private final int resourceId;
	public RouteMapAdapter(Context context, int textViewResourceId, List objects) {
		super(context, textViewResourceId, objects);
		resourceId = textViewResourceId;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = LayoutInflater.from(getContext()).inflate(resourceId, null);
		TextView up_bus = (TextView) v.findViewById(R.id.up_bus);
		TextView up_sub = (TextView) v.findViewById(R.id.up_sub);
		TextView station = (TextView) v.findViewById(R.id.station);
		TextView hint = (TextView) v.findViewById(R.id.hint);
		TextView down_sub = (TextView) v.findViewById(R.id.down_sub);
		TextView down_bus = (TextView) v.findViewById(R.id.down_bus);
		String circle = "・①②③④⑤⑥⑦⑧⑨⑩";
		線路圖結點 n = (線路圖結點) getItem(position);
		station.setText(Parser.簡化站名(n.對應車站.車站名));
		hint.setText(Parser.提取站名注解(n.對應車站.車站名));
		if(hint.getText().equals("")) {
			hint.setVisibility(View.GONE);
		}
		if ( n.上行分站 != null ) {
			try {
				int t = Integer.parseInt(n.上行分站.分站號);
				up_sub.setText(circle.substring(t, t+1));
			} catch (Exception e) {
				if(Pattern.matches("[NS][0-9]", n.上行分站.分站號)) {
					up_sub.setText(n.上行分站.分站號);
					up_sub.setTextColor(Color.parseColor("#F26010"));
					up_sub.setTypeface(up_sub.getTypeface(), Typeface.BOLD);
				}
			}
			String bus_text = "";
			for (int i=0; i<n.上行將到車輛表.size(); i++) {
				車輛 b = n.上行將到車輛表.get(i);
				if ( !b.短線終到站.equals("") ) {
					bus_text += String.format("(%s)\n↓(%s) 🚌\n", Parser.簡化站名(b.短線終到站), b.發班時間);
				} else {
					bus_text += String.format("↓(%s) 🚌\n", b.發班時間);
				}
			}
			up_bus.setText(bus_text);
		} else {
			up_sub.setText("※");
			up_sub.setTextColor(Color.parseColor("#AEAEAE"));
		}
		if ( n.下行分站 != null ) {
			try {
				int t = Integer.parseInt(n.下行分站.分站號);
				down_sub.setText(circle.substring(t, t+1));
			} catch (Exception e) {
				if(Pattern.matches("[NS][0-9]", n.下行分站.分站號)) {
					down_sub.setText(n.下行分站.分站號);
					down_sub.setTextColor(Color.parseColor("#F26010"));
					down_sub.setTypeface(down_sub.getTypeface(), Typeface.BOLD);
				}
			}
			String bus_text = " ";
			for (int i=0; i<n.下行將到車輛表.size(); i++) {
				車輛 b = n.下行將到車輛表.get(i);
				if ( !b.短線終到站.equals("") ) {
					bus_text += String.format("🚌 (%s)↑\n(%s)\n", b.發班時間, Parser.簡化站名(b.短線終到站));
				} else {
					bus_text += String.format("🚌 (%s)↑\n", b.發班時間);
				}
			}
			down_bus.setText(bus_text);
		} else {
			down_sub.setText("※");
			down_sub.setTextColor(Color.parseColor("#AEAEAE"));
		}
		return v;
	}
}
