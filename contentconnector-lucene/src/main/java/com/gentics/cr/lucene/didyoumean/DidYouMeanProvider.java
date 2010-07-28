package com.gentics.cr.lucene.didyoumean;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.spell.SpellChecker;

import com.gentics.cr.CRConfig;
import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.events.Event;
import com.gentics.cr.events.EventManager;
import com.gentics.cr.events.IEventReceiver;
import com.gentics.cr.lucene.events.IndexingFinishedEvent;
import com.gentics.cr.lucene.indexaccessor.IndexAccessor;
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
	private LuceneIndexLocation source;
	private LuceneIndexLocation didyoumeanLocation;
	
	private static final String GRAMMED_WORDS_FIELD ="grammedwords";
	public static final String COUNT_FIELD="count";
	public static final String SOURCE_WORD_FIELD ="word";
	
	private static final String SOURCE_INDEX_KEY="srcindexlocation";
	private static final String DIDYOUMEAN_INDEY_KEY="didyoumeanlocation";
	
	private static final String DIDYOUMEAN_FIELD_KEY="didyoumeanfield";
	
	private String didyoumeanfield = "content";
	
	private SpellChecker spellchecker=null;
	
	public DidYouMeanProvider(CRConfig config)
	{
		GenericConfiguration src_conf = (GenericConfiguration)config.get(SOURCE_INDEX_KEY);
		GenericConfiguration auto_conf = (GenericConfiguration)config.get(DIDYOUMEAN_INDEY_KEY);
		source = LuceneIndexLocation.getIndexLocation(new CRConfigUtil(src_conf,"SOURCE_INDEX_KEY"));
		didyoumeanLocation = LuceneIndexLocation.getIndexLocation(new CRConfigUtil(auto_conf,DIDYOUMEAN_INDEY_KEY));
		
		String s_autofield = config.getString(DIDYOUMEAN_FIELD_KEY);
		if(s_autofield!=null)this.didyoumeanfield=s_autofield;
		
		try
		{
			spellchecker = new SpellChecker(didyoumeanLocation.getFirstDirectory());
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
	
	
	private void reIndex() throws IOException
	{
		// build a dictionary (from the spell package) 
		log.debug("Starting to reindex didyoumean index.");
		IndexAccessor sia = this.source.getAccessor();
        IndexReader sourceReader = sia.getReader(false);
        LuceneDictionary dict = new LuceneDictionary(sourceReader, this.didyoumeanfield); 
        try{
        	spellchecker.indexDictionary(dict);
        }
        finally{
	                
	        sia.release(sourceReader, false);
	        // close writer 
        }
        didyoumeanLocation.createReopenFile();
        log.debug("Finished reindexing didyoumean index.");
	}
	
	public void finalize()
	{
		source.finalize();
		didyoumeanLocation.finalize();
		EventManager.getInstance().unregister(this);
	}

}
