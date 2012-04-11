package org.apache.lucene.search.vectorhighlight;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.MapFieldSelector;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.highlight.Encoder;
import org.apache.lucene.search.vectorhighlight.FieldFragList.WeightedFragInfo;
import org.apache.lucene.search.vectorhighlight.FieldFragList.WeightedFragInfo.SubInfo;
import org.apache.lucene.search.vectorhighlight.FieldPhraseList.WeightedPhraseInfo.Toffs;

/**
 * THIS CLASS SHOULD BE REMOVED WHEN UPDATING TO LUCENE 4.0.
 * @author Christopher
 *
 */
public class WhitespaceFragmentsBuilder implements FragmentsBuilder {

	/**
	 * PRE and POST Tags.
	 */
	private String[] preTags, postTags;

	/**
	 * a constructor.
	 */
	public WhitespaceFragmentsBuilder() {
		this(new String[] { "<b>" }, new String[] { "</b>" });
	}

	/**
	 * a constructor.
	 * 
	 * @param prTags array of pre-tags for markup terms.
	 * @param poTags array of post-tags for markup terms.
	 */
	public WhitespaceFragmentsBuilder(final String[] prTags, final String[] poTags) {
		this.preTags = prTags;
		this.postTags = poTags;
	}

	/**
	 * do nothing. return the source list.
	 * @param src src
	 * @return src
	 */
	public final List<WeightedFragInfo> getWeightedFragInfoList(final List<WeightedFragInfo> src) {
		return src;
	}

	/**
	 * make Fragment.
	 * @param buffer buffer
	 * @param index index
	 * @param values values
	 * @param fragInfo fragInfo
	 * @return fragment
	 */
	/*
	 * protected final String makeFragment(final StringBuilder buffer, final int[] index, final String[] values, final
	 * WeightedFragInfo fragInfo) { StringBuilder fragment = new StringBuilder(); String src = getFragmentSource(buffer,
	 * index, values, fragInfo); final int s = fragInfo.startOffset; int srcIndex = 0; for (SubInfo subInfo :
	 * fragInfo.subInfos) { for (Toffs to : subInfo.termsOffsets) { fragment.append(src.substring(srcIndex,
	 * to.startOffset - s)) .append(getPreTag(subInfo.seqnum)) .append(src.substring(to.startOffset - s, to.endOffset -
	 * s)) .append(getPostTag(subInfo.seqnum)); srcIndex = to.endOffset - s; } }
	 * fragment.append(src.substring(srcIndex)); return fragment.toString(); }
	 */

	/**
	 * getFragmentSource.
	 * @param buffer buffer
	 * @param index index
	 * @param values values
	 * @param fragInfo fragInfo
	 * @return fragSource
	 */
	protected final String getFragmentSource(final StringBuilder buffer, final int[] index, final Field[] values,
			final WeightedFragInfo fragInfo) {
		while (buffer.length() < fragInfo.endOffset && index[0] < values.length) {
			if (index[0] > 0 && values[index[0]].stringValue().length() > 0) {
				buffer.append(' ');
			}
			buffer.append(values[index[0]++].stringValue());
		}
		while (fragInfo.startOffset > 0 && !Character.isWhitespace(buffer.charAt(fragInfo.startOffset))) {
			fragInfo.startOffset--;
		}
		while (fragInfo.endOffset < buffer.length() && !Character.isWhitespace(buffer.charAt(fragInfo.endOffset - 1))) {
			fragInfo.endOffset++;
		}
		return buffer.substring(fragInfo.startOffset, Math.min(buffer.length(), fragInfo.endOffset));
	}

	/**
	 * Fetch pre tag.
	 * @param num seq number.
	 * @return tag
	 */
	protected final String getPreTag(final int num) {
		int n = num % preTags.length;
		return preTags[n];
	}

	/**
	 * Fetch post tag.
	 * @param num seq number.
	 * @return tag
	 */
	protected final String getPostTag(final int num) {
		int n = num % postTags.length;
		return postTags[n];
	}

