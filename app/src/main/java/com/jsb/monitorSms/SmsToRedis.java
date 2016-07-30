package com.jsb.monitorSms;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.jsb.db.DBHelper;
import com.jsb.pool.BoundedPool;
import com.jsb.pool.ComObjectFactory;
import com.jsb.pool.ComValidator;
import com.jsb.pool.Pool;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SmsToRedis  {
	private Context context;	
	private SQLiteDatabase db = null;
	private  DBHelper.DatabaseHelper	dbHelper = null;
	public final static byte[] _writeLock = new byte[0];
	private final static String TABLENAME = "sms_translate";
	private final static String  TAG= "test";
	
	public SmsToRedis(Context context) {
			this.context = context;
			dbHelper = new DBHelper.DatabaseHelper(context);
			db = dbHelper.getWritableDatabase();
	}
			
	public void exec() {
		    Log.e("test", "SmsToRedis------Start" );
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Cursor smsCursor = null;
			smsCursor = db.query(TABLENAME, new String[] { "smsid","smsbody", "mobile", "receiveTime", "time", "name","reply", "status" }, " reply is null and status < ? ",
							new String[] { "1" }, null, null, null);
			while (smsCursor.moveToNext()) {
				String time = df.format(new Date());
				String receiveTime = df.format(new Date(Long.valueOf(smsCursor.getString(smsCursor.getColumnIndex("receiveTime")))));
				
				NameValuePair pair1 = new BasicNameValuePair("smsid",smsCursor.getString(smsCursor.getColumnIndex("smsid")));
				NameValuePair pair2 = null;
				try {
					pair2 = new BasicNameValuePair("smsbody",URLEncoder.encode(smsCursor.getString(smsCursor.getColumnIndex("smsbody")), "UTF-8"));
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				NameValuePair pair3 = new BasicNameValuePair("mobile",smsCursor.getString(smsCursor.getColumnIndex("mobile")));
				NameValuePair pair4 = new BasicNameValuePair("receiveTime",receiveTime);
				NameValuePair pair5 = new BasicNameValuePair("time", time);
				NameValuePair pair6 = new BasicNameValuePair("name", " ");
				if (smsCursor.getString(smsCursor.getColumnIndex("name")) != null&& smsCursor.getString(smsCursor.getColumnIndex("name")) != "")
				try {
					pair6 = new BasicNameValuePair("name",URLEncoder.encode(smsCursor.getString(smsCursor.getColumnIndex("name")), "UTF-8"));
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
				}
				NameValuePair pair7 = new BasicNameValuePair("reply",smsCursor.getString(smsCursor.getColumnIndex("reply")));
				NameValuePair pair8 = new BasicNameValuePair("status",String.valueOf(smsCursor.getInt(smsCursor.getColumnIndex("status"))));
				
				List<NameValuePair> pairList = new ArrayList<NameValuePair>();
				pairList.add(pair1);
				pairList.add(pair2);
				pairList.add(pair3);
				pairList.add(pair4);
				pairList.add(pair5);
				pairList.add(pair6);
				pairList.add(pair7);
				pairList.add(pair8);
				String url = "http://114.55.108.39/index.php?g=Restful&m=Sms&a=add";
				HttpEntity requestHttpEntity = null;
				try {
					requestHttpEntity = new UrlEncodedFormEntity(pairList);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}finally{
					smsCursor.close();
					db.close();
				}
				
				HttpPost httpPost = new HttpPost(url);
				httpPost.setEntity(requestHttpEntity);
				HttpClient httpClient = new DefaultHttpClient();
				HttpResponse response = null;
				try {
					response = httpClient.execute(httpPost);
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}finally{
					smsCursor.close();
					db.close();
				}
				
				List<ContentValues> list = new ArrayList<ContentValues>();
				List<String> wlist = new ArrayList<String>();
				if (response.getStatusLine().getStatusCode() == 200) {
					String strResult = null;
				    try {
						strResult = EntityUtils.toString(response.getEntity());
					} catch (ParseException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}finally{
						smsCursor.close();
						db.close();
					}
					
					
					JSONObject strJsonObj = null;
					try {
						strJsonObj = new JSONObject(strResult);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}finally{
						smsCursor.close();
						db.close();
					}  
					
					// 结果 {"code":1,"msg":"\u64cd\u4f5c\u6210\u529f.","data":""}
					try {
						if (strJsonObj.get("code").toString().trim().equals("1") == true) {
								ContentValues upSms = new ContentValues();
								upSms.put("status", 1);
								String where = "smsid ='"+ smsCursor.getString(smsCursor.getColumnIndex("smsid")) + "'";

								list.add(upSms);
								wlist.add(where);
								updateStatus(list,wlist);
						} else {
							Log.i(TAG, "服务器解析失败.");
						}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}finally{
						smsCursor.close();
						db.close();
					}
				} else {
					Log.i(TAG, "网络异常.");
				}
			}
			smsCursor.close();
			db.close();
			Log.e("test", "SmsToRedis------end" );
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
				db.setTransactionSuccessful();
			} finally {
				db.endTransaction();
				db.close();
			}
		}
	}
	
}
