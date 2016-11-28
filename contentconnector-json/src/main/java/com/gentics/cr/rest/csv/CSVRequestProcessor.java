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
import java.util.Vector;

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
	 * @param config - configuration of the request processor. Contains the separator value of the CSV data
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
			String line;

			String csvFile = config.getString("file");
			String separator = config.getString("separator");

			int count = 0;

			if (csvFile != null) {
				try {
					br = new BufferedReader(new FileReader(csvFile));
					while ((line = br.readLine()) != null) {
						if (count == 0) {
							attributes = line.split(separator);
						} else { // other rows
							CRResolvableBean bean = new CRResolvableBean();

							String[] data = line.split(separator);
							if (data.length > 1) {
								int i = 0;
								for (String d : data) {
									bean.set(attributes[i], d);
									i++;
								}
								objects.add(bean);
							}
						}
						count++;
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
