package com.gentics.cr;

import java.util.Collections;
import java.util.Map;
import java.util.Vector;

import com.gentics.api.lib.datasource.DatasourceException;
import com.gentics.api.lib.datasource.WriteableDatasource;
import com.gentics.api.lib.expressionparser.filtergenerator.DatasourceFilter;
import com.gentics.api.lib.resolving.Changeable;
import com.gentics.api.portalnode.connector.PortalConnectorFactory;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.testutils.GenticsCRHelper;

public class HSQLTestHandler extends AbstractTestHandler{
	
	WriteableDatasource wDs; 
	
	public HSQLTestHandler(CRConfigUtil rpConfig) throws CRException {
		wDs = (WriteableDatasource) rpConfig.getDatasource();
		try {
			GenticsCRHelper.importObjectTypes(wDs);
		} catch (Exception e) {
			throw new CRException(e);
		}
	}

	private Vector<String> contentids = new Vector<String>();
	
	public void createBean(CRResolvableBean bean) throws CRException{
		Map<String, Object> map = bean.getAttrMap();
		map.put("obj_type", bean.getObj_type());
		try {
			Changeable changeable = wDs.create(map);
			wDs.store(Collections.singleton(changeable));
		} catch (DatasourceException e) {
			throw new CRException(e);
		}
		contentids.add(bean.getContentid());
	}
	
	public void cleanUp() throws CRException {
		DatasourceFilter dsFilter;
		try {
			CRResolvableBean base = new CRResolvableBean();
			base.set("contentids", contentids);
			dsFilter = wDs.createDatasourceFilter(PortalConnectorFactory.createExpression("object.contentid CONTAINSONEOF base.contentids"));
			dsFilter.addBaseResolvable("base", base);
			wDs.delete(dsFilter);
		} catch (Exception e) {
			throw new CRException(e);
		} finally {
			CRDatabaseFactory.releaseDatasource(wDs);
		}
	}
}
