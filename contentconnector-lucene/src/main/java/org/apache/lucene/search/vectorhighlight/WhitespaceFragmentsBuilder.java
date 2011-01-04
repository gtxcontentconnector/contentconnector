package org.apache.lucene.search.vectorhighlight;

import java.util.List;

import org.apache.lucene.search.vectorhighlight.FieldFragList.WeightedFragInfo;
import org.apache.lucene.search.vectorhighlight.FieldFragList
	.WeightedFragInfo.SubInfo;
import org.apache.lucene.search.vectorhighlight.FieldPhraseList
	.WeightedPhraseInfo.Toffs;
/**
 * THIS CLASS SHOULD BE REMOVED WHEN UPDATING TO LUCENE 4.0.
 * @author Christopher
 *
 */
public class WhitespaceFragmentsBuilder extends BaseFragmentsBuilder {


	/**
	 * a constructor.
	 */
	public WhitespaceFragmentsBuilder() {
		super();
	}

	/**
	 * a constructor.
	 * 
	 * @param preTags array of pre-tags for markup terms.
	 * @param postTags array of post-tags for markup terms.
	 */
	public WhitespaceFragmentsBuilder(final String[] preTags
			, final String[] postTags) {
		super(preTags, postTags);
	}

	/**
	 * do nothing. return the source list.
	 * @param src src
	 * @return src
	 */
	public final List<WeightedFragInfo> getWeightedFragInfoList(
			final List<WeightedFragInfo> src) {
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
	protected final String makeFragment(final StringBuilder buffer,
			final int[] index, final String[] values,
			final WeightedFragInfo fragInfo) {
		StringBuilder fragment = new StringBuilder();
		String src = getFragmentSource(buffer, index, values, fragInfo);
		final int s = fragInfo.startOffset;
		int srcIndex = 0;
		for (SubInfo subInfo : fragInfo.subInfos) {
			for (Toffs to : subInfo.termsOffsets) {
				fragment.append(src.substring(srcIndex, to.startOffset - s))
					.append(getPreTag(subInfo.seqnum))
					.append(src.substring(to.startOffset - s, to.endOffset - s))
					.append(getPostTag(subInfo.seqnum));
				srcIndex = to.endOffset - s;
			}
		}
		fragment.append(src.substring(srcIndex));
		return fragment.toString();
	}

	/**
	 * getFragmentSource.
	 * @param buffer buffer
	 * @param index index
	 * @param values values
	 * @param fragInfo fragInfo
	 * @return fragSource
	 */
	protected final String getFragmentSource(final StringBuilder buffer, 
			final int[] index, final String[] values, 
			final WeightedFragInfo fragInfo) {
		while (buffer.length() < fragInfo.endOffset 
				&& index[0] < values.length) {
			if (index[0] > 0 && values[index[0]].length() > 0) {
				buffer.append(' ');
			}
			buffer.append(values[index[0]++]);
		}
		while (fragInfo.startOffset > 0 && !Character
				.isWhitespace(buffer.charAt(fragInfo.startOffset))) {
			fragInfo.startOffset--;
		}
		while (fragInfo.endOffset <  buffer.length() && !Character
				.isWhitespace(buffer.charAt(fragInfo.endOffset - 1))) {
			fragInfo.endOffset++;
		}
		return buffer.substring(fragInfo.startOffset, 
				Math.min(buffer.length(), fragInfo.endOffset));
	}

}