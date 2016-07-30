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
	// �����ݿ�
	public void OpenDB(Context context) {
		dbHelper = new DatabaseHelper(context);
		db = dbHelper.getWritableDatabase();
	}
	// �ر����ݿ�
	public void Close() {
		dbHelper.close();
		if(db!=null){
			db.close();
		}
	}

	/**
	 * ����
	 * 
	 * @param list
	 * @param table
	 *            ����
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
	 * ����״̬
	 * 
	 * @param list
	 * @param table
	 *            ����
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
	 * ���ڳ�ʼ�����ݿ�
	 * 
	 * @author Administrator
	 * 
	 */
	public static class DatabaseHelper extends SQLiteOpenHelper {
		// �������ݿ��ļ�
		private static final String DB_NAME = "sms_translate";
		// �������ݿ�汾
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
			Log.i("sms_translate", "����sms��ɹ�");	
		}
		
		/**
		 * ����sms��
		* uid smsbody �������� mobile �����˺��� receivetime �յ�ʦ������ʱ�� name ���������� reply
		* �ͻ��ظ���Ϣ
		* status 0 Ϊδ��ʼ���� 1 �Ѿ�����redis 2 �Ѿ��յ��ظ���Ϣ ������reply status 2�� 3
		* �Ѿ����ͻظ���Ϣδ�յ��Է���ִ 4 �յ��Է���ִ ���
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
		 * ���°汾ʱ���±�
		 */
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			DropTable(db);
			onCreate(db);
			Log.e("User", "onUpgrade");
		}
		
		/**
		 * ɾ����
		 * 
		 * @param db
		 */
		public void DropTable(SQLiteDatabase db) {
			StringBuilder sb = new StringBuilder();
			sb.append("DROP TABLE IF EXISTS sms_translate;");
		
			db.execSQL(sb.toString());
		}
		
		/**
		 * ������ݱ�������������ݣ�
		 * @param db
		 */
		public static void ClearData(Context context){
			DatabaseHelper dbHelper = new DBHelper.DatabaseHelper(context);
			SQLiteDatabase db=dbHelper.getWritableDatabase();
			StringBuilder sb=new StringBuilder();
			sb.append("DELETE FROM sms_translate;");//������ű�
		
			db.execSQL(sb.toString());
		}
	}
}
