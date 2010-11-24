package com.gentics.cr.rest.velocity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.apache.velocity.tools.generic.EscapeTool;

import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.CRError;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.rest.ContentRepository;
import com.gentics.cr.template.FileTemplate;
import com.gentics.cr.template.ITemplate;
import com.gentics.cr.template.ITemplateManager;
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
  private static final String TEMPLATERELOADING_KEY =
    "cr.velocity.templatereloading";

  /**
   * Log4j logger to log errors and debug.
   */
  private Logger logger = Logger.getLogger(VelocityContentRepository.class);

  /**
   * Create new Instance of VelocityContentRepository.
   * @param attr TODO javadoc
   * @param encoding TODO javadoc
   * @param options TODO javadoc
   * @param configUtil TODO javadoc
   */
  public VelocityContentRepository(final String[] attr, final String encoding,
      final String[] options, final CRConfigUtil configUtil) {
    super(attr, encoding, options);
    config = configUtil;
    templateReloading = Boolean.parseBoolean(
        (String) config.get(TEMPLATERELOADING_KEY));
  }

  @Override
  public final void respondWithError(final OutputStream stream,
      final CRException ex, final boolean isDebug) {
    logger.error("Error getting result.", ex);
    ensureTemplateManager();
    try {
      loadTemplate(true);
      templateManager.put("exception", ex);
      templateManager.put("debug", isDebug);
      String encoding = this.getResponseEncoding();
      templateManager.put("encoding", encoding);
      //TODO use errorTemplateName (absolute) instead of errorTemplate.getKey()
      String output = templateManager.render(errorTemplate.getKey(),
          errorTemplate.getSource());
      stream.write(output.getBytes(encoding));
    } catch (Exception e) {
      logger.error("Cannot succesfully respond with error template.", e);
    }
  }

  @Override
  public final void toStream(final OutputStream stream) throws CRException {
    try {
      ensureTemplateManager();
      loadTemplate();
      templateManager.put("resolvables", this.resolvableColl);
      HashMap<String, Object> additionalObjects = 
    	  this.getAdditionalDeployableObjects();
      if (additionalObjects != null) {
    	  for (Entry<String, Object> e : additionalObjects.entrySet()) {
    		  templateManager.put(e.getKey(), e.getValue());
    	  }
      }
      String encoding = this.getResponseEncoding();
      templateManager.put("encoding", encoding);
      //TODO use templateName (absolute) instead of errorTemplate.getKey()
      String output = templateManager.render(template.getKey(),
          template.getSource());
      stream.write(output.getBytes(encoding));
    } catch (CRException e) {
      respondWithError(stream, e, false);
    } catch (IOException e) {
      logger.error("Cannot write to Output stream.", e);
    }
  }

  /**
   * TODO javadoc.
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
  private void loadTemplate(final boolean loadErrorTemplate) throws CRException
  {
    if ((template == null && !loadErrorTemplate)
        || (errorTemplate == null && loadErrorTemplate) || templateReloading) {
      String templatePath = (String) config.get(TEMPLATEPATH_KEY);
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
        log.error("FAILED TO LOAD VELOCITY TEMPLATE FROM " + template, e);
      }
    }
    if (template == null && !loadErrorTemplate) {
      throw new CRException(new CRError("ERROR",
          "The template " + template + " cannot be found."));
    }
    if (errorTemplate == null && loadErrorTemplate) {
      throw new CRException(new CRError("ERROR",
          "The template " + template + " cannot be found."));
    }
  }

  /**
   * TODO javadoc.
   * @param templatePath TODO javadoc
   * @return TODO javadoc
   * @throws FileNotFoundException TODO javadoc
   * @throws CRException TODO javadoc
   */
  private FileTemplate getFileTemplate(final String templatePath)
      throws FileNotFoundException, CRException {
    File file = new File(templatePath);
    if (!file.isAbsolute()) {
      file = new File(CRConfigUtil.DEFAULT_TEMPLATE_PATH + File.separator
          + templatePath);
    }
    return new FileTemplate(new FileInputStream(file));
  }

}
