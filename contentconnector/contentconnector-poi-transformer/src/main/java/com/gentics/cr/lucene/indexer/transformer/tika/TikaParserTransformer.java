package com.gentics.cr.lucene.indexer.transformer.tika;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.language.LanguageIdentifier;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.lucene.indexer.transformer.ContentTransformer;

/**
 * Transformer to automatically parse various filetypes using autodetect of the mimetype.
 * Uses the Tika framework internally for detection and parsing.
 * Tika uses Apache POI and pdfbox and some other parsing libraries underneath.
 * {@link http://tika.apache.org/}
 * 
 * @author Friedreich Bernhard
 */
public class TikaParserTransformer extends ContentTransformer {

	/**
	 * Config key to set the attribute to use for parsing.
	 */
	private static final String TRANSFORMER_CONTENT_ATTRIBUTE_FIELD_KEY = "contentAttribute";

	/**
	 * Config key to define the field used for writing the resulting string to.
	 */
	private static final String TRANSFORMER_TARGET_ATTRIBUTE_FIELD_KEY = "targetAttribute";

	/**
	 * Config key to define the field used for writing the creationtimestamp to.
	 */
	private static final String TRANSFORMER_CREATETIMESTAMP_ATTRIBUTE_FIELD_KEY = "createTimestampField";

	/**
	 * Config key to define the field used for writing the edittimestamp to.
	 */
	private static final String TRANSFORMER_EDITTIMESTAMP_ATTRIBUTE_FIELD_KEY = "editTimestampField";

	/**
	 * Config key to define the field used for writing the publishtimestamp to.
	 */
	private static final String TRANSFORMER_PUBLISHTIMESTAMP_ATTRIBUTE_FIELD_KEY = "publishTimestampField";

	/**
	 * Config key to define the field used for writing the heading to.
	 */
	private static final String TRANSFORMER_HEADING_ATTRIBUTE_FIELD_KEY = "headingField";

	/**
	 * Config key to define the field used for writing the search keywords to.
	 */
	private static final String TRANSFORMER_KEYWORDS_ATTRIBUTE_FIELD_KEY = "keywordsField";

	/**
	 * Config key to define the field used for writing the mimetype to (if not already set properly).
	 */
	private static final String TRANSFORMER_MIMETYPE_ATTRIBUTE_FIELD_KEY = "mimetypeField";

	/**
	 * Config key to configure allowed languages, separated by ','.
	 */
	private static final String TRANSFORMER_ALLOWED_LANGS_FIELD_KEY = "allowedLanguages";

	/**
	 * Config key to enable or disable language detection of content.
	 */
	private static final String TRANSFORMER_DETECT_LANGUAGES_FIELD_KEY = "detectLanguages";

	/**
	 * Field to store the config value representing the attribute to use for parsing.
	 */
	private String contentAttributeField = "content";

	/**
	 * Default field for storing the transformed cotent to.
	 */
	private String targetAttributeField = "binarycontent";

	/**
	 * Default field for reading the creation timestamp of a file from contentrepository. If not set it will be set by best guess using tika.
	 */
	private String createTimestampField = "createtimestamp";

	/**
	 * Default field for reading the publish timestamp of a file from contentrepository. If not set it will be set by best guess using tika.
	 */
	private String publishTimestampField = "publishtimestamp";

	/**
	 * Default field for reading the edit timestamp of a file from contentrepository. If not set it will be set by best guess using tika.
	 */
	private String editTimestampField = "edittimestamp";

	/**
	 * Field in the contentrepository containing the heading of a file. If not set it will be set by best guess using tika.
	 */
	private String headingField = "heading";

	/**
	 * Field in the contentrepository containing keywords for search. If not set it will be set by best guess using tika.
	 */
	private String keywordsField = "keywords";

	/**
	 * Field in the contentrepository of the mimetype. If not set it will be set by best guess using tika.
	 */
	private String mimetypeField = "mimetype";

	/**
	 * By default all languages are allowed. This may result in funny entries if tika detection is used.
	 */
	private List<String> allowedLanguages = null;

	/**
	 * Language detection is disabled by default.
	 */
	private boolean languageDetection = false;

	/**
	 * Disable file length limit.
	 */
	private int fileLengthLimit = -1;

	/**
	 * Parser provided by Tika to automatically detect the mimetype and afterwards do the parsing.
	 */
	private final AutoDetectParser parser = new AutoDetectParser();

	/**
	 * Tika parser framework.
	 */
	private final Tika tika = new Tika();

