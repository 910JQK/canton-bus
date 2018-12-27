package xyz.cirno.clever.bus;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class StartActivity extends Activity implements OnItemClickListener {
	final String NOTICE_URL = "http://www.gzjt.gov.cn/gzjt/yjzjjsh/list.shtml";
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start);
        ListView list_view = (ListView) findViewById(R.id.nav_list);
        List<NavItem> list = new ArrayList<NavItem>();
        list.add(new NavItem(R.drawable.search, "搜索", "搜索线路或车站"));
        list.add(new NavItem(R.drawable.had_star, "收藏", "查看收藏夹"));
        list.add(new NavItem(R.drawable.route, "通知", "查看线路调整通知"));
        list.add(new NavItem(R.drawable.info, "关于", "关于本软件"));
        NavAdapter adapter = new NavAdapter(
        		getBaseContext(),
        		R.layout.nav_item,
        		list
        );
        list_view.setAdapter(adapter);
        list_view.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int index, long id) {
		if (index == 0) {
			Intent search = new Intent(this, SearchActivity.class);
			startActivity(search);
		}
		if (index == 1) {
			Intent bookmarks = new Intent(this, BookmarkListActivity.class);
			startActivity(bookmarks);
		}
		if (index == 2) {
			Intent notice = new Intent(Intent.ACTION_VIEW, Uri.parse(NOTICE_URL));
			startActivity(notice);
		}
		if (index == 3) {
			
		}
	}
}
