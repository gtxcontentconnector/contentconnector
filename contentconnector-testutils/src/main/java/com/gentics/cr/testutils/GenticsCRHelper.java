package com.gentics.cr.testutils;

import java.io.InputStream;

import com.gentics.api.lib.datasource.WriteableDatasource;
import com.gentics.lib.datasource.CNDatasource;
import com.gentics.lib.datasource.mccr.MCCRDatasource;
import com.gentics.portalnode.genericmodules.admin.ObjectManagementException;
import com.gentics.portalnode.genericmodules.admin.ObjectManagementManager;

/**
 * ATTENTION!
 * This Class uses non-API methods and should only be used for tests.
 * @author Christopher
 *
 */
public final class GenticsCRHelper {
	/**
	 * Private Construtor.
	 */
	private GenticsCRHelper() { }
	
	/**
	 * This method can import datatypes from an xml
	 * file into a contentrepository.
	 * Do not use this method in production code.
	 * This method is only for tests.
	 * @param ds WriteableDatasource that can be cast to CNDatasource.
	 * @throws Exception in case of ObjectManagementError.
	 */
	public static void importObjectTypes(WriteableDatasource ds) throws Exception {
		importObjectTypes(ds, GenticsCRHelper.class.getResourceAsStream("dsstructure.xml"));
	}
	
	/**
	 * This method can import datatypes from an xml
	 * file into a contentrepository.
	 * Do not use this method in production code.
	 * This method is only for tests.
	 * @param ds WriteableDatasource that can be cast to CNDatasource.
	 * @throws Exception in case of ObjectManagementError.
	 */
	public static void importObjectTypes(MCCRDatasource ds) throws Exception {
		System.err.println("Loading dsstructure.xml from:: " + GenticsCRHelper.class.getResource("dsstructure.xml"));
		importObjectTypes(ds, GenticsCRHelper.class.getResourceAsStream("dsstructure.xml"));
	}
	
	/**
	 * Import the object types
	 * @param ds datasource
	 * @throws ObjectManagementException
	 */
	public static void importObjectTypes(MCCRDatasource ds, InputStream stream) throws ObjectManagementException {
		ObjectManagementManager.importTypes(ds, stream);
	}
	
	/**
	 * This method can import datatypes from an xml
	 * file into a contentrepository.
	 * Do not use this method in production code.
	 * This method is only for tests.
	 * @param ds WriteableDatasource that can be cast to CNDatasource.
	 * @throws Exception in case of ObjectManagementError.
	 */
	public static void importObjectTypes(WriteableDatasource ds, InputStream stream) throws Exception {
		CNDatasource cnds = (CNDatasource) ds;
		try {
			ObjectManagementManager.importTypes((CNDatasource) ds, stream);
		} catch (ObjectManagementException e) {
			throw new Exception(e);
		}
	}
}
