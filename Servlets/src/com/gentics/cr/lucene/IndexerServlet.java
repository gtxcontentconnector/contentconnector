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
		this.indexer.startJob();
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
		IndexerStatus status  = this.indexer.getStatus(); 
		response.getWriter().write("Last run in ms: "+status.getLastRunDuration()+"\n");
		response.getWriter().write("Running: "+status.isRunning()+"\n");
		if(status.isRunning())
		{
			Format formatter = new SimpleDateFormat("yyyy.MM.dd HH.mm.ss");
		    response.getWriter().write("Start Date: "+formatter.format(status.getStartTime())+"\n");
		
		
		    response.getWriter().write("Objects to index in current run: "+status.getObjectCount()+"\n");
		    response.getWriter().write("Objects indexed in current run: "+status.getObjectsDone()+"\n");
		}
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