package com.gentics.cr;

import java.util.Properties;

import org.apache.log4j.Logger;

import com.gentics.api.lib.datasource.Datasource;
import com.gentics.api.portalnode.connector.PortalConnectorFactory;
import com.gentics.portalnode.portal.Portal;
/**
 * 
 * Last changed: $Date$
 * @version $Revision$
 * @author $Author$
 *
 */
public class CRDatabaseFactory {
	private static Logger log = Logger.getLogger(CRDatabaseFactory.class);
	/**
	 * Gets a Datasource instance that ins configured within the given requestProcessorConfig
	 * @param requestProcessorConfig containing the datasource config
	 * @return Datasource if correctly configured, otherwise null
	 */
	public static Datasource getDatasource(CRConfigUtil requestProcessorConfig)
	{
		Datasource ds = null;
		Properties ds_handle = requestProcessorConfig.getDatasourceHandleProperties();
		Properties ds_props = requestProcessorConfig.getDatasourceProperties();
		if(ds_handle!=null && ds_handle.size()!=0)
		{
			if(ds_handle.containsKey("portalnodedb"))
			{
				String key = (String)ds_handle.get("portalnodedb");
				ds = Portal.getCurrentPortal().createDatasource(key);
			}
			else if(ds_props!=null && ds_props.size()!=0)
			{
				ds = PortalConnectorFactory.createWriteableDatasource(ds_handle,ds_props);
			}
			else
			{
				ds = PortalConnectorFactory.createWriteableDatasource(ds_handle);
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
