package com.gentics.cr.lucene.indexer.transformer.ppt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.hpsf.PropertySetFactory;
import org.apache.poi.poifs.eventfilesystem.POIFSReader;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderEvent;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderListener;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.util.LittleEndian;

/**
 * 
 * Last changed: $Date: 2009-06-24 17:10:19 +0200 (Mi, 24 Jun 2009) $
 * @version $Revision: 99 $
 * @author $Author: supnig@constantinopel.at $
 */
public class MSPowerPointParser implements POIFSReaderListener {

	private InputStream is;
	private ByteArrayOutputStream writer;

	/**
	 * Create new Instance of MSPowerPointParser.
	 * @param is
	 */
	public MSPowerPointParser(InputStream is) {
		this.is = is;
	}

	/**
	 * Get contents of ppt document.
	 */
	public String getContents() {
		String contents = "";
		try {
			POIFSReader reader = new POIFSReader();
			writer = new ByteArrayOutputStream();
			reader.registerListener(this);
			reader.read(is);
			contents = writer.toString(getEncoding());
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				this.is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return contents;

	}

	/**
	 * Hashmap containing the mapping between codepages (office documents) and encodings (java streams).
	 */
	private final static HashMap<Integer, String> ENCODINGMAPPING = new HashMap<Integer, String>();
	static {
		ENCODINGMAPPING.put(1252, "WINDOWS-1252");
	}

	private String getEncoding() {
		if (ps != null) {
			// get the encoding from the document:
			// http://poi.terra-intl.com/hpsf/how-to.html
			int codepage = ps.getFirstSection().getCodepage();
			if (ENCODINGMAPPING.containsKey(codepage)) {
				return ENCODINGMAPPING.get(codepage);
			}
		}
		//return system default charset
		return java.nio.charset.Charset.defaultCharset().toString();
	}

	PropertySet ps = null;

	/**
	 * @param event 
	 */
	public void processPOIFSReaderEvent(POIFSReaderEvent event) {
		try {
			if (event.getName().equalsIgnoreCase("PowerPoint Document")) {
				DocumentInputStream input = event.getStream();
				byte[] buffer = new byte[input.available()];
				input.read(buffer, 0, input.available());
				processContent(0, buffer.length, buffer);
			} else if (event.getName().equalsIgnoreCase("DocumentSummaryInformation")
					|| event.getName().equalsIgnoreCase("SummaryInformation")) {
				ps = PropertySetFactory.create(event.getStream());
			}
		} catch (Exception e) {
			throw new RuntimeException("Cannot process PPT Document.", e);
		}
	}

	private void processContent(int beginIndex, int endIndex, byte[] buffer) {
		while (beginIndex < endIndex) {
			int containerFlag = LittleEndian.getUShort(buffer, beginIndex);
			int recordType = LittleEndian.getUShort(buffer, beginIndex + 2);
			long recordLength = LittleEndian.getUInt(buffer, beginIndex + 4);
			beginIndex += 8;
			if ((containerFlag & 0x0f) == 0x0f) {
				processContent(beginIndex, beginIndex + (int) recordLength, buffer);
			} else if (recordType == 4008) {
				writer.write(buffer, beginIndex, (int) recordLength);
				writer.write(' ');
			}
			beginIndex += (int) recordLength;
		}
	}

}
