package com.jsb.pool;

public interface ObjectFactory<T> {
	/**
	  * Returns a new instance of an object of type T.
	  *
	  * @return T an new instance of the object of type T
	  */
	 public abstract T createNew();

}
