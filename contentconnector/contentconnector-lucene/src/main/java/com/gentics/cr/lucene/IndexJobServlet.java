package com.gentics.cr.lucene;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map.Entry;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.lucene.indexer.index.LockedIndexException;
import com.gentics.cr.lucene.indexer.index.LuceneIndexLocation;
import com.gentics.cr.lucene.indexer.index.LuceneSingleIndexLocation;
import com.gentics.cr.lucene.information.SpecialDirectoryRegistry;
import com.gentics.cr.monitoring.MonitorFactory;
import com.gentics.cr.servlet.VelocityServlet;
import com.gentics.cr.util.file.ArchiverUtil;
import com.gentics.cr.util.indexing.IndexController;
import com.gentics.cr.util.indexing.IndexJobQueue;
import com.gentics.cr.util.indexing.IndexLocation;
import com.gentics.cr.util.indexing.IndexExtension;

/**
 * @author Christopher Supnig
 */
public class IndexJobServlet extends VelocityServlet {

	private static final long serialVersionUID = 0002L;
	private static final Logger LOGGER = Logger.getLogger(IndexJobServlet.class);
	protected IndexController indexer;

	public void init(final ServletConfig config) throws ServletException {

		super.init(config);
		this.indexer = initIndexController(config);
	}

	/**
	 * implemented as own method to change executed context.
	 * 
	 * @param config
	 * @return indexController
	 */
	public IndexController initIndexController(final ServletConfig config) {
		return new IndexController(config.getServletName());
	}

	@Override
	public final void destroy() {
		if (indexer != null) {
			indexer.stop();
		}
	}

	public void doService(HttpServletRequest request, HttpServletResponse response) throws IOException {

		// starttime
		long s = new Date().getTime();
		// get the objects

		String action = getAction(request);
		String index = request.getParameter("idx");
		if ("download".equals(action)) {
			generateArchive(index, response);
			skipRenderingVelocity();
		} else {
			response.setContentType("text/html");
			Hashtable<String, IndexLocation> indexTable = indexer.getIndexes();

			setTemplateVariables(request);

			for (Entry<String, IndexLocation> e : indexTable.entrySet()) {
				IndexLocation loc = e.getValue();
				IndexJobQueue queue = loc.getQueue();
				Hashtable<String, CRConfigUtil> map = loc.getCRMap();
				if (e.getKey().equalsIgnoreCase(index)) {
					if ("stopWorker".equalsIgnoreCase(action)) {
						queue.pauseWorker();
					}
					if ("startWorker".equalsIgnoreCase(action)) {
						queue.resumeWorker();
					}
					if ("clear".equalsIgnoreCase(action)) {
						loc.createClearJob();
					}
					if ("optimize".equalsIgnoreCase(action)) {
						loc.createOptimizeJob();
					}
					if ("addJob".equalsIgnoreCase(action)) {
						String cr = request.getParameter("cr");
						if ("all".equalsIgnoreCase(cr)) {
							loc.createAllCRIndexJobs();
						} else {
							if (cr != null) {
								CRConfigUtil crc = map.get(cr);
								loc.createCRIndexJob(crc, map);
							}
						}
					}
					if ("addExtensionJob".equalsIgnoreCase(action)) {
						String sExt = request.getParameter("ext");
						try {
							HashMap<String, IndexExtension> extensions = ((LuceneIndexLocation) loc).getExtensions();
							if (extensions.containsKey(sExt)) {
								IndexExtension extension = extensions.get(sExt);
								String job = request.getParameter("job");
								extension.addJob(job);
							}
						} catch (Exception ex) {
							LOGGER.info("Couldn not add extension Job");
						}
					}
				}
			}
			render(response);
		}
		// endtime
		long e = new Date().getTime();
		LOGGER.info("Executiontime for getting " + action + " " + (e - s));
	}

	/**
	 * Create an archive of the index.
	 * @param index Index to create a tarball of.
	 * @param response 
	 */
	private void generateArchive(final String index, final HttpServletResponse response) {
		IndexLocation location = indexer.getIndexes().get(index);
		if (location instanceof LuceneSingleIndexLocation) {
			LuceneSingleIndexLocation indexLocation = (LuceneSingleIndexLocation) location;
			File indexDirectory = new File(indexLocation.getReopenFilename()).getParentFile();
			File writeLock = null;
			boolean weWroteTheWriteLock = false;
			try {
				indexLocation.checkLock();
				if (indexDirectory.canWrite()) {
					writeLock = new File(indexDirectory, "write.lock");
					if (writeLock.createNewFile()) {
						weWroteTheWriteLock = true;
					} else {
						throw new LockedIndexException(
								new Exception("the write lock file already exists in the index."));
					}
					//set to read only so the index jobs will not delete it.
					writeLock.setReadOnly();
					response.setContentType("application/x-compressed, application/x-tar");
					response.setHeader("Content-Disposition", "attachment; filename=" + index + ".tar.gz");
					ArchiverUtil.generateGZippedTar(response.getOutputStream(), indexDirectory);
				} else {
					LOGGER.error("Cannot lock the index directory to ensure the consistency of the archive.");
				}
			} catch (IOException e) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				LOGGER.error("Cannot generate the archive correctly.", e);
			} catch (LockedIndexException e) {
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				LOGGER.error("Cannot generate the archive while the index is locked.", e);
			} finally {
				if (writeLock != null && writeLock.exists() && weWroteTheWriteLock) {
					writeLock.delete();
				}
			}

		} else {
			LOGGER.error("generating an archive for " + location + " not supported yet.");
		}

	}

	/**
	 * set variables for velocity template.
	 * @param request .
	 */
	protected final void setTemplateVariables(final HttpServletRequest request) {
		Hashtable<String, IndexLocation> indexTable = indexer.getIndexes();
		String nc = "&t=" + System.currentTimeMillis();
		String selectedIndex = request.getParameter("index");
		Long totalMemory = Runtime.getRuntime().totalMemory();
		Long freeMemory = Runtime.getRuntime().freeMemory();
		Long maxMemory = Runtime.getRuntime().maxMemory();

		setTemplateVariable("specialDirs", SpecialDirectoryRegistry.getInstance().getSpecialDirectories());
		setTemplateVariable("indexes", indexTable.entrySet());
		setTemplateVariable("nc", nc);
		setTemplateVariable("selectedIndex", selectedIndex);
		String action = getAction(request);
		if ("report".equalsIgnoreCase(action)) {
			setTemplateVariable("report", MonitorFactory.getSimpleReport());
		}
		setTemplateVariable("action", action);
		setTemplateVariable("maxmemory", maxMemory);
		setTemplateVariable("totalmemory", totalMemory);
		setTemplateVariable("freememory", freeMemory);
		setTemplateVariable("usedmemory", totalMemory - freeMemory);
	}

	/**
	 * Get action parameter from request.
	 * @param request Request to get the action parameter of.
	 * @return String containing the action.
	 */
	protected final String getAction(final HttpServletRequest request) {
		return request.getParameter("action");
	}

}
