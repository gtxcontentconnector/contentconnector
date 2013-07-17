package com.gentics.cr.mccr;

import java.util.Comparator;

import com.gentics.api.lib.datasource.Datasource;
import com.gentics.api.lib.resolving.Resolvable;

public class PNSortingComparator<T extends Resolvable> implements Comparator<Resolvable> {

	private String columnName;
	private int sortOrder;
	
	public PNSortingComparator(String columnName, int sortOrder) {
		this.columnName = columnName;
		this.sortOrder = sortOrder;
	}
	
	
	@Override
	public int compare(Resolvable o1, Resolvable o2) {
		Comparable key1 = (Comparable) o1.get(this.columnName);
		Comparable key2 = (Comparable) o2.get(this.columnName);
		if (this.sortOrder == Datasource.SORTORDER_DESC) {
			return key2.compareTo(key1);
		} else {
			return key1.compareTo(key2);
		}
	}

}
