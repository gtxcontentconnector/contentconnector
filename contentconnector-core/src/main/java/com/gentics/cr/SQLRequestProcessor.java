package com.gentics.cr;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.gentics.cr.exceptions.CRException;

public class SQLRequestProcessor extends RequestProcessor {
	private static Logger logger = Logger.getLogger(SQLRequestProcessor.class);

	private static final String DSHDRIVERCLASS = "driverClass";
	private static final String DSHURL = "url";
	private static final String TABLEATTRIBUTE = "table";
	private static final String COLUMNATTRIBUTE = "columns";
	private static final String IDCOLUMNKEY = "idcolumn";
	
	private String dshDriverClass = "";
	private String dshUrl = "";
	private String table = "";
	private String[] columns = new String[]{};
	private String idcolumn="";
	
	
	/**
	* Create a new instance of SQLRequestProcessor
	* @param config
	* @throws CRException
	*/
	public SQLRequestProcessor (CRConfig config) throws CRException {
		super(config);
		
		Properties dshprop = ((CRConfigUtil)config).getDatasourceHandleProperties();
		dshDriverClass = dshprop.getProperty(DSHDRIVERCLASS);
		dshUrl = dshprop.getProperty(DSHURL);
		
		Properties dsprops = ((CRConfigUtil)config).getDatasourceProperties();
		table = dsprops.getProperty(TABLEATTRIBUTE);
		
		String colatt = dsprops.getProperty(COLUMNATTRIBUTE);
		if (colatt != null) {
			columns = colatt.split(",");
		}
		 
		idcolumn = dsprops.getProperty(IDCOLUMNKEY);
	}
	
	private static final Pattern CONTAINSONEOFPATTERN = Pattern.compile("object\\.([a-zA-Z0-9_]*)[ ]*CONTAINSONEOF[ ]*\\[(.*)\\]");

	private String translate(String requestFilter)
	{
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

	private String getStatement(String requestFilter, String[] attributes)
	{
		String statement = new String();
		if(attributes == null || attributes.length == 0 || this.columns.length == 0) {
			statement = "*";
		} else {
			for(String att:attributes) {
				if( Arrays.asList(this.columns).contains(att)) {
					if(!statement.isEmpty()) 
						statement += ",";
					statement += att;
				}
			}
			
			
		}
		statement = "SELECT " + statement + " FROM " + this.table + " WHERE " + translate(requestFilter);
		return statement;
	}
	
	/**
	*
	* getObjects 
	* @param request CRRequest
	* @param doNavigation boolean
	* @return resulting objects
	* @throws CRException
	*/
	@Override
	public Collection<CRResolvableBean> getObjects(CRRequest request, boolean doNavigation) throws CRException {
		ArrayList<CRResolvableBean> result = new ArrayList<CRResolvableBean>();

		Statement stmt = null;
		ResultSet rset = null;
		Connection conn = null;
		
		try {
			Class.forName(this.dshDriverClass);
			conn = DriverManager.getConnection(this.dshUrl);

			stmt = conn.createStatement();
			String s_statement = getStatement(request.getRequestFilter(),request.getAttributeArray(idcolumn));
			logger.debug("Using statement: " + s_statement);
			rset = stmt.executeQuery(s_statement);

			int numcols = rset.getMetaData().getColumnCount();
			String[] colnames = new String[numcols];

			for(int i=1;i<=numcols;i++) {
				colnames[i-1] = rset.getMetaData().getColumnName(i);
			}

			while(rset.next()) {
				CRResolvableBean bean = new CRResolvableBean();
				for(int i=1; i<=numcols; i++) {
					if (rset.getObject(i) != null)
						bean.set(colnames[i-1], rset.getObject(i));
				}
				result.add(bean);
			}
		} catch(SQLException e) {
			e.printStackTrace();
		} catch(Exception ex){
			ex.printStackTrace();
		}
		finally {
			try { if (rset != null) rset.close(); } catch(Exception e) { }
			try { if (stmt != null) stmt.close(); } catch(Exception e) { }
			try { if (conn != null) conn.close(); } catch(Exception e) { }
		}
		return result;
	}

	@Override
	public void finalize() {
		// TODO Auto-generated method stub
	}
}