package com.gentics.cr.lucene.tagging;

public class TagTerm implements Comparable<TagTerm> {

	private String term;
	private int freq;
	
	public TagTerm(String term, int freq)
	{
		this.term = term;
		this.freq = freq;
	}
	
	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public int getFreq() {
		return freq;
	}

	public void setFreq(int freq) {
		this.freq = freq;
	}

	@Override
	public int compareTo(TagTerm obj) {
		if(obj.freq > this.freq)
			return 1;
		else if(obj.freq<this.freq)
			return -1;
		else
			return 0;
	}
	
	@Override
	public String toString()
	{
		return(this.term+":"+this.freq);
	}

}
