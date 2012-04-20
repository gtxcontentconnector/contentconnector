package com.gentics.cr.lucene.search.query.mocks;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;

public class ComparableDocument {

	Document document;

	public ComparableDocument(Document doc) {
		document = doc;
	}
	
	public Document getDocument() {
		return document;
	}
	
	@Override
	public int hashCode() {
		return document.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ComparableDocument) {
			Document givenDocument = ((ComparableDocument) obj).getDocument();
			for (Fieldable field : givenDocument.getFields()) {
				Field myField = document.getField(field.name());
				//TODO: check binary or not stored fields to.
				if (myField == null || !field.stringValue().equals(myField.stringValue())) {
					return false;
				}
			}
			return true;
		}
		return super.equals(obj);
	}
}
