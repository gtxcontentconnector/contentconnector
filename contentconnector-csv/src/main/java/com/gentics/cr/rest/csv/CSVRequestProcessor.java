package com.gentics.cr.rest.csv;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import com.gentics.cr.CRConfig;
import com.gentics.cr.CRRequest;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.RequestProcessor;
import com.gentics.cr.exceptions.CRException;

/**
 * Request Processor to create simple structures via CSV.<br />
 * Needs a root json object that has an array of other objects in his object
 * attribute:
 * 
 * @author jmaina@gentics.com
 */
public class CSVRequestProcessor extends RequestProcessor {

	/**
	 * Configuration of the request processor.
	 */
	CRConfig conf;

	/**
	 * Objects initialized from the CSV file.
	 */
	Collection<CRResolvableBean> objects;

	/**
	 * column names of the csv file
	 */
	private String[] attributes;

	/**
	 * initialize a new {@link CSVRequestProcessor}
	 * 
	 * @param config - configuration of the request processor
	 * contains the path of CSV file and separator of the data
	 * @throws CRException - if the config wasn't valid.
	 */
	public CSVRequestProcessor(CRConfig config) throws CRException {
		super(config);
		conf = config;
	}

	@Override
	public void finalize() {
		// TODO Auto-generated method stub

	}

	@Override
	public Collection<CRResolvableBean> getObjects(CRRequest request,
			boolean doNavigation) throws CRException {
		initObjects();
		return objects;
	}

	/**
	 * init the CSV file and convert it into {@link CRResolvableBean}s.
	 *  
	 * @throws CRException
	 */
	private synchronized void initObjects() throws CRException {
		if (objects == null) {
			objects = new Vector<CRResolvableBean>();

			BufferedReader br = null;
			
			String csvFile = config.getString("file");// name of csv file
			String s = config.getString("separator");			
			char separator = (s == null) ? ',' : s.charAt(0);// separator 
			
			String es = config.getString("escape");
			char escape = (es == null) ? '\\' : es.charAt(0);

			if (csvFile != null) {
				try {
					br = new BufferedReader(new FileReader(csvFile));
					Iterable<CSVRecord> records = CSVFormat.DEFAULT
							.withFirstRecordAsHeader()
							.withDelimiter(separator)
							.withEscape(escape)
							.withTrim()
							.parse(new FileReader(csvFile));
					
					for (CSVRecord record : records) {
						CRResolvableBean bean = new CRResolvableBean();
						for(String key: record.toMap().keySet()) {							
							bean.set(key, record.toMap().get(key));
						}
						objects.add(bean);
					}										
				} catch (FileNotFoundException fe) {
					throw new CRException("Cannot find the given CSV file.", fe);
				} catch (IOException ie) {
					throw new CRException("Error while reading CSV file.", ie);
				} finally {
					if (br != null) {
						try {
							br.close();
						} catch (IOException e) {

						}
					}
				}
			}			
		}
	}
}
