package com.gentics.cr.lucene.indexer.pdf;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.pdfbox.exceptions.CryptographyException;
import org.apache.pdfbox.exceptions.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.searchengine.lucene.LucenePDFDocument;
import org.apache.pdfbox.util.PDFTextStripper;

/**
 * 
 * Last changed: $Date$
 * @version $Revision$
 * @author $Author$
 *
 */
public class PDFHelper {

	private static PDFTextStripper stripper = null;
	
	/**
	 * Converts a byte array containing a pdf file to Field that can be indexed by lucene
	 * @param binarycontent
	 * @return Field contents
	 */
	public static Field getPDFText(byte[] binarycontent)
	{
		try {
			Document doc = LucenePDFDocument.getDocument(new ByteArrayInputStream(binarycontent));
			return(doc.getField("contents"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return(null);
	}
	
	/**
	 * Converts a byte array containing a pdf file to a StringReader that can be indexed by lucene
	 * @param binarycontent
	 * @return StringReader of contents
	 */
	public static StringReader getContents(byte[] binarycontent)
	{
		ByteArrayInputStream is = new ByteArrayInputStream(binarycontent);
		PDDocument pdfDocument = null;
		StringReader reader=null;
    
        try {
			pdfDocument = PDDocument.load( is );
		

	        if( pdfDocument.isEncrypted() )
	        {
	            //Just try using the default password and move on
	            pdfDocument.decrypt( "" );
	        }
	
	        //create a writer where to append the text content.
	        StringWriter writer = new StringWriter();
	        if( stripper == null )
	        {
	            stripper = new PDFTextStripper();
	        }
	        else
	        {
	            stripper.resetEngine();
	        }
	        stripper.writeText( pdfDocument, writer );
	        
	
	        // Note: the buffer to string operation is costless;
	        // the char array value of the writer buffer and the content string
	        // is shared as long as the buffer content is not modified, which will
	        // not occur here.
	        String contents = writer.getBuffer().toString();
	
	        reader = new StringReader( contents );
        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CryptographyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidPasswordException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch(Exception e)
		{
			//Catch all Exceptions happening here to not disturb the indexer
			e.printStackTrace();
		}
		finally
        {
            if( pdfDocument != null )
            {
                try {
					pdfDocument.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        }
        return(reader);
    }
}
