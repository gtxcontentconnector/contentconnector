package com.gentics.cr.lucene.indexer.index;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;

import com.gentics.api.lib.resolving.Resolvable;
import com.gentics.cr.lucene.indexaccessor.IndexAccessor;
import com.gentics.cr.monitoring.MonitorFactory;
import com.gentics.cr.monitoring.UseCase;
import com.gentics.cr.util.indexing.IndexUpdateChecker;

/**
 * Lucene Implementation of IndexUpdateChecker.
 * Walks an Index and compares Identifyer/Timestamp pairs to the Objects in the Index
 * 
 * Last changed: $Date: 2009-09-02 17:57:48 +0200 (Mi, 02 Sep 2009) $
 * @version $Revision: 180 $
 * @author $Author: supnig@constantinopel.at $
 */
public class LuceneIndexUpdateChecker extends IndexUpdateChecker {

	LuceneIndexLocation indexLocation;
	IndexAccessor indexAccessor;
	LinkedHashMap<String, Integer> docs;
	Iterator<String> docIT;
	Vector<String> checkedDocuments;
	String idField;
	private static final Logger log = Logger.getLogger(LuceneIndexUpdateChecker.class);

	/**
	 * Initializes the Lucene Implementation of {@link IndexUpdateChecker}.
	 * @param indexLocation
	 * @param termKey - Key under wich the termValue is stored in the Index e.g. CRID
	 * @param termValue - Value wich to use for iteration e.g. CRID_1
	 * @param idAttribute - ID-Attribute key that will be used for Identifyer
	 * comparison. This has to represent the field where the identifyer in the
	 * method {@link com.gentics.cr.lucene.indexer.index.LuceneIndexUpdateChecker#checkUpToDate(String, Object, String, Resolvable)} 
	 * is present.
	 * @throws IOException 
	 */
	public LuceneIndexUpdateChecker(final LuceneIndexLocation indexLocation, final String termKey, final String termValue,
		final String idAttribute) {
		this.indexLocation = indexLocation;
		this.idField = idAttribute;
		indexAccessor = indexLocation.getAccessor();
		IndexReader reader = null;
		try {
			reader = indexAccessor.getReader();
			Term term = new Term(termKey, termValue);
			Map<String, Integer> docMap = new HashMap<String, Integer>();
			for (AtomicReaderContext rc : reader.leaves()) {
				fillDocs(rc, docMap, term);
			}
			
			log.debug("Fetching sorted documents from index...");
			docs = toSortedMap(docMap);
			log.debug("Fetched sorted docs from index");
			docIT = docs.keySet().iterator();

			checkedDocuments = new Vector<String>(100);

			//TODO CONTINUE HERE PREPARE TO USE ITERATOR IN CHECK METHOD
		} catch (Throwable e) {
			log.error("Error while retrieving termdocs. Next step: close down connection in finally block", e);
		} finally {
			if (indexAccessor != null && reader != null) {
				log.debug("Closing down indexreader with write permissions (LuceneIndexUpdateChecker instantiation failed)");
				indexAccessor.release(reader);
			}
		}
	}
	
	/**
	 * Fetch documents from atomic readers.
	 * @param rc atomic reader context
	 * @param docMap documents to fetch
	 * @param term term to search documents by
	 * @throws IOException in case of low level IO error
	 */
	private void fillDocs(AtomicReaderContext rc, Map<String, Integer> docMap, Term term) throws IOException {
		AtomicReader reader = rc.reader();
		DocsEnum termDocs = reader.termDocsEnum(term);
		int d;
		if (termDocs != null) {
			while((d = termDocs.nextDoc()) != DocsEnum.NO_MORE_DOCS) {
				Document doc = reader.document(d);
				String docID = doc.get(this.idField);
				docMap.put(docID, rc.docBase + d);
			}
		}
	}

	@Override
	protected final boolean checkUpToDate(final String identifyer, final Object timestamp, final String timestampattribute,
			final Resolvable object) {
		String timestampString;
		if (timestamp == null) {
			return false;
		} else {
			timestampString = timestamp.toString();
		}
		if ("".equals(timestampString)) {
			return false;
		}

		boolean readerWithWritePermissions = false;
		if (docs.containsKey(identifyer)) {

			Integer documentId = docs.get(identifyer);
			IndexReader reader = null;
			try {
				reader = indexAccessor.getReader();
				Document document = reader.document(documentId);
				checkedDocuments.add(identifyer);
				Object documentUpdateTimestamp = null;
				try {
					documentUpdateTimestamp = document.get(timestampattribute);
				} catch (NumberFormatException e) {
					log.debug("Got an error getting the document for " + identifyer + " from index", e);
				}
				indexAccessor.release(reader);
				//Use strings to compare the attributes
				if (documentUpdateTimestamp != null && !(documentUpdateTimestamp instanceof String)) {
					documentUpdateTimestamp = documentUpdateTimestamp.toString();
				}
				if (documentUpdateTimestamp == null || !documentUpdateTimestamp.equals(timestampString)) {
					log.debug(identifyer + ": object is not up to date.");
					return false;
				}
				log.debug(identifyer + ": object is up to date.");
				return true;
			} catch (IOException e) {
				//TODO specify witch index is not readable
				StringBuilder directories = new StringBuilder();
				Directory[] dirs = indexLocation.getDirectories();
				for (Directory dir : dirs) {
					directories.append(dir.toString() + '\n');
				}
				log.error("Cannot open index for reading. (Directory: " + directories.toString() + ")", e);
				return true;
			} finally {
				if (indexAccessor != null) {
					indexAccessor.release(reader);
					log.debug("Released reader with write permission: " + readerWithWritePermissions + " at thread: "
							+ Thread.currentThread().getName() + " - threadid: " + Thread.currentThread().getId());
				}
			}
		} else {
			//object is not yet in the index => it is not up to date
			return false;
		}
	}

	@Override
	public void deleteStaleObjects() {
		log.debug(checkedDocuments.size() + " objects checked, " + docs.size() + " objects already in the index.");
		IndexWriter writeReader = null;
		UseCase deleteStale = MonitorFactory.startUseCase("LuceneIndexUpdateChecker.deleteStaleObjects(" + indexLocation.getName() + ")");
		try {
			boolean objectsDeleted = false;
			for (String contentId : docs.keySet()) {
				if (!checkedDocuments.contains(contentId)) {
					log.debug("Object " + contentId + " wasn't checked in the last run. So i will delete it.");
					if (writeReader == null) {
						writeReader = indexAccessor.getWriter();
					}
					writeReader.deleteDocuments(new Term(this.idField,contentId));
					objectsDeleted = true;
				}
			}
			if (objectsDeleted) {
				indexLocation.createReopenFile();
			}
		} catch (IOException e) {
			log.error("Cannot delete objects from index.", e);
		} finally {
			//always release writeReader it blocks other threads if you don't 
			if (writeReader != null) {
				indexAccessor.release(writeReader);
			}
			log.debug("Finished cleaning stale documents");
			deleteStale.stop();
		}
		checkedDocuments.clear();
	}

	/**
	 * Sorts the given map by its keys.
	 * @param map map to sort.
	 * @return sorted map
	 */
	private LinkedHashMap<String, Integer> toSortedMap(Map<String, Integer> map) {
		LinkedHashMap<String, Integer> ret = new LinkedHashMap<String, Integer>(map.size());
		Vector<String> v = new Vector<String>(map.keySet());
		Collections.sort(v);
		for (String id : v) {
			ret.put(id, map.get(id));
		}
		return ret;
	}

}
