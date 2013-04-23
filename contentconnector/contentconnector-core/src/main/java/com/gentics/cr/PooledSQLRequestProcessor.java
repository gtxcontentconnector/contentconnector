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
 * PooledSQLRequestProcessor.
 * Last changed: $Date: 2010-04-01 15:24:02 +0200 (Do, 01 Apr 2010) $
 * @version $Revision: 541 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class PooledSQLRequestProcessor extends RequestProcessor {

	/**
	 * Logger.
	 */
	private static Logger log = Logger.getLogger(PooledSQLRequestProcessor.class);
	/**
	 * Table key.
	 */
	private static final String TABLEATTRIBUTE = "table";
	/**
	 * ID-Column key.
	 */
	private static final String IDCOLUMNKEY = "idcolumn";

	/**
	 * Table.
	 */
	private String table = "";
	/**
	 * ID-Column.
	 */
	private String idcolumn = "";

	/**
	 * Constructor. Create a new PooledSQLRequestProcessor.
	 * @param config configuration
	 * @throws CRException exception in case of error.
	 */
	public PooledSQLRequestProcessor(final CRConfig config) throws CRException {
		super(config);
		Properties dsprops = ((CRConfigUtil) config).getDatasourceProperties();
		table = dsprops.getProperty(TABLEATTRIBUTE);
		idcolumn = dsprops.getProperty(IDCOLUMNKEY);
	}

	@Override
	public final void finalize() {
		try {
			((CRConfigUtil) this.config).releaseJDBCPool();
		} catch (Exception s) {
			log.error("Could not release connection pool.", s);
		}

	}

	/**
	 * Pattern.
	 */
	private static final Pattern CONTAINSONEOFPATTERN = Pattern.compile("object\\.([a-zA-Z0-9_]*)[ ]*CONTAINSONEOF[ ]*\\[(.*)\\]");

	/**
	 * Translate.
	 * @param requestFilter request filter
	 * @return translation
	 */
	private String translate(final String requestFilter) {
		//TANSLATE CONTAINSONEOF
		Matcher matcher = CONTAINSONEOFPATTERN.matcher(requestFilter);

		StringBuffer buf = new StringBuffer();
		while (matcher.find()) {
			String attrib = matcher.group(1);
			String group = matcher.group(2);
			matcher.appendReplacement(buf, attrib + " IN (" + group + ")");
		}
		matcher.appendTail(buf);

		String buffer = buf.toString();

		return buffer.replaceAll("==", "=").replaceAll("\"", "'");

	}

	/**
	 * Create Statement.
	 * @param requestFilter requestFIlter
	 * @param attributes attributes
	 * @return statement
	 */
	private String getStatement(final String requestFilter, final String[] attributes) {
		StringBuilder statement = new StringBuilder();
		statement.append("SELECT ");
		if (attributes == null || attributes.length == 0) {
			statement.append("*");
		} else {
			boolean first = true;
			for (String att : attributes) {
				if (!first) {
					statement.append(",");
				} else {
					first = false;
				}
				statement.append(att);
			}
		}
		statement.append(" FROM " + this.table + " WHERE " + translate(requestFilter));
		return statement.toString();
	}

	@Override
	public final Collection<CRResolvableBean> getObjects(final CRRequest request, final boolean doNavigation) throws CRException {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rset = null;
		ArrayList<CRResolvableBean> result = new ArrayList<CRResolvableBean>();
		try {

			conn = ((CRConfigUtil) this.config).getPooledJDBCConnection();

			stmt = conn.createStatement();
			String stringStatement = getStatement(request.getRequestFilter(), request.getAttributeArray(idcolumn));
			log.debug("Using statement: " + stringStatement);
			rset = stmt.executeQuery(stringStatement);

			int numcols = rset.getMetaData().getColumnCount();
			String[] colnames = new String[numcols];

			for (int i = 1; i <= numcols; i++) {
				colnames[i - 1] = rset.getMetaData().getColumnName(i);
			}

			while (rset.next()) {
				CRResolvableBean bean = new CRResolvableBean();
				for (int i = 1; i <= numcols; i++) {
					if (rset.getObject(i) != null) {
						bean.set(colnames[i - 1], rset.getObject(i));
					}
				}
				result.add(bean);
			}
		} catch (SQLException e) {
			throw new CRException(e);
		} catch (Exception ex) {
			throw new CRException(ex);
		} finally {
			try {
				if (rset != null) {
					rset.close();
				}
				if (stmt != null) {
					stmt.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				throw new CRException(e);
			}
		}
		return result;
	}
}
