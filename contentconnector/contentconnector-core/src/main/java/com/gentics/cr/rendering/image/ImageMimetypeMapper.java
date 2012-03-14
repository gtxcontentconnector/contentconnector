package com.gentics.cr.rendering.image;

import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.gentics.cr.CRResolvableBean;

/**
 * Class to map mimetypes to image types.
 * @author Christopher
 *
 */
public final class ImageMimetypeMapper {
	/**
	 * Default.
	 */
	private static final String DEFAULT_IMAGE_TYPE = "png";
	/**
	 * Logger.
	 */
	private static Logger log = Logger.getLogger(ImageMimetypeMapper.class);
	/**
	 * Mimetypeattribute.
	 */
	private static final String MIMETYPE_ATTRIBUTE = "mimetype";
	/**
	 * Filenameattribute.
	 */
	private static final String FILENAME_ATTRIBUTE = "filename";
	/**
	 * private constructor to prevent instantiation.
	 */
	private ImageMimetypeMapper() { }
	/**
	 * Mimetypemap.
	 */
	private static Properties mimemap = new Properties();
	/**
	 * Extensionmap.
	 */
	private static Properties extmap = new Properties();
	
	static {
		//Load mimemap
		try {
			mimemap.load(ImageMimetypeMapper.class.getResourceAsStream(
					"mimemap.properties"));
		} catch (IOException e) {
			log.error("Error while loading mimemap.", e);
		}
		//Load extmap
		try {
			extmap.load(ImageMimetypeMapper.class.getResourceAsStream(
						"extmap.properties"));
		} catch (IOException e) {
			log.error("Error while loading extmap.", e);
		}
	}
	/**
	 * Maps the image mimetype to the image type.
	 * @param mimetype mime
	 * @return image type
	 */
	public static String getTypeFromMimetype(final String mimetype) {
		if (mimetype != null) {
			return mimemap.getProperty(mimetype, DEFAULT_IMAGE_TYPE);
		} else {
			return DEFAULT_IMAGE_TYPE;
		}
	}
	/**
	 * Maps the filename to the image type.
	 * @param filename filename
	 * @return image type
	 */
	public static String getTypeFromFilename(final String filename) {
		if (filename != null) {
			int dot = filename.lastIndexOf('.');
		    String ext = filename.substring(dot + 1);
		    return extmap.getProperty(ext, DEFAULT_IMAGE_TYPE);
		} else {
			return DEFAULT_IMAGE_TYPE;
		}
	}
	
	/**
	 * Maps the beans mimetype or filename (whatever is present) to the image
	 * type.
	 * @param bean bean
	 * @return image type.
	 */
	public static String getTypeFromBean(final CRResolvableBean bean) {
		String type = DEFAULT_IMAGE_TYPE;
		if (bean != null) {
			String mime = (String) bean.get(MIMETYPE_ATTRIBUTE);
			if (mime != null) {
				return getTypeFromMimetype(mime);
			} else {
				String filename = (String) bean.get(FILENAME_ATTRIBUTE);
				if (filename != null) {
					return getTypeFromFilename(filename);
				}
			}
		}
		return type;
		
	}

}
