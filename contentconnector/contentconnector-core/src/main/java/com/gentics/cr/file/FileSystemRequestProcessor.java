package com.gentics.cr.file;

import java.io.File;
import java.util.Collection;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.gentics.cr.CRConfig;
import com.gentics.cr.CRRequest;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.RequestProcessor;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.portalnode.expressions.ExpressionParserHelper;
import com.gentics.api.lib.exception.ParserException;
import com.gentics.api.lib.expressionparser.Expression;
import com.gentics.api.lib.expressionparser.ExpressionParserException;

/**
 * {@link RequestProcessor} for requesting and indexing a filesystem.
 * @author bigbear3001
 *
 */
public class FileSystemRequestProcessor extends RequestProcessor {

	/**
	 * Log4j logger to write errors to.
	 */
	private static Logger logger = Logger.getLogger(FileSystemRequestProcessor.class);

	/**
	 * parameter name of the parameter containing the URL.
	 */
	private static final String URL_PARAMETER = "url";
	/**
	 * root document entry that will be permanently watched for changes.
	 */
	private ResolvableFileBean monitoredPath = null;

	/**
	 * {@link FileSystemChecker} to check the given directories.
	 */
	private static FileSystemChecker myFileSystemChecker = null;

	/**
	 * Initialize a new {@link FileSystemRequestProcessor}.
	 * @param config Configuration for the FileSystemRequestProcessor
	 * @throws CRException TODO javadoc in
	 * {@link RequestProcessor#RequestProcessor(CRConfig)}
	 */
	public FileSystemRequestProcessor(final CRConfig config) throws CRException {
		super(config);
		String url = config.getString(URL_PARAMETER);
		if (url != null) {
			monitoredPath = getResolvableFileBeanForPath(url);
			if (myFileSystemChecker == null) {
				myFileSystemChecker = new FileSystemChecker(true);
			}
			myFileSystemChecker.addPathToMonitoring(monitoredPath);
		}
	}

	@Override
	public final void finalize() {
		if (myFileSystemChecker != null) {
			myFileSystemChecker.removePathFromMonitoring(monitoredPath);
		}

	}

	@Override
	public final Collection<CRResolvableBean> getObjects(final CRRequest request, final boolean doNavigation)
			throws CRException {
		Collection<CRResolvableBean> result = new Vector<CRResolvableBean>();
		String requestFilter = request.getRequestFilter();
		try {
			Expression expressionFromFilter = ExpressionParserHelper.parse(requestFilter);
			for (CRResolvableBean item : new Vector<CRResolvableBean>(monitoredPath.getRegisteredDescendants())) {
				if (ExpressionParserHelper.match(expressionFromFilter, item)) {
					result.add(item);
				}
			}
			return result;
		} catch (ParserException e) {
			logger.error("Cannot parse filter " + requestFilter + " to a valid expression.", e);
		} catch (ExpressionParserException e) {
			logger.error("Filter " + requestFilter + " doesn't results into a boolean.", e);
		}

		return null;
	}

	/**
	 * Helper method to get the {@link ResolvableFileBean} for a specific path.
	 * @param path Path to create the {@link ResolvableFileBean} for.
	 * @return {@link ResolvableFileBean} for the specified path.
	 */
	private ResolvableFileBean getResolvableFileBeanForPath(final String path) {
		File fileForPath = new File(path);
		ResolvableFileBean resolvableFileBeanForPath = new ResolvableFileBean(fileForPath);
		return resolvableFileBeanForPath;
	}

}
