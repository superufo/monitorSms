package com.jsb.monitorSms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import com.jsb.util.*;

public class BootReceive extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
//		for (int i = 1000; i < 1010; i++) {
//			SystemClock.sleep(1000);
//			Log.i("test", "test-111--------:" + Integer.toString(i));
//		}
		Intent appStarActivity = new Intent(context, AppStarActivity.class);
		context.startActivity(appStarActivity);
		//context.startService(appStarActivity);
	}

}
