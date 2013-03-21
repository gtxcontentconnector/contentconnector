package com.gentics.cr.lucene.indexer.transformer.html;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLEditorKit.ParserCallback;
import javax.swing.text.html.parser.ParserDelegator;

import org.apache.log4j.Logger;

import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.lucene.indexer.transformer.ContentTransformer;

/**
 * 
 * Last changed: $Date: 2009-06-24 17:10:19 +0200 (Mi, 24 Jun 2009) $
 * @version $Revision: 99 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class HTMLEditorKitStripper extends ContentTransformer {

	private static Logger LOGGER = Logger.getLogger(HTMLEditorKitStripper.class);

	/**
	 * Create new instance of HTMLEditorKitStripper.
	 * @param config
	 */
	public HTMLEditorKitStripper(GenericConfiguration config) {
		super(config);

	}

	private static List<String> extractText(Reader reader) throws IOException {
		final ArrayList<String> list = new ArrayList<String>();

		ParserDelegator parserDelegator = new ParserDelegator();
		ParserCallback parserCallback = new ParserCallback() {
			public void handleText(final char[] data, final int pos) {
				list.add(new String(data));
			}

			public void handleStartTag(Tag tag, MutableAttributeSet attribute, int pos) {
			}

			public void handleEndTag(Tag t, final int pos) {
			}

			public void handleSimpleTag(Tag t, MutableAttributeSet a, final int pos) {
			}

			public void handleComment(final char[] data, final int pos) {
			}

			public void handleError(final java.lang.String errMsg, final int pos) {
			}
		};
		parserDelegator.parse(reader, parserCallback, true);
		return list;
	}

	private InputStream convertToInputStream(Object obj) {
		ByteArrayInputStream s;
		if (obj instanceof String) {
			s = new ByteArrayInputStream(((String) obj).getBytes());
		} else {
			throw new IllegalArgumentException("Parameter must be instance of String");
		}
		return s;
	}

	/**
	 * @param obj 
	 */
	public Reader getContents(Object obj) {
		String s = getStringContents(obj);
		if (s != null) {
			return new StringReader(s);
		}
		return null;
	}

	private Reader tidy(InputStream is) {
		return TidyHelper.tidy(is);
	}

	private static String newline = System.getProperty("line.separator");

	/**
	 * @param obj 
	 */
	public String getStringContents(Object obj) {

		StringBuilder ret = new StringBuilder();
		List<String> lines;
		try {
			Reader r = tidy(convertToInputStream(obj));
			lines = HTMLEditorKitStripper.extractText(r);
			for (String line : lines) {
				ret.append(line + newline);
			}
		} catch (Exception e) {
			// Catch all exceptions to not disturb indexer
			LOGGER.error("Error while extracting text", e);
		}
		return ret.toString();
	}

	@Override
	public void processBean(CRResolvableBean bean) {
		// TODO Auto-generated method stub

	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}
}
