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

	/**
	 * Instantiates a new velocity template wrapper.
	 * 
	 * @param template
	 *            the template
	 */
	public VelocityTemplateWrapper(Template template) {
		this.template = template;
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

}
