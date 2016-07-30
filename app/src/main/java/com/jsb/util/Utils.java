/**
 * 
 */
/**
 * @author dev_003
 *
 */
package com.jsb.util;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.text.TextUtils;

public class  Utils {

	// ≈–∂œserviceName «∑Ò‘À––
	
	public static class  CheckService { 
		public  boolean isServiceWork(Context mContext, String serviceName) {
			boolean isWork = false;
			ActivityManager myAM = (ActivityManager) mContext
					.getSystemService(Context.ACTIVITY_SERVICE);
			List<RunningServiceInfo> myList = myAM.getRunningServices(40);
			if (myList.size() <= 0) {
				return false;
			}
			for (int i = 0; i < myList.size(); i++) {
				String mName = myList.get(i).service.getClassName().toString();
				if (mName.equals(serviceName)) {
					isWork = true;
					break;
				}
			}
			return isWork;
		}
	}

	final static String[] PHONENUMBER_PREFIX = { "130", "131", "132", "145", "155", "156", "185", "186", "134", "135", "136", "137", "138",
        "139", "147", "150", "151", "152", "157", "158", "159", "182", "183", "187", "188", "133", "153", "189", "180" };
	public static boolean isMobile(String number) {
	    int len = PHONENUMBER_PREFIX.length;
	    if (number != null) {
	        for (int i = 0; i < len; i++) {
	            Pattern p = Pattern.compile(PHONENUMBER_PREFIX[i] + "\\d{8}");
	            if (p.matcher(number).matches()) {
	                return true;
	            }
	        }
	    }
	    return false;
	}
	
	static final String PATTERN_CODER = "\\d{6}";
	public static String patternCode(String patternContent,String patternCode ) {
		if (TextUtils.isEmpty(patternContent)) {
			return null;
		}

		Pattern p = Pattern.compile(patternCode);
		Matcher matcher = p.matcher(patternContent);
		if (matcher.find()) {
			return matcher.group();
		}
		return null;
	}
	
}