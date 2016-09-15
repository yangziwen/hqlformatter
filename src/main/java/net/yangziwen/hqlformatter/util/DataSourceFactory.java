package net.yangziwen.hqlformatter.util;

import javax.sql.DataSource;

import org.apache.tomcat.jdbc.pool.PoolProperties;

public class DataSourceFactory {
	
	private static final DataSource DATA_SOURCE = initDataSource();
	
	private static DataSource initDataSource() {
		PoolProperties config = new PoolProperties();
		config.setDriverClassName("org.sqlite.JDBC");
		config.setUrl("jdbc:sqlite:" + System.getProperty("user.dir") + "/db/hqlformatter.db");
		config.setUsername("sa");
		config.setPassword("");
		config.setMinIdle(1);
		config.setMaxIdle(10);
		config.setMaxActive(10);
		return new org.apache.tomcat.jdbc.pool.DataSource(config);
	}
	
	public static DataSource getDataSource() {
		return DATA_SOURCE;
	}

}
