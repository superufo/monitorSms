package com.jsb.monitorSms;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jsb.app.bean.OrderPriceSms;
import com.jsb.db.DBHelper;
import com.jsb.pool.BoundedPool;
import com.jsb.pool.ComObjectFactory;
import com.jsb.pool.ComValidator;
import com.jsb.pool.Pool;

public class ReplyRedisSms {
	private Context context = null;	
	private SQLiteDatabase db = null;
	private  DBHelper.DatabaseHelper	dbHelper = null;
	public final static byte[] _writeLock = new byte[0];
	private final static String TABLENAME = "sms_translate";
	private final static String  TAG= "test";
	private Gson gson = new Gson();
	private HttpGet httpGet = null;
	
	public ReplyRedisSms(Context context) {
			this.setContext(context);
			dbHelper = new DBHelper.DatabaseHelper(context);
			db = dbHelper.getWritableDatabase();
	}
	
	public void exec() throws IOException, JSONException {
		Log.e("test", "ReplyRedisSms------start" );
		String url = "http://114.55.108.39/index.php?g=Restful&m=Sms&a=get_reply";
		httpGet = new HttpGet(url);

		ArrayList<NameValuePair> headerList = new ArrayList<NameValuePair>();
		headerList.add(new BasicNameValuePair("Content-Type","text/html; charset=utf-8"));
		for (int i = 0; i < headerList.size(); i++) {
			httpGet.addHeader(headerList.get(i).getName(), headerList.get(i).getValue());
		}

		HttpClient httpClient = new DefaultHttpClient();
		HttpResponse response = httpClient.execute(httpGet);
		if (response.getStatusLine().getStatusCode() == 200) {
			String strResult = EntityUtils.toString(response.getEntity());
			JSONObject strJsonObj = new JSONObject(strResult);
			String data =   (String) strJsonObj.get("data");
			data = data.trim();
			
			if( data == null || data == "" ||  "null".equals(data) ){
				db.close();
				return;
			}
			
			Type type = new TypeToken<ArrayList<OrderPriceSms>>() {}.getType();
			ArrayList<OrderPriceSms> list = gson.fromJson(data,type);
			if (list == null) {   //Log.e("test", "ReplyRedisSms------m2" );
				db.close();
				return;
			}

			List<ContentValues> upList = new ArrayList<ContentValues>();
			List<String> wList = new ArrayList<String>();
			for (Iterator<OrderPriceSms> iterator = list.iterator(); iterator.hasNext();) {
				OrderPriceSms item = (OrderPriceSms) iterator.next();
				String reply = item.getReply();

				ContentValues upSms = new ContentValues();
				upSms.put("status", 2);
				upSms.put("reply", reply);
				//Log.i(TAG, "fetchRedisReplySms-----reply：" + reply);
				//Log.i(TAG,"fetchRedisReplySms-----Smsid："+ item.getSmsid());
				upList.add(upSms);
				wList.add("smsid ='" + item.getSmsid() + "'");
				updateStatus(upList,wList);
			}
		}
		db.close();
		
		Log.e("test", "ReplyRedisSms------end" );
	}
	/**
	 * 更新状态
	 * 
	 * @param list
	 * @param table
	 *            表名
	 */
	public void updateStatus(List<ContentValues> list,List<String> wherelist) {
		synchronized (_writeLock) {
			db.beginTransaction();
			try {
				for (int i = 0, len = list.size(); i < len; i++){
					db.update(TABLENAME, list.get(i), wherelist.get(i), null);
				}
				Log.e("test", "ReplyRedisSms------mdb1" );
				db.setTransactionSuccessful();
				Log.e("test", "ReplyRedisSms------mdb2" );
			} finally {
				db.endTransaction();
			}
		}
	}

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}
	
	
}
