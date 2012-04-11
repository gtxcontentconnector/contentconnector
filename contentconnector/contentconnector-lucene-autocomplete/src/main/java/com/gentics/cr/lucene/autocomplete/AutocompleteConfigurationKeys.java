package com.gentics.cr.lucene.autocomplete;

/**
 * This interface holds all relevant keys for the configuration of the
 * {@link AutocompleteIndexExtension}
 * 
 * @author Sebastian Vogel <s.vogel@gentics.com>
 * 
 */
public interface AutocompleteConfigurationKeys {

	public static final String GRAMMED_WORDS_FIELD = "grammedwords";

	public static final String COUNT_FIELD = "count";

	public static final String SOURCE_WORD_FIELD = "word";

	public static final String SOURCE_INDEX_KEY = "srcindexlocation";

	public static final String AUTOCOMPLETE_INDEX_KEY = "autocompletelocation";

	public static final String AUTOCOMPLETE_FIELD_KEY = "autocompletefield";

	public static final String REINDEXSTRATEGYCLASS_KEY = "reindexStrategyClass";

	public static final String AUTOCOMPLETE_REOPEN_UPDATE = "autocompletereopenupdate";

	public static final String AUTOCOMPLETE_SUBSCRIBE_TO_INDEX_FINISHED = "reindexOnCRIndexFinished";

	public static final String AUTOCOMPLETE_USE_AUTCOMPLETE_INDEXER = "useAutocompleteIndexer";

}
