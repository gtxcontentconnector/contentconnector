package com.gentics.cr.lucene.tagging;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Collections;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;

public class TagGenerator {

	/**
	 * Returns a sorted list of terms generated from the text
	 * @param text
	 * @return
	 */
	public static List<TagTerm> getSortedTerms(String text) {
		long start = System.currentTimeMillis();
		List<TagTerm> termlist = new ArrayList<TagTerm>();
		Document doc = new Document();
		Analyzer analyzer = new StandardAnalyzer();
		Directory indexLocation = new RAMDirectory();
		try {
			IndexWriter indexWriter = new IndexWriter(indexLocation,analyzer, true,IndexWriter.MaxFieldLength.LIMITED);
			Field f = new Field("content",text, Store.YES ,Field.Index.ANALYZED,Field.TermVector.YES);
			doc.add(f);
			indexWriter.addDocument(doc);
			
			indexWriter.close();
			
			IndexReader reader = IndexReader.open(indexLocation);
			TermFreqVector v = reader.getTermFreqVector(0, "content");
			if(v!=null)
			{
				int[] freqs = v.getTermFrequencies();
				
				String[] terms = v.getTerms();
				for (int i=0;i<v.size();i++)
				{
					termlist.add(new TagTerm(terms[i],freqs[i]));
				}
				
				Collections.sort(termlist);
			}
		} catch (CorruptIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LockObtainFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long end = System.currentTimeMillis();
		System.out.println("doTime: "+(end-start)+"ms");
		return termlist;
	}
	
}
