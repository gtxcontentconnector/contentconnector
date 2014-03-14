package com.gentics.cr;

import java.sql.Connection;

import org.apache.log4j.Logger;

import com.gentics.cr.exceptions.CRException;

/**
 * PooledSQLRequestProcessor.
 * Last changed: $Date: 2010-04-01 15:24:02 +0200 (Do, 01 Apr 2010) $
 * @version $Revision: 541 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class PooledSQLRequestProcessor extends AbstractSQLRequestProcessor {

	/**
	 * Logger.
	 */
	private static Logger log = Logger.getLogger(PooledSQLRequestProcessor.class);
	
	/**
	 * Constructor. Create a new PooledSQLRequestProcessor.
	 * @param config configuration
	 * @throws CRException exception in case of error.
	 */
	public PooledSQLRequestProcessor(final CRConfig config) throws CRException {
		super(config);
	}

	@Override
	public final void finalize() {
		try {
			((CRConfigUtil) this.config).releaseJDBCPool();
		} catch (Exception s) {
			log.error("Could not release connection pool.", s);
		}

	}

	@Override
	protected Connection getConnection() throws CRException {
		Connection conn = null;
		try {
			conn = ((CRConfigUtil) this.config).getPooledJDBCConnection();
		} catch (Exception e) {
			throw new CRException(e);
		}
		return conn;
	}
}
