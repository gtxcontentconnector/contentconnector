package com.gentics.cr;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.gentics.cr.exceptions.CRException;

/**
 * 
 * Last changed: $Date: 2010-04-01 15:24:02 +0200 (Do, 01 Apr 2010) $
 * @version $Revision: 541 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class PooledSQLRequestProcessor extends RequestProcessor {

	private static Logger log = Logger.getLogger(PooledSQLRequestProcessor.class);
	private static final String TABLEATTRIBUTE = "table";
	private static final String IDCOLUMNKEY = "idcolumn";

	private String table = "";
	private String idcolumn = "";

	public PooledSQLRequestProcessor(CRConfig config) throws CRException {
		super(config);
		Properties dsprops = ((CRConfigUtil) config).getDatasourceProperties();
		table = dsprops.getProperty(TABLEATTRIBUTE);
		idcolumn = dsprops.getProperty(IDCOLUMNKEY);
	}

	@Override
	public void finalize() {
		try {
			((CRConfigUtil) this.config).releaseJDBCPool();
		} catch (Exception s) {
			log.error("Could not release connection pool.", s);
		}

	}

	private static final Pattern CONTAINSONEOFPATTERN = Pattern
			.compile("object\\.([a-zA-Z0-9_]*)[ ]*CONTAINSONEOF[ ]*\\[(.*)\\]");

	private String translate(String requestFilter) {
		//TANSLATE CONTAINSONEOF
		Matcher matcher = CONTAINSONEOFPATTERN.matcher(requestFilter);

		StringBuffer buf = new StringBuffer();
		while (matcher.find()) {
			String attrib = matcher.group(1);
			String group = matcher.group(2);
			matcher.appendReplacement(buf, attrib + " IN (" + group + ")");
		}
		matcher.appendTail(buf);

		requestFilter = buf.toString();

		return requestFilter.replaceAll("==", "=").replaceAll("\"", "'");

	}

	private String getStatement(String requestFilter, String[] attributes) {
		String statement = "SELECT ";
		if (attributes == null || attributes.length == 0) {
			statement += "*";
		} else {
			boolean first = true;
			for (String att : attributes) {
				if (!first) {
					statement += ",";
				} else {
					first = false;
				}
				statement += att;
			}
		}
		statement += " FROM " + this.table + " WHERE " + translate(requestFilter);
		return statement;
	}

	@Override
	public Collection<CRResolvableBean> getObjects(CRRequest request, boolean doNavigation) throws CRException {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rset = null;
		ArrayList<CRResolvableBean> result = new ArrayList<CRResolvableBean>();
		try {

			conn = ((CRConfigUtil) this.config).getPooledJDBCConnection();

			stmt = conn.createStatement();
			String s_statement = getStatement(request.getRequestFilter(), request.getAttributeArray(idcolumn));
			log.debug("Using statement: " + s_statement);
			rset = stmt.executeQuery(s_statement);

			int numcols = rset.getMetaData().getColumnCount();
			String[] colnames = new String[numcols];

			for (int i = 1; i <= numcols; i++) {
				colnames[i - 1] = rset.getMetaData().getColumnName(i);
			}

			while (rset.next()) {
				CRResolvableBean bean = new CRResolvableBean();
				for (int i = 1; i <= numcols; i++) {
					if (rset.getObject(i) != null)
						bean.set(colnames[i - 1], rset.getObject(i));
				}
				result.add(bean);
			}
		} catch (SQLException e) {
			throw new CRException(e);
		} catch (Exception ex) {
			throw new CRException(ex);
		} finally {
			try {
				if (rset != null)
					rset.close();
			} catch (Exception e) {
			}
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
			}
			try {
				if (conn != null)
					conn.close();
			} catch (Exception e) {
			}
		}
		return result;
	}
}
