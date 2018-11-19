package xyz.cirno.clever.bus;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

public class Parser {
	static class 線路 {
		String 編號;
		String 線路名;
		String 始發;
		String 終到;
	}
	static 線路 解析線路(JSONObject 線路資料) throws Exception {
		線路 r = new 線路();
		r.編號 = 線路資料.getString("i");
		r.線路名 = 線路資料.getString("n");
		r.始發 = 線路資料.getString("start");
		r.終到 = 線路資料.getString("end");
		return r;
	}
	static class 車站 {
		String 編號;
		String 車站名;
	}
	static 車站 解析車站(JSONObject 車站資料) throws Exception {
		車站 s = new 車站();
		s.編號 = 車站資料.getString("i");
		s.車站名 = 車站資料.getString("n");
		return s;
	}
	static class 搜尋結果 {
		List<線路> 線路列表 = new ArrayList<線路>();
		List<車站> 車站列表 = new ArrayList<車站>(); 
	}
	static 搜尋結果 解析搜尋結果 (String response) throws Exception {
		JSONObject wrapper = new JSONObject(response);
		int ret = wrapper.getInt("retCode");
		if ( ret == 0 ) {
			搜尋結果 結果 = new 搜尋結果();
			JSONObject inner = wrapper.getJSONObject("retData");
			JSONArray route = inner.getJSONArray("route");
			for (int i=0; i<route.length(); i++) {
				JSONObject 條目 = route.getJSONObject(i);
				結果.線路列表.add(解析線路(條目));
			}
			JSONArray station = inner.getJSONArray("station");
			for (int i=0; i<station.length(); i++) {
				JSONObject 條目 = station.getJSONObject(i);
				結果.車站列表.add(解析車站(條目));
			}
			return 結果;
		} else {
			throw new Exception();
		}
	}
	static class 距離 {
		int 上行距離 = -2;
		int 下行距離 = -2;
	}
	static class 線路狀態 {
		線路 線路物件 = new 線路();
		距離 次車距離 = new 距離();
		String 線路車站號;
	}
	static class 車站資訊 {
		車站 車站物件 = new 車站();
		List<線路狀態> 站距表 = new ArrayList<線路狀態>();
	}
	static 車站資訊 解析車站資訊 (String response_route_station, String response_runbus) throws Exception {
		JSONObject data = new JSONObject(response_route_station);
		JSONObject runbus = new JSONObject(response_runbus);
		int ret1 = data.getInt("retCode");
		int ret2 = runbus.getInt("retCode");
		if ( ret1 == 0 && ret2 == 0 ) {
			JSONObject meta_data = data.getJSONObject("retData");
			JSONArray info_list = runbus.getJSONArray("retData");
			JSONArray meta_list = meta_data.getJSONArray("l");
			JSONObject added = new JSONObject();
			車站資訊 r = new 車站資訊();
			r.車站物件 = 解析車站(meta_data);
			for(int i=0; i<meta_list.length(); i++) {
				JSONObject meta = meta_list.getJSONObject(i);
				int 次車距離 = -1;
				for (int j=0; j<info_list.length(); j++) {
					JSONObject info = info_list.getJSONObject(j);
					if (info.getString("i").equals(meta.getString("rsi"))) {
						次車距離 = info.getInt("c");
					}
				}
				String 線路編號 = meta.getString("ri");
				if (!added.has(線路編號)) {
					線路狀態 s = new 線路狀態();
					s.線路物件.線路名 = meta.getString("rn");
					s.線路物件.編號 = meta.getString("ri");
					s.線路車站號 = meta.getString("rsi");
					if (meta.getString("d").equals("0")) {
						s.次車距離.上行距離 = 次車距離;
						s.線路物件.始發 = meta.getString("dn").split("-")[0];
						s.線路物件.終到 = meta.getString("dn").split("-")[1];
					} else {
						s.次車距離.下行距離 = 次車距離;
						s.線路物件.始發 = meta.getString("dn").split("-")[1];
						s.線路物件.終到 = meta.getString("dn").split("-")[0];
					}
					added.put(線路編號, r.站距表.size());
					r.站距表.add(s);
				} else {
					int index = added.getInt(線路編號);
					if (meta.getString("d") == "0") {
						r.站距表.get(index).次車距離.上行距離 = 次車距離;
					} else {
						r.站距表.get(index).次車距離.下行距離 = 次車距離;
					}
				}
			}
			return r;
		} else {
			throw new Exception();
		}
	}
}
