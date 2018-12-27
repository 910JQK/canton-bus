package xyz.cirno.clever.bus;

import java.util.ArrayList;
import java.util.Collections;
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
		if ( ret1 != 0 || ret2 != 0 ) {
			throw new Exception();
		}
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
	}
	static class 車輛標識 {
		String SubID;
		String BusID;
	}
	static List<車輛標識> 取得車輛標識表 (String r_route_meta) throws Exception {
		JSONObject meta_data = new JSONObject(r_route_meta);
		if ( meta_data.getInt("retCode") != 0 ) {
			throw new Exception();
		}
		JSONArray runb = meta_data.getJSONObject("retData").getJSONArray("runb");
		List<JSONObject> bus_list = new ArrayList<>();
		for (int i=0; i<runb.length(); i++) {
			JSONObject t = runb.getJSONObject(i);
			JSONArray bl = t.getJSONArray("bl");
			JSONArray bbl = t.getJSONArray("bbl");
			for (int j=0; j<bl.length(); j++) {
				bus_list.add(bl.getJSONObject(j));
			}
			for (int j=0; j<bbl.length(); j++) {
				bus_list.add(bbl.getJSONObject(j));
			}
		}
		List<車輛標識> L = new ArrayList<>();
		for (int i=0; i<bus_list.size(); i++) {
			JSONObject bus = bus_list.get(i);
			車輛標識 ID = new 車輛標識();
			ID.BusID = bus.getString("i");
			ID.SubID = bus.getString("sub");
			L.add(ID);
		}
		return L;
	}
	static class 分站 {
		String 車站編號;
		String 車站名;
		String 分站號;
		String 過站號;		
	}
	static class 車輛 {
		String 發班時間;
		String 短線終到站 = "";
		String 次站號 = "";
	}
	static class 單向線路資訊 {
		String 線路名;
		String 首班時間;
		String 尾班時間;
		List<分站> 過站列表 = new ArrayList<>();
		List<車輛> 車輛列表 = new ArrayList<>();
	}
	static class 線路資訊 {
		單向線路資訊 上行資訊;
		單向線路資訊 下行資訊;
	}
	static 單向線路資訊 解析單向線路資訊 (String r_meta, List<String> r_bus) throws Exception {
		JSONObject meta_data = new JSONObject(r_meta);
		List<JSONObject> bus_data = new ArrayList<>();
		if ( meta_data.getInt("retCode") != 0 ) {
			throw new Exception();
		}
		for (int i=0; i<r_bus.size(); i++) {
			JSONObject t = new JSONObject(r_bus.get(i));
			if ( t.getInt("retCode") == 0 ) {
				bus_data.add(t);
			}
		}
		單向線路資訊 I = new 單向線路資訊();
		JSONObject rb = meta_data.getJSONObject("retData").getJSONObject("rb");
		I.線路名 = rb.getString("rn");
		I.首班時間 = rb.getString("ft");
		I.尾班時間 = rb.getString("lt");
		JSONArray l = rb.getJSONArray("l");
		for (int i=0; i<l.length(); i++) {
			JSONObject s = l.getJSONObject(i);
			分站 S = new 分站();
			S.車站名 = s.getString("n");
			S.車站編號 = s.getString("sni");
			S.過站號 = s.getString("si");
			I.過站列表.add(S);
			S.分站號 = s.getString("order");
			if (s.getString("brt").equals("0")) {
				S.分站號 = S.分站號.substring(1, S.分站號.length());
			}
		}
		for (int i=0; i<bus_data.size(); i++) {
			車輛 B = new 車輛();
			JSONObject b = bus_data.get(i);
			if ( !b.getJSONObject("retData").has("d") ) {
				continue;
			}
			JSONObject d = b.getJSONObject("retData").getJSONObject("d");
			B.發班時間 = d.getString("fbt").substring(0, 5);
			JSONArray l1 = d.getJSONArray("l");
			if ( l1.length() > 1 && l1.getJSONObject(1).getInt("ti") > 0 ) {
				B.次站號 = l1.getJSONObject(0).getString("i");
			} else {
				for (int j=0; j<l1.length(); j++) {
					JSONObject s = l1.getJSONObject(j);
					if ( s.getInt("ti") >= 0 ) {
						B.次站號 = s.getString("i");
						break;
					}
				}
			}
			JSONObject last = l1.getJSONObject(l1.length()-1);
			if ( !last.getString("i").equals(I.過站列表.get(I.過站列表.size()-1).過站號) ) {
				B.短線終到站 = last.getString("n");
			}
			if ( !B.次站號.equals("") ) {
				I.車輛列表.add(B);
				/* 運營結束的車輛可返回 {
				 * '發班時間': '13:12',
				 * '班次類型': '全程',
				 * '過站表': [
				 * 		{
				 * 			'車站名稱':'天河客运站总站',
				 * 			'預估時間': '-1',
				 * 			'過站車站編號': '10000303'
				 * 		}
				 * 	]} 故次站號可能不存在 */
			}
		}
		return I;
	}
	static class 線路圖結點 {
		車站 對應車站 = new 車站();
		分站 上行分站 = null;
		分站 下行分站 = null;
		List<車輛> 上行將到車輛表 = new ArrayList<>();
		List<車輛> 下行將到車輛表 = new ArrayList<>();
	}
	static class 線路全資訊 {
		線路資訊 基本資訊;
		List<線路圖結點> 線路圖 = new ArrayList<>();
	}
	static 線路全資訊 解析線路全資訊 (String r_up, String r_down, List<String> b_up, List<String> b_down) throws Exception {
		線路資訊 I = new 線路資訊();
		I.上行資訊 = 解析單向線路資訊(r_up, b_up);
		I.下行資訊 = 解析單向線路資訊(r_down, b_down);
		線路全資訊 A = new 線路全資訊();
		A.基本資訊 = I;
		List<分站> 上行站表 = I.上行資訊.過站列表;
		List<分站> 下行站表 = I.下行資訊.過站列表;
		JSONObject 上行有站 = new JSONObject();
		JSONObject 下行有站 = new JSONObject();
		for (int i=0; i<上行站表.size(); i++) {
			上行有站.put(上行站表.get(i).車站編號, true);
		}
		for (int i=0; i<下行站表.size(); i++) {
			下行有站.put(下行站表.get(i).車站編號, true);
		}
		Collections.reverse(下行站表);
		int 上行索引 = 0;
		int 下行索引 = 0;
		while ( 上行索引 < 上行站表.size() && 下行索引 < 下行站表.size() ) {
			分站 上行站 = 上行站表.get(上行索引);
			分站 下行站 = 下行站表.get(下行索引);
			線路圖結點 結點 = new 線路圖結點();
			if ( 上行站.車站編號.equals(下行站.車站編號) ) {
				上行索引++;
				下行索引++;
				結點.上行分站 = 上行站;
				結點.下行分站 = 下行站;
			} else if ( 下行有站.has(上行站.車站編號) ) {
				下行索引++;
				結點.下行分站 = 下行站;
			} else {
				上行索引++;
				結點.上行分站 = 上行站;
			}
			if ( 結點.上行分站 != null ) {
				結點.對應車站.編號 = 上行站.車站編號;
				結點.對應車站.車站名 = 上行站.車站名;
				List<車輛> 車輛列表 = I.上行資訊.車輛列表;
				for (int i=0; i<車輛列表.size(); i++) {
					車輛 B = 車輛列表.get(i);
					if ( B.次站號.equals(上行站.過站號) ) {
						結點.上行將到車輛表.add(B);
					}
				}
			}
			if ( 結點.下行分站 != null ) {
				結點.對應車站.編號 = 下行站.車站編號;
				結點.對應車站.車站名 = 下行站.車站名;
				List<車輛> 車輛列表 = I.下行資訊.車輛列表;
				for (int i=0; i<車輛列表.size(); i++) {
					車輛 B = 車輛列表.get(i);
					if ( B.次站號.equals(下行站.過站號) ) {
						結點.下行將到車輛表.add(B);
					}
				}
			}
			A.線路圖.add(結點);
		}
		return A;
	}
}
