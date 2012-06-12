package com.gentics.cr.lucene.indexer.transformer;

import org.apache.lucene.index.IndexWriter;

import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.monitoring.MonitorFactory;
import com.gentics.cr.monitoring.UseCase;

public abstract class AbstractLuceneMonitoringTransformer extends ContentTransformer implements
		LuceneContentTransformer {

	protected AbstractLuceneMonitoringTransformer(GenericConfiguration config) {
		super(config);
	}

	/**
	 * Process the specified bean with monitoring.
	 * @param bean
	 * @throws CRException
	 */
	public void processBeanWithMonitoring(CRResolvableBean bean, IndexWriter writer) throws CRException {
		UseCase pcase = MonitorFactory.startUseCase("Transformer:" + this.getClass());
		try {
			processBean(bean, writer);
		} finally {
			pcase.stop();
		}
	}

}
