package com.gentics.cr.rest.velocity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;
import org.apache.log4j.Logger;
import org.apache.velocity.tools.generic.EscapeTool;

import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.CRError;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.rest.ContentRepository;
import com.gentics.cr.template.FileTemplate;
import com.gentics.cr.template.ITemplate;
import com.gentics.cr.template.ITemplateManager;
import com.gentics.cr.util.StringUtils;
import com.gentics.cr.util.velocity.VelocityTools;

/**
 * VelocityContentRepository allows you to render the result of the
 * request to the ContentConnnector with velocity.
 * Last changed: $Date: 2010-02-26 17:25:31 +0100 (Fr, 26 Feb 2010) $
 * @version $Revision: 456 $
 * @author $Author: bigbear.ap $
 *
 */
public class VelocityContentRepository extends ContentRepository {

	/**
	 * Generated version identifier for serialization.
	 */
	private static final long serialVersionUID = -3555742920271252693L;

	/**
	 * CRConfig to use by the instance.
	 */
	private CRConfigUtil config;

	/**
	 * Template manager for velocity.
	 */
	private ITemplateManager templateManager;

	/**
	 * Velocity Template to use for rendering a sucessful response.
	 */
	private ITemplate template;

	/**
	 * Velocity Template used for rendering the error response.
	 */
	private ITemplate errorTemplate;

	/**
	 * When true templates are loaded from file system with every request.
	 * Otherwise the will be cached between the requests.
	 */
	private boolean templateReloading = false;

	/**
	 * Key for the configuration of the template in the config.
	 */
	private static final String TEMPLATEPATH_KEY = "cr.velocity.defaulttemplate";

	/**
	 * Key for the configuration of the error template in the config.
	 */
	private static final String TEMPLATERELOADING_KEY = "cr.velocity.templatereloading";

	/**
	 * Configuration key for the frame url.
	 */
	private static final String FRAME_KEY = "cr.velocity.frame";

	/**
	 * Configuration key for the placeholder in the frame source.
	 */
	private static final String FRAMEPLACEHOLDER_KEY = "cr.velocity.frameplaceholder";

	/**
	 * Configuration key holding pre defined variables for velocity.
	 */
	private static final String VARIABLES_KEY = "cr.velocity.variables";

	/**
	 * Configuration key for contenttype to set for response.
	 */
	private static final String CONTENTTYPE_KEY = "cr.velocity.contenttype";

	/**
	 * Cache key suffix for the header parsed from the frame.
	 */
	private static final String HEADER_CACHE_KEY_SUFFIX = ".header";

	/**
	 * Cache key suffix for the footer parsed from the frame.
	 */
	private static final String FOOTER_CACHE_KEY_SUFFIX = ".footer";

	/**
	 * Log4j logger to log errors and debug.
	 */
	private Logger logger = Logger.getLogger(VelocityContentRepository.class);

	/**
	 * header to write out before the rendered template.
	 */
	private String header = "";

	/**
	 * footer to append after the rendered template.
	 */
	private String footer = "";

	/**
	 * <code>true</code> if the frame is already parsed, regardless if this was
	 * successful.
	 */
	private boolean frameParsed = false;

	/**
	 * {@link VelocityTools} to deploy into the template context.
	 */
	private VelocityTools tools = new VelocityTools();

	/**
	 * Create new Instance of VelocityContentRepository.
	 * @param attr TODO javadoc
	 * @param encoding TODO javadoc
	 * @param options TODO javadoc
	 * @param configUtil TODO javadoc
	 */
	public VelocityContentRepository(final String[] attr, final String encoding, final String[] options, final CRConfigUtil configUtil) {
		super(attr, encoding, options);
		config = configUtil;
		templateReloading = Boolean.parseBoolean((String) config.get(TEMPLATERELOADING_KEY));
	}

	@Override
	public final void respondWithError(final OutputStream stream, final CRException ex, final boolean isDebug) {
		logger.error("Error getting result.", ex);
		ensureTemplateManager();
		try {
			loadTemplate(true);
			templateManager.put("exception", ex);
			templateManager.put("debug", isDebug);
			String encoding = this.getResponseEncoding();
			templateManager.put("encoding", encoding);
			String output = templateManager.render(errorTemplate.getKey(), errorTemplate.getSource());
			stream.write(getHeader().getBytes());
			stream.write(output.getBytes(encoding));
			stream.write(getFooter().getBytes());
		} catch (Exception e) {
			logger.error("Cannot succesfully respond with error template.", e);
		}
	}

	@Override
	public final String getContentType() {
		return config.getString(CONTENTTYPE_KEY, "text/html");
	}

	@Override
	public final void toStream(final OutputStream stream) throws CRException {
		try {
			ensureTemplateManager();
			loadTemplate();
			templateManager.put("resolvables", this.resolvableColl);
			putObjectsIntoTemplateManager(this.getAdditionalDeployableObjects());
			GenericConfiguration variables = (GenericConfiguration) config.get(VARIABLES_KEY);
			if (variables != null) {
				putObjectsIntoTemplateManager(variables.getProperties());
			}
			String encoding = this.getResponseEncoding();
			templateManager.put("encoding", encoding);
			templateManager.put("tools", tools);
			String output = templateManager.render(template.getKey(), template.getSource());
			stream.write(getHeader().getBytes());
			stream.write(output.getBytes(encoding));
			stream.write(getFooter().getBytes());
		} catch (CRException e) {
			respondWithError(stream, e, false);
		} catch (IOException e) {
			logger.error("Cannot write to Output stream.", e);
		}
	}

