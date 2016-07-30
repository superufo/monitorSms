package com.jsb.monitorSms;

import android.app.Application;
import android.content.Context;

import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

public class JsbApplication extends Application {
	public static com.squareup.leakcanary.watcher.RefWatcher getRefWatcher(Context context) {
		JsbApplication application = (JsbApplication) context
				.getApplicationContext();
		return application.refWatcher;
	}

	private com.squareup.leakcanary.watcher.RefWatcher refWatcher;

	@Override
	public void onCreate() {
		super.onCreate();
		refWatcher = LeakCanary.install(this);
	}
}