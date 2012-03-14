package com.gentics.cr.http;
 
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
 
import com.gentics.cr.CRRequest;
 
/**
 *
 * @author Markus Burchhart, s IT Solutions
 *
 */
public class GetUrlBuilder {
	final int DEFAULT_BUFFER_SIZE = 100;
	final StringBuffer sb;
	boolean first = true;
 
	public GetUrlBuilder() {
		this(null);
	}
 

	public GetUrlBuilder(String path) {
		if (path!=null) {
			sb = new StringBuffer(DEFAULT_BUFFER_SIZE + path.length());
			sb.append(path.trim());
		} else {
			sb = new StringBuffer(DEFAULT_BUFFER_SIZE);
		}
	}
 
	public GetUrlBuilder append(String key, Object value) {
		if (first) {
			sb.append('?');
			first = false;
		} else {
			sb.append('&');
		}
		sb.append(key);
		sb.append('=');
		if (value!=null) {
			sb.append(encode(value.toString().trim()));
		}
		return this;
	}
 
 
	public GetUrlBuilder append(CRRequest req, String key) {
		return append(key, req.get(key));
	}
 
	public GetUrlBuilder appendSkipNull(String key, Object value) {
		if (value!=null) {
			append(key, value);
		}
		return this;
	}
 
	public GetUrlBuilder appendSkipNull(CRRequest req, String key) {
		return appendSkipNull(key, req.get(key));
	}
 
 
	public GetUrlBuilder appendSkipFalse(String key, Object booleanValue) {
		if (booleanValue!=null) {
			boolean b = Boolean.parseBoolean(booleanValue.toString());
			if (b) {
				append(key, Boolean.toString(b));
			}
		}
		return this;
	}
 
	public GetUrlBuilder appendSkipFalse(CRRequest req, String key) {
		appendSkipFalse(key, req.get(key));
		return this;
	}
 
	public GetUrlBuilder appendArray(String key, String[] valueArray) {
		if (valueArray!=null) {
			for (String value : valueArray) {
				append(key, value);
			}
		}
		return this;
	}
	
	@Override
	public String toString() {
		return sb.toString();
	}
	
	private String encode(String str)
	{
		if(str!=null)
		{
			try {
				return URLEncoder.encode(str, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}