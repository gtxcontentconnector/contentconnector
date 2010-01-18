package com.gentics.cr.rest.rss;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.rest.ContentRepository;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedOutput;
/**
 * 
 * Last changed: $Date$
 * @version $Revision$
 * @author $Author$
 *
 */
public class RomeContentRepository extends ContentRepository {
	
	private static final String[] POSSIBLE_FEED_TYPES = {"rss_0.9", "rss_0.91", "rss_0.92", "rss_0.93",
		 "rss_0.94", "rss_1.0", "rss_2.0", "atom_0.3","atom_1.0"};
	
	private SyndFeedOutput output;
	private ArrayList<SyndEntryImpl> entryList;
	
	/**
	 * 
	 * @param attr
	 * @param encoding
	 * @param options
	 */
	public RomeContentRepository(String[] attr, String encoding,
			String[] options) {
		super(attr, encoding, options);
		
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 5652365520470929051L;

	/**
	 * Returns "text/xml"
	 * @return 
	 */
	public String getContentType() {
		return("text/xml");
	}
	
	/**
	 * @param stream 
	 * @param ex 
	 * @param isDebug 
	 * 
	 */
	public void respondWithError(OutputStream stream,CRException ex, boolean isDebug)
	{
		
		OutputStreamWriter writer = new OutputStreamWriter(stream);
		try {
			writer.write("FEHLER "+ex.getMessage());
		
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private String getFeedType() throws CRException
	{
		String feedtype=null;
		if(this.getOptionsArray()==null || this.getOptionsArray().length==0)
		{
			feedtype="rss_0.9";
			log.warn("No feedtype in parameter options received, using default type rss_0.9");
		}
		else
		{
			ArrayList<String> options = new ArrayList<String>(Arrays.asList(this.getOptionsArray()));
			ArrayList<String> possibletypes = new ArrayList<String>(Arrays.asList(RomeContentRepository.POSSIBLE_FEED_TYPES));
			for(String option : options)
			{
				if(possibletypes.contains(option))
				{
					feedtype=option;
				}
			}
			
			if(feedtype==null)
			{
				throw new CRException("ParameterException","The parameter options does not contain a valid RSS type use one of the following "+RomeContentRepository.POSSIBLE_FEED_TYPES.toString());
			}
		}
		return feedtype;
	}

	/**
	 * @param stream 
	 * @throws CRException 
	 * 
	 */
	public void toStream(OutputStream stream) throws CRException{
		output = new SyndFeedOutput();
		
		entryList = new ArrayList<SyndEntryImpl>();
		
		for (Iterator<CRResolvableBean> it = this.resolvableColl.iterator(); it.hasNext();) {
			
			CRResolvableBean crBean =  it.next();
			entryList.add(processElement(crBean));
		}
		
		
		
		SyndFeed feed = new SyndFeedImpl();
		feed.setFeedType(getFeedType());
		feed.setEntries(entryList);
		
		
		try {
			output.output(feed, new OutputStreamWriter(stream));
		} catch (IOException e) {
			e.printStackTrace();
		} catch (FeedException e) {
			e.printStackTrace();
		}
	}

	private SyndEntryImpl processElement(CRResolvableBean crBean)
	{
		SyndEntryImpl feedentry = new SyndEntryImpl();
		//feedentry.setAuthor("GTX RESTConnector");
		
		ArrayList<SyndContentImpl> contList = new ArrayList<SyndContentImpl>();
		
		

		if (crBean.getAttrMap() != null && (!crBean.getAttrMap().isEmpty())) {
			
			
			Iterator<String> bit = crBean.getAttrMap().keySet().iterator();
			while (bit.hasNext()) {
				
				SyndContentImpl content = new SyndContentImpl();
				
				
				
				String entry = bit.next();
				// Element attrElement = doc.createElement(entry);
				Object bValue=crBean.getAttrMap().get(entry);
				
				if(bValue!=null)
				{
					content.setValue(bValue.toString());
				}
				contList.add(content);
				
			}
			
		}
		feedentry.setContents(contList);
		
		
		return(feedentry);
	}
}
