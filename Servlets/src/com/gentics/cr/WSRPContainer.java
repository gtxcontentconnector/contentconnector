package com.gentics.cr;

import com.gentics.cr.exceptions.CRException;

public class WSRPContainer {

	private static CRConfigFileLoader crConf;
	
	private static RequestProcessor rp;
	
	public static RequestProcessor getRP()
	{
		if(rp==null)
		{
			crConf = new CRConfigFileLoader("Webservice","");
			try {
				rp = crConf.getNewRequestProcessorInstance(1);
			} catch (CRException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return rp;
	}
}
