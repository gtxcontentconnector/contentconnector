package com.gentics.cr;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.gentics.api.lib.datasource.DatasourceException;
import com.gentics.api.lib.datasource.DatasourceInfo;
import com.gentics.api.lib.datasource.WriteableDatasource;
import com.gentics.api.lib.expressionparser.filtergenerator.DatasourceFilter;
import com.gentics.api.lib.resolving.Changeable;
import com.gentics.api.lib.resolving.Resolvable;
import com.gentics.api.portalnode.connector.PortalConnectorFactory;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.testutils.AbstractTestHandler;
import com.gentics.cr.testutils.GenticsCRHelper;

public class HSQLTestHandler extends AbstractTestHandler {

	private final static Logger LOGGER = Logger.getLogger(HSQLTestHandler.class);

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

	@SuppressWarnings("unchecked")
	public CRResolvableBean createBean(CRResolvableBean bean, String [] attributes) throws CRException {
		Map<String, Object> map = bean.getAttrMap();
		map.put("obj_type", bean.getObj_type());
		try {
			Changeable changeable = wDs.create(map);
			DatasourceInfo result = wDs.store(Collections.singleton(changeable));
			Collection<Resolvable> records = result.getAffectedRecords();

			Iterator<Resolvable> it = records.iterator();
			if (it.hasNext()) {
				if(attributes != null) {
					bean = new CRResolvableBean(it.next(), attributes);
				} else {
					bean = new CRResolvableBean(it.next());
				}
			}

		} catch (DatasourceException e) {
			throw new CRException(e);
		}
		
		contentids.add(bean.getContentid());
		
		return bean;
	}

	public void cleanUp() throws CRException {
		DatasourceFilter dsFilter;
		try {
			CRResolvableBean base = new CRResolvableBean();
			base.set("contentids", contentids);
			dsFilter = wDs.createDatasourceFilter(PortalConnectorFactory
					.createExpression("object.contentid CONTAINSONEOF base.contentids"));
			dsFilter.addBaseResolvable("base", base);
			wDs.delete(dsFilter);
		} catch (Exception e) {
			throw new CRException(e);
		} finally {
			CRDatabaseFactory.releaseDatasource(wDs);
		}
	}
}
