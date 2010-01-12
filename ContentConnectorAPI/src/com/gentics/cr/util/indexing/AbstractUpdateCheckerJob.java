package com.gentics.cr.util.indexing;

import java.io.IOException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;

import com.gentics.api.lib.datasource.Datasource;
import com.gentics.api.lib.datasource.DatasourceException;
import com.gentics.api.lib.exception.NodeException;
import com.gentics.api.lib.exception.ParserException;
import com.gentics.api.lib.expressionparser.ExpressionParser;
import com.gentics.api.lib.expressionparser.ExpressionParserException;
import com.gentics.api.lib.resolving.Resolvable;
import com.gentics.cr.CRConfig;
import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.CRException;
import com.gentics.cr.lucene.indexer.IndexerStatus;
import com.gentics.cr.lucene.indexer.index.CRLuceneIndexJob;
import com.gentics.cr.lucene.indexer.index.LockedIndexException;
import com.gentics.cr.util.CRUtil;

//TODO: complete JavaDoc when class is finished
/**
 * This class is designed as an UpdateChecker for a ContentRepository. It checks a Gentics ContentRepository for Updates and gives updated Documents to some class
 * @author perhab
 *
 */
public abstract class AbstractUpdateCheckerJob implements Runnable {

	protected static Logger log = Logger.getLogger(CRLuceneIndexJob.class);
	
	protected static final String ID_ATTRIBUTE_KEY = "IDATTRIBUTE";
	protected static final String DEFAULT_IDATTRIBUTE = "contentid";
	protected static final String TIMESTAMP_ATTR = "updatetimestamp";
	
	protected CRConfig config;
	protected String identifyer;
	protected IndexerStatus status;
	
	private Hashtable<String,CRConfigUtil> configmap;
	private IndexLocation indexLocation;
	
	private long duration = 0;
	
	
	/**
	 * TODO comment
	 * @param config
	 * @param indexLoc
	 * @param configmap
	 */
	public AbstractUpdateCheckerJob(CRConfig config, IndexLocation indexLoc,Hashtable<String,CRConfigUtil> configmap)
	{
		this.config = config;
		this.configmap = configmap;
		if(this.configmap==null)log.debug("Configmap is empty");
		this.identifyer = (String) config.getName();
		this.indexLocation = indexLoc;
		status = new IndexerStatus();
		
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
	 * Get Current Status
	 * @return
	 */
	public String getStatusString()
	{
		return status.getCurrentStatusString();
	}
	
	/**
	 * Tests if a {@link AbstractUpdateCheckerJob} has the same identifier as the given object being an instance of {@link AbstractUpdateCheckerJob}
	 */
	@Override
	public boolean equals(Object obj)
	{
		if(obj instanceof AbstractUpdateCheckerJob)
		{
			if(this.identifyer.equalsIgnoreCase(((CRLuceneIndexJob)obj).getIdentifyer()))
			{
				return true;
			}
		}
		return false;
	}
	
	protected abstract void indexCR(IndexLocation indexLocation, CRConfigUtil config) throws NodeException, CorruptIndexException, IOException, CRException, LockedIndexException;
	
	@SuppressWarnings("unchecked")
	protected Collection<Resolvable> getObjectsToUpdate(String rule, Datasource ds, boolean forceFullUpdate, IIndexUpdateChecker indexUpdateChecker){
		Collection<Resolvable> updateObjects = new Vector<Resolvable>();
		if(forceFullUpdate){
			try {
				updateObjects = (Collection<Resolvable>) ds.getResult(ds.createDatasourceFilter(ExpressionParser.getInstance().parse(rule)), null);
			} catch (DatasourceException e) {
				log.error("Error getting results for full index from datasource",e);
			} catch (ExpressionParserException e) {
				log.error("Error parsing the given rule ("+rule+") for the datasource",e);
			} catch (ParserException e) {
				log.error("Error parsing the given rule ("+rule+") for full index",e);
			}
		}
		else{
			String idAttribute = (String)config.get(ID_ATTRIBUTE_KEY);
			if(idAttribute == null)
				idAttribute = DEFAULT_IDATTRIBUTE;

			Collection<Resolvable> objectsToIndex = (Collection<Resolvable>)ds.getResult(ds.createDatasourceFilter(ExpressionParser.getInstance().parse(rule)), new String[]{TIMESTAMP_ATTR}, 0, -1, CRUtil.convertSorting("contentid:asc"));
			
			Iterator<Resolvable> resolvableIterator = objectsToIndex.iterator();
			while(resolvableIterator.hasNext())
			{
				
			}	
				
				
				
				
				
				
				
				LinkedHashMap<String,Integer> docs = fetchSortedDocs(termDocs, reader, idAttribute);
				Iterator docIT = docs.keySet().iterator();
				
				Resolvable CRlem = resolvableIterator.next();
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
						if(resolvableIterator.hasNext())
						{
							CRlem = resolvableIterator.next();
							crElemID =(String) CRlem.get(idAttribute);
						}
					}
					else if(docID!=null && docID.compareTo(crElemID) > 0 && resolvableIterator.hasNext())
					{
						//ELEMENT NOT IN INDEX
						//ADD TO DIFF OBJECTS and STEP CR
						diffObjects.add(CRlem);
						CRlem = resolvableIterator.next();
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
		}
		
		
		
		
		return updateObjects;
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

}
