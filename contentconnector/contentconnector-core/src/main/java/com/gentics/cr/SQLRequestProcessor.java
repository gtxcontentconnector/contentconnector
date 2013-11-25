package com.gentics.cr;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import com.gentics.cr.exceptions.CRException;

/**
 * {@link SQLRequestProcessor} fetches data from a mysql table
 * @author bigbear3001
 *
 */
public class SQLRequestProcessor extends AbstractSQLRequestProcessor {
	
	/**
	 * configuration key for the datasource driver class.
	 */
	private static final String DSHDRIVERCLASS_KEY = "driverClass";
	/**
	 * configuration key for the datasource url.
	 */
	private static final String DSHURL_KEY = "url";
	
	public SQLRequestProcessor(CRConfig config) throws CRException {
		super(config);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void finalize() {
		// TODO Auto-generated method stub
	}

	@Override
	protected Connection getConnection() throws CRException {
		Connection conn = null;
		try {
			Properties datasourceHandleProperties = ((CRConfigUtil) this.config).getDatasourceHandleProperties();
			Class.forName(datasourceHandleProperties.getProperty(DSHDRIVERCLASS_KEY));
			conn = DriverManager.getConnection(datasourceHandleProperties.getProperty(DSHURL_KEY));
		} catch (Exception ex) {
			throw new CRException(ex);
		}
		return conn;
	}
}
