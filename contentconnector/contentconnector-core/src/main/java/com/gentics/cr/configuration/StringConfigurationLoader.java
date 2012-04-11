package com.gentics.cr.configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Map.Entry;

import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.util.CRUtil;

/**
 * Class that loads a <code>String</code> into the configuraiton.
 * Last changed: $Date: 2009-06-22 17:49:58 +0200 (Mo, 22 Jun 2009) $
 * @version $Revision: 95 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class StringConfigurationLoader extends CRConfigUtil {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7725953433422855180L;

	/**
	 * Loads the configuration from a String formated like a java
	 *  properties file.
	 * @param configstring <code>String</code> containing the configuration in
	 *  properties form
	 * @throws IOException if String could not be read properly.
	 */
	public final void load(final String configstring) throws IOException {
		if (configstring != null) {
			Properties props = new Properties();
			props.load(new ByteArrayInputStream(configstring.getBytes()));
			for (Entry<Object, Object> e : props.entrySet()) {
				String value = CRUtil.resolveSystemProperties((String) e.getValue());
				this.set((String) e.getKey(), value);
			}
		}
	}

}
