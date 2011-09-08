package com.gentics.cr.lucene;

import java.util.Arrays;
import java.util.Collection;

import org.apache.lucene.document.Document;

import com.gentis.cr.lucene.search.query.mocks.ComparableDocument;

import junit.framework.TestCase;

public class AbstractLuceneTest extends TestCase {
	
	protected void containsAll(Collection<Document> matchedDocuments,
			ComparableDocument[] documents) {
		assertTrue(matchedDocuments.containsAll(Arrays.asList(documents)));
		assertTrue(matchedDocuments.size() == documents.length);
		
	}

	protected void containsOnly(Collection<Document> matchedDocuments, ComparableDocument containedDocument) {
		assertTrue(matchedDocuments.contains(containedDocument));
		assertTrue(matchedDocuments.size() == 1);
		
	}
}
