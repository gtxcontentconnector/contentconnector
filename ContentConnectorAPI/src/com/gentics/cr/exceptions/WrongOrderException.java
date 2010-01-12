package com.gentics.cr.exceptions;

import com.gentics.cr.CRException;
/**
 * 
 * Last changed: $Date: 2009-09-02 17:57:48 +0200 (Mi, 02 Sep 2009) $
 * @version $Revision: 180 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class WrongOrderException extends CRException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5604529440335377742L;
	
	/**
	 * Create new WrongOrderException
	 * Creates a wrapped @link CRException with the parameters
	 * ("WRONGORDER", "The checked objects have to be ascending order", CRException.ERRORTYPE.FATAL_ERROR)
	 */
	public WrongOrderException()
	{
		super("WRONGORDER", "The checked objects have to be ascending order", CRException.ERRORTYPE.FATAL_ERROR);
	}

}
