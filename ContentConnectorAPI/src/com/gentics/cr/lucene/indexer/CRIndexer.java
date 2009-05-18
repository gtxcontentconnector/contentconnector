package com.gentics.cr.lucene.indexer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.FSDirectory;

import com.gentics.api.lib.exception.NodeException;
import com.gentics.api.lib.expressionparser.ExpressionParser;
import com.gentics.api.lib.resolving.Resolvable;
import com.gentics.contentnode.content.GenticsContentFactory;
import com.gentics.contentnode.datasource.CNWriteableDatasource;
import com.gentics.cr.util.CRUtil;

/**
 * 
 * Last changed: $Date$
 * @version $Revision$
 * @author $Author$
 *
 */
public class CRIndexer {
	/**
	 * Key to be used for saving state to contentstatus
	 */
	public final static String PARAM_LASTINDEXRUN = "lastindexrun";
	/**
	 * Key to be used for saving state to contentstatus
	 */
	public final static String PARAM_LASTINDEXRULE = "lastindexrule";

	protected HashMap<String,IndexerCRConfig> crConfigs = new HashMap<String,IndexerCRConfig>();
	
	protected String indexLocation = null;
	
	protected int interval = -1;
	
	protected String name = null;
	
	protected int batchSize = 1000;
	
	protected Logger log = Logger.getLogger("com.gentics.cr.lucene.indexer");
	
	protected IndexerStatus status = new IndexerStatus();
	
	protected BackgroundJob indexerJob;
	
	protected Thread backgroundThread;
	
	protected boolean periodicalRun=false;
	
	
	/**
	 * Create new instance of CRIndexer
	 * @param name name of the CRIndexer
	 */
	public CRIndexer(String name)
	{
		this.name = name;
		loadConfig();
		indexerJob = new BackgroundJob(this.periodicalRun);
		backgroundThread = new Thread(indexerJob);
		//initializes background job
	}
	
