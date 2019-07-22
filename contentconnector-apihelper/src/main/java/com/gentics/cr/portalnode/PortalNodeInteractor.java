package com.gentics.cr.portalnode;

import org.apache.log4j.Logger;

import com.gentics.api.lib.datasource.Datasource;
import com.gentics.api.lib.exception.NodeException;
import com.gentics.api.portalnode.connector.PortalConnectorFactory;
import com.gentics.lib.image.GenticsImageResizer;

/**
 * 
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class PortalNodeInteractor {

	private final static Logger LOG = Logger.getLogger(PortalNodeInteractor.class);

	/**
	 * Get a configured Instance of Datasource from a running Portal.Node Instance.
	 * @param key datasource identifyer e.g.: ccr, pcr,...
	 * @return get a new datasource instance created by createDatasource called on Portal#getCurrentPortal().
	 */
	public static Datasource getPortalnodeDatasource(final String key) {
		try {
			return PortalConnectorFactory.createDatasource(key);
		} catch (NodeException e) {
			LOG.error("Could not create datasource for key {" + key + "}", e);
		}
		return null;
	}

	/**
	 * Use Portal.Node functionality to resize Images.
	 * @param binary - the image data as byte array
	 * @param width - the desired with
	 * @param height - the desired height
	 * @param imageType - the image type e.g. png
	 * @return resized image (resized by GenticsImageResizer) as byte array.
	 */
	public static byte[] resizeImage(final byte[] binary, final int width,
			final int height, final String imageType) {
		return GenticsImageResizer.resize(binary, Math.max(width, 0),
				Math.max(height, 0), imageType);
	}
}
