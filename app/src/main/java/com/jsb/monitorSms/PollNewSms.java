package com.jsb.monitorSms;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.jsb.db.DBHelper;
import com.jsb.db.DBHelper.DatabaseHelper;
import com.jsb.util.Utils;

@SuppressLint("DefaultLocale")
public class PollNewSms extends TimerTask {
	private Context context = null;

	// 6位纯数字验证码
	private static final String PATTERN_CODER = "\\d{6}";
	private SQLiteDatabase db = null;
	private  DBHelper.DatabaseHelper	dbHelper = null;
	public final static byte[] _writeLock = new byte[0];
	
	public PollNewSms(Context context) {
		super();
	
		this.context = context;
		dbHelper = new DBHelper.DatabaseHelper(context);
		db = dbHelper.getWritableDatabase();
	}

	@Override 
	public void run() {
		Log.e("test", "PollNewSms------start" );
		Uri inBoxUri = Uri.parse("content://sms/inbox");
		String[] projection = new String[] { "service_center", "read", "_id","address", "person", "body", "date", "type" };
		Cursor cur = this.context.getContentResolver().query(inBoxUri, projection,"read < 2 ", null, "date desc");
		if (null == cur)
			return;
		
		//for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
		while ( cur.moveToNext() ) { 
			boolean flag = false;
			String mobile = cur.getString(cur.getColumnIndex("address"));
			String body = cur.getString(cur.getColumnIndex("body"));
			if (mobile.contains("+") == true) {
				mobile = mobile.substring(3, mobile.length());
			}

			//系统短消息read 设置为 2
			if (Utils.isMobile(mobile) == false) {
				setSmsRead( cur.getString(cur.getColumnIndex("_id")));
				continue;
			}

			String smsbody = cur.getString(cur.getColumnIndex("body"));
			String verifyCode =  Utils.patternCode(smsbody,PATTERN_CODER) ;
			if (verifyCode != null || verifyCode != "") {
				ContentValues sms = new ContentValues();
				sms.put("smsid", UUID.randomUUID().toString().toUpperCase().replace("-", ""));
				sms.put("smsbody", cur.getString(cur.getColumnIndex("body")));
				sms.put("mobile", cur.getString(cur.getColumnIndex("address")));
				sms.put("receiveTime", cur.getString(cur.getColumnIndex("date")).toString());
				if (cur.getString(cur.getColumnIndex("person")) != null && cur.getString(cur.getColumnIndex("person")) != "")
					sms.put("name", cur.getString(cur.getColumnIndex("person")));
			
				synchronized(_writeLock){
					db.beginTransaction();
					try{
						db.insert("sms_translate", null, sms);
						db.setTransactionSuccessful();
						flag = true;
					}catch(Exception ex){
						flag = false;
						Log.e("fav_insert", ex.getMessage());
					}finally{
						db.endTransaction();
						cur.close();
						db.close();
					}
				}
			}else{
				//将非匹配短消息设置为2
				setSmsRead( cur.getString(cur.getColumnIndex("_id")));
			}
			// 将短消息read 设置为 3
			if ( flag == true ) {
				setSmsRead( cur.getString(cur.getColumnIndex("_id")));
			}
		}
		cur.close();
		db.close();
		
		Log.e("test", "PollNewSms------end" );
	}

	private void  setSmsRead(String id){
		Uri inBoxUri = Uri.parse("content://sms/inbox");
		ContentValues smsInfo = new ContentValues();
		smsInfo.put("read", "2");
		try {
			context.getContentResolver().update(inBoxUri,smsInfo," _id = ?",new String[] { id } );
		} catch (Exception e) {
			Log.e("test", "SmsReceive:error" + e.getMessage());
			e.printStackTrace();
		}finally{
			db.close();
		}
	}
}
