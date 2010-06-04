package com.gentics.cr;

import java.util.ArrayList;
import java.util.Collection;

import com.gentics.cr.exceptions.CRException;
/**
 * 
 * Last changed: $Date: 2010-04-01 15:24:02 +0200 (Do, 01 Apr 2010) $
 * @version $Revision: 541 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class RPMergerRequestProcessor extends RequestProcessor {

  private RequestProcessor rp1;
  private RequestProcessor rp2;
  private String mergeattribute;

  /**
   * Configuration key for defining the merge attribute.
   */
  private static final String MERGE_ATTRIBUTE_KEY = "mergeattribute";

  /**
   * Default value for the merge attribute if it is not definied in the
   * configuration.
   */
  private static final String MERGE_ATTRIBUTE_DEFAULT = "contentid";

  /**
   * Initialize the RequestProcessorMergerRequestProcessor.
   * @param config Configuration for the RequestProcessor
   * @throws CRException in case one of the RequestProcessors could not be
   * initialized.
   */
  public RPMergerRequestProcessor(final CRConfig config) throws CRException {
    super(config);
    rp1 = config.getNewRequestProcessorInstance(1);
    rp2 = config.getNewRequestProcessorInstance(2);
    mergeattribute = (String) config.get(MERGE_ATTRIBUTE_KEY);
    if (mergeattribute == null) {
      mergeattribute = MERGE_ATTRIBUTE_DEFAULT;
    }
  }



  @Override
  public final Collection<CRResolvableBean> getObjects(final CRRequest request,
      final boolean doNavigation) throws CRException {
    ArrayList<CRResolvableBean> coll = null;
    coll = (ArrayList<CRResolvableBean>) RequestProcessorMerger
        .merge(mergeattribute, rp1, rp2, request);
    return coll;
  }

  /**
   * clean up all RequestProcessors we initialized.
   */
  @Override
  public final void finalize() {
    rp1.finalize();
    rp2.finalize();
  }


}
