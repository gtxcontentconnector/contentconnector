package com.gentics.cr.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Properties;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDriver;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;

import com.gentics.cr.CRConfig;
import com.gentics.cr.CRConfigUtil;
import com.gentics.lib.log.NodeLogger;

/**
 * 
 * Last changed: $Date: 2010-04-01 15:24:02 +0200 (Do, 01 Apr 2010) $
 * @version $Revision: 541 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class ConnectionProvider {
	private static NodeLogger log = NodeLogger.getNodeLogger(ConnectionProvider.class);
	private static HashMap<String, Boolean> connectionpools;

	/**
	  * Get a pooled JDBCConnection and create a new pool if none exists.
	  * Such a direct connection can be used for custom request processors that require a pooled connection
	  * 
	  * ATTENTION: The pool has to be released when the application shuts down using the releaseJDBCPool method
	  * 
	  * @throws Exception
	  */
	public static synchronized Connection getPooledJDBCConnection(CRConfigUtil config) throws Exception {
		//CREATE CONNECTION POOL
		String name = getName(config);
		if (!isPoolCreated(name)) {
			Properties props = config.getDatasourceHandleProperties();

			String connectionuri = props.getProperty("url");
			String driverclass = props.getProperty("driverClass");
			try {
				Class.forName(driverclass);
			} catch (ClassNotFoundException e) {
				log.error("Could not load driver class.", e);
			}
			setupPoolingDriver(connectionuri, name);
			setPoolCreated(name);
		}
		return DriverManager.getConnection("jdbc:apache:commons:dbcp:" + name);
		
	}
	
	/**
	 * Get Name for pool.
	 * @param config
	 * @return
	 */
	public static String getName(CRConfig config) {
		String name = config.getName();
		if (name != null) {
			name = name.replaceAll("\\.", "_");
		}
		return name;
	}

	/**
	   * Releases the JDBC Connection pool that was used by any connection from getPooledJDBCConnection.
	   * @throws Exception
	   */
	public static synchronized void releaseJDBCPool(CRConfigUtil config) throws Exception {
		String name = getName(config);
		if (isPoolCreated(name)) {
			try {
				PoolingDriver driver = (PoolingDriver) DriverManager.getDriver("jdbc:apache:commons:dbcp:");
				driver.closePool(name);
				setPoolReleased(name);
				log.debug("Connection pool for " + name + " has been released.");
			} catch (Exception e) {
				log.error("Could not unload JDBCPool " + name, e);
			}
		} else {
			log.error("JDBCPool " + name + " could not be found.");
		}
	}

	private static synchronized void setupPoolingDriver(String connectionURI, String name) throws Exception {

		//
		// First, we'll need a ObjectPool that serves as the
		// actual pool of connections.
		//
		// We'll use a GenericObjectPool instance, although
		// any ObjectPool implementation will suffice.
		//
		ObjectPool connectionPool = new GenericObjectPool(null);

		//
		// Next, we'll create a ConnectionFactory that the
		// pool will use to create Connections.
		// We'll use the DriverManagerConnectionFactory,
		// using the connect string passed in the command line
		// arguments.
		//
		ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(connectionURI, null);

		//
		// Now we'll create the PoolableConnectionFactory, which wraps
		// the "real" Connections created by the ConnectionFactory with
		// the classes that implement the pooling functionality.
		//
		@SuppressWarnings("unused")
		PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory,
				connectionPool, null, null, false, true);
		
		//
		// Finally, we create the PoolingDriver itself...
		//
		Class.forName("org.apache.commons.dbcp.PoolingDriver");
		PoolingDriver driver = (PoolingDriver) DriverManager.getDriver("jdbc:apache:commons:dbcp:");

		//
		// ...and register our pool with it.
		//
		driver.registerPool(name, connectionPool);
		log.debug("Connection pool for " + name + " has been set up.");
		// Now we can just use the connect string "jdbc:apache:commons:dbcp:example"
		// to access our pool of Connections.
		//	    
	}

	private static synchronized boolean isPoolCreated(String name) {
		if (connectionpools == null)
			return false;
		Boolean b = connectionpools.get(name);
		if (b == null)
			return false;
		return b.booleanValue();
	}

	private static synchronized void setPoolCreated(String name) {
		if (connectionpools == null)
			connectionpools = new HashMap<String, Boolean>();
		connectionpools.put(name, true);
	}

	private static synchronized void setPoolReleased(String name) {
		if (connectionpools != null) {
			connectionpools.remove(name);
		}
	}
}
