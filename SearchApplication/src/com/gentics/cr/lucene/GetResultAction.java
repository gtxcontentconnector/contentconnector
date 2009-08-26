package com.gentics.cr.lucene;

import java.util.Collection;

import org.apache.log4j.Logger;

import com.gentics.api.portalnode.action.GenericPluggableAction;
import com.gentics.api.portalnode.action.PluggableActionException;
import com.gentics.api.portalnode.action.PluggableActionRequest;
import com.gentics.api.portalnode.action.PluggableActionResponse;
import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.CRException;
import com.gentics.cr.CRRequest;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.RequestProcessor;
import com.gentics.cr.StaticRPContainer;
import com.gentics.cr.configuration.StaticConfigurationContainer;


public class GetResultAction extends GenericPluggableAction {
	
	Logger logger = Logger.getLogger("GetResultAction");

	public boolean processAction(PluggableActionRequest req, PluggableActionResponse res) throws PluggableActionException {
		
		String query = (String)req.getParameter("query");
		
		if(query!=null)
		{
			try
			{
				long s1 = System.currentTimeMillis();
				CRConfigUtil config = StaticConfigurationContainer.getConfig(this.getModule().getModuleId(), null);
				RequestProcessor rp = StaticRPContainer.getRP(config, 1);
				long e1 = System.currentTimeMillis();
				System.out.println("INSTANTIATE TOOK "+(e1-s1)+"ms");
				CRRequest rq = new CRRequest();
				rq.setRequestFilter(query);
				rq.set("secondary", "true");
				rq.setAttributeArray(new String[]{"content","binarycontent","editor","name"});
				Collection<CRResolvableBean> coll = rp.getObjects(rq);
				res.setParameter("result", coll);
				return true;
			}
			catch(CRException ex)
			{
				logger.error(ex.getMessage());
				ex.printStackTrace();
			}
		}
		
		
		return false;
	}
}
