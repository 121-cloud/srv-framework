package otocloud.framework.app.common;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class BoPagingCounts {

	public BoPagingCounts() {
		this.total = new AtomicLong(0);
		this.subTotals = new ArrayList<Integer>();
	}

	public AtomicLong total;
	
	public List<Integer> subTotals;
	
}
