package com.gentics.cr.plink;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.gentics.api.portalnode.connector.CCPLinkInformation;
import com.gentics.api.portalnode.connector.PLinkReplacer;

public class PLinkInputStream extends InputStream {

	private InputStream is;

	private PLinkReplacer pr;

	private static final byte[] plinkTemplate = new byte[] { '<', 'p', 'l', 'i', 'n', 'k' };

	private byte plinkClose = '>';

	private final Queue<Byte> backBuf = new LinkedList<Byte>();

	public PLinkInputStream(InputStream inputStream, PLinkReplacer plinkReplacer) {
		is = inputStream;
		pr = plinkReplacer;
	}

	@Override
	public int read() throws IOException {
		if (!backBuf.isEmpty()) {
			return backBuf.poll();
		}
		int b = is.read();
		if (b == plinkTemplate[0]) {
			b = peekAndReplace();
		}
		return b;
	}

	private byte peekAndReplace() throws IOException {
		List<Byte> peekFailBuffer = new ArrayList<Byte>();
		int templatePos = 1;
		int c = -1;
		peekFailBuffer.add((byte) plinkTemplate[0]);

		while (templatePos < plinkTemplate.length && (c = is.read()) == plinkTemplate[templatePos]) {
			templatePos++;
			peekFailBuffer.add((byte) c);
		}

		if (templatePos == plinkTemplate.length) {
			//fount PLINK
			StringBuffer plinkBuffer = new StringBuffer();
			c = is.read();
			while (c != plinkClose && c != -1) {
				plinkBuffer.append((char) c);
				c = is.read();
			}
			String currentPLink = plinkBuffer.toString();
			int posID = currentPLink.indexOf("id=");
			if (posID >= 0) {
				posID = posID + "id=\"".length();
				int posIDEnd = currentPLink.indexOf('"', posID + 1);
				if (posIDEnd == -1) {
					posIDEnd = currentPLink.indexOf('\'', posID + 1);
				}
				// found valid plink
				if (posIDEnd >= 0) {
					String linkID = currentPLink.substring(posID, posIDEnd);
					String out = pr.replacePLink(new CCPLinkInformation(linkID));
					byte[] bytes = out.getBytes();
					for (byte b : bytes) {
						backBuf.offer(b);
					}
				}
			}
		} else {
			if (c != -1) {
				peekFailBuffer.add((byte) c);
			}
			for (Byte b : peekFailBuffer) {
				backBuf.offer(b);
			}
		}

		Byte ret = null;
		if (backBuf.isEmpty()) {
			ret = (byte) is.read();
		} else {
			ret = backBuf.poll();
		}

		return ret;
	}

}
