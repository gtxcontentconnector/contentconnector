package com.gentics.cr.lucene.indexer.index;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;

import com.gentics.api.lib.exception.NodeException;
import com.gentics.api.lib.resolving.Resolvable;
import com.gentics.cr.CRConfig;
import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.CRError;
import com.gentics.cr.CRRequest;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.RequestProcessor;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.lucene.indexaccessor.IndexAccessor;
import com.gentics.cr.lucene.indexer.IndexerUtil;
import com.gentics.cr.lucene.indexer.transformer.ContentTransformer;
import com.gentics.cr.util.indexing.AbstractUpdateCheckerJob;
import com.gentics.cr.util.indexing.IndexLocation;
/**
 * 
 * Last changed: $Date: 2009-09-02 17:57:48 +0200 (Mi, 02 Sep 2009) $
 * @version $Revision: 180 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class CRLuceneIndexJob extends AbstractUpdateCheckerJob{
	protected static Logger log = Logger.getLogger(CRLuceneIndexJob.class);
	
	/**
	 * Name of class to use for IndexLocation, must extend {@link com.gentics.cr.util.indexing.IndexLocation}
	 */
	public static final String INDEXLOCATIONCLASS = "com.gentics.cr.lucene.indexer.index.LuceneSingleIndexLocation";
	private RequestProcessor rp = null;
	
	/**
	 * Create new instance of IndexJob
	 * @param config
	 * @param indexLoc
	 * @param configmap
	 */
	public CRLuceneIndexJob(CRConfig config, IndexLocation indexLoc, 
			Hashtable<String,CRConfigUtil> configmap)
	{
		super(config,indexLoc,configmap);
		try {
			rp = config.getNewRequestProcessorInstance(1);
		} catch (CRException e) {
			log.error("Could not create RequestProcessor instance.",e);
		}
	}
	
	
	
	/**
	 * Tests if a CRIndexJob has the same identifier as the given object being an instance of CRIndexJob
	 */
	@Override
	public boolean equals(Object obj)
	{
		if(obj instanceof CRLuceneIndexJob)
		{
			if(this.identifyer.equalsIgnoreCase(((CRLuceneIndexJob)obj).getIdentifyer()))
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Key to be used for saving state to contentstatus
	 */
	public final static String PARAM_LASTINDEXRUN = "lastindexrun";
	/**
	 * Key to be used for saving state to contentstatus
	 */
	public final static String PARAM_LASTINDEXRULE = "lastindexrule";
	private static final String RULE_KEY = "rule";

	private static final String CONTAINED_ATTRIBUTES_KEY = "CONTAINEDATTRIBUTES";
	private static final String INDEXED_ATTRIBUTES_KEY = "INDEXEDATTRIBUTES";
	
	private static final String BATCH_SIZE_KEY = "BATCHSIZE";
	private static final String CR_FIELD_KEY = "CRID";
	
	/**
	 * Default batch size is set to 1000 elements
	 */
	protected int batchSize = 1000;

		
	/**
	 * Index a single configured ContentRepository
	 * @param indexLocation
	 * @param config
	 * @throws CRException
	 */
	@SuppressWarnings("unchecked")
	protected void indexCR(IndexLocation indexLocation, CRConfigUtil config)
			throws CRException{

		String crid = config.getName();
		if(crid ==null)crid = this.identifyer;

		
		//IndexWriter indexWriter = new IndexWriter(indexLocation.getDirectory(),analyzer, create,IndexWriter.MaxFieldLength.LIMITED);
		IndexAccessor indexAccessor = null;
		IndexWriter indexWriter = null;
		LuceneIndexUpdateChecker luceneIndexUpdateChecker = null;
		
		
		
		try {
			indexLocation.checkLock();
			
			try
			{ 
				status.setCurrentStatusString("Writer accquired. Starting index job.");
							
				if(rp==null)
				{
					throw new CRException("FATAL ERROR","Datasource not available");
				}
							
				String bsString = (String)config.get(BATCH_SIZE_KEY);
				
				int CRBatchSize = batchSize;
				
				if(bsString!=null)
				{
					try
					{
						CRBatchSize = Integer.parseInt(bsString);
					}
					catch(NumberFormatException e)
					{
						log.error("The configured "+BATCH_SIZE_KEY+" for the Current CR did not contain a parsable integer. "+e.getMessage());
					}
					
				}
				
				// and get the current rule
				String rule = (String)config.get(RULE_KEY);
		
				if (rule == null) {
					rule = "";
				}
				if (rule.length() == 0) {
					rule = "(1 == 1)";
				} else {
					rule = "(" + rule + ")";
				}
				
				List<ContentTransformer> transformerlist = ContentTransformer.getTransformerList(config);
				
				boolean create = true;
				
				if(indexLocation.isContainingIndex())
				{
					create=false;
					log.debug("Index already exists.");
				}
				
				if(indexLocation instanceof LuceneIndexLocation){
					luceneIndexUpdateChecker = new LuceneIndexUpdateChecker((LuceneIndexLocation) indexLocation,CR_FIELD_KEY,crid,idAttribute);
				}
				else{
					log.error("IndexLocation is not created for Lucene. Using the "+CRLuceneIndexJob.class.getName()+" requires that you use the "+LuceneIndexLocation.class.getName()+". You can configure another Job by setting the "+IndexLocation.UPDATEJOBCLASS_KEY+" key in your config.");
					throw new CRException(new CRError("Error","IndexLocation is not created for Lucene."));
				}
				
				Collection<CRResolvableBean> objectsToIndex=null;
				//Clear Index and remove stale Documents
				if(!create)
				{
					log.debug("Will do differential index.");
					try {
						CRRequest req = new CRRequest();
						req.setRequestFilter(rule);
						status.setCurrentStatusString("Cleaning index from stale objects...");
						objectsToIndex = getObjectsToUpdate(req,rp,false,luceneIndexUpdateChecker);
					} catch (Exception e) {
						log.error("ERROR while cleaning index");
						e.printStackTrace();
					}
					
				}
							
				//Obtain accessor and writer after clean
				if(indexLocation instanceof LuceneIndexLocation){
					indexAccessor = ((LuceneIndexLocation)indexLocation).getAccessor();
					indexWriter = indexAccessor.getWriter();
				}
				else{
					log.error("IndexLocation is not created for Lucene. Using the "+CRLuceneIndexJob.class.getName()+" requires that you use the "+LuceneIndexLocation.class.getName()+". You can configure another Jo by setting the "+IndexLocation.UPDATEJOBCLASS_KEY+" key in your config.");
				}
				log.debug("Using rule: "+rule);
				
		
				// prepare the map of indexed/stored attributes
				Map<String,Boolean> attributes = new HashMap<String,Boolean>();
				List<String> containedAttributes = IndexerUtil.getListFromString((String)config.get(CONTAINED_ATTRIBUTES_KEY), ",");
				List<String> indexedAttributes = IndexerUtil.getListFromString((String)config.get(INDEXED_ATTRIBUTES_KEY), ",");
		
				// first put all indexed attributes into the map
				for (String name:indexedAttributes) {
					attributes.put(name, Boolean.FALSE);
				}
		
				// now put all contained attributes
				for (String name:containedAttributes) {
					attributes.put(name, Boolean.TRUE);
				}
				// finally, put the "contentid" (always contained)
				attributes.put(idAttribute, Boolean.TRUE);
		
				// get all objects to index
				if(objectsToIndex==null)
				{
					CRRequest req = new CRRequest();
					req.setRequestFilter(rule);
					objectsToIndex = getObjectsToUpdate(req,rp,true,luceneIndexUpdateChecker);
				}
				if(objectsToIndex==null)
				{
					log.debug("Rule returned no objects to index. Skipping run");
					return;
				}
				
				status.setObjectCount(objectsToIndex.size());
				log.debug(" index job with "+objectsToIndex.size()+" objects to index.");
				// now get the first batch of objects from the collection
				// (remove them from the original collection) and index them
				Collection<CRResolvableBean> slice = new Vector(CRBatchSize);
				int sliceCounter = 0;
				
				status.setCurrentStatusString("Starting to index slices.");
				boolean interrupted = Thread.currentThread().isInterrupted();
				for (Iterator<CRResolvableBean> iterator = objectsToIndex.iterator(); iterator.hasNext();) {
					CRResolvableBean obj = iterator.next();
					slice.add(obj);
					iterator.remove();
					sliceCounter++;
					if(Thread.currentThread().isInterrupted())
					{
						interrupted = true;
						break;
					}
					if (sliceCounter == CRBatchSize) {
						// index the current slice
						log.debug("Indexing slice with "+slice.size()+" objects.");
						indexSlice(indexWriter, slice, attributes, rp, create,config,transformerlist);
						status.setObjectsDone(status.getObjectsDone()+slice.size());
						// clear the slice and reset the counter
						slice.clear();
						sliceCounter = 0;
					}
				}

				if (!slice.isEmpty()) {
					// index the last slice
					indexSlice(indexWriter, slice, attributes, rp, create,config, transformerlist);
					status.setObjectsDone(status.getObjectsDone()+slice.size());
				}
				if(!interrupted)
				{
					//Only Optimize the Index if the thread has not been interrupted
					indexWriter.optimize();
				}
				else
				{
					log.debug("Job has been interrupted and will now be closed. Objects will be reindexed next run.");
				}
				
				
			}catch(Exception ex)
			{
				log.error("Could not complete index run... indexed Objects: "+status.getObjectsDone()+", trying to close index and remove lock.",ex);
			}finally{
				//Set status for job if it was not locked
				status.setCurrentStatusString("Finished job.");
				int objectCount = status.getObjectsDone();
				log.debug("Indexed "+objectCount+" objects...");

				if ( indexAccessor != null && indexWriter != null) {
					indexAccessor.release(indexWriter);
				}

				if(objectCount > 0){
					indexLocation.createReopenFile();
				}
			}
		}
		catch(LockedIndexException ex)
		{
			log.debug("LOCKED INDEX DETECTED. TRYING AGAIN IN NEXT JOB.");
			if(this.indexLocation!=null && !this.indexLocation.hasLockDetection())
			{
				log.error("IT SEEMS THAT THE INDEX HAS UNEXPECTEDLY BEEN LOCKED. PLEASE REMOVE LOCK");
				ex.printStackTrace();
			}
		}
	}

	/**
	 * Index a single slice
	 * @param indexWriter
	 * @param slice
	 * @param attributes
	 * @param ds
	 * @param create
	 * @param config
	 * @param transformerlist
	 * @throws NodeException
	 * @throws CorruptIndexException
	 * @throws IOException
	 */
	private void indexSlice(IndexWriter indexWriter, Collection<CRResolvableBean> slice, Map<String,Boolean> attributes, RequestProcessor rp, boolean create, CRConfigUtil config, List<ContentTransformer> transformerlist) throws CRException,
			CorruptIndexException, IOException {
		// prefill all needed attributes
		CRRequest req = new CRRequest();
		req.setAttributeArray(
				(String[]) attributes.keySet().toArray(
						new String[attributes.keySet().size()]));
		rp.fillAttributes(slice, req,idAttribute);
		
		for (Resolvable objectToIndex:slice) {
			
			CRResolvableBean bean = new CRResolvableBean(objectToIndex);
			//CALL PRE INDEX PROCESSORS/TRANSFORMERS
			//TODO This could be optimized for multicore servers with a map/reduce algorithm
			for(ContentTransformer transformer:transformerlist)
			{
				
				try{
					status.setCurrentStatusString("TRANSFORMING... TRANSFORMER: "+transformer.getTransformerKey()+"; BEAN: "+bean.get(idAttribute));
					if(transformer.match(bean))
						transformer.processBean(bean);
				}
				catch(Exception e)
				{
					//TODO Remember broken files
					log.error("ERROR WHILE TRANSFORMING CONTENTBEAN. ID: "+bean.get(idAttribute));
					e.printStackTrace();
				}
			}
			
			if(!create)
			{
				indexWriter.updateDocument(new Term(idAttribute, (String)bean.get(idAttribute)), getDocument(bean, attributes,config));
			}
			else
			{
				indexWriter.addDocument(getDocument(bean, attributes, config));
			}
			//Stop Indexing when thread has been interrupted
			if(Thread.currentThread().isInterrupted())break;
		}
	}
	
	/**
	 * Convert a resolvable to a Lucene Document
	 * @param resolvable
	 * 				Contains the resolvable to be indexed
	 * @param attributes
	 * 				A map of attribute names, which values are true if the attribute should be stored or fales if the attribute should only be indexed.
	 * 				Only attributes configured in this map will be indexed
	 * @param config
	 * 				The name of this config will be used as CRID (ContentRepository Identifyer).
	 * 				The ID-Attribute should also be configured in this config (usually contentid).
	 * @return
	 * 				Returns a Lucene Document, ready to be added to the index.
	 */
	private Document getDocument(Resolvable resolvable, Map<String,Boolean> attributes, CRConfigUtil config) {
		Document doc = new Document();
		
		String crID = (String)config.getName();
		if(crID!=null)
		{
			//Add content repository identification
			doc.add(new Field(CR_FIELD_KEY, crID, Field.Store.YES, Field.Index.NOT_ANALYZED));
		}
		Integer upTS = (Integer)resolvable.get(TIMESTAMP_ATTR);
		if(upTS!=null)
		{
			doc.add(new Field(TIMESTAMP_ATTR, upTS.toString(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		}
		
		for (Entry<String,Boolean> entry:attributes.entrySet()) {
			
			String attributeName = (String) entry.getKey();
			Boolean storeField = (Boolean) entry.getValue();

			Object value = resolvable.getProperty(attributeName);
			
			
			if(idAttribute.equalsIgnoreCase(attributeName))
			{
				doc.add(new Field(idAttribute, (String)value, Field.Store.YES, Field.Index.NOT_ANALYZED));
			}
			else if(value!=null){
				
				if(value instanceof String || value instanceof Number)
				{
					doc.add(new Field(attributeName, value.toString(),storeField.booleanValue() ? Store.YES : Store.NO,Field.Index.ANALYZED));
				}
			}
		}
		return doc;
	}
	
}

