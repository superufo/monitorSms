package com.jsb.app.bean;

import java.net.URLEncoder;

public class Urls {
	//http://114.55.108.39/index.php?g=admin&m=public&a=login
	//http://114.55.108.39/index.php?g=rest&m=public&a=login
	public final static String HOST = "114.55.108.39";//192.168.1.213  www.oschina.net
	public final static String HTTP = "http://";
	public final static String HTTPS = "https://";
	
	
	public  String DIR = "restful" ;
	
	private final static String URL_SPLITTER = "/";
	private final static String URL_UNDERLINE = "_";
	
	private final static String URL_API_HOST = HTTP + HOST + URL_SPLITTER;

	public  String getUrl(String mValue,String aValue){
		return    HTTP + HOST + URL_SPLITTER+"index.php?g="+DIR+"&m="+mValue+"&a="+aValue;
	}
	
	/**
	 * 对URL进行格式处理
	 * @param path
	 * @return
	 */
	private final static String formatURL(String path) {
		if(path.startsWith("http://") || path.startsWith("https://"))
			return path;
		return "http://" + URLEncoder.encode(path);
	}	
	

}