	/**
	 * @return true if the background index job has been started
	 */
	public boolean isStarted()
	{
		return(this.indexerJob.isStarted());
	}
	
	
	/**
	 * starts the background index job
	 */
	public void startJob()
	{
		this.periodicalRun=true;
		this.indexerJob.stop=false;
		this.backgroundThread.start();
		try {
			Thread.sleep(5);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/**
	 * @return IndexerStatus of current Indexer
	 */
	public IndexerStatus getStatus()
	{
		return(this.status);
	}
	
	/**
	 * 
	 * @return indexing interval 
	 */
	public int getInterval()
	{
		return(this.interval);
	}
	
	/**
	 * stops the background indexing job
	 */
	public void stopJob()
	{
		this.periodicalRun=false;
		this.indexerJob.stop=true;
	}
	
	/**
	 * starts a single indexing run
	 */
	public void startSingleRun()
	{
		//TODO check if periodical backgroundjob is started and only execute if not
		this.indexerJob.runSingle();
	}
	
	protected void loadConfig()
	{
		Properties props = new Properties();
		try {
			String confpath = CRUtil.resolveSystemProperties("${com.gentics.portalnode.confpath}/rest/"+this.name+".properties");
			
			props.load(new FileInputStream(confpath));
			
			for (Iterator<Entry<Object,Object>> i = props.entrySet().iterator() ; i.hasNext() ; ) {
				Map.Entry<Object,Object> entry = (Entry<Object,Object>) i.next();
				Object value = entry.getValue();
				Object key = entry.getKey();
				this.setProperty((String)key, (String)value);
			}
			
		} catch (FileNotFoundException e1) {
			this.log.error("Could not load configuration file at: "+CRUtil.resolveSystemProperties("${com.gentics.portalnode.confpath}/rest/"+this.name+".properties")+"!");
		} catch (IOException e1) {
			this.log.error("Could not load configuration file at: "+CRUtil.resolveSystemProperties("${com.gentics.portalnode.confpath}/rest/"+this.name+".properties")+"!");
		}catch(NullPointerException e){
			this.log.error("Could not load configuration file at: "+CRUtil.resolveSystemProperties("${com.gentics.portalnode.confpath}/rest/"+this.name+".properties")+"!");
			e.printStackTrace();
		}
	}
	
	protected void setProperty(String key, String value)
	{
		if(key instanceof String)
		{
			if(key.toUpperCase().equals("INDEXLOCATION"))
			{
				this.indexLocation = value;
			}
			else if(key.toUpperCase().equals("INTERVAL"))
			{
				this.interval = Integer.parseInt(value);
			}
			else if(key.toUpperCase().equals("BATCHSIZE"))
			{
				this.batchSize = Integer.parseInt(value);
			}
			else if("PERIODICAL".equalsIgnoreCase(key))
			{
				if("TRUE".equalsIgnoreCase(value))
					this.periodicalRun = true;
			}
			else if(key.toUpperCase().startsWith("CR"))
			{
				// DO CR SPECIFIC CONFIG
				String[] keyArr = key.split("\\.");
				if(keyArr.length>=3)
				{
					String crID = keyArr[1];
					String newKey = keyArr[2];
					IndexerCRConfig crConfig;
					if(this.crConfigs.containsKey(crID))
					{
						crConfig = this.crConfigs.get(crID);
					}
					else
					{
						this.crConfigs.put(crID, new IndexerCRConfig(crID));
						crConfig = this.crConfigs.get(crID);
					}
					
					if("DS-HANDLE".equals(newKey.toUpperCase()))
					{
						if(keyArr.length>=4)
						{
							crConfig.putDatasourceHandleProperty(keyArr[3], value);
						}
					}
					else if ("RULE".equals(newKey.toUpperCase()))
					{
						crConfig.setRule(value);
					}
					else if	("INDEXEDATTRIBUTES".equals(newKey.toUpperCase()))
					{
						crConfig.setIndexedAttributes(value.split(","));
					}
					else if("CONTAINEDATTRIBUTES".equals(newKey.toUpperCase()))
					{
						crConfig.setContainedAttributes(value.split(","));
					}
					else if("IDATTRIBUTE".equals(newKey.toUpperCase()))
					{
						crConfig.setIdattribute(value);
					}
					else if("HTMLATTRIBUTE".equals(newKey.toUpperCase()))
					{
						crConfig.setHtmlattribute(value);
					}
					else if("STOPWORDFILE".equalsIgnoreCase(newKey))
					{
						crConfig.setStopwordfilepath(value);
					}
				}
				
			}
		}
	}
	
	protected class BackgroundJob implements Runnable{

		/**
		 * set to true if the backgroundjob has to stop
		 */
		public boolean stop = false;
		
		/**
		 * Create new instace of Backgroundjob
		 * @param startPeriodicalRun true if the Backgroundjob should be periodical
		 */
		public BackgroundJob(boolean startPeriodicalRun)
		{
			this.stop = !startPeriodicalRun;
		}
		
		/**
		 * 
		 * @return true if the backgroundjob has been started
		 */
		public boolean isStarted()
		{
			return(!this.stop);
		}
		
		/**
		 * Run single index creation run
		 */
		public void runSingle()
		{
			Thread d = new Thread(new Runnable(){

				public void run() {
					recreateIndex();
				}
				
			});
			d.start();
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		/**
		 * Run periodical index run
		 */
		public void run() {
			boolean interrupted = false;

			while (!interrupted && !stop && interval>0) {
				try {
					// recreate the search index
					recreateIndex();
					// Wait for next cycle
					Thread.sleep(interval * 1000);
				} catch (InterruptedException e) {
					interrupted = true;
				}
			}
			this.stop=true;
		}

		private void recreateIndex() {
			status.setRunning(true);
			long startTime = System.currentTimeMillis();
			status.setStartTime(new Date());
			
			int timestamp = (int)(System.currentTimeMillis() / 1000);

			// create an index writer
			File indexLoc = new File(indexLocation);
					
			

			for (Iterator<IndexerCRConfig> iterator = crConfigs.values().iterator(); iterator
					.hasNext();) {
				IndexerCRConfig crConfig = iterator.next();
				try {
					indexCR(indexLoc, timestamp, crConfig);
				} catch (Exception e){
					log.error("Error while recreating index for "+crConfig.getCrName());
					e.printStackTrace();
				}
			}

				
			
			long endTime = System.currentTimeMillis();
			status.setLastRunDuration(endTime-startTime);
			status.reset();
		}

		@SuppressWarnings("unchecked")
		private void indexCR(File indexLocation, int timestamp, IndexerCRConfig config)
				throws NodeException, CorruptIndexException, IOException {
			// get the datasource
			CNWriteableDatasource ds = config.getDatasource();

			// get the last index timestamp
			int lastIndexRun = ds.getIntContentStatus(name + "."
					+ PARAM_LASTINDEXRUN);

			// get the last index rule
			String lastIndexRule = ds.getStringContentStatus(name + "."
					+ PARAM_LASTINDEXRULE);

			if (lastIndexRule == null) {
				lastIndexRule = "";
			}
				

			// and get the current rule
			String rule = config.getRule();

			if (rule == null) {
				rule = "";
			}
			
			boolean create = true;
			
			if(indexLocation.exists() && indexLocation.isDirectory())
				create=false;

			// check whether the rule has changed, if yes, do a full index run
			boolean doFullIndexRun = lastIndexRun <= 0 || !lastIndexRule.equals(rule);

			if (rule.length() == 0) {
				rule = "(1 == 1)";
			} else {
				rule = "(" + rule + ")";
			}
			
			//Clear Index and remove stale Documents
			if(!create)
			{
				
				try {
					cleanIndex(ds,rule,indexLocation,config);
				} catch (Exception e) {
					log.error("ERROR while cleaning index");
					e.printStackTrace();
				}
				
			}
			
			
			
			if (!doFullIndexRun) {
				// do a differential index run, so just get the objects modified since the last index run
				rule += " AND (object.updatetimestamp > " + lastIndexRun
						+ " AND object.updatetimestamp <= " + timestamp + ")";
			}
			
			
			
			//Update/add Documents
			StandardAnalyzer analyzer;
			//Load StopWordList
			File stopWordFile = config.getStopWordFile();
			if(stopWordFile!=null)
			{
				//initialize Analyzer with stop words
				analyzer = new StandardAnalyzer(stopWordFile);
			}
			else
			{
				//if no stop word list exists load fall back
				analyzer = new StandardAnalyzer();
			}
			
			IndexWriter indexWriter = new IndexWriter(indexLocation,
					analyzer, create,
					IndexWriter.MaxFieldLength.LIMITED);

			// prepare the map of indexed/stored attributes
			Map<String,Boolean> attributes = new HashMap<String,Boolean>();
			List<String> containedAttributes = config.getContainedAttributes();
			List<String> indexedAttributes = config.getIndexedAttributes();

			// first put all indexed attributes into the map
			for (Iterator<String> iterator = indexedAttributes.iterator(); iterator
					.hasNext();) {
				String name =  iterator.next();
				attributes.put(name, Boolean.FALSE);
			}

			// now put all contained attributes
			for (Iterator<String> iterator = containedAttributes.iterator(); iterator
					.hasNext();) {
				String name =  iterator.next();
				attributes.put(name, Boolean.TRUE);
			}

			// finally, put the "contentid" (always contained)
			attributes.put(config.getIdattribute(), Boolean.TRUE);

			// get all objects to index
			Collection<Resolvable> objectsToIndex = (Collection<Resolvable>) ds.getResult(ds
					.createDatasourceFilter(ExpressionParser.getInstance()
							.parse(rule)), null);

			status.setObjectCount(objectsToIndex.size());
			// TODO now get the first batch of objects from the collection
			// (remove them from the original collection) and index them
			Collection<Resolvable> slice = new Vector(batchSize);
			int sliceCounter = 0;

			for (Iterator<Resolvable> iterator = objectsToIndex.iterator(); iterator.hasNext();) {
				Resolvable obj = iterator.next();
				slice.add(obj);
				iterator.remove();
				sliceCounter++;

				if (sliceCounter == batchSize) {
					// index the current slice
					indexSlice(indexWriter, slice, attributes, ds, create,config);
					status.setObjectsDone(status.getObjectsDone()+slice.size());
					// clear the slice and reset the counter
					slice.clear();
					sliceCounter = 0;
				}
			}

			if (!slice.isEmpty()) {
				// index the last slice
				indexSlice(indexWriter, slice, attributes, ds, create,config);
			}
			indexWriter.optimize();
			indexWriter.close();
		}

		private void indexSlice(IndexWriter indexWriter, Collection<Resolvable> slice,
				Map<String,Boolean> attributes, CNWriteableDatasource ds, boolean create, IndexerCRConfig config) throws NodeException,
				CorruptIndexException, IOException {
			// prefill all needed attributes
			GenticsContentFactory.prefillContentObjects(ds, slice,
					(String[]) attributes.keySet().toArray(
							new String[attributes.keySet().size()]));

			for (Iterator<Resolvable> iterator = slice.iterator(); iterator.hasNext();) {
				Resolvable objectToIndex =  iterator.next();
				
				if(!create)
				{
					indexWriter.updateDocument(new Term(config.getIdattribute(), (String)objectToIndex.get(config.getIdattribute())), getDocument(objectToIndex, attributes,config));
				}
				else
				{
					indexWriter.addDocument(getDocument(objectToIndex, attributes, config));
				}
			}
		}

		private Document getDocument(Resolvable resolvable, Map<String,Boolean> attributes, IndexerCRConfig config) {
			Document doc = new Document();
			for (Iterator<Entry<String,Boolean>> iterator = attributes.entrySet().iterator(); iterator
					.hasNext();) {
				Map.Entry<String,Boolean> entry = (Map.Entry<String,Boolean>) iterator.next();

				String attributeName = (String) entry.getKey();
				Boolean storeField = (Boolean) entry.getValue();

				Object value = resolvable.getProperty(attributeName);

				//TODO make indexfield configurable
				if(config.getIdattribute().equalsIgnoreCase(attributeName))
				{
					doc.add(new Field(config.getIdattribute(), (String)value, Field.Store.YES, Field.Index.NOT_ANALYZED));
				}
				else if (value instanceof String) {
					String val = (String) value;
					if(config.getHtmlattribute()!=null && attributeName.equalsIgnoreCase(config.getHtmlattribute()))
					{
						
						doc.add(new Field(attributeName, new HTMLStripReader(new StringReader((String)value))));
					}
					else
					{
						doc.add(new Field(attributeName, val, storeField
							.booleanValue() ? Store.YES : Store.NO,
							Field.Index.ANALYZED));
					}
				} else if (value instanceof byte[]) {
					// currently just ignore binary data
				} else if (value instanceof Number) {
					doc.add(new Field(attributeName, value.toString(),
							storeField.booleanValue() ? Store.YES : Store.NO,
							Field.Index.ANALYZED));
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
		@SuppressWarnings("unchecked")
		private void cleanIndex(CNWriteableDatasource ds, String rule, File indexLocation, IndexerCRConfig config) throws Exception
		{
				
			IndexReader reader = IndexReader.open(FSDirectory.getDirectory(indexLocation), false);// open existing index
			
			TermEnum uidIter = reader.terms(new Term(config.getIdattribute(), "")); // init uid iterator
			
			Collection<Resolvable> objectsToIndex = (Collection<Resolvable>)ds.getResult(ds.createDatasourceFilter(ExpressionParser.getInstance().parse(rule)), null, 0, -1, CRUtil.convertSorting("contentid:asc"));
			
			Iterator<Resolvable> resoIT = objectsToIndex.iterator();
			
			Resolvable CRlem = resoIT.next();
			String crElemID =(String) CRlem.get(config.getIdattribute());
			
			//solange index id kleiner cr id delete from index
			boolean finish=false;
			
			while(!finish)
			{
				
				if(uidIter.term() != null && uidIter.term().field() == config.getIdattribute() && uidIter.term().text().compareTo(crElemID) == 0)
				{
					//step both
					finish = !uidIter.next();
					if(resoIT.hasNext())
					{
						CRlem = resoIT.next();
						crElemID =(String) CRlem.get(config.getIdattribute());
					}
				}
				else if(uidIter.term() != null && uidIter.term().field() == config.getIdattribute() && uidIter.term().text().compareTo(crElemID) > 0 && resoIT.hasNext())
				{
					//step cr
					CRlem = resoIT.next();
					crElemID =(String) CRlem.get(config.getIdattribute());
					
				}
				else
				{
					//delete UIDITER
					reader.deleteDocuments(uidIter.term());
					finish = !uidIter.next();
				}
				
			}
			uidIter.close();  // close uid iterator
		    reader.close();	//close reader
		    
				
		}
	}

	/**
	 * 
	 * @return true if running periodical indexing jobs
	 */
	public boolean isPeriodicalRun() {
		return periodicalRun;
	}

	/**
	 * 
	 * @param periodicalRun true when to run periodical indexing jobs
	 */
	public void setPeriodicalRun(boolean periodicalRun) {
		this.periodicalRun = periodicalRun;
	}
}
