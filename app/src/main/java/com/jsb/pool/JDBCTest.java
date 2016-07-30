package com.jsb.pool;

import java.sql.Connection;

public class JDBCTest {
	public static void main(String[] args) {
		Pool<Connection> pool = PoolFactory.newBoundedBlockingPool(10,
				new JDBCConnectionFactory("", "", "", ""),
				new JDBCConnectionValidator());
		Connection con = pool.get();
		// do whatever you like
	}

}
