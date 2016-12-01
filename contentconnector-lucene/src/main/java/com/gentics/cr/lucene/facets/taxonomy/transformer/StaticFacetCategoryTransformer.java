package com.gentics.cr.lucene.facets.taxonomy.transformer;

import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.lucene.indexer.transformer.ContentTransformer;

/**
 * sets the value of the targetattribute to the value specified in the
 * staticvalue
 * 
 * @author Sebastian Vogel <s.vogel@gentics.com>
 * 
 */
public class StaticFacetCategoryTransformer extends ContentTransformer {
	// the name of the attribute where the value should be stored
	public static final String TRANSFORMER_TARGET_ATTRIBUTE_KEY = "targetattribute";
	// the static value to store in this attribute
	public static final String TRANSFORMER_STATIC_VALUE_KEY = "staticvalue";

	private String targetAttribute = "";
	private String staticValue = "";

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public void processBean(CRResolvableBean bean) throws CRException {
		if ((this.staticValue != null && !"".equals(this.staticValue))
				&& (this.targetAttribute != null && !""
						.equals(this.targetAttribute))) {
			bean.set(targetAttribute, staticValue);
		}
	}

	public StaticFacetCategoryTransformer(GenericConfiguration config) {
		super(config);

		staticValue = (String) config.getString(TRANSFORMER_STATIC_VALUE_KEY,
				"");
		targetAttribute = (String) config.getString(
				TRANSFORMER_TARGET_ATTRIBUTE_KEY, "");
	}

}
