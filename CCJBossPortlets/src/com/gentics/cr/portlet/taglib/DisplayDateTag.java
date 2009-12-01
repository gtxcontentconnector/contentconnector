package com.gentics.cr.portlet.taglib;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import com.gentics.cr.CRResolvableBean;

/**
 * Displays a date by a given format out of a timestamp
 * 
 * @author philipp
 */
public class DisplayDateTag extends SimpleTagSupport {
	/**
	 * The object which holds the different attributes to display
	 */
	protected CRResolvableBean object;

	/**
	 * Holds the name of the attribute where the timestamp is fetched from. <br />
	 * Per default "createtimestamp"
	 */
	protected String timestampAttribute = "createtimestamp";

	/**
	 * The format of the rendered Date
	 */
	protected String format = "";

	/**
	 * Set the object to be rendered. Must be an instance of
	 * {@link CRResolvableBean}.
	 * 
	 * @param object
	 *            rendered object
	 */
	public void setObject(Object object) {
		if (object instanceof CRResolvableBean) {
			this.object = (CRResolvableBean) object;
		}
	}

	/**
	 * Sets the name of the attribute from where the timestamp is fetched from
	 * 
	 * @param timestampAttribute
	 *            Name of the timestamp-attribute in the object
	 */
	public void setTimestampAttribute(String timestampAttribute) {
		this.timestampAttribute = timestampAttribute;
	}

	/**
	 * Set the format of the rendered date
	 * 
	 * @param format
	 *            A java date-format
	 */
	public void setFormat(String format) {
		this.format = format;
	}

	/**
	 * Displays a formatted date out of a timestamp 
	 * 
	 * @see javax.servlet.jsp.tagext.SimpleTagSupport#doTag()
	 */
	public void doTag() throws JspException, IOException {
		JspWriter out = getJspContext().getOut();

		if (object != null) {
			try {
				long timestamp = Long.parseLong(object.get(this.timestampAttribute).toString());

				SimpleDateFormat formatter = new SimpleDateFormat(this.format);
				Date date = new Date(timestamp * 1000);
				
				out.write(formatter.format(date));
			} catch (ClassCastException cce) {
				out.write("-- timestamp is not an integer --");
			} catch (NullPointerException e) {
				out.write("-- timestamp not valid: "+object.get(this.timestampAttribute)+" --");
			}
		} else {
			getJspContext().getOut().write(" -- no object set --");
		}
	}
}
