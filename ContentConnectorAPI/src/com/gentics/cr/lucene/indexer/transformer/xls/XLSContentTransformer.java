package com.gentics.cr.lucene.indexer.transformer.xls;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFComment;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.lucene.indexer.transformer.ContentTransformer;

/**
 * 
 * Last changed: $Date$
 * @version $Revision$
 * @author $Author$
 *
 */
public class XLSContentTransformer extends ContentTransformer{
	
	/**
	 * Get new instance of XLSContentTransformer
	 * @param config
	 */
	public XLSContentTransformer(GenericConfiguration config)
	{
		super(config);
	}
	
	/**
	 * Converts a byte array that contains a excel file into a string with its contents
	 * @param obj
	 * @return
	 */
	public String getStringContents(Object obj)
	{
		String contents = "";
		ByteArrayInputStream is;
		if(obj instanceof byte[])
		{
			is= new ByteArrayInputStream((byte[])obj);
		}
		else
		{
			throw new IllegalArgumentException("Parameter must be instance of byte[]");
		}

		POIFSFileSystem fs;
		HSSFWorkbook wb;
		try {
			fs = new POIFSFileSystem(is);
			wb = new HSSFWorkbook(fs);
			if(wb!=null)
			{
				int Scount = wb.getNumberOfSheets();
				for(int s=0;s<Scount;s++)
				{
					HSSFSheet sheet = wb.getSheetAt(s);
					if(sheet!=null)
					{
						int Rcount = sheet.getPhysicalNumberOfRows();
						for(int r=0;r<Rcount;r++)
						{
							HSSFRow row = sheet.getRow(r);
							if(row!=null)
							{
								int Ccount = row.getPhysicalNumberOfCells();
								for(int c=0;c<Ccount;c++)
								{
									HSSFCell cell = row.getCell(c);
									if(cell!=null)
									{
										int celltype = cell.getCellType();
										if(celltype==HSSFCell.CELL_TYPE_STRING)
										{
											HSSFRichTextString rts = cell.getRichStringCellValue();
											if(rts!=null)
											{
												contents = contents + rts.getString()+",";
											}
										}else if(celltype == HSSFCell.CELL_TYPE_NUMERIC)
										{
											double num = cell.getNumericCellValue();
											contents = contents + num+",";
										}
										HSSFComment comm = cell.getCellComment();
										if(comm!=null)
										{
											contents = contents + comm+",";
										}
										
									}
								}
							}
						}
					}
				}
				if("".equals(contents))
				{
					contents=null;
				}
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e)
		{
			//Catch all exceptions to not disturb the indexer
			e.printStackTrace();
		}
		
		return(contents);
	}
	
	/**
	 * Converts a byte array that contains a excel file into a string reader with its contents
	 * @param obj
	 * @return StringReader or null if something bad happens
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
