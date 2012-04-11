package com.gentics.cr.plink;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.gentics.api.portalnode.connector.CCPLinkInformation;
import com.gentics.api.portalnode.connector.PLinkReplacer;

public class PLinkOutputStream extends OutputStream {

	private OutputStream os;
	private PLinkReplacer pr;

	private static final byte[] plinkTemplate = new byte[] { '<', 'p', 'l', 'i', 'n', 'k' };

	private int nextPlinkPos = 0;

	private byte plinkClose = '>';

	private List<Byte> backBuffer;

	private StringBuffer plinkBuffer = null;

	private boolean plinkOpen = false;

	public PLinkOutputStream(OutputStream outputStream, PLinkReplacer pLinkReplacer) {
		os = outputStream;
		pr = pLinkReplacer;
	}

	@Override
	public void write(int b) throws IOException {

		if (plinkOpen) {
			if (b == plinkClose) {
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
						os.write(out.getBytes());
					}
				}

				nextPlinkPos = 0;
				plinkOpen = false;
				plinkBuffer = null;
			} else {
				plinkBuffer.append((char) b);
			}

		} else {
			if (nextPlinkPos == 0 && b == plinkTemplate[nextPlinkPos]) {
				backBuffer = new ArrayList<Byte>();
			}

			if (b == plinkTemplate[nextPlinkPos]) {
				backBuffer.add((byte) b);
				if (nextPlinkPos == plinkTemplate.length - 1) {
					plinkOpen = true;
					plinkBuffer = new StringBuffer();
				}
				nextPlinkPos++;
			} else if (nextPlinkPos != 0 && b != plinkTemplate[nextPlinkPos]
					&& nextPlinkPos != (plinkTemplate.length - 1)) {
				for (byte bb : backBuffer) {
					os.write(bb);
				}
				os.write(b);
				backBuffer = null;
				nextPlinkPos = 0;
			} else {
				os.write(b);
			}
		}
	}

}
