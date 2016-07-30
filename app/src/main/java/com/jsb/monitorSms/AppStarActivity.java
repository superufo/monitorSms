package com.jsb.monitorSms;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class AppStarActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_app_star);

		Intent grayIntent = new Intent(getApplicationContext(),
				MonSmsService.class);
		startService(grayIntent);

		// Intent mBootIntent = new Intent( this, MonSmsServerBak.class);
		// startService(mBootIntent);

		// Intent hBootIntent = new Intent(Intent.ACTION_RUN);
		// mBootIntent.setClass(this, HotDogServer.class);
		// startService(hBootIntent);
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.app_star, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
