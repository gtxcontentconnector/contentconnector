package com.gentics.cr.lucene;

import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.gentics.cr.lucene.indexer.CRIndexer;
import com.gentics.cr.lucene.indexer.IndexerStatus;


/**
 * @author haymo
 * 
 * Used to render the Rest xml.
 * 
 */
public class IndexerServlet extends HttpServlet {

	private static final long serialVersionUID = 0002L;
	private Logger log;
	
	private CRIndexer indexer;
	
	
	
	public void init(ServletConfig config) throws ServletException {

		super.init(config);
		this.log = Logger.getLogger("com.gentics.cr.lucene");
		this.indexer = new CRIndexer(config.getServletName());
		if(this.indexer.isPeriodicalRun())
		{
			this.indexer.startJob();
		}
	}

	/**
	 * Wrapper Method for the doGet and doPost Methods
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	public void doService(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		this.log.debug("Request:" + request.getQueryString());
		
		// starttime
		long s = new Date().getTime();
		// get the objects
		
		String action = request.getParameter("action");
		if(action!=null)
		{
			if("start".equalsIgnoreCase(action))
			{
				this.indexer.startJob();
			}
			else if("stop".equalsIgnoreCase(action))
			{
				this.indexer.stopJob();
			}
			else if("single".equalsIgnoreCase(action))
			{
				this.indexer.startSingleRun();
			}
				
		}
		
		response.setContentType("text/html");
		response.getWriter().write("<html>\r\n"+
				"<head>\r\n" +
				"<title>IndexerServlet for Lucene by Gentics</title>\r\n" +
				"<meta http-equiv=\"refresh\" content=\"5; URL=\"?ts="+(new Date().getTime())+"\" />" +
				"</head>\r\n" +
				"<body>\r\n");
		IndexerStatus status  = this.indexer.getStatus(); 
		response.getWriter().write("Last run in ms: "+status.getLastRunDuration()+"<br/>\n");
		response.getWriter().write("Periodical run: "+this.indexer.isPeriodicalRun()+"<br/>\n");
		response.getWriter().write("Running: "+status.isRunning()+"<br/>\n");
		if(status.isRunning())
		{
			Format formatter = new SimpleDateFormat("yyyy.MM.dd HH.mm.ss");
		    response.getWriter().write("Start Date: "+formatter.format(status.getStartTime())+"<br/>\n");
		
		
		    response.getWriter().write("Objects to index in current run: "+status.getObjectCount()+"<br/>\n");
		    response.getWriter().write("Objects indexed in current run: "+status.getObjectsDone()+"<br/>\n");
		    
		    
		}
		else
		{
			if(this.indexer.isStarted())
			{
				response.getWriter().write("Configured interval: "+this.indexer.getInterval()+" sec<br/>\n");
				response.getWriter().write("<a href=\"?action=stop\">Stop periodical background job</a><br/>\n");	
			}
			else
			{
				response.getWriter().write("<a href=\"?action=start\">Start periodical background job</a><br/>\n");
				response.getWriter().write("<a href=\"?action=single\">Start single index job</a><br/>\n");
			}
			
		}
		response.getWriter().write("<a href=\"?action=show\">Refresh status</a><br/>\n");
		response.getWriter().write("</body></html>");
		response.getWriter().flush();
		response.getWriter().close();
		// endtime
		long e = new Date().getTime();
		this.log.info("Executiontime for getting Status " + (e - s));

	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doService(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doService(request, response);
	}

}