package com.gentics.cr.lucene.indexer.transformer.other;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.lucene.indexer.transformer.ContentTransformer;
import com.gentics.cr.util.CRUtil;



/**
 * Transforms a date string to a timestamp int and vice versa
 * 
 * @author Sebastian Vogel <s.vogel@gentics.com>
 * 
 */
public class DateTimestampTransformer extends ContentTransformer {
	// which contentrepository attribute should be mapped
	public static final String TRANSFORMER_SOURCE_ATTRIBUTE_KEY = "sourceattribute";
	// the name of the attribute where the value should be stored (optional) if not provided source attribute will be used
	public static final String TRANSFORMER_TARGET_ATTRIBUTE_KEY = "targetattribute";
	/*
	 *  what should be converted 2 values allowed - "timestamp" (default) and "date"
	 *  "timestamp" will try to convert a formatted date string to a timestamp int and
	 *  "date" will try to convert an int timestamp to a formatted date string
	 */
	public static final String TRANSFORMER_CONVERT_TO_KEY = "convertto";
	// the format of the date input (when "convertto=timestamp") or the date output (when "convertto=date") - default "dd.MM.yy"
	public static final String TRANSFORMER_DATE_FORMAT_KEY = "dateformat";
	// fallback attribute (optional): the value stored in this attribute is used when conversion fails  
	public static final String TRANSFORMER_FALLBACK_ATTRIBUTE_KEY = "fallbackattribute";
	// fallback value (optional): alternatively a static value can be provided which is used when conversion fails (and no fallback attribute was provided)
	public static final String TRANSFORMER_FALLBACK_VALUE_KEY = "fallbackvalue";
	// seconds: true if timestamp is provided in seconds (if false milliseconds are used) - default true
	public static final String TRANSFORMER_SECONDS_KEY = "seconds";
	// define a custom locale for the date formatter (default: locale.language=de) 
	public static final String TRANSFORMER_LOCALE_LANGUAGE_KEY = "locale.language";
	public static final String TRANSFORMER_LOCALE_COUNTRY_KEY = "locale.country";
	
	private final String defaultLanguage = "de";
	
	private String sourceAttribute = null;
	private String targetAttribute = null;
	private String convertTo = "timestamp";
	private String dateFormat = "dd.MM.yy";
	private String fallbackAttribute = null;
	private String fallbackValue = null;
	private boolean seconds = true;
	private DateFormat formatter;

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
	}

	@Override
	public void processBean(CRResolvableBean bean) throws CRException {
		// don't do anything when there is no source attribute
		if (this.sourceAttribute == null || "".equals(this.sourceAttribute)) {
			LOGGER.error("DateTimestampTransformer: Configured attributes are null or empty. Bean will not be processed");
			return;
		}
		Object obj = bean.get(this.sourceAttribute);
		if (obj != null && getStringContents(obj) != null && !"".equals(getStringContents(obj).trim())) {
			// do date to time conversion
			String sourceValue = getStringContents(obj).trim();
			Object targetValue = null;
			try {
				if (this.convertTo == "timestamp") {
					// do date to timestamp conversion
					Long timeStamp = this.formatter.parse(sourceValue).getTime();
					if(this.seconds) {
						timeStamp /= 1000;
					}
					targetValue = timeStamp;
				} else {
					// do timestamp to date conversion
					Long timeStamp = Long.parseLong(sourceValue);
					if(this.seconds) {
						timeStamp *= 1000;
					}
					targetValue = this.formatter.format(new Timestamp(timeStamp));
				}				
			} catch (Exception e) {
				if(LOGGER.isInfoEnabled()) {
					LOGGER.info("DateTimestampTransformer: Could not convert values to date or timestamp");
					LOGGER.info(e.getMessage());
				}				
				if (fallbackValue != null) {
					targetValue = fallbackValue;
					LOGGER.debug("DateTimestampTransformer: used fallback value");
				} else if (fallbackAttribute != null) {
					Object fallbackObj = bean.get(this.fallbackAttribute);
					if (fallbackObj != null && getStringContents(fallbackObj) != null) {
						targetValue = getStringContents(fallbackObj);
						LOGGER.debug("DateTimestampTransformer: used fallback attribute");
					}
				}
			}
			if (targetValue != null) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("DateTimestampTransformer: converted " + sourceValue + " to " + targetValue);
				}				
				bean.set(this.targetAttribute, targetValue);
			}
		}

	}

	public DateTimestampTransformer(GenericConfiguration config) {
		super(config);
		String language = this.defaultLanguage;
		String country = null;
		Locale locale = null;
		String targetAttribute = config.getString(TRANSFORMER_TARGET_ATTRIBUTE_KEY, "");
		
		this.sourceAttribute = config.getString(TRANSFORMER_SOURCE_ATTRIBUTE_KEY, "");
		if (!"".equals(this.sourceAttribute)) {
			LOGGER.error("DateTimestampTransformer: Please configure " + TRANSFORMER_SOURCE_ATTRIBUTE_KEY + " for my config.");
		}
		// if target attribute is not set or empty then use the source attribute
		if (!"".equals(targetAttribute)) {
			this.targetAttribute = targetAttribute;
		} else {
			this.targetAttribute = sourceAttribute;
		}
		// overwrite default value only when "date" is set
		if ("date".equals(config.getString(TRANSFORMER_CONVERT_TO_KEY))) {
			this.convertTo = config.getString(TRANSFORMER_CONVERT_TO_KEY);
		}
		
		this.dateFormat= config.getString(TRANSFORMER_DATE_FORMAT_KEY, this.dateFormat);
		
		if (!"".equals(config.getString(TRANSFORMER_LOCALE_LANGUAGE_KEY, ""))) {
			language = config.getString(TRANSFORMER_LOCALE_LANGUAGE_KEY);
		}
		if (!"".equals(config.getString(TRANSFORMER_LOCALE_COUNTRY_KEY, ""))) {
			country = config.getString(TRANSFORMER_LOCALE_COUNTRY_KEY);
		}
		if (country != null) {
			locale = new Locale(language, country);
		} else {
			locale = new Locale(language);
		}
		
		this.formatter = new SimpleDateFormat(this.dateFormat, locale);

		if (!"".equals(config.getString(TRANSFORMER_FALLBACK_ATTRIBUTE_KEY, ""))) {
			this.fallbackAttribute = config.getString(TRANSFORMER_FALLBACK_ATTRIBUTE_KEY);
		}
		// if targetAttribute is not set use the target value
		if (this.fallbackAttribute == null) {
			this.fallbackValue = config.getString(TRANSFORMER_FALLBACK_VALUE_KEY);
		}
		this.seconds = config.getBoolean(TRANSFORMER_SECONDS_KEY, true);	
	}

	private String getStringContents(Object obj) {
		String str = "";
		if (obj instanceof String) {
			str = (String) obj;
		} else if (obj instanceof Number) {
			str = obj.toString();
		} else if (obj instanceof Date) {
			str = Long.toString(((Date) obj).getTime());
		} else if (obj instanceof byte[]) {
			try {
				str = CRUtil.readerToString(new InputStreamReader(
						new ByteArrayInputStream((byte[]) obj)));
			} catch (IOException e) {
				LOGGER.error(
						"DateTimestampTransformer: could not read from byte array",
						e);
				e.printStackTrace();
			}
		}
		return str;
	}

}
