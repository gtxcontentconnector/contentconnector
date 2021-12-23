package com.gentics.cr.lucene.indexer.transformer.other;

import java.net.URISyntaxException;
import java.util.Calendar;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.conf.gentics.ConfigDirectory;
import com.gentics.cr.exceptions.CRException;

public class DateTimestampTransformerTest {
	public static long local = 946681200L;

	public static String formatted = "";

	@BeforeClass
	public static void setupOnce() {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(local * 1000L);

		int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
		int month = cal.get(Calendar.MONTH) + 1;
		int year = cal.get(Calendar.YEAR);

		if (year >= 2000) {
			year -= 2000;
		} else if (year < 2000) {
			year -= 1900;
		}

		formatted = String.format("%02d.%02d.%02d", dayOfMonth, month, year);
	}

	@Before
	public void init() throws URISyntaxException {
		ConfigDirectory.useThis();

	}

	@Test
	public void testTimestampToDate() throws CRException {
		CRResolvableBean beanToProcess = new CRResolvableBean();
		beanToProcess.set("src", local);
		CRConfigUtil conf = new CRConfigUtil();
		conf.set("sourceattribute", "src");
		conf.set("targetattribute", "target");
		conf.set("convertto", "date");
		DateTimestampTransformer transformer = new DateTimestampTransformer(conf);

		transformer.processBean(beanToProcess);

		Assert.assertEquals("Timestamp has not been converted properly.", formatted, beanToProcess.get("target"));
	}

	@Test
	public void testDateToTimestamp() throws CRException {
		CRResolvableBean beanToProcess = new CRResolvableBean();

		beanToProcess.set("src", formatted);
		CRConfigUtil conf = new CRConfigUtil();
		conf.set("sourceattribute", "src");
		conf.set("targetattribute", "target");
		conf.set("convertto", "timestamp");
		DateTimestampTransformer transformer = new DateTimestampTransformer(conf);

		transformer.processBean(beanToProcess);

		Assert.assertEquals("Timestamp has not been converted properly.", local, beanToProcess.get("target"));
	}
}
