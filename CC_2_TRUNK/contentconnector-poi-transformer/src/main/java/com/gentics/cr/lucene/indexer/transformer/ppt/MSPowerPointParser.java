package com.gentics.cr.lucene.indexer.transformer.ppt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

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
 *
 */
public class MSPowerPointParser implements POIFSReaderListener {

	
	private InputStream is;
	private ByteArrayOutputStream writer;
	
	/**
	 * Create new Instance of MSPowerPointParser
	 * @param is
	 */
	public MSPowerPointParser(InputStream is)
	{
		this.is=is;
		
	}
	
	/**
	 * Get contents of ppt document
	 * @return
	 */
	public String getContents(){
		String contents = "";
		try
		{
	        
	        POIFSReader reader = new POIFSReader();
	        writer = new ByteArrayOutputStream();
	        reader.registerListener(this);
	        reader.read(is);
	        contents = writer.toString();
		}catch(Exception ex)
		{
			ex.printStackTrace();
		}finally
		{
			try {
				this.is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
        return contents;

    }


	
	
	/**
	 * @param event 
	 * 
	 */
	public void processPOIFSReaderEvent(POIFSReaderEvent event){
			if(!event.getName().equalsIgnoreCase("PowerPoint Document"))
            	return;
			try
			{
	            DocumentInputStream input = event.getStream();
	            byte[] buffer = new byte[input.available()];
	            input.read(buffer, 0, input.available());
	            processContent(0, buffer.length, buffer);
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
            
	}
	
	 private void processContent(int beginIndex, int endIndex, byte[] buffer) {
	        while (beginIndex < endIndex) {
	            int containerFlag = LittleEndian.getUShort(buffer, beginIndex);
	            int recordType = LittleEndian.getUShort(buffer, beginIndex + 2);
	            long recordLength = LittleEndian.getUInt(buffer, beginIndex + 4);
	            beginIndex += 8;
	            if ((containerFlag & 0x0f) == 0x0f) {
	                processContent(beginIndex, beginIndex + (int)recordLength, buffer);
	            } else if (recordType == 4008) {
	                writer.write(buffer, beginIndex, (int)recordLength);
	                writer.write(' ');
	            }
	            beginIndex += (int)recordLength;
	        }
	    }


}
