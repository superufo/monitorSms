/**
 * 
 */
/**
 * @author Administrator
 *
 */
package com.jsb.app.bean;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * 实体类
 * 
 * {['code':1]['data':{[]}]} code -1 失败 1 为成功 data 后面为数据
 * 
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
@SuppressLint("SimpleDateFormat")
public abstract class Entity implements Serializable {

	public final static String UTF8 = "UTF-8";

	// public final static String NODE_ROOT = "jsb";

	public final static SimpleDateFormat SDF_IN = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	public final static SimpleDateFormat SDF_OUT = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm");

	//php 返回的信息字段
	protected int code = -1;
	protected String msg = null;
	protected Map<String,String> data = null;
	
	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Map<String, String> getData() {
		return data;
	}

	public void setData(Map<String, String> data) {
		this.data = data;
	}

	public void setCode(int code) {
		this.code = code;
	}
	
	public int getCode() {
		return code;
	}

	public Map<String,String> post(String url, RequestParams params) {
		final Map<String, String> responseResult = new HashMap();
		AsyncHttpClient client = new AsyncHttpClient();
		params.put("time", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

		client.post(url, params, new AsyncHttpResponseHandler() {

			@Override
			public void onFailure(int statusCode,
					org.apache.http.Header[] headers, byte[] responseBody,
					Throwable error) {
				// TODO Auto-generated method stub
				responseResult.put("code", "1");
				responseResult.put("error", error.getMessage().toString());

				error.printStackTrace();
			}

			@Override
			public void onSuccess(int statusCode,
					org.apache.http.Header[] headers, byte[] responseBody) {

				if (statusCode == 200) {
					responseResult.put("code", "0");
					responseResult.put("response", new String(responseBody));
				}else{
					responseResult.put("code", "2");
					responseResult.put("error", "服务器错误");
				}
				// TODO Auto-generated method stub

			}

		});
		return responseResult;
	}

	public Map<String, String> get(String url, RequestParams params) {
		final Map responseResult = new HashMap();
		AsyncHttpClient client = new AsyncHttpClient();
		params.put("time", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

		client.get(url, params, new AsyncHttpResponseHandler() {
			@Override
			public void onFailure(int statusCode,
					org.apache.http.Header[] headers, byte[] responseBody,
					Throwable error) {
				// TODO Auto-generated method stub
				responseResult.put("code", "1");
				responseResult.put("error", error.getMessage().toString());

				error.printStackTrace();
			}

			@Override
			public void onSuccess(int statusCode,
					org.apache.http.Header[] headers, byte[] responseBody) {

				if (statusCode == 200) {
					responseResult.put("code", "0");
					responseResult.put("response",new String(responseBody));
				}else{
					responseResult.put("code", "2");
					responseResult.put("error", "服务器错误");
				}
				// TODO Auto-generated method stub

			}

		});
		return responseResult;
	}
	
	
	public Map<String,String> get(String url) {
		final Map<String,String> responseResult = new HashMap();
		AsyncHttpClient client = new AsyncHttpClient(); 
		RequestParams params = new   RequestParams();
	
		client.get(url,params, new AsyncHttpResponseHandler() {
			@Override
			public void onFailure(int statusCode,
					org.apache.http.Header[] headers, byte[] responseBody,
					Throwable error) {
				// TODO Auto-generated method stub
				responseResult.put("code", "1");
				responseResult.put("error", error.getMessage().toString());

				Log.i("test","PPPP:"+error.getMessage().toString());
				error.printStackTrace();
			}

			@Override
			public void onSuccess(int statusCode,
					org.apache.http.Header[] headers, byte[] responseBody) {
				if (statusCode == 200) {
					responseResult.put("code", "0");
					responseResult.put("response",new String(responseBody));
					
					Log.i("test","PPPP:"+(new String(responseBody)));
				}else{
					responseResult.put("code", "2");
					responseResult.put("error", "服务器错误");
					Log.i("test","PPPP:服务器错误");
				}
			}
		});
		
		return responseResult;
	}

}
