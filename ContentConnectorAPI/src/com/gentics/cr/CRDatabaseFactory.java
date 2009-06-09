package com.gentics.cr;

import org.apache.log4j.Logger;

import com.gentics.api.lib.datasource.Datasource;
import com.gentics.api.portalnode.connector.PortalConnectorFactory;
import com.gentics.portalnode.portal.Portal;
/**
 * 
 * Last changed: $Date: 2009-05-28 09:10:54 +0200 (Do, 28 Mai 2009) $
 * @version $Revision: 62 $
 * @author $Author: bigbear.ap $
 *
 */
public class CRDatabaseFactory {
	private static Logger log = Logger.getLogger(CRConfigUtil.class);
	/**
	 * Gets a Datasource instance that ins configured within the given requestProcessorConfig
	 * @param requestProcessorConfig containing the datasource config
	 * @return Datasource if correctly configured, otherwise null
	 */
	public static Datasource getDatasource(CRConfigUtil requestProcessorConfig)
	{
		Datasource ds = null;
		
		if(requestProcessorConfig.handle_props!=null && requestProcessorConfig.handle_props.size()!=0)
		{
			if(requestProcessorConfig.handle_props.containsKey("portalnodedb"))
			{
				String key = (String)requestProcessorConfig.handle_props.get("portalnodedb");
				ds = Portal.getCurrentPortal().createDatasource(key);
			}
			else if(requestProcessorConfig.dsprops!=null && requestProcessorConfig.dsprops.size()!=0)
			{
				ds = PortalConnectorFactory.createWriteableDatasource(requestProcessorConfig.handle_props,requestProcessorConfig.dsprops);
			}
			else
			{
				ds = PortalConnectorFactory.createWriteableDatasource(requestProcessorConfig.handle_props);
			}	
			log.debug("Datasource created for "+requestProcessorConfig.getName());
		}
		else
		{
			log.debug("No Datasource created for"+requestProcessorConfig.getName());
		}
		return(ds);
	}
}
