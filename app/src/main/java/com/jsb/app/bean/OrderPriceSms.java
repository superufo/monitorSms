package com.jsb.app.bean;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.util.Log;

import com.jsb.util.PhoneInfo;
import com.jsb.util.UUID22;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;

public class OrderPriceSms extends Entity {
	private PhoneInfo  pm  =null;
	private String phoneNum = null; //��������
	private String  smsid = null;  //����id
	private String smsbody  = null;      //����
 	private long mobile  =0;       //�ֻ�
 	private String reply  = null; //�ظ�����
 	private int status  = 0; //status 0 Ϊδ��ʼ���� 1 �Ѿ�����redis 2 �Ѿ��յ��ظ���Ϣ ������reply status 2�� 3 �Ѿ����ͻظ���Ϣδ�յ��Է���ִ 4 �յ��Է���ִ ���
 	private String receiveTime  = null; //ʦ������ʱ��
 	private String time  = "";
 	private String name  = null; //����ʦ������
 	
 	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getReceiveTime() {
		return receiveTime;
	}
	public void setReceiveTime(String receiveTime) {
		this.receiveTime = receiveTime;
	}
	public String getReply() {
		return reply;
	}
	public void setReply(String reply) {
		this.reply = reply;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}

	private byte[] responseString = null;
 	private HashMap responseResult = new HashMap();
 	
	public String getSmsid() {
		return smsid;
	}
	public void setSmsid(String smsid) {
		this.smsid = smsid;
	}
	public String getSmsbody() {
		return smsbody;
	}
	public void setSmsbody(String smsbody) {
		this.smsbody = smsbody;
	}
	public long getMobile() {
		return mobile;
	}
	public void setMobile(long mobile) {
		this.mobile = mobile;
	}
	
	public void OrderPriceSms(Context context){
		 PhoneInfo  pm = new PhoneInfo(context);
		 phoneNum =  pm.getNativePhoneNumber(); 
	}
	
	public Map<String, String> postNewSms(Map<String,String> fileds){
		String url = "http://114.55.108.39/index.php?g=Restful&m=Sms&a=add";
		
		RequestParams params = new RequestParams();
		for(Map.Entry<String, String> fd : fileds.entrySet()){
			params.put(fd.getKey(), fd.getValue());
		}
		params.put("id", new UUID22().getUUID());
		params.put("mobil", phoneNum); 

		// ִ��post����
		return  post(url, params);
	} 
	
	public Map<String,String> getReplyFromServer(){
		String url = "http://114.55.108.39/index.php?g=Restful&m=Sms&a=get_reply";
		Log.i("test","PPPP:get"+url);	
		return this.get(url);
	}

}
