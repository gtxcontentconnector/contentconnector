package com.gentics.cr;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.testutils.AbstractTestHandler;


public class HSQLTestHandler extends AbstractTestHandler {
	
	/**
	 * configuration key for the datasource driver class.
	 */
	private static final String DSHDRIVERCLASS_KEY = "driverClass";
	/**
	 * configuration key for the datasource url.
	 */
	private static final String DSHURL_KEY = "url";
	
	private String table;
	private String attribInsertString;
	private CRConfigUtil rpConfig;
	
	private Collection<String> attributes = new ArrayList<String>();
	
	public HSQLTestHandler(CRConfigUtil rpConfig, String table, String[] attributeConfig) throws CRException {
		this.table = table;
		this.rpConfig = rpConfig;
		Connection conn = getConnection(rpConfig);
		PreparedStatement ps;
		try {
			ps = conn.prepareCall("CREATE TABLE " + table + " (" + getAttributeCreateString(attributeConfig) + ")");
			ps.executeUpdate();
			conn.close();
		} catch (Exception e) {
			throw new CRException (e);
		}
	}
	
	private String getAttributeCreateString(String[] attributeConfig) {
		StringBuffer attribString = new StringBuffer();
		StringBuffer attribInsertParams = new StringBuffer();
		for (String attribute:attributeConfig) {
			String[] a = attribute.split(":");
			attributes.add(a[0]);
			if (attribString.length() > 0) {
				attribString.append(", ");
				attribInsertParams.append(",");
			}
			attribInsertParams.append("?");
			attribString.append(a[0]);
			attribString.append(" ");
			attribString.append(a[1]);
		}
		attribInsertString = attribInsertParams.toString();
		return attribString.toString();
	}
	
	private Connection getConnection(CRConfigUtil config) throws CRException {
		Connection conn = null;
		try {
			Properties datasourceHandleProperties = config.getDatasourceHandleProperties();
			Class.forName(datasourceHandleProperties.getProperty(DSHDRIVERCLASS_KEY));
			conn = DriverManager.getConnection(datasourceHandleProperties.getProperty(DSHURL_KEY));
		} catch (Exception ex) {
			throw new CRException(ex);
		}
		return conn;
	}

	public CRResolvableBean createBean(CRResolvableBean bean) throws CRException {
		
		StringBuffer stmt = new StringBuffer();
		stmt.append("INSERT INTO ");
		stmt.append(table);
		stmt.append(" VALUES (");
		stmt.append(attribInsertString);
		stmt.append(")");
		
		Connection conn = getConnection(rpConfig);
		PreparedStatement ps;
		try {
			ps = conn.prepareStatement(stmt.toString());
		
			int index = 1;
			for (String attribute : attributes) {
				ps.setString(index, bean.getString(attribute));
				index++;
			}
			ps.executeUpdate();
			conn.close();
		} catch (SQLException e) {
			throw new CRException(e);
		}
		
		return bean;
	}

	public void cleanUp() throws CRException {
		Connection conn = getConnection(rpConfig);
		PreparedStatement ps;
		try {
			ps = conn.prepareCall("DROP TABLE " + table);
			ps.executeUpdate();
			conn.close();
		} catch (Exception e) {
			throw new CRException (e);
		}
	}
}
