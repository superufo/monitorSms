package com.jsb.db;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper {

	private SQLiteDatabase db;
	private DatabaseHelper dbHelper;
	private final static String TABLENAME = "sms_translate";
	public final static byte _writeLock[] = new byte[0];
	// 打开数据库
	public void OpenDB(Context context) {
		dbHelper = new DatabaseHelper(context);
		db = dbHelper.getWritableDatabase();
	}
	// 关闭数据库
	public void Close() {
		dbHelper.close();
		if(db!=null){
			db.close();
		}
	}

	/**
	 * 插入
	 * 
	 * @param list
	 * @param table
	 *            表名
	 */
	public void Insert(List<ContentValues> list, String tableName) {
		synchronized (_writeLock) {
			db.beginTransaction();
			try {
				db.delete(tableName, null, null);
				for (int i = 0, len = list.size(); i < len; i++)
					db.insert(tableName, null, list.get(i));
				db.setTransactionSuccessful();
			} finally {
				db.endTransaction();
			}
		}
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
			}
		}
	}
	
	
	public DBHelper(Context context) {
		this.dbHelper = new DatabaseHelper(context);
	}
	
	
	/**
	 * 用于初始化数据库
	 * 
	 * @author Administrator
	 * 
	 */
	public static class DatabaseHelper extends SQLiteOpenHelper {
		// 定义数据库文件
		private static final String DB_NAME = "sms_translate";
		// 定义数据库版本
		private static final int DB_VERSION = 1;
		public DatabaseHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onOpen(SQLiteDatabase db) {
			super.onOpen(db);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			CreateSmsDb(db);
			Log.i("sms_translate", "创建sms表成功");	
		}
		
		/**
		 * 创建sms表
		* uid smsbody 短信内容 mobile 发送人号码 receivetime 收到师傅短信时间 name 发送人姓名 reply
		* 客户回复信息
		* status 0 为未开始处理 1 已经推送redis 2 已经收到回复信息 （更新reply status 2） 3
		* 已经发送回复信息未收到对方回执 4 收到对方回执 完成
		* String sql1 = " drop table  sms_translate ";
		* db.execSQL(sql1);
	   * @param db
		*/
		public void CreateSmsDb(SQLiteDatabase db) {
			//DropTable(db);
			StringBuilder sb = new StringBuilder();
			sb.append("CREATE TABLE IF NOT EXISTS sms_translate (");
			sb.append("smsid varchar(48) NOT NULL DEFAULT '', ");
			sb.append("smsbody NVARCHAR(500)  DEFAULT (''), ");
			sb.append("mobile NVARCHAR(20)  DEFAULT (''), ");
			sb.append("receiveTime NVARCHAR(15)  DEFAULT (''), ");
			sb.append("time NVARCHAR(15)  DEFAULT (''), ");
			sb.append("name NVARCHAR(32) DEFAULT (''), ");
			sb.append("reply NVARCHAR(500)  DEFAULT NULL, ");
			sb.append("status int(1) DEFAULT (0)  )");

			db.execSQL(sb.toString());
		}
		
		/**
		 * 更新版本时更新表
		 */
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			DropTable(db);
			onCreate(db);
			Log.e("User", "onUpgrade");
		}
		
		/**
		 * 删除表
		 * 
		 * @param db
		 */
		public void DropTable(SQLiteDatabase db) {
			StringBuilder sb = new StringBuilder();
			sb.append("DROP TABLE IF EXISTS sms_translate;");
		
			db.execSQL(sb.toString());
		}
		
		/**
		 * 清空数据表（仅清空无用数据）
		 * @param db
		 */
		public static void ClearData(Context context){
			DatabaseHelper dbHelper = new DBHelper.DatabaseHelper(context);
			SQLiteDatabase db=dbHelper.getWritableDatabase();
			StringBuilder sb=new StringBuilder();
			sb.append("DELETE FROM sms_translate;");//清空新闻表
		
			db.execSQL(sb.toString());
		}
	}
}
