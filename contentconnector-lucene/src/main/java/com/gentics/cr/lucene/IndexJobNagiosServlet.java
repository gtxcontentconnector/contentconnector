package com.gentics.cr.lucene;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.util.indexing.AbstractUpdateCheckerJob;
import com.gentics.cr.util.indexing.IndexController;
import com.gentics.cr.util.indexing.IndexJobQueue;
import com.gentics.cr.util.indexing.IndexLocation;
import com.gentics.lib.log.NodeLogger;

/**
 * Nagios Servlet for IndexJobServlet. 
 */
public class IndexJobNagiosServlet extends HttpServlet {

	/**
	 * Serial version.
	 */
	private static final long serialVersionUID = -1686996634146038875L;

	/**
	 * Log4J logger.
	 */
	private static final NodeLogger LOGGER = NodeLogger.getNodeLogger(IndexJobNagiosServlet.class);

	/**
	 * IndexController to access the indixes for fetching the needed information.
	 */
	protected IndexController indexer;

	/**
	 * Index param.
	 */
	private static final String NAGIOS_PARAM = "nagios";

	/**
	 * 
	 */
	public void init(final ServletConfig config) throws ServletException {
		super.init(config);
		this.indexer = initIndexController(config);
	}

	/**
	 * implemented as own method to change executed context.
	 * 
	 * @param config ServletConfig
	 * @return indexController
	 */
	public IndexController initIndexController(final ServletConfig config) {
		return IndexController.get("indexer");
	}

	@Override
	public final void destroy() {
		if (indexer != null) {
			indexer.stop();
		}
	}

	/**
	 * Perform the actual display of nagios status.
	 * @param request Request to get all parameters from.
	 * @param response Write the status into the response.
	 * @throws IOException in case writing to response fails.
	 */
	public final void doService(final HttpServletRequest request, final HttpServletResponse response) throws IOException {

		LOGGER.debug("Request:" + request.getQueryString());

		String nagString = request.getParameter(NAGIOS_PARAM);

		String index = request.getParameter("idx");
		boolean showAll = index.equals("*");
		response.setContentType("text/plain");
		ConcurrentHashMap<String, IndexLocation> indexTable = indexer.getIndexes();
		indexTable = indexer.getIndexes();
		ResponseWriter writer = new ResponseWriter(response.getWriter());
		for (Entry<String, IndexLocation> e : indexTable.entrySet()) {
			if (showAll || e.getKey().equalsIgnoreCase(index)) {
				IndexLocation loc = e.getValue();
				IndexJobQueue queue = loc.getQueue();
				Map<String, CRConfigUtil> map = loc.getCRMap();
				if (queue != null && queue.isRunning()) {
					writer.write(e.getKey(), ".WorkerThread: OK\n");
				} else {
					writer.write(e.getKey(), ".WorkerThread: NOK\n");
				}
				writer.write(e.getKey(), ".ObjectsInIndex: ", loc.getDocCount() + "", "\n");
				if (queue != null) {
					AbstractUpdateCheckerJob j = queue.getCurrentJob();
					if (j != null) {
						writer.write(e.getKey(), ".CurrentJobObjectsToIndex: ", j.getObjectsToIndex() + "", "\n");
						writer.write(e.getKey(), ".CurrentJobStarted: ", j.getStart() + "", "\n");
					}
				}

			}
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doService(req, resp);
	}

	protected String getAction(HttpServletRequest request) {
		return request.getParameter("action");
	}
	
	private class ResponseWriter {

		PrintWriter writer;
		
		public ResponseWriter(PrintWriter writer) {
			this.writer = writer;
		}

		public void write(String... data) {
			for (String value : data) {
				writer.write(value);
			}
		}
		
	}

}
