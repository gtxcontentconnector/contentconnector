package com.gentics.cr.lucene.indexer.transformer;

import java.io.IOException;

import com.gentics.api.lib.resolving.Resolvable;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.file.ResolvableFileBean;

/**
 * This transformer takes the provided bean, reads the underlying file and detects the mimetype of it
 * using {@link com.gentics.cr.file.ResolvableFileBean#getMimetype()} and 
 * {@link com.gentics.cr.file.ResolvableFileBean#getDetectedMimetype()}. 
 * @author Friedreich Bernhard
 */
public class FileMimeTypeCreator extends ContentTransformer {

	/**
	 * Config attribute to write the detected mimetype to.
	 */
	private static final String TARGET_ATTRIBUTE_KEY = "targetattribute";

	/**
	 * Default value for the field to write the mimetype to.
	 */
	private String targetAttribute = "mimetype";

	/**
	 * Create a new instance of FileMimeTypeCreator using the provided config.
	 * @param config needed to read the target field.
	 */
	public FileMimeTypeCreator(final GenericConfiguration config) {
		super(config);
		
		targetAttribute = (String) config.get(TARGET_ATTRIBUTE_KEY);
	}
	
	@Override
	public void processBean(final CRResolvableBean bean) throws CRException {
		if (bean instanceof CRResolvableBean) {
			Resolvable res = bean.getResolvable();
			if (res instanceof ResolvableFileBean) {
				ResolvableFileBean fileBean = (ResolvableFileBean) res;
				try {
					bean.set(this.targetAttribute, fileBean.getDetectedMimetype());
				} catch (IOException e) {
					bean.set(this.targetAttribute, fileBean.getMimetype());
				}
			}
		}
	}

	@Override
	public void destroy() {
		
	}

}
