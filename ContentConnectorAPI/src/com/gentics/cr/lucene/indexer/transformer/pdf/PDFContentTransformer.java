package com.gentics.cr.lucene.indexer.transformer.pdf;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.apache.pdfbox.exceptions.CryptographyException;
import org.apache.pdfbox.exceptions.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.lucene.indexer.transformer.ContentTransformer;

/**
 * 
 * Last changed: $Date$
 * @version $Revision$
 * @author $Author$
 *
 */
public class PDFContentTransformer extends ContentTransformer{

	private PDFTextStripper stripper = null;
	
	/**
	 * Get new instance of PDFContentTransformer
	 * @param config
	 */
	public PDFContentTransformer(GenericConfiguration config)
	{
		super(config);
	}
	
	/**
	 * Converts a byte array containing a pdf file to a String that can be indexed by lucene
	 * @param obj
	 * @return
	 */
	public String getStringContents(Object obj)
	{
		ByteArrayInputStream is;
		if(obj instanceof byte[])
		{
			is= new ByteArrayInputStream((byte[])obj);
		}
		else
		{
			throw new IllegalArgumentException("Parameter must be instance of byte[]");
		}
		PDDocument pdfDocument = null;
		String contents=null;
    
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
	        contents = writer.getBuffer().toString();
	
	     
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
		return(contents);
	}
	
	/**
	 * Converts a byte array containing a pdf file to a StringReader that can be indexed by lucene
	 * @param obj
	 * @return StringReader of contents
	 */
	public StringReader getContents(Object obj)
	{
		String s = getStringContents(obj);
		if(s!=null)
		{
			return new StringReader(s);
		}
		return null;
    }
}
