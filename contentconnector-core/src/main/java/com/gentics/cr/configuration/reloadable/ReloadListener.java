package com.gentics.cr.configuration.reloadable;

import com.gentics.cr.configuration.GenericConfiguration;

/**
 * Operates as an Interface between the servlet and the Indexer Engine
 * Last changed: $Date: 2009-09-02 17:57:48 +0200 (Mi, 02 Sep 2009) $
 * @version $Revision: 180 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public interface ReloadListener {
	/**
	 * This Method will be executed by the configuration before it reloads
	 */
	public void onBeforeReload();
	
	/**
	 * This Method will be executed by the configuration after it finished reloading
	 * @param newConfiguration - the reloaded configuration
	 * 
	 */
	public void onReloadFinished(GenericConfiguration newConfiguration);
}
