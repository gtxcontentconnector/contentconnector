package com.gentics.cr.lucene;

import java.util.Collection;
import java.util.Iterator;

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
import com.gentics.cr.lucene.search.LuceneRequestProcessor;


public class GetResultAction extends GenericPluggableAction {
	
	private static final Logger logger = Logger.getLogger(GetResultAction.class);

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
				logger.debug("INSTANTIATE TOOK "+(e1-s1)+"ms");
				long s2 = System.currentTimeMillis();
				CRRequest rq = new CRRequest();
				rq.setRequestFilter(query);
				rq.set("secondary", "true");
				rq.set(LuceneRequestProcessor.META_RESOLVABLE_KEY, true);
				rq.setAttributeArray(new String[]{"content","binarycontent","editor","name"});
				Collection<CRResolvableBean> coll = rp.getObjects(rq);
				if(coll!=null)
				{
					Iterator<CRResolvableBean> it = coll.iterator();
					if(it.hasNext())
					{
						CRResolvableBean meta = it.next();
						it.remove();
						res.setParameter("hits", meta.get(LuceneRequestProcessor.META_HITS_KEY));
					}
				}
				long e2 = System.currentTimeMillis();
				logger.debug("Search took "+(e2-s2)+"ms");
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
