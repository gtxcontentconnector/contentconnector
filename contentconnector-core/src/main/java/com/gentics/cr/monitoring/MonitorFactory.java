package com.gentics.cr.monitoring;

import java.text.DecimalFormat;

import com.gentics.cr.configuration.GenericConfiguration;

public class MonitorFactory {

	private static boolean monitorenabled = false;
	private static final String MONITOR_ENABLE_KEY="monitoring";
	
	public static synchronized void init(GenericConfiguration config)
	{
		String s_mon = config.getString(MONITOR_ENABLE_KEY);
		if(s_mon!=null)
		{
			monitorenabled = Boolean.parseBoolean(s_mon);
		}
	}
	
	public static UseCase startUseCase(String identifyer)
	{
		if(monitorenabled)
		{
			return new UseCase(com.jamonapi.MonitorFactory.start(identifyer),monitorenabled);
		}
		return new UseCase(null,monitorenabled);
		
	}
	
	public static String getSimpleReport()
	{
		String ret = "<table class=\"report_table\">";
		int max_field = 7;
		
		String[] header = com.jamonapi.MonitorFactory.getHeader();
		ret+="<tr>";
		for(int i=0;i<=max_field;i++)
		{
			ret+="<th>"+header[i]+"</th>";
		}
		ret+="</tr>";
		
		Object[][] data = com.jamonapi.MonitorFactory.getData();
		
		DecimalFormat output = new DecimalFormat("#0.00");
		
		for(int j=0;j<data.length;j++)
		{
			if((j%2) == 0)
				ret+="<tr class=\"even\">";
			else
				ret+="<tr class=\"odd\">";
			for(int i=0;i<=max_field;i++)
			{
				Object obj = data[j][i];
				
				if(obj instanceof Double)
				{
					obj = output.format(((Double)obj));
				}
				if(i>0)
				{
					ret+="<td class=\"value\">"+obj.toString()+"</td>";
				}
				else
				{
					ret+="<td>"+obj.toString()+"</td>";
				}
			}
			ret+="</tr>";
		}
		ret+="</table>";
		return ret;
	}
}
