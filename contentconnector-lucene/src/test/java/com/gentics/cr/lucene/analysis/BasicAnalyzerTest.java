package com.gentics.cr.lucene.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.Attribute;
import org.apache.lucene.util.AttributeImpl;

public class BasicAnalyzerTest {
	
	public static void assertTokenStreamContents(TokenStream ts, String[] output) throws IOException {
		assertTokenStreamContents(ts, output, null, null, null, null, null);
	}
	
	public static void assertTokenStreamContents(TokenStream ts, String[] output, int startOffsets[], int endOffsets[], String types[], int posIncrements[], Integer finalOffset) throws IOException {
	    assertNotNull(output);
	    CheckClearAttributesAttribute checkClearAtt = ts.addAttribute(CheckClearAttributesAttribute.class);
	    
	    assertTrue("has no CharTermAttribute", ts.hasAttribute(CharTermAttribute.class));
	    CharTermAttribute termAtt = ts.getAttribute(CharTermAttribute.class);
	    
	    OffsetAttribute offsetAtt = null;
	    if (startOffsets != null || endOffsets != null || finalOffset != null) {
	      assertTrue("has no OffsetAttribute", ts.hasAttribute(OffsetAttribute.class));
	      offsetAtt = ts.getAttribute(OffsetAttribute.class);
	    }
	    
	    TypeAttribute typeAtt = null;
	    if (types != null) {
	      assertTrue("has no TypeAttribute", ts.hasAttribute(TypeAttribute.class));
	      typeAtt = ts.getAttribute(TypeAttribute.class);
	    }
	    
	    PositionIncrementAttribute posIncrAtt = null;
	    if (posIncrements != null) {
	      assertTrue("has no PositionIncrementAttribute", ts.hasAttribute(PositionIncrementAttribute.class));
	      posIncrAtt = ts.getAttribute(PositionIncrementAttribute.class);
	    }
	    
	    ts.reset();
	    for (int i = 0; i < output.length; i++) {
	      // extra safety to enforce, that the state is not preserved and also assign bogus values
	      ts.clearAttributes();
	      termAtt.setEmpty().append("bogusTerm");
	      if (offsetAtt != null) offsetAtt.setOffset(14584724,24683243);
	      if (typeAtt != null) typeAtt.setType("bogusType");
	      if (posIncrAtt != null) posIncrAtt.setPositionIncrement(45987657);
	      
	      checkClearAtt.getAndResetClearCalled(); // reset it, because we called clearAttribute() before
	      assertTrue("token "+i+" does not exist", ts.incrementToken());
	      assertTrue("clearAttributes() was not called correctly in TokenStream chain", checkClearAtt.getAndResetClearCalled());
	      
	      assertEquals("term "+i, output[i], termAtt.toString());
	      if (startOffsets != null)
	        assertEquals("startOffset "+i, startOffsets[i], offsetAtt.startOffset());
	      if (endOffsets != null)
	        assertEquals("endOffset "+i, endOffsets[i], offsetAtt.endOffset());
	      if (types != null)
	        assertEquals("type "+i, types[i], typeAtt.type());
	      if (posIncrements != null)
	        assertEquals("posIncrement "+i, posIncrements[i], posIncrAtt.getPositionIncrement());
	      
	      // we can enforce some basic things about a few attributes even if the caller doesn't check:
	      if (offsetAtt != null) {
	        assertTrue("startOffset must be >= 0", offsetAtt.startOffset() >= 0);
	        assertTrue("endOffset must be >= 0", offsetAtt.endOffset() >= 0);
	        assertTrue("endOffset must be >= startOffset", offsetAtt.endOffset() >= offsetAtt.startOffset());
	      }
	      if (posIncrAtt != null) {
	        assertTrue("posIncrement must be >= 0", posIncrAtt.getPositionIncrement() >= 0);
	      }
	    }
	    assertFalse("end of stream", ts.incrementToken());
	    ts.end();
	    if (finalOffset != null)
	      assertEquals("finalOffset ", finalOffset.intValue(), offsetAtt.endOffset());
	    if (offsetAtt != null) {
	      assertTrue("finalOffset must be >= 0", offsetAtt.endOffset() >= 0);
	    }
	    ts.close();
	  }
	
	public static interface CheckClearAttributesAttribute extends Attribute {
	    boolean getAndResetClearCalled();
	  }

	  public static final class CheckClearAttributesAttributeImpl extends AttributeImpl implements CheckClearAttributesAttribute {
	    private boolean clearCalled = false;
	    
	    public boolean getAndResetClearCalled() {
	      try {
	        return clearCalled;
	      } finally {
	        clearCalled = false;
	      }
	    }

	    @Override
	    public void clear() {
	      clearCalled = true;
	    }

	    @Override
	    public boolean equals(Object other) {
	      return (
	        other instanceof CheckClearAttributesAttributeImpl &&
	        ((CheckClearAttributesAttributeImpl) other).clearCalled == this.clearCalled
	      );
	    }

	    @Override
	    public int hashCode() {
	      return 76137213 ^ Boolean.valueOf(clearCalled).hashCode();
	    }
	    
	    @Override
	    public void copyTo(AttributeImpl target) {
	      ((CheckClearAttributesAttributeImpl) target).clear();
	    }
	  }
}
