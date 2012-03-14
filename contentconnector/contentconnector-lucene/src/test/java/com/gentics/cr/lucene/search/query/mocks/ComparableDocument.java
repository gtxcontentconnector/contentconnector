package com.gentics.cr.lucene.search.query.mocks;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;

public class ComparableDocument {
	
	Document document;
	
	public ComparableDocument(Document doc) {
		document = doc;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Document) {
			Document givenDocument = (Document) obj;
			for(Fieldable field : givenDocument.getFields()) {
				Field myField = document.getField(field.name());
				//TODO: check binary or not stored fields to.
				if(myField == null || !field.stringValue().equals(myField.stringValue())) {
					return false;
				}
			}
			return true;
		}
		return super.equals(obj);
	}
}
