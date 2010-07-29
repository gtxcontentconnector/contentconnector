package com.gentics.cr.lucene.didyoumean;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;

import com.gentics.cr.CRConfig;
import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.events.Event;
import com.gentics.cr.events.EventManager;
import com.gentics.cr.events.IEventReceiver;
import com.gentics.cr.lucene.events.IndexingFinishedEvent;
import com.gentics.cr.lucene.indexer.index.LuceneIndexLocation;

/**
 * This class can be used to build an autocomplete index over an existing lucene index.
 * 
 * Last changed: $Date: 2010-04-01 15:20:21 +0200 (Do, 01 Apr 2010) $
 * @version $Revision: 528 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class DidYouMeanProvider implements IEventReceiver{

	protected static final Logger log = Logger.getLogger(DidYouMeanProvider.class);
	private Directory source;
	private Directory didyoumeanLocation;
	
	
	
	private static final String SOURCE_INDEX_KEY="srcindexlocation";
	private static final String DIDYOUMEAN_INDEY_KEY="didyoumeanlocation";
	
	private static final String DIDYOUMEAN_FIELD_KEY="didyoumeanfield";
	
	private String didyoumeanfield = "content";
	
	private SpellChecker spellchecker=null;
	
	public DidYouMeanProvider(CRConfig config)
	{
		GenericConfiguration src_conf = (GenericConfiguration)config.get(SOURCE_INDEX_KEY);
		GenericConfiguration auto_conf = (GenericConfiguration)config.get(DIDYOUMEAN_INDEY_KEY);
		source = LuceneIndexLocation.createDirectory(new CRConfigUtil(src_conf,"SOURCE_INDEX_KEY"));
		didyoumeanLocation = LuceneIndexLocation.createDirectory(new CRConfigUtil(auto_conf,DIDYOUMEAN_INDEY_KEY));
		
		String s_autofield = config.getString(DIDYOUMEAN_FIELD_KEY);
		if(s_autofield!=null)this.didyoumeanfield=s_autofield;
		
		try
		{
			spellchecker = new SpellChecker(didyoumeanLocation);
			reIndex();
		}
		catch(IOException e)
		{
			log.error("Could not create didyoumean index.", e);
		}
		EventManager.getInstance().register(this);
	}
	
	
	public void processEvent(Event event) {
		if(IndexingFinishedEvent.INDEXING_FINISHED_EVENT_TYPE.equals(event.getType()))
		{
			try
			{
				reIndex();
			}
			catch(IOException e)
			{
				log.error("Could not reindex didyoumean index.", e);
			}
		}
	}
	
	public SpellChecker getInitializedSpellchecker()
	{
		return this.spellchecker;
	}
	
		
	public Map<String,String[]> getSuggestions(Set<Term> termlist,int count,IndexReader reader)
	{
		Map<String,String[]> result = new LinkedHashMap<String,String[]>();
		Set<String> uniquetermset = new HashSet<String>();
		
		for(Term t:termlist)
		{
			uniquetermset.add(t.text());
		}
				
		for(String term:uniquetermset)
		{
			try
			{
				if(!this.spellchecker.exist(term))
				{
					String[] ts = this.spellchecker.suggestSimilar(term, count, reader, didyoumeanfield, true);
					if(ts!=null && ts.length>0)
					{
						result.put(term, ts);
					}
				}
			}
			catch(IOException ex)
			{
				log.error("Could not suggest terms",ex);
			}
		}
		
		return result;
	}
	
	
	private void reIndex() throws IOException
	{
		// build a dictionary (from the spell package) 
		log.debug("Starting to reindex didyoumean index.");
		
        IndexReader sourceReader = IndexReader.open(source);
        LuceneDictionary dict = new LuceneDictionary(sourceReader, this.didyoumeanfield); 
        try{
        	spellchecker.indexDictionary(dict);
        }
        finally{    
	        sourceReader.close(); 
        }
        
        log.debug("Finished reindexing didyoumean index.");
	}
	
	public void finalize()
	{
		EventManager.getInstance().unregister(this);
	}

}
