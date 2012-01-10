package com.gentics.cr.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.gentics.cr.RequestProcessor;

public class PrefixHostRelativeLinksTest {
	
	@Test
	public void testPrefixHostRelativeLinks() {
		String source = "Prefixed:<a href=\"/asdf/blah\">hehe</a>\n";
		source += "Ignored:<a href=\"http://meinhost/blahblah/xyz\">wargl</a>\n";
		source += "Prefixed image:<img alt=\"test\" src=\"/meinbild.jpg\" />\n";
		source += "Prefixed single quote:<a title=\"Test Title\" href='/mein/relativer/link.html'>testing</a>\n";
		String result = RequestProcessor.prefixHostRelativeLinks(source, "http://mein.prefix.host");
		System.out.println("before: \n" + source + "after: \n" + result);
		assertEquals("Prefixed:<a href=\"http://mein.prefix.host/asdf/blah\">hehe</a>\n",RequestProcessor.prefixHostRelativeLinks("Prefixed:<a href=\"/asdf/blah\">hehe</a>\n", "http://mein.prefix.host"));
		assertEquals("Ignored:<a href=\"http://meinhost/blahblah/xyz\">wargl</a>\n",RequestProcessor.prefixHostRelativeLinks("Ignored:<a href=\"http://meinhost/blahblah/xyz\">wargl</a>\n", "http://mein.prefix.host"));
		assertEquals("Prefixed image:<img alt=\"test\" src=\"http://mein.prefix.host/meinbild.jpg\" />\n",RequestProcessor.prefixHostRelativeLinks("Prefixed image:<img alt=\"test\" src=\"/meinbild.jpg\" />\n", "http://mein.prefix.host"));
		assertEquals("Prefixed single quote:<a title=\"Test Title\" href='http://mein.prefix.host/mein/relativer/link.html'>testing</a>\n",RequestProcessor.prefixHostRelativeLinks("Prefixed single quote:<a title=\"Test Title\" href='/mein/relativer/link.html'>testing</a>\n", "http://mein.prefix.host"));
	}
	
}
