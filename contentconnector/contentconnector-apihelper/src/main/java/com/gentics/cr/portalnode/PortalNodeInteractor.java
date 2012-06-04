package com.gentics.cr.portalnode;

import com.gentics.api.lib.datasource.Datasource;
import com.gentics.api.portalnode.portlet.GenticsPortlet;
import com.gentics.api.portalnode.templateengine.TemplateProcessor;
import com.gentics.lib.image.GenticsImageResizer;
import com.gentics.portalnode.portal.Portal;

/**
 * 
 * Last changed: $Date: 2009-06-26 15:48:16 +0200 (Fr, 26 Jun 2009) $
 * @version $Revision: 105 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class PortalNodeInteractor {

	/**
	 * Get a configured Instance of Datasource from a running Portal.Node Instance.
	 * @param key datasource identifyer e.g.: ccr, pcr,...
	 * @return get a new datasource instance created by createDatasource called on Portal#getCurrentPortal().
	 */
	public static Datasource getPortalnodeDatasource(final String key) {
		return (Portal.getCurrentPortal().createDatasource(key));
	}

	/**
	 * Get a TemplateProcessor from a running Portal.Node Instance.
	 * @param portlet
	 * @return TemplateProcessor used by the provided portlet
	 */
	@SuppressWarnings("deprecation")
	public static TemplateProcessor getPortletTemplateProcessor(final GenticsPortlet portlet) {
		//TODO Get not depricated method from DEV
		return (portlet.getTemplateProcessor(null, null));
	}

	/**
	 * Use Portal.Node functionality to resize Images.
	 * @param binary - the image data as byte array
	 * @param width - the desired with
	 * @param height - the desired height
	 * @param imageType - the image type e.g. png
	 * @return resized image (resized by GenticsImageResizer) as byte array.
	 */
	public static byte[] resizeImage(final byte[] binary, final int width, final int height, final String imageType) {
		return GenticsImageResizer.resize(binary, Math.max(width, 0), Math.max(height, 0), imageType);
	}
}
