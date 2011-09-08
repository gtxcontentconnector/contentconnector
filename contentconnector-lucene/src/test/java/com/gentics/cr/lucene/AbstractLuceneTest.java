package com.gentics.cr.lucene;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import junit.framework.TestCase;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import com.gentis.cr.lucene.search.query.mocks.ComparableDocument;
import com.gentis.cr.lucene.search.query.mocks.SimpleLucene;

public abstract class AbstractLuceneTest extends TestCase {
	
	private Collection<Document> documents;
	private Document document1;
	private Document document3;
	private Document document2;

	public AbstractLuceneTest(String name) {
		super(name);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		document1 = new Document();
		document1.add(new Field(SimpleLucene.CONTENT_ATTRIBUTE, "document1", Field.Store.YES, Field.Index.ANALYZED));
		document2 = new Document();
		document2.add(new Field(SimpleLucene.CONTENT_ATTRIBUTE, "document2", Field.Store.YES, Field.Index.ANALYZED));
		document3 = new Document();
		document3.add(new Field(SimpleLucene.CONTENT_ATTRIBUTE, "document3", Field.Store.YES, Field.Index.ANALYZED));
		
		documents = new ArrayList<Document>();
		documents.add(document1);
		documents.add(document2);
		
	}
	
	public void selfTest() {
		containsAll(documents, new ComparableDocument[]{
				new ComparableDocument(document1),
				new ComparableDocument(document2),
				new ComparableDocument(document3)});
		documents.remove(document2);
		documents.remove(document3);
		containsOnly(documents, new ComparableDocument(document1));
	}

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
