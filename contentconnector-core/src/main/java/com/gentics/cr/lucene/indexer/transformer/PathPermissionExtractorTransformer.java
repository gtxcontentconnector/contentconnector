package com.gentics.cr.lucene.indexer.transformer;

import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.exceptions.CRException;

/**
 * Extracts the first directory of the pubdir and saves it to the target field.
 * 
 * Example: 
 * pubdir: /blub/testdir/
 * permission: blub
 * 
 * @author Friedreich Bernhard
 */
public class PathPermissionExtractorTransformer extends ContentTransformer {

	/**
	 * Config attribute to specify the source.
	 * Example value: pub_dir
	 */
	private static final String SRC_ATTRIBUTE_KEY = "srcattribute";

	/**
	 * Config attribute to specify the target.
	 * Example value: permissions
	 */
	private static final String TARGET_ATTRIBUTE_KEY = "targetattribute";

	/**
	 * This property is set in case of no permissions are found for a path.
	 * Needed because lucene can't search for empty fields as those are not even stored in the index.
	 */
	public static final String NORESTRICTIONS = "everyone";

	/**
	 * Field to store the src attribute to.
	 */
	private String srcAttribute = "pub_dir";

	/**
	 * Field to store the target attribute to.
	 */
	private String targetAttribute = "permissions";

	/**
	 * Initialize extractor with given config.
	 * @param config used to retrieve src and targetattribute from.
	 */
	public PathPermissionExtractorTransformer(final GenericConfiguration config) {
		super(config);

		srcAttribute = (String) config.get(SRC_ATTRIBUTE_KEY);
		targetAttribute = (String) config.get(TARGET_ATTRIBUTE_KEY);
	}

	@Override
	public void processBean(final CRResolvableBean bean) throws CRException {
		if (this.srcAttribute != null) {
			Object obj = bean.get(this.srcAttribute);
			bean.set(this.targetAttribute, extractPermission(obj));
		} else {
			LOGGER.error("Configured attribute is null. Bean will not be processed");
		}
	}

	/**
	 * Splits the path by / and uses the first part of it as return value (trimmed).
	 * @param obj should be an object supporting toString() returning a directory (e.g: /blub/testdir)
	 * @return first parent directory (e.g.: blub)
	 */
	public static String extractPermission(final Object obj) {
		String path = obj.toString();
		return extractPermission(path);
	}

	/**
	 * Splits the path by / and uses the first part of it as return value (trimmed).
	 * @param path e.g.: /blub/testdir
	 * @return first parent directory (e.g.: blub)
	 */
	public static String extractPermission(final String path) {
		try {
			String perm = path.split("/")[1].toLowerCase();
			if (perm == null || perm.trim().isEmpty()) {
				return NORESTRICTIONS;
			}
			return perm;
		} catch (Exception e) {
			return NORESTRICTIONS;
		}
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}
}
