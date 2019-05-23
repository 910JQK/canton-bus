package xyz.cirno.clever.bus;

import xyz.cirno.clever.bus.StreamTool;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;
import android.os.Bundle;


public class Request {
	
	private static final int timeout = 8000;
	
	static void send(final String url_str, final Handler handler) {
		Runnable request = new Runnable() {
	    	@Override
	    	public void run () {
	    		int status = -1;
	    		String response = "";
	    		String traceback = "";
	    		try {
	    			URL url = new URL(url_str);
	    			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	    			conn.setConnectTimeout(timeout);
	    			conn.setRequestMethod("GET");
	    			status = conn.getResponseCode();
	    			if (status == 200) {
	    				InputStream in = conn.getInputStream();
	    				byte[] data = StreamTool.read(in);
	    				response = new String(data, "UTF-8");
	    	        }
	    	    } catch(Exception err) {
	    	    	StringWriter sw = new StringWriter();
	    	    	PrintWriter pw = new PrintWriter(sw);
	    	    	err.printStackTrace(pw);
	    	    	traceback = sw.toString();
	    	    } finally {
	    	        Message msg = handler.obtainMessage();
	    	        Bundle bundle = new Bundle();
	    	        bundle.putInt("status", status);
	    	        bundle.putString("response", response);
	    	        bundle.putString("traceback", traceback);
	    	        msg.setData(bundle);
	    	        handler.sendMessage(msg);
	    	    }
	    	}
		};
		new Thread(request).start();
	}

}
