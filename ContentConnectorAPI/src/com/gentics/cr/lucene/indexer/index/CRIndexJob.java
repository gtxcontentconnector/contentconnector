package com.gentics.cr.lucene.indexer.index;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;

import com.gentics.api.lib.exception.NodeException;
import com.gentics.api.lib.expressionparser.ExpressionParser;
import com.gentics.api.lib.resolving.Resolvable;
import com.gentics.contentnode.content.GenticsContentFactory;
import com.gentics.contentnode.datasource.CNWriteableDatasource;
import com.gentics.cr.CRConfig;
import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.CRDatabaseFactory;
import com.gentics.cr.CRException;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.lucene.indexaccessor.IndexAccessor;
import com.gentics.cr.lucene.indexer.IndexerStatus;
import com.gentics.cr.lucene.indexer.IndexerUtil;
import com.gentics.cr.lucene.indexer.transformer.ContentTransformer;
import com.gentics.cr.util.CRUtil;
/**
 * 
 * Last changed: $Date: 2009-09-02 17:57:48 +0200 (Mi, 02 Sep 2009) $
 * @version $Revision: 180 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class CRIndexJob implements Runnable{
	protected static Logger log = Logger.getLogger(CRIndexJob.class);
	
	private String identifyer;
	private IndexLocation indexLocation;
	private IndexerStatus status;
	private CRConfig config;
	private long duration = 0;
	private Hashtable<String,CRConfigUtil> configmap;
	/**
	 * Create new instance of IndexJob
	 * @param config
	 * @param indexLoc
	 * @param configmap
	 */
	public CRIndexJob(CRConfig config, IndexLocation indexLoc,Hashtable<String,CRConfigUtil> configmap)
	{
		this.config = config;
		this.configmap = configmap;
		if(this.configmap==null)log.debug("Configmap is empty");
		this.identifyer = (String) config.getName();
		this.indexLocation = indexLoc;
		status = new IndexerStatus();
		
	}
	
	/**
	 * Get Current Status
	 * @return
	 */
	public String getStatusString()
	{
		return status.getCurrentStatusString();
	}
	
	/**
	 * Gets the Job Identifyer. In most cases this is the CR id.
	 * @return identifyer as string
	 */
	public String getIdentifyer()
	{
		return identifyer;
	}
	
	/**
	 * Get job duration as ms;
	 * @return
	 */
	public long getDuration()
	{
		return duration;
	}
	
	/**
	 * Get total count of objects to index
	 * @return
	 */
	public int getObjectsToIndex()
	{
		return status.getObjectCount();
	}
	
	/**
	 * Get the number ob objects already indexed
	 * @return
	 */
	public int getObjectsDone()
	{
		return status.getObjectsDone();
	}
	
	
	
	/**
	 * Tests if a CRIndexJob has the same identifyer as the given object being an instance of CRIndexJob
	 */
	@Override
	public boolean equals(Object obj)
	{
		if(obj instanceof CRIndexJob)
		{
			if(this.identifyer.equalsIgnoreCase(((CRIndexJob)obj).getIdentifyer()))
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Executes the index process
	 */
	public void run()
	{
		long start = System.currentTimeMillis();
		try{
			indexCR(this.indexLocation,(CRConfigUtil)this.config);
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
		catch(Exception ex)
		{
			log.error(ex.getMessage());
			ex.printStackTrace();
		}
		long end = System.currentTimeMillis();
		this.duration = end-start;
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
	private static final String ID_ATTRIBUTE_KEY = "IDATTRIBUTE";
	private static final String CONTAINED_ATTRIBUTES_KEY = "CONTAINEDATTRIBUTES";
	private static final String INDEXED_ATTRIBUTES_KEY = "INDEXEDATTRIBUTES";
	
	private static final String BATCH_SIZE_KEY = "BATCHSIZE";
	private static final String CR_FIELD_KEY = "CRID";
	
	private static final String TIMESTAMP_ATTR = "updatetimestamp";
	/**
	 * Default batch size is set to 1000 elements
	 */
	protected int batchSize = 1000;

		
	/**
	 * Index a single configured ContentRepository
	 * @param indexLocation
	 * @param config
	 * @throws NodeException
	 * @throws CorruptIndexException
	 * @throws IOException
	 * @throws CRException
	 * @throws LockedIndexException 
	 */
	@SuppressWarnings("unchecked")
	private void indexCR(IndexLocation indexLocation, CRConfigUtil config)
			throws NodeException, CorruptIndexException, IOException, CRException, LockedIndexException{
		String crID = config.getName();
		if(crID ==null)crID = this.identifyer;
		
		//IndexWriter indexWriter = new IndexWriter(indexLocation.getDirectory(),analyzer, create,IndexWriter.MaxFieldLength.LIMITED);
		IndexAccessor indexAccessor = null;
		IndexWriter indexWriter = null;
		indexLocation.checkLock();
		// get the datasource
		CNWriteableDatasource ds=null;
		try
		{ 
			status.setCurrentStatusString("Writer accquired. Starting index job.");
			ds = (CNWriteableDatasource)config.getDatasource();
		
			if(ds==null)
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
				catch(NumberFormatException nfx)
				{
					log.error("The configured "+BATCH_SIZE_KEY+" for the Current CR did not contain a parsable integer. "+nfx.getMessage());
				}
				
			}
			
			// and get the current rule
			String rule = (String)config.get(RULE_KEY);
	
			if (rule == null) {
				rule = "";
			}
			
			List<ContentTransformer> transformerlist = ContentTransformer.getTransformerList(config);
			
			boolean create = true;
			
			if(indexLocation.isContainingIndex())
			{
				create=false;
				log.debug("Index already exists.");
			}
			// check whether the rule has changed, if yes, do a full index run
			
			if (rule.length() == 0) {
				rule = "(1 == 1)";
			} else {
				rule = "(" + rule + ")";
			}
			
			Collection<Resolvable> objectsToIndex=null;
			
			//Clear Index and remove stale Documents
			if(!create)
			{
				log.debug("Will do differential index.");
				try {
					status.setCurrentStatusString("Cleaning index from stale objects...");
					objectsToIndex = cleanAndGetDiffObjects(ds,rule,indexLocation,config);
				} catch (Exception e) {
					log.error("ERROR while cleaning index");
					e.printStackTrace();
				}
				
			}
						
			//Obtain accessor and writer after clean
			indexAccessor = indexLocation.getAccessor();
			indexWriter = indexAccessor.getWriter();
			
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
	
			String idAttribute = (String)config.get(ID_ATTRIBUTE_KEY);
			// finally, put the "contentid" (always contained)
			attributes.put(idAttribute, Boolean.TRUE);
	
			// get all objects to index
			if(objectsToIndex==null)
			{
				objectsToIndex = (Collection<Resolvable>) ds.getResult(ds
					.createDatasourceFilter(ExpressionParser.getInstance()
							.parse(rule)), null);
			}
			if(objectsToIndex==null)
			{
				log.debug("Rule returned no objects to index. Skipping run");
				return;
			}
			
			status.setObjectCount(objectsToIndex.size());
			log.debug("Starting index job with "+objectsToIndex.size()+" objects to index.");
			// now get the first batch of objects from the collection
			// (remove them from the original collection) and index them
			Collection<Resolvable> slice = new Vector(CRBatchSize);
			int sliceCounter = 0;
			
			status.setCurrentStatusString("Starting to index slices.");
			boolean interrupted = Thread.currentThread().isInterrupted();
			for (Iterator<Resolvable> iterator = objectsToIndex.iterator(); iterator.hasNext();) {
				Resolvable obj = iterator.next();
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
					indexSlice(indexWriter, slice, attributes, ds, create,config,transformerlist);
					status.setObjectsDone(status.getObjectsDone()+slice.size());
					// clear the slice and reset the counter
					slice.clear();
					sliceCounter = 0;
				}
			}

			if (!slice.isEmpty()) {
				// index the last slice
				indexSlice(indexWriter, slice, attributes, ds, create,config, transformerlist);
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
			log.error("Could not complete index run... indexed Objects: "+status.getObjectsDone()+", trying to close index and remove lock.");
			ex.printStackTrace();
		}finally{
			status.setCurrentStatusString("Finished job.");
			log.debug("Indexed "+status.getObjectsDone()+" objects...");
			indexAccessor.release(indexWriter);
			CRDatabaseFactory.releaseDatasource(ds);
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
	private void indexSlice(IndexWriter indexWriter, Collection<Resolvable> slice,
			Map<String,Boolean> attributes, CNWriteableDatasource ds, boolean create, CRConfigUtil config, List<ContentTransformer> transformerlist) throws NodeException,
			CorruptIndexException, IOException {
		// prefill all needed attributes
		GenticsContentFactory.prefillContentObjects(ds, slice,
				(String[]) attributes.keySet().toArray(
						new String[attributes.keySet().size()]));
		String idAttribute = (String)config.get(ID_ATTRIBUTE_KEY);
		
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
		
		String idAttribute = (String)config.get(ID_ATTRIBUTE_KEY);
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
	
	
	
	/**
	 * Deletes all Objects from index, which are not returned from the datasource using the given rule
	 * @param ds
	 * @param rule
	 * @param indexLocation
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	private void cleanIndex(CNWriteableDatasource ds, String rule, IndexLocation indexLocation, CRConfigUtil config) throws Exception
	{
		String idAttribute = (String)config.get(ID_ATTRIBUTE_KEY);
		
		//IndexReader reader = IndexReader.open(indexLocation.getDirectory(), false);// open existing index
		IndexAccessor indexAccessor = indexLocation.getAccessor();
		IndexReader reader = indexAccessor.getReader(true);
		try
		{
			TermEnum uidIter = reader.terms(new Term(idAttribute, "")); // init uid iterator
			
			Collection<Resolvable> objectsToIndex = (Collection<Resolvable>)ds.getResult(ds.createDatasourceFilter(ExpressionParser.getInstance().parse(rule)), null, 0, -1, CRUtil.convertSorting("contentid:asc"));
			
			Iterator<Resolvable> resoIT = objectsToIndex.iterator();
			
			Resolvable CRlem = resoIT.next();
			String crElemID =(String) CRlem.get(idAttribute);
			
			//solange index id kleiner cr id delete from index
			boolean finish=false;
			
			while(!finish)
			{
				
				if(uidIter.term() != null && uidIter.term().field() == idAttribute && uidIter.term().text().compareTo(crElemID) == 0)
				{
					//step both
					finish = !uidIter.next();
					if(resoIT.hasNext())
					{
						CRlem = resoIT.next();
						crElemID =(String) CRlem.get(idAttribute);
					}
				}
				else if(uidIter.term() != null && uidIter.term().field() == idAttribute && uidIter.term().text().compareTo(crElemID) > 0 && resoIT.hasNext())
				{
					//step cr
					CRlem = resoIT.next();
					crElemID =(String) CRlem.get(idAttribute);
					
				}
				else
				{
					//delete UIDITER
					Term t = uidIter.term();
					if(t!=null)
					{
						reader.deleteDocuments(t);
						
					}
					finish = !uidIter.next();
				}
				
			}
			uidIter.close();  // close uid iterator
		   
		}
		finally
		{
			indexAccessor.release(reader,true);
		}
			
	}
	
	private LinkedHashMap<String,Integer> fetchSortedDocs(TermDocs termDocs, IndexReader reader, String idAttribute) throws IOException
	{
		LinkedHashMap<String,Integer> tmp = new LinkedHashMap<String,Integer>();
		boolean finish=!termDocs.next();
		
		while(!finish)
		{
			Document doc = reader.document(termDocs.doc());
			String docID = doc.get(idAttribute);
			tmp.put(docID, termDocs.doc());
			finish=!termDocs.next();
		}
		
		LinkedHashMap<String,Integer> ret = new LinkedHashMap<String,Integer>(tmp.size());
		Vector<String> v = new Vector<String>(tmp.keySet());
		Collections.sort(v);
		for(String id:v)
		{
			ret.put(id, tmp.get(id));
		}
		return ret;
	}
	
	/**
	 * Deletes all Objects from index, which are not returned from the datasource using the given rule
	 * @param ds
	 * @param rule
	 * @param indexLocation
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private Collection<Resolvable> cleanAndGetDiffObjects(CNWriteableDatasource ds, String rule, IndexLocation indexLocation, CRConfigUtil config) throws Exception
	{
		String idAttribute = (String)config.get(ID_ATTRIBUTE_KEY);
		
		String CRID = (String)config.getName();
		Collection<Resolvable> diffObjects = new Vector<Resolvable>();
		
		//IndexReader reader = IndexReader.open(indexLocation.getDirectory(), false);// open existing index
		IndexAccessor indexAccessor = indexLocation.getAccessor();
		IndexReader reader = indexAccessor.getReader(true);
		try
		{
			TermDocs termDocs = reader.termDocs(new Term(CR_FIELD_KEY,CRID));
						
			Collection<Resolvable> objectsToIndex = (Collection<Resolvable>)ds.getResult(ds.createDatasourceFilter(ExpressionParser.getInstance().parse(rule)), new String[]{TIMESTAMP_ATTR}, 0, -1, CRUtil.convertSorting("contentid:asc"));
			
			Iterator<Resolvable> resoIT = objectsToIndex.iterator();
			if(resoIT.hasNext())
			{
				LinkedHashMap<String,Integer> docs = fetchSortedDocs(termDocs, reader, idAttribute);
				Iterator docIT = docs.keySet().iterator();
				
				Resolvable CRlem = resoIT.next();
				String crElemID =(String) CRlem.get(idAttribute);
				String docID="";
				//solange index id kleiner cr id delete from index
				boolean finish=!docIT.hasNext();
				if(finish)
				{
					//IF THERE ARE NO INDEXED OBJECTS => ADD ALL
					diffObjects = objectsToIndex;
				}
				else
				{
					docID = (String)docIT.next();
				}
				
				while(!finish)
				{
					
					
					if(docID!=null && docID.compareTo(crElemID) == 0)
					{
						//ELEMENT IN BOTH, CR AND INDEX
						//COMPARE UPDATE and STEP BOTH
						Integer crUpt = (Integer)CRlem.get(TIMESTAMP_ATTR);
						Integer docNR = docs.get(docID);
						Document doc = reader.document(docNR);
						String docUpt = doc.get(TIMESTAMP_ATTR);
						if(!(crUpt!=null && docUpt!=null && Integer.parseInt(docUpt)>=crUpt.intValue()))
						{
							//IF UPDATE TS DOES NOT EXIST OR TS IN INDEX SMALLER THAN IN CR
							diffObjects.add(CRlem);
						}
						finish = !docIT.hasNext();
						if(!finish)docID = (String)docIT.next();
						if(resoIT.hasNext())
						{
							CRlem = resoIT.next();
							crElemID =(String) CRlem.get(idAttribute);
						}
					}
					else if(docID!=null && docID.compareTo(crElemID) > 0 && resoIT.hasNext())
					{
						//ELEMENT NOT IN INDEX
						//ADD TO DIFF OBJECTS and STEP CR
						diffObjects.add(CRlem);
						CRlem = resoIT.next();
						crElemID =(String) CRlem.get(idAttribute);
					}
					else
					{
						//delete Document
						Integer docNR = docs.get(docID);
						reader.deleteDocument(docNR);
						finish = !docIT.hasNext();
						if(!finish)docID = (String)docIT.next();
					}
					
				}
			}
			else
			{
				//NO OBJECTS IN CONTENT REPOSITORY
				//DELETE ALL FROM INDEX
				boolean finish=!termDocs.next();
				
				while(!finish)
				{
					reader.deleteDocument(termDocs.doc());
					
					finish = !termDocs.next();
				}
			}
			termDocs.close();  // close docs iterator
		    
		}
		finally
		{
			indexAccessor.release(reader, true);
		}
		return(diffObjects);
			
	}

	
}

