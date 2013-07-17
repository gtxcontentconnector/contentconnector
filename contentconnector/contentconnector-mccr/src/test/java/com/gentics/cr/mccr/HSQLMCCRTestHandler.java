package com.gentics.cr.mccr;

import java.util.Collections;
import java.util.Map;
import java.util.Vector;

import com.gentics.api.lib.datasource.ChannelTree;
import com.gentics.api.lib.datasource.ChannelTreeNode;
import com.gentics.api.lib.datasource.DatasourceChannel;
import com.gentics.api.lib.datasource.DatasourceException;
import com.gentics.api.lib.datasource.WritableMultichannellingDatasource;
import com.gentics.api.lib.expressionparser.filtergenerator.DatasourceFilter;
import com.gentics.api.lib.resolving.Changeable;
import com.gentics.api.portalnode.connector.PortalConnectorFactory;
import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.CRDatabaseFactory;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.testutils.AbstractTestHandler;
import com.gentics.cr.testutils.GenticsCRHelper;
import com.gentics.lib.datasource.mccr.MCCRDatasource;
import com.gentics.lib.datasource.mccr.WritableMCCRDatasource;

public class HSQLMCCRTestHandler extends AbstractTestHandler{
	
	WritableMCCRDatasource wDs; 
	
	public HSQLMCCRTestHandler(CRConfigUtil rpConfig) throws CRException {
		rpConfig.set("ds.mccr", "true");
		wDs = (WritableMCCRDatasource) rpConfig.getDatasource();
		try {
			GenticsCRHelper.importObjectTypes((MCCRDatasource) wDs);
			setUpChannelStrcture();
		} catch (Exception e) {
			throw new CRException(e);
		}
	}
	
	private void setUpChannelStrcture() throws DatasourceException {
		// create and save the channel structure
		ChannelTree tree = new ChannelTree();
		ChannelTreeNode master = new ChannelTreeNode(new DatasourceChannel(1, "Master"));
		tree.getChildren().add(master);
		ChannelTreeNode channel = new ChannelTreeNode(new DatasourceChannel(2, "Channel"));
		master.getChildren().add(channel);
		ChannelTreeNode master2 = new ChannelTreeNode(new DatasourceChannel(3, "Master 2"));
		tree.getChildren().add(master2);
		wDs.saveChannelStructure(tree);
	}

	private Vector<String> contentids = new Vector<String>();
	
	public void createBean(CRResolvableBean bean, int channelid, int channelSetId) throws CRException{
		Map<String, Object> map = bean.getAttrMap();
		map.put("contentid", bean.getContentid());
		map.put("obj_type", bean.getObj_type());
		map.put(WritableMCCRDatasource.MCCR_CHANNELSET_ID, channelSetId);
		map.put(WritableMultichannellingDatasource.MCCR_CHANNEL_ID, channelid);
		try {
			wDs.setChannel(channelid);
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
