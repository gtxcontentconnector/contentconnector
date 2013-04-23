package com.gentics.cr.lucene.indexer.transformer.xls;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFComment;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.lucene.indexer.transformer.ContentTransformer;

/**
 * 
 * Last changed: $Date$
 * @version $Revision$
 * @author $Author$
 *
 */
public class XLSContentTransformer extends ContentTransformer {
	private static final String TRANSFORMER_ATTRIBUTE_KEY = "attribute";
	private String attribute = "";

	/**
	 * Get new instance of XLSContentTransformer
	 * @param config
	 */
	public XLSContentTransformer(GenericConfiguration config) {
		super(config);
		attribute = (String) config.get(TRANSFORMER_ATTRIBUTE_KEY);
	}

	/**
	 * Converts a byte array that contains a excel file into a string with its contents
	 * @param obj
	 * @return
	 */
	private String getStringContents(Object obj) throws CRException {
		StringBuilder contents = new StringBuilder();
		ByteArrayInputStream is;
		if (obj instanceof byte[]) {
			is = new ByteArrayInputStream((byte[]) obj);
		} else {
			throw new IllegalArgumentException("Parameter must be instance of byte[]");
		}

		POIFSFileSystem fs;
		HSSFWorkbook wb;
		try {
			fs = new POIFSFileSystem(is);
			wb = new HSSFWorkbook(fs);
			if (wb != null) {
				int Scount = wb.getNumberOfSheets();
				for (int s = 0; s < Scount; s++) {
					HSSFSheet sheet = wb.getSheetAt(s);
					if (sheet != null) {
						int Rcount = sheet.getPhysicalNumberOfRows();
						for (int r = 0; r < Rcount; r++) {
							HSSFRow row = sheet.getRow(r);
							if (row != null) {
								int cCount = row.getPhysicalNumberOfCells();
								for (int c = 0; c < cCount; c++) {
									HSSFCell cell = row.getCell(c);
									if (cell != null) {
										int celltype = cell.getCellType();
										if (celltype == HSSFCell.CELL_TYPE_STRING) {
											HSSFRichTextString rts = cell.getRichStringCellValue();
											if (rts != null) {
												contents.append(rts.getString() + ",");
											}
										} else if (celltype == HSSFCell.CELL_TYPE_NUMERIC) {
											double num = cell.getNumericCellValue();
											contents.append(num + ",");
										}
										HSSFComment comm = cell.getCellComment();
										if (comm != null) {
											contents.append(comm + ",");
										}

									}
								}
							}
						}
					}
				}
			}

		} catch (IOException e) {
			throw new CRException(e);
		} catch (Exception e) {
			throw new CRException(e);
		}

		return contents.toString();
	}

	@Override
	public void processBean(CRResolvableBean bean) throws CRException {
		if (this.attribute != null) {
			Object obj = bean.get(this.attribute);
			if (obj != null) {
				String newString = getStringContents(obj);
				if (newString != null) {
					bean.set(this.attribute, newString);
				}
			}
		} else {
			LOGGER.error("Configured attribute is null. Bean will not be processed");
		}

	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}
}
