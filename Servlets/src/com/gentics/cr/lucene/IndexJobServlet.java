package com.gentics.cr.lucene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map.Entry;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.lucene.indexer.IndexController;
import com.gentics.cr.lucene.indexer.index.CRIndexJob;
import com.gentics.cr.lucene.indexer.index.IndexJobQueue;
import com.gentics.cr.lucene.indexer.index.IndexLocation;


/**
 * @author haymo
 * 
 * Used to render the Rest xml.
 * 
 */
public class IndexJobServlet extends HttpServlet {

	private static final long serialVersionUID = 0002L;
	private Logger log;
	
	private IndexController indexer;
	
	
	
	public void init(ServletConfig config) throws ServletException {

		super.init(config);
		this.log = Logger.getLogger("com.gentics.cr.lucene");
		this.indexer = new IndexController(config.getServletName());
		
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
		String index = request.getParameter("idx");
		
		String nc = "&t="+System.currentTimeMillis();
		
		response.setContentType("text/html");
		Hashtable<String,IndexLocation> indexTable = indexer.getIndexes();
		for (Entry<String,IndexLocation> e:indexTable.entrySet()) {
			
			response.getWriter().write("<h1>Index "+e.getKey()+"</h1><br/>\n");
			IndexLocation loc = e.getValue();
			IndexJobQueue queue = loc.getQueue();
			
			Hashtable<String,CRConfigUtil> map = loc.getCRMap();
			
			if(e.getKey().equalsIgnoreCase(index))
			{
				if("stopWorker".equalsIgnoreCase(action))
					queue.stopWorker();
				if("startWorker".equalsIgnoreCase(action))
					queue.startWorker();
				if("addJob".equalsIgnoreCase(action))
				{
					String cr = request.getParameter("cr");
					if("all".equalsIgnoreCase(cr))
						loc.createAllCRIndexJobs();
					else
					{
						if(cr!=null)
						{
							CRConfigUtil crc=map.get(cr);
							loc.createCRIndexJob(crc,map);
						}
					}
				}
			}
			
			ArrayList<CRIndexJob> lastJobs = queue.getLastJobs();
			for(CRIndexJob lj:lastJobs)
			{
				response.getWriter().write("Job "+lj.getIdentifyer()+" took "+lj.getDuration()+"ms for "+lj.getObjectsDone()+" objects<br/>\n");
			}
			
			response.getWriter().write(queue.getSize()+" Elements in queue<br/>\n");
			response.getWriter().write("Current interval: "+queue.getInterval()+"s<br/>\n");
			if(loc.isPeriodical())
			{
				response.getWriter().write("All CRs will be indexed periodical<br/>\n");
			}
			if(queue.isRunning())
			{
				response.getWriter().write("Worker is processing queue (<a href=\"?action=stopWorker&idx="+e.getKey()+nc+"\">stop</a>)<br/>\n");
			}
			else
			{
				response.getWriter().write("Worker not running (<a href=\"?action=startWorker&idx="+e.getKey()+nc+"\">start</a>)<br/>\n");
			}
			CRIndexJob j = queue.getCurrentJob();
			
			if(j!=null)
			{
				response.getWriter().write("<hr><br/>\n");
				response.getWriter().write("Current Job "+j.getIdentifyer()+"<br/>\n");
				response.getWriter().write("Objects to index "+j.getObjectsToIndex()+"<br/>\n");
				response.getWriter().write("Objects done "+j.getObjectsDone()+"<br/>\n");
				response.getWriter().write("<hr><br/>\n");
			}
			for (Entry<String,CRConfigUtil> crc:map.entrySet()) {
				response.getWriter().write("Add CR "+crc.getKey()+" for indexing (<a href=\"?action=addJob&idx="+e.getKey()+"&cr="+crc.getKey()+nc+"\">add</a>)<br/>\n");
			}
			response.getWriter().write("Add all CRs for indexing (<a href=\"?action=addJob&idx="+e.getKey()+"&cr=all"+nc+"\">add</a>)<br/>\n");
			
		}
		
		response.getWriter().write("<a href=\"?action=show"+nc+"\">Refresh status</a><br/>\n");
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