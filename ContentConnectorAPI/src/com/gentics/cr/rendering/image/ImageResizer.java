package com.gentics.cr.rendering.image;

import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;
import org.apache.log4j.Logger;

import com.gentics.cr.CRResolvableBean;
import com.gentics.lib.image.GenticsImageResizer;

/**
 * 
 * Last changed: $Date$
 * @version $Revision$
 * @author $Author$
 *
 */
public class ImageResizer {
	
	private static Logger log = Logger.getLogger(ImageResizer.class);
	
	private final static String IMAGE_CACHE_REGION = "gentics-portal-contentmodule-image";
	
	/**
	 * Name of the parameter that holds the maxheight
	 */
	public final static String IMAGE_MAXHEIGHT_NAME = "maxheight";
	
	/**
	 * Name of the parameter that holds the maxwidth
	 */
	public final static String IMAGE_MAXWIDTH_NAME = "maxwidth";
	
	private static int maxAllowedWidth = 16384;

	private static int maxAllowedHeight = 16384;
	
	private static JCS imageCache;
	
	
	private static JCS initCache()
	{
		JCS cache = null;
		try {
			
			cache = JCS.getInstance(IMAGE_CACHE_REGION);
			log.debug("Using cache zone "+IMAGE_CACHE_REGION+" for image resizing");
		} catch (final CacheException e) {
			log.warn("Could not initialize Cache for PlinkProcessor.");
		}
		return cache;
	}
	
	
	
	/**
	 * Get the binary contents of a CRResolvableBean which have to be an image and resize the image according to the given parameters
	 * @param bean
	 * @param maxWidth
	 * @param maxHeight
	 * @return the resized image data or null if resizing could not be commenced
	 */
	public static byte[] getResizedImage(final CRResolvableBean bean, final String maxWidth, final String maxHeight)
	{
		byte[] binary=null;
		if (maxWidth != null|| maxHeight != null) {
            //we need to compute the resized version, save it to
            // the db, and return it to the user
            int maxw = 0;
            int maxh = 0;
            // parse Sting to integer
            if (maxWidth != null) {
                maxw = Integer.parseInt(maxWidth);
            }
            if (maxHeight != null) {
                maxh = Integer.parseInt(maxHeight);
            }

            if (log.isDebugEnabled()) {
                log.debug("Requested resizing of image {" + bean.getContentid()
                        + "} to {" + maxw + " x " + maxh + "}");
            }
            binary = getResizedImage(bean, maxw, maxh);
            
		}
		return binary;
	}
	
	
	/**
	 * Get the binary contents of a CRResolvableBean which have to be an image and resize the image according to the given parameters
	 * @param bean
	 * @param maxw
	 * @param maxh
	 * @return the resized image data or null if resizing could not be commenced
	 */
	public static byte[] getResizedImage(final CRResolvableBean bean, int maxw, int maxh)
	{
		final String imageType = null;
		byte[] binary=null;
		String maxWidth = Integer.toString(maxw);
		String maxHeight = Integer.toString(maxh);
		// chech if we are in maximum allowed size range for
        // security
        if (maxw > maxAllowedWidth) {
            log.warn("Width {" + maxw
                    + "} is greater than maxallowedwidth {"
                    + maxAllowedWidth + "}, using the maxallowedwidth instead!");
            maxw = maxAllowedWidth;
            maxWidth = Integer.toString(maxw);
        }
        if (maxh > maxAllowedHeight) {
            log.warn("Height {" + maxh
                    + "} is greater than maxallowedheight {"
                    + maxAllowedHeight + "}, using the maxallowedheight instead!");
            maxh = maxAllowedHeight;
            maxHeight = Integer.toString(maxh);
        }
        
        // only do resizing, if at least one value is positive
        if (maxw > 0 || maxh > 0) {
        	binary = bean.getBinaryContent();
            // lookup resized image in cache
            final String cacheKey = bean.getContentid() + "." + bean.get("edittimestamp") +
                "." + maxWidth + "." + maxHeight; 
            //GenticsPortletContext context = this.getGenticsPortletContext();
            if(imageCache==null)
    		{
    			imageCache = initCache();
    		}
            Object cacheObject = null;
            cacheObject = imageCache.get(cacheKey);
            // if an object was found in the cache we'll use it
            if (null == cacheObject) {
                binary = resizeImage(bean, maxw, maxh, imageType);
                try {
                    imageCache.put(cacheKey, binary);
                } catch (final CacheException e) {
                    log.warn("could not put object into cache", e);
                }
            } else {
                binary = (byte[]) cacheObject;
            }
        } else {
            log.error("Cannot resize image to {" + maxw + " x " + maxh
                    + "}, skipping resizing");
        }
        return binary;
	}
	
	private static byte[] resizeImage(final CRResolvableBean motherObj, final int maxwidth, final int maxheight, String imageType) {

        byte[] binary = null;

        try {
        	binary = motherObj.getBinaryContent();
            if (binary == null) {
                // object has no binary content.. so .. we cannot resize
                log.error("Unable to find binarycontent for object {" + motherObj.getContentid() + "} to resize image.");
                return null;
            }
            
            // TODO Resolve approprate ImageType -> Mimetype
            imageType = "png";
            
            // do the resizing
            binary = GenticsImageResizer.resize(binary, Math.max(maxwidth, 0), Math.max(maxheight, 0), imageType);

       } catch (final Exception e) {
            log.warn("Could not save resized image due a Portal Error.", e);
            return null;
        } 
        return binary;
    }
}
