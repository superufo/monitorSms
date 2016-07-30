package com.jsb.pool;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import android.content.Context;
import android.util.Log;

import com.jsb.monitorSms.ReplyRedisSms;
import com.jsb.monitorSms.SmsToRedis;
import com.jsb.pool.*;

public class ComObjectFactory<T> implements ObjectFactory<T> {
	private static final String TAG = "test";
	private T t = null;

	@SuppressWarnings("unchecked")
	public ComObjectFactory(String className,Context context) {
		if( "SmsToRedis".equals(className) ){
			t = (T) new SmsToRedis(context );
		}else if("ReplyRedisSms".equals(className)){
			t = (T) new ReplyRedisSms(context);
		 }
	}

	@Override
	public T createNew() {
		return this.t;
	}

}
