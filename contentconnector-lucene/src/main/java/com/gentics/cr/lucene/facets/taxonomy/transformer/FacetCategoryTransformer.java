package com.gentics.cr.lucene.facets.taxonomy.transformer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.lucene.indexer.transformer.ContentTransformer;
import com.gentics.cr.util.CRUtil;



/**
 * Maps several sourceattribute values to "category"-values for facted indexing
 * and stores them in the targetattribute
 * 
 * Ideal to map enum (or cms-datasource) attributes to specific categories
 * 
 * @author Sebastian Vogel <s.vogel@gentics.com>
 * 
 */
public class FacetCategoryTransformer extends ContentTransformer {
	// which contentrepository attribute should be mapped
	public static final String TRANSFORMER_SOURCE_ATTRIBUTE_KEY = "sourceattribute";
	// the name of the attribute where the value should be stored
	public static final String TRANSFORMER_TARGET_ATTRIBUTE_KEY = "targetattribute";

	public static final String TRANSFORMER_VALUE_MAPPINGS_KEY = "valuemappings";
	public static final String TRANSFORMER_VALUE_MAPPING_SOURCE_KEY = "source";
	public static final String TRANSFORMER_VALUE_MAPPING_TARGET_KEY = "target";

	private String sourceAttribute = "";
	private String targetAttribute = "";
	private Map<String, String> valueMap = null;

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
	}

	@Override
	public void processBean(CRResolvableBean bean) throws CRException {

		if ((this.sourceAttribute != null && !"".equals(this.sourceAttribute)) && (this.targetAttribute != null && !"".equals(this.targetAttribute))
				&& this.valueMap != null) {
			Object obj = bean.get(this.sourceAttribute);
			LOGGER.debug("FacetCategoryTransformer - Indexing Contentid:"
					+ bean.get("contentid"));
			if (obj != null) {
				String sourceValue = getStringContents(obj);
				if (sourceValue != null) {
					String targetValue = this.valueMap.get(sourceValue);
					if (targetValue != null) {
						bean.set(this.targetAttribute, targetValue);
					}
				}
			}
		} else {
			LOGGER.error("FacetCategoryTransformer: Configured attributes are null or empty. Bean will not be processed");
		}

	}

	public FacetCategoryTransformer(GenericConfiguration config) {
		super(config);
		sourceAttribute = (String) config.getString(
				TRANSFORMER_SOURCE_ATTRIBUTE_KEY, "");
		targetAttribute = (String) config.getString(
				TRANSFORMER_TARGET_ATTRIBUTE_KEY, "");

		valueMap = new HashMap<String, String>();
		GenericConfiguration mappingsConf = (GenericConfiguration) config
				.get(TRANSFORMER_VALUE_MAPPINGS_KEY);

		if (mappingsConf != null) {
			Map<String, GenericConfiguration> mappingsMap = mappingsConf
					.getSortedSubconfigs();
			if (mappingsMap != null) {
				for (GenericConfiguration mapConf : mappingsMap.values()) {
					String source = mapConf.getString(
							TRANSFORMER_VALUE_MAPPING_SOURCE_KEY, "");
					String target = mapConf.getString(
							TRANSFORMER_VALUE_MAPPING_TARGET_KEY, "");
					if ((source != null && !"".equals(source)) && (target != null && !"".equals(target))) {
						valueMap.put(source, target);
						LOGGER.debug("FacetCategoryTransformer: Added new Source - Target Value pair: "
								+ source + " and the attribute: " + target);
					}
				}
			}
		}
	}

	private String getStringContents(Object obj) {
		String str = "";
		if (obj instanceof String) {
			str = (String) obj;
		} else if (obj instanceof Number) {
			str = obj.toString();
		} else if (obj instanceof Date) {
			str = Long.toString(((Date) obj).getTime());
		} else if (obj instanceof byte[]) {
			try {
				str = CRUtil.readerToString(new InputStreamReader(
						new ByteArrayInputStream((byte[]) obj)));
			} catch (IOException e) {
				LOGGER.error(
						"FacetCategoryTransformer: could not read from byte array",
						e);
				e.printStackTrace();
			}
		}

		return str;
	}

}