	/**
	 * Create Fragment.
	 * @param reader reader
	 * @param docId docId
	 * @param fieldName fieldName
	 * @param fieldFragList fragList
	 * @throws IOException ex
	 * @return test
	 */
	public final String createFragment(final IndexReader reader, final int docId, final String fieldName,
			final FieldFragList fieldFragList) throws IOException {
		String[] fragments = createFragments(reader, docId, fieldName, fieldFragList, 1);
		if (fragments == null || fragments.length == 0) {
			return null;
		}
		return fragments[0];
	}

	/**
	 * CreateFragments.
	 * @param reader Reader
	 * @param docId docId
	 * @param fieldName fieldName
	 * @param fieldFragList fragLIst
	 * @param maxNumFragments Fragnum
	 * @throws IOException ex
	 * @return fragments
	 */
	public final String[] createFragments(final IndexReader reader, final int docId, final String fieldName,
			final FieldFragList fieldFragList, final int maxNumFragments) throws IOException {
		if (maxNumFragments < 0) {
			throw new IllegalArgumentException("maxNumFragments(" + maxNumFragments + ") must be positive number.");
		}
		List<WeightedFragInfo> fragInfos = getWeightedFragInfoList(fieldFragList.fragInfos);

		List<String> fragments = new ArrayList<String>(maxNumFragments);
		Field[] values = getFields(reader, docId, fieldName);
		if (values.length == 0) {
			return null;
		}
		StringBuilder buffer = new StringBuilder();
		int[] nextValueIndex = { 0 };
		for (int n = 0; n < maxNumFragments && n < fragInfos.size(); n++) {
			WeightedFragInfo fragInfo = fragInfos.get(n);
			fragments.add(makeFragment(buffer, nextValueIndex, values, fragInfo));
		}
		return fragments.toArray(new String[fragments.size()]);
	}

	/**
	 * Make Fragment.
	 * @param buffer buffer
	 * @param index index
	 * @param values values
	 * @param fragInfo fragInfo
	 * @return fragment
	 */
	protected final String makeFragment(final StringBuilder buffer, final int[] index, final Field[] values,
			final WeightedFragInfo fragInfo) {
		String src = getFragmentSource(buffer, index, values, fragInfo);
		final int s = fragInfo.startOffset;
		return makeFragment(fragInfo, src, s);
	}

	/**
	 * make Fragment.
	 * @param fragInfo frag info
	 * @param src src
	 * @param s s
	 * @return fragment
	 */
	private String makeFragment(final WeightedFragInfo fragInfo, final String src, final int s) {
		StringBuilder fragment = new StringBuilder();
		int srcIndex = 0;
		for (SubInfo subInfo : fragInfo.subInfos) {
			for (Toffs to : subInfo.termsOffsets) {
				fragment.append(src.substring(srcIndex, to.startOffset - s)).append(getPreTag(subInfo.seqnum))
						.append(src.substring(to.startOffset - s, to.endOffset - s)).append(getPostTag(subInfo.seqnum));
				srcIndex = to.endOffset - s;
			}
		}
		fragment.append(src.substring(srcIndex));
		return fragment.toString();
	}

	/**
	 * fetFields.
	 * @param reader reader
	 * @param docId docid
	 * @param fieldName fielname
	 * @return fields
	 * @throws IOException err
	 */
	protected final Field[] getFields(final IndexReader reader, final int docId, final String fieldName)
			throws IOException {
		// according to javadoc, doc.getFields(fieldName) 
		// cannot be used with lazy loaded field???
		Document doc = reader.document(docId, new MapFieldSelector(new String[] { fieldName }));
		return doc.getFields(fieldName);
		// according to Document class javadoc, this never returns null
	}

	public String createFragment(IndexReader arg0, int arg1, String arg2, FieldFragList arg3, String[] arg4,
			String[] arg5, Encoder arg6) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public String[] createFragments(IndexReader arg0, int arg1, String arg2, FieldFragList arg3, int arg4,
			String[] arg5, String[] arg6, Encoder arg7) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
}
