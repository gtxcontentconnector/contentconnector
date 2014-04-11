/*
 * 
 */
package com.gentics.cr.template;

import java.io.Serializable;

import org.apache.velocity.Template;

/**
 * The Class VelocityTemplateWrapper.
 */
public class VelocityTemplateWrapper implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -6870612259011831815L;

	/** The template. */
	private Template template;
	/** The source of the template. */
	private String source;

	/**
	 * Instantiates a new velocity template wrapper.
	 * 
	 * @param template
	 *            the template
	 * @param source the source of the template
	 */
	public VelocityTemplateWrapper(Template template, String source) {
		this.template = template;
		this.source = source;
	}

	/**
	 * Gets the template.
	 * 
	 * @return the template
	 */
	public Template getTemplate() {
		return template;
	}

	/**
	 * Sets the template.
	 * 
	 * @param template
	 *            the new template
	 */
	public void setTemplate(Template template) {
		this.template = template;
	}
	/**
	 * Returns the source of the template
	 * 
	 * @return the source
	 */
	public String getSource() {
	    return source;
	    
	}

}