	/**
	 * Insert objects to deploy into the templateManger.
	 * @param additionalDeployableObjects HashMap with objects to deploy.
	 */
	private void putObjectsIntoTemplateManager(final HashMap<String, Object> additionalDeployableObjects) {
		if (additionalDeployableObjects != null) {
			for (Entry<String, Object> e : additionalDeployableObjects.entrySet()) {
				templateManager.put(e.getKey().toString(), e.getValue());
			}
		}
	}

	/**
	 * Insert objects to deploy into the templateManager.
	 * @param additionalDeployableObjects Map with objects to deploy.
	 */
	private void putObjectsIntoTemplateManager(final Map<Object, Object> additionalDeployableObjects) {
		if (additionalDeployableObjects != null) {
			for (Entry<Object, Object> e : additionalDeployableObjects.entrySet()) {
				templateManager.put(e.getKey().toString(), e.getValue());
			}
		}
	}

	/**
	 * @return the footer from the frame template
	 */
	private String getFooter() {
		initFrame();
		return footer;
	}

	/**
	 * @return the header from the frame template.
	 */
	private String getHeader() {
		initFrame();
		return header;
	}

	/**
	 * initialize the frame template given in the configuration.
	 */
	private void initFrame() {
		if (!frameParsed) {
			String framePath = config.getString(FRAME_KEY);
			String framePlaceholder = config.getString(FRAMEPLACEHOLDER_KEY);
			if (framePath != null && !framePath.equals("") && framePlaceholder != null && !framePlaceholder.equals("")) {
				try {
					JCS cache = JCS.getInstance(VelocityContentRepository.class.getSimpleName() + ".famecache");
					footer = (String) cache.get(framePath + FOOTER_CACHE_KEY_SUFFIX);
					header = (String) cache.get(framePath + HEADER_CACHE_KEY_SUFFIX);
					if (header == null || footer == null) {
						URL frameURL = new URL(framePath);
						URLConnection conn = frameURL.openConnection();
						Object content = conn.getContent();
						if (content instanceof InputStream) {
							header = StringUtils.readUntil((InputStream) content, framePlaceholder);
							footer = StringUtils.streamToString((InputStream) content);
						} else {
							logger.error(
								"Error reading frame source" + framePath,
								new CRException("Unknown response type (" + content.getClass() + ")"));
						}
						cache.put(framePath + HEADER_CACHE_KEY_SUFFIX, header);
						cache.put(framePath + FOOTER_CACHE_KEY_SUFFIX, footer);

					}
				} catch (MalformedURLException e) {
					logger.error("Error reading frame source " + framePath, e);
				} catch (IOException e) {
					logger.error("Error reading frame source " + framePath, e);
				} catch (CacheException e) {
					logger.error("Cannot initalize frame cache.", e);
				}
			}
			frameParsed = true;
		}
	}

	/**
	 * ensure the template manager is initialized and deploy the needed velocity
	 * tools into it.
	 */
	private void ensureTemplateManager() {
		if (templateManager == null) {
			templateManager = config.getTemplateManager();
			templateManager.put("esc", new EscapeTool());
		}
	}

	/**
	 * TODO javadoc.
	 * @throws CRException TODO javadoc
	 */
	private void loadTemplate() throws CRException {
		loadTemplate(false);
	}

	/**
	 * TODO javadoc.
	 * @param loadErrorTemplate TODO javadoc
	 * @throws CRException TODO javadoc
	 */
	private void loadTemplate(final boolean loadErrorTemplate) throws CRException {
		if (template == null && !loadErrorTemplate || errorTemplate == null && loadErrorTemplate || templateReloading) {
			String templatePath = config.getString(TEMPLATEPATH_KEY);
			try {
				if (loadErrorTemplate) {
					//make velocity.vm to velocity.error.vm
					File errorTemplateFile = new File(templatePath);
					String directoryName = errorTemplateFile.getParent();
					String fileName = errorTemplateFile.getName();
					String fileExtension = fileName.replaceAll(".*\\.", "");
					fileName = fileName.replaceAll("(.*)\\..*", "$1");
					templatePath = fileName + ".error." + fileExtension;
					if (directoryName != null) {
						templatePath = directoryName + File.separator + templatePath;
					}
					errorTemplate = getFileTemplate(templatePath);
				} else {
					template = getFileTemplate(templatePath);
				}
			} catch (Exception e) {
				log.error("Failed to load velocity template from " + template, e);
			}
		}
		if (template == null && !loadErrorTemplate) {
			throw new CRException(new CRError("ERROR", "The template " + template + " cannot be found."));
		}
		if (errorTemplate == null && loadErrorTemplate) {
			throw new CRException(new CRError("ERROR", "The template " + template + " cannot be found."));
		}
	}

	/**
	 * TODO javadoc.
	 * @param templatePath TODO javadoc
	 * @return TODO javadoc
	 * @throws FileNotFoundException TODO javadoc
	 * @throws CRException TODO javadoc
	 */
	private FileTemplate getFileTemplate(final String templatePath) throws FileNotFoundException, CRException {
		File file = new File(templatePath);
		if (!file.isAbsolute()) {
			file = new File(CRConfigUtil.DEFAULT_TEMPLATE_PATH + File.separator + templatePath);
		}
		return new FileTemplate(new FileInputStream(file), file);
	}

}
