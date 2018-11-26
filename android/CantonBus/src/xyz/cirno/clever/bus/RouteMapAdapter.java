package xyz.cirno.clever.bus;

import java.util.List;

import android.content.Context;
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
		TextView down_sub = (TextView) v.findViewById(R.id.down_sub);
		TextView down_bus = (TextView) v.findViewById(R.id.down_bus);
		線路圖結點 n = (線路圖結點) getItem(position);
		station.setText(n.對應車站.車站名);
		if ( n.上行分站 != null ) {
			if (n.上行分站.分站號.equals("0")) {
				up_sub.setText("・ ");
			} else {
				up_sub.setText(String.format("(%s) ", n.上行分站.分站號));
			}
			String bus_text = "";
			for (int i=0; i<n.上行將到車輛表.size(); i++) {
				車輛 b = n.上行將到車輛表.get(i);
				if ( !b.短線終到站.equals("") ) {
					bus_text += String.format("[%s|%s] ", b.發班時間, b.短線終到站);
				} else {
					bus_text += String.format("[%s] ", b.發班時間);
				}
			}
			up_bus.setText(bus_text);
		} else {
			up_sub.setText("＊ ");
		}
		if ( n.下行分站 != null ) {
			if ( n.下行分站.分站號.equals("0") ) {
				down_sub.setText(" ・");
			} else {
				down_sub.setText(String.format(" (%s)", n.下行分站.分站號));
			}
			String bus_text = " ";
			for (int i=0; i<n.下行將到車輛表.size(); i++) {
				車輛 b = n.下行將到車輛表.get(i);
				if ( !b.短線終到站.equals("") ) {
					bus_text += String.format("[%s|%s] ", b.發班時間, b.短線終到站);
				} else {
					bus_text += String.format("[%s] ", b.發班時間);
				}
			}
			down_bus.setText(bus_text);
		} else {
			down_sub.setText(" ＊");
		}
		return v;
	}
}
