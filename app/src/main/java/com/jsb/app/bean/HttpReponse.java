package com.jsb.app.bean;

import java.util.Map;

public class HttpReponse {
	//php 返回的信息字段
		protected int code = -1;
		protected String msg = null;
		protected String data = null;
		
		public String getMsg() {
			return msg;
		}

		public void setMsg(String msg) {
			this.msg = msg;
		}

		public String getData() {
			return data;
		}

		public void setData(String data) {
			this.data = data;
		}

		public void setCode(int code) {
			this.code = code;
		}
		
		public int getCode() {
			return code;
		}
}
