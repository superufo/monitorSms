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

public class SmsToRedisObjectFactory implements ObjectFactory<SmsToRedis> {
	private static final String TAG = "test";
	private SmsToRedis t = null;

	@SuppressWarnings("unchecked")
	public SmsToRedisObjectFactory(Context context) {
		new SmsToRedis(context);
	}

	@Override
	public SmsToRedis createNew() {
		return this.t;
	}

}
