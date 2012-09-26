package com.gentics.cr.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
/**
 * WriterStream. 
 * Stream that wraps a writer.
 * @author Christopher
 *
 */
public class WriterStream extends OutputStream {
	/**
	 * Writer that should be streamed to.
	 */
	private Writer w;

	/**
	 * Create a new Instance of WriterStream.
	 * @param writer Writer that should be streamed to.
	 */
	public WriterStream(final Writer writer) {
		w = writer;
	}
	
	@Override
	public final void write(final int b) throws IOException {
		w.write(b);
	}

}
