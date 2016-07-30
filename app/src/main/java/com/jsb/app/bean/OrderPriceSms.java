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
	private String phoneNum = null; //本机号码
	private String  smsid = null;  //短信id
	private String smsbody  = null;      //内容
 	private long mobile  =0;       //手机
 	private String reply  = null; //回复内容
 	private int status  = 0; //status 0 为未开始处理 1 已经推送redis 2 已经收到回复信息 （更新reply status 2） 3 已经发送回复信息未收到对方回执 4 收到对方回执 完成
 	private String receiveTime  = null; //师傅发送时间
 	private String time  = "";
 	private String name  = null; //发送师傅名称
 	
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

		// 执行post方法
		return  post(url, params);
	} 
	
	public Map<String,String> getReplyFromServer(){
		String url = "http://114.55.108.39/index.php?g=Restful&m=Sms&a=get_reply";
		Log.i("test","PPPP:get"+url);	
		return this.get(url);
	}

}