	/**
	 * Get new instance of TikaParserTransformer.
	 * @param config Configuration for the transformer.
	 */
	public TikaParserTransformer(final GenericConfiguration config) {
		super(config);
		if (config.get(TRANSFORMER_CONTENT_ATTRIBUTE_FIELD_KEY) != null) {
			contentAttributeField = config.getString(TRANSFORMER_CONTENT_ATTRIBUTE_FIELD_KEY);
		}
		if (config.get(TRANSFORMER_TARGET_ATTRIBUTE_FIELD_KEY) != null) {
			targetAttributeField = config.getString(TRANSFORMER_TARGET_ATTRIBUTE_FIELD_KEY);
		}

		if (config.get(TRANSFORMER_CREATETIMESTAMP_ATTRIBUTE_FIELD_KEY) != null) {
			createTimestampField = config.getString(TRANSFORMER_CREATETIMESTAMP_ATTRIBUTE_FIELD_KEY);
		}
		if (config.get(TRANSFORMER_EDITTIMESTAMP_ATTRIBUTE_FIELD_KEY) != null) {
			editTimestampField = config.getString(TRANSFORMER_EDITTIMESTAMP_ATTRIBUTE_FIELD_KEY);
		}
		if (config.get(TRANSFORMER_PUBLISHTIMESTAMP_ATTRIBUTE_FIELD_KEY) != null) {
			publishTimestampField = config.getString(TRANSFORMER_PUBLISHTIMESTAMP_ATTRIBUTE_FIELD_KEY);
		}
		if (config.get(TRANSFORMER_HEADING_ATTRIBUTE_FIELD_KEY) != null) {
			headingField = config.getString(TRANSFORMER_HEADING_ATTRIBUTE_FIELD_KEY);
		}
		if (config.get(TRANSFORMER_KEYWORDS_ATTRIBUTE_FIELD_KEY) != null) {
			keywordsField = config.getString(TRANSFORMER_KEYWORDS_ATTRIBUTE_FIELD_KEY);
		}
		if (config.get(TRANSFORMER_MIMETYPE_ATTRIBUTE_FIELD_KEY) != null) {
			mimetypeField = config.getString(TRANSFORMER_MIMETYPE_ATTRIBUTE_FIELD_KEY);
		}

		if (config.get(TRANSFORMER_ALLOWED_LANGS_FIELD_KEY) != null) {
			allowedLanguages = Arrays.asList(config.getString(TRANSFORMER_ALLOWED_LANGS_FIELD_KEY).split(","));
		}
		if (config.get(TRANSFORMER_DETECT_LANGUAGES_FIELD_KEY) != null) {
			languageDetection = config.getBoolean(TRANSFORMER_DETECT_LANGUAGES_FIELD_KEY);
		}
	}

	@Override
	public void processBean(final CRResolvableBean bean) throws CRException {
		if (this.contentAttributeField != null) {
			Object obj = bean.get(this.contentAttributeField);
			if (obj != null) {
				TikaInputStream inputStream = null;
				if (obj instanceof byte[]) {
					inputStream = TikaInputStream.get((byte[]) obj);
				} else {
					throw new IllegalArgumentException("Parameter must be instance of byte[]");
				}

				ContentHandler textHandler = new BodyContentHandler(fileLengthLimit);
				Metadata metadata = new Metadata();
				ParseContext context = new ParseContext();

				try {
					metadata.set(Metadata.CONTENT_TYPE, tika.detect(inputStream));

					parser.parse(inputStream, textHandler, metadata, context);

					bean.set(headingField, metadata.get(TikaCoreProperties.TITLE));

					if (bean.get(createTimestampField) == null) {
						bean.set(createTimestampField, metadata.get(TikaCoreProperties.CREATED));
					}
					if (bean.get(editTimestampField) == null) {
						bean.set(editTimestampField, metadata.get(TikaCoreProperties.MODIFIED));
					}
					if (bean.get(keywordsField) == null) {
						bean.set(keywordsField, metadata.get(TikaCoreProperties.KEYWORDS));
					}
					if (bean.get(publishTimestampField) == null) {
						bean.set(publishTimestampField, metadata.get(TikaCoreProperties.PRINT_DATE));
					}
					if (bean.get(mimetypeField) == null) {
						//HttpHeaders.CONTENT_TYPE
						bean.set(mimetypeField, metadata.get(Metadata.CONTENT_TYPE));
					}

					String content = prepareContent(bean, textHandler.toString());
					bean.set(this.targetAttributeField, content);

				} catch (IOException e) {
					LOGGER.error("Error reading inputstream from bean: " + getBeanInfo(bean), e);
				} catch (SAXException e) {
					LOGGER.error("Sax Parser Exception while reading inputstream from bean: " + getBeanInfo(bean), e);
				} catch (TikaException e) {
					LOGGER.error("Tika Parser Exception while reading inputstream  from bean: " + getBeanInfo(bean), e);
				} catch (Exception e) {
					LOGGER.error("Exception occured while indexing file at bean: " + getBeanInfo(bean), e);
				} finally {
					try {
						if (inputStream != null) {
							inputStream.close();
						}
					} catch (IOException e) {
						LOGGER.error("Could not close inputstream of bean: " + getBeanInfo(bean), e);
					}
				}
			}
		} else {
			LOGGER.error("Configured attribute is null. Bean will not be processed");
		}
	}

	/**
	 * Extract debug information of bean for display on error output.
	 * @param bean extract information from this bean.
	 * @return String to be used for log output.
	 */
	private String getBeanInfo(final CRResolvableBean bean) {
		return bean.getContentid() + " (NodeId: " + bean.getString("node_id") + ") - " + bean.getString("pub_dir") + "/"
				+ bean.getString("filename");
	}

	/**
	 * Analyze and prepare content for further processing.
	 * Atm. it analyzes the language of the provided bean if the feature is enabled through TRANSFORMER_DETECT_LANGUAGES_FIELD_KEY.
	 * @param bean used to analyze the language. it first tries to read languagecode and if that fails tika is used.
	 * @param content useful for analyzing and modifying content.
	 * @return content which may have been altered.
	 */
	private String prepareContent(final CRResolvableBean bean, final String content) {
		if (languageDetection) {
			String languageCode = bean.getString("languagecode");
			if (languageCode == null || languageCode.equals("")) {
				LanguageIdentifier identifier = new LanguageIdentifier(content);
				String lang = identifier.getLanguage();
				if (identifier.isReasonablyCertain() && (allowedLanguages == null || allowedLanguages.contains(lang))) {
					bean.set("languagecode", lang);
				}
			}
		}
		return content;
	}

	@Override
	public void destroy() {

	}
}
