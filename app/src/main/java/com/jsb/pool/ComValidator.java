package com.jsb.pool;

import java.sql.Connection;
import java.sql.SQLException;

import com.jsb.pool.Validator;

public final class ComValidator<T> implements Validator<T> {
	public  boolean isValid(T con) {
		if (con == null) {
			return false;
		}
		
		return true;
	}

	@Override
	public void invalidate(T t) {
		// TODO Auto-generated method stub
		t = null;
		System.gc();
	}
}
