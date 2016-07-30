package com.jsb.monitorSms;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.jsb.util.Utils;

@SuppressLint("SimpleDateFormat")
public class SmsReceive extends BroadcastReceiver {
	private SQLiteDatabase db = null;

	// 6位纯数字验证码
	static final String PATTERN_CODER ="\\d{6}"; 
	
	@SuppressLint("DefaultLocale")
	@Override
	public void onReceive(Context context, Intent intent) {
		Uri inBoxUri = Uri.parse("content://sms/inbox");
		Log.i("test","GGGGGGG1");
		// 提取未读的短消息
		 Bundle bundle = intent.getExtras();
         if (bundle != null) {
             Object[] pdus = (Object[])bundle.get("pdus");  
             if (pdus != null && pdus.length > 0) {  
                 SmsMessage[] messages = new SmsMessage[pdus.length];  
                 for (int i = 0; i < pdus.length; i++) {  
                     byte[] pdu = (byte[]) pdus[i];  
                     messages[i] = SmsMessage.createFromPdu(pdu);  
                 }  
             Log.i("test","GGGGGGG2");        
                 for (SmsMessage message : messages) {  
                	 int flag = 0;
                     String smsbody = message.getMessageBody();// 得到短信内容  
                     String mobile = message.getOriginatingAddress();// 得到发信息的号码  

                     Date receiveTime = new Date(message.getTimestampMillis());  
                     SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                     format.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));
       
                     if( mobile.contains("+") == true ){
         				mobile = mobile.substring(3, mobile.length());
         			 }
         			
         			 if( Utils.isMobile(mobile) == false ){  
         				continue;
         			 }
         			 Log.i("test","GGGGGGG3");  		
         			String verifyCode = patternCode(smsbody); 
        			if (verifyCode != null || verifyCode != "") {  	 
	         			ContentValues sms = new ContentValues();
	    				sms.put("smsid", UUID.randomUUID().toString().toUpperCase().replace("-", ""));
	    				sms.put("smsbody", smsbody);
	    				sms.put("mobile", mobile);
	    				sms.put("receiveTime", format.format(receiveTime));
	                     
	    				db.insert("sms_translate", null, sms);
	    				flag = 1;
        			} 
        			 Log.i("test","GGGGGGG4");      
        			if( flag == 1 ){
        				int indexOnSim = message.getIndexOnSim();
        				
        				Log.i("test","GGGGGGG5");  
        				ContentValues smsInfo = new ContentValues();
        				smsInfo.put("read", "2");
        				try {
        					context.getContentResolver().update(inBoxUri,smsInfo," _id = ? ",new String[]{ Integer.toString(indexOnSim)} );
        				} catch (Exception e) {
        					Log.e("test", "SmsReceive:error" + e.getMessage());
        					e.printStackTrace();
        				}
        			}
        			
                 }
             }
         }
     }
	
	private String patternCode(String patternContent) {
		if (TextUtils.isEmpty(patternContent)) {
			return null;
		}
		Pattern p = Pattern.compile(PATTERN_CODER);
		Matcher matcher = p.matcher(patternContent);
		if (matcher.find()) {
			return matcher.group();
		}
		return null;
	}
	
	
}
