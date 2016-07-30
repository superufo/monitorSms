package com.jsb.pool;

public abstract class AbstractPool<T> implements  Pool<T>   {
	@Override
	public void release(T t) {
		// TODO Auto-generated method stub
		if( isValid(t) ){
			returnToPool(t);
		}else{
			handleInvalidReturn(t);
		}
	}

	 protected abstract void handleInvalidReturn(T t);
	 protected abstract void returnToPool(T t);
	 protected abstract boolean isValid(T t);
}
