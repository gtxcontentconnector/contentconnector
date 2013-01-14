package com.gentics.cr.lucene.indexer.transformer.other;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import com.gentics.api.lib.resolving.Resolvable;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.lucene.indexer.transformer.ContentTransformer;

/**
 * Merge the contentattributes from the provided folder (folderattributes) to the targetattribute.
 * Example:
 * index.test.CR.FILES.transformer.10.contentattributes=metatags_seiten
 * index.test.CR.FILES.transformer.10.folderattributes=metatags
 * index.test.CR.FILES.transformer.10.targetattribute=search_keywords
 * @author Friedreich Bernhard
 */
public class FolderAttributeMerger extends ContentTransformer {

	private static final String FOLDERATTRIBUTE = "folder_id";

	private static final String CONTENTATTRIBUTES_KEY = "contentattributes";
	private static final String FOLDERATTRIBUTES_KEY = "folderattributes";
	private static final String TARGETATTRIBUTE_KEY = "targetattribute";

	private List<String> folderAttributes = new ArrayList<String>();
	private List<String> contentAttributes = new ArrayList<String>();

	private String targetAttribute = "";

	/**
	 * log4j logger.
	 */
	private static Logger logger = Logger.getLogger(FolderAttributeMerger.class);

	/**
	 * Creates instance of FolderAttributeMerger.
	 * @param config Configuration for Transformer
	 */
	public FolderAttributeMerger(final GenericConfiguration config) {
		super(config);
		Object folderAttr = config.get(FOLDERATTRIBUTES_KEY);
		if (folderAttr != null) {
			Collections.addAll(folderAttributes, folderAttr.toString().split(";"));
		}

		Object contentAttr = config.get(CONTENTATTRIBUTES_KEY);
		if (contentAttr != null) {
			Collections.addAll(contentAttributes, contentAttr.toString().split(";"));
		}

		Object target = config.get(TARGETATTRIBUTE_KEY);
		if (target != null) {
			targetAttribute = target.toString();
		}
	}

	@Override
	public void processBean(final CRResolvableBean contentBean) {
		String targetAttributeValues = "";
		CRResolvableBean folderBean = null;

		Resolvable contentResolvable = contentBean.getResolvable();
		if (contentResolvable != null) {
			folderBean = (CRResolvableBean) contentResolvable.get(FOLDERATTRIBUTE);
			if (folderBean != null) {
				Resolvable folderResolvable = folderBean.getResolvable();
				if (folderResolvable != null) {
					for (String attribute : folderAttributes) {
						Object attributeObject = folderResolvable.getProperty(attribute);
						if (attributeObject != null) {
							String folderAttributesString = attributeObject.toString();
							targetAttributeValues += folderAttributesString;
						}
					}
				}
			}
		}

		for (String attribute : contentAttributes) {
			Object attributeObject = contentBean.get(attribute);
			if (attributeObject != null) {
				String contentAttributesString = attributeObject.toString();
				targetAttributeValues += contentAttributesString;
			}
		}

		if (logger.isDebugEnabled() && !targetAttributeValues.equals("") && folderBean != null) {
			logger.debug("contentid: " + contentBean.getContentid() + " " + " - folder: " + folderBean.getContentid()
					+ " - targetattribute: " + targetAttributeValues);
		}
		contentBean.set(targetAttribute, targetAttributeValues);
	}

	@Override
	public void destroy() {

	}

}
