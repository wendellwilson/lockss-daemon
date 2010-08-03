/*
 * $Id: TestBePressMetadataExtractor.java,v 1.11 2010-08-03 11:52:25 dsferopoulos Exp $
 */

/*

 Copyright (c) 2000-2010 Board of Trustees of Leland Stanford Jr. University,
 all rights reserved.

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of his software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL
 STANFORD UNIVERSITY BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
 IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 Except as contained in this notice, the name of Stanford University shall not
 be used in advertising or otherwise to promote the sale, use or other dealings
 in this Software without prior written authorization from Stanford University.

 */

package org.lockss.plugin.bepress;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import org.lockss.test.*;
import org.lockss.util.*;
import org.lockss.config.*;
import org.lockss.daemon.*;
import org.lockss.crawler.*;
import org.lockss.repository.*;
import org.lockss.extractor.*;
import org.lockss.plugin.*;
import org.lockss.plugin.base.*;
import org.lockss.plugin.simulated.*;

public class TestBePressMetadataExtractor extends LockssTestCase {

	static Logger log = Logger.getLogger("TestBePressMetadataExtractor");

	private MockLockssDaemon theDaemon;
	private SimulatedArchivalUnit sau; // Simulated AU to generate content
	private ArchivalUnit bau; // BePress AU
	private static final String issnTemplate = "%1%2%3%1-%3%1%2%3";	

	private static String PLUGIN_NAME = "org.lockss.plugin.bepress.ClockssBerkeleyElectronicPressPlugin";

	private static String BASE_URL = "http://www.bepress.com/";
	private static String SIM_ROOT = BASE_URL + "xyzjn/";

	public void setUp() throws Exception {
		super.setUp();
		String tempDirPath = getTempDir().getAbsolutePath() + File.separator;
		ConfigurationUtil.setFromArgs(
				LockssRepositoryImpl.PARAM_CACHE_LOCATION, tempDirPath);

		theDaemon = getMockLockssDaemon();
		theDaemon.getAlertManager();
		theDaemon.getPluginManager().setLoadablePluginsReady(true);
		theDaemon.setDaemonInited(true);
		theDaemon.getPluginManager().startService();
		theDaemon.getCrawlManager();

		sau = PluginTestUtil.createAndStartSimAu(MySimulatedPlugin.class,
				simAuConfig(tempDirPath));
		bau = PluginTestUtil.createAndStartAu(PLUGIN_NAME, bePressAuConfig());
	}

	public void tearDown() throws Exception {
		sau.deleteContentTree();
		theDaemon.stopDaemon();
		super.tearDown();
	}

	Configuration simAuConfig(String rootPath) {
		Configuration conf = ConfigManager.newConfiguration();
		conf.put("root", rootPath);
		conf.put("base_url", SIM_ROOT);
		conf.put("depth", "2");
		conf.put("branch", "3");
		conf.put("numFiles", "7");
		conf.put("fileTypes",""	+ (SimulatedContentGenerator.FILE_TYPE_PDF + SimulatedContentGenerator.FILE_TYPE_HTML));
		conf.put("default_article_mime_type", "application/html");
		return conf;
	}

	Configuration bePressAuConfig() {
		Configuration conf = ConfigManager.newConfiguration();
		conf.put("base_url", BASE_URL);
		conf.put("volume", "1");
		conf.put("journal_abbr", "xyzjn");
		return conf;
	}

	String goodDOI = "10.2202/2153-3792.1037";
	String goodVolume = "13";
	String goodIssue = "4";
	String goodStartPage = "123";
	String goodISSN = "1234-5678";
	String goodDate = "4/1/2000";
	String goodAuthor = "Gandhi, Pankaj J.; Talia, Yogen H.; Murthy, Z.V.P.";
	String goodArticleTitle = "Spurious Results";
	String goodJournalTitle = "Chemical Product and Process Modeling";
	String goodAbsUrl = "http://www.example.com/bogus/vol13/iss4/art123/abs";
	String goodPdfUrl = "http://www.example.com/bogus/vol13/iss4/art123/pdf";
	String goodHtmUrl = "http://www.example.com/bogus/vol13/iss4/art123/full";

	String goodContent = "<HTML><HEAD><TITLE>"+ goodArticleTitle+ "</TITLE></HEAD><BODY>\n"
			+ "<meta name=\"bepress_citation_journal_title\" content=\""+ goodJournalTitle+ "\">\n"
			+ "<meta name=\"bepress_citation_authors\" content=\""+ goodAuthor+ "\">\n"
			+ "<meta name=\"bepress_citation_title\" content=\""+ goodArticleTitle+ "\">\n"
			+ "<meta name=\"bepress_citation_date\" content=\""+ goodDate+ "\">\n"
			+ "<meta name=\"bepress_citation_volume\""+ " content=\""+ goodVolume+ "\">\n"
			+ "<meta name=\"bepress_citation_issue\" content=\""+ goodIssue	+ "\">\n"
			+ "<meta name=\"bepress_citation_firstpage\""+ " content=\""+ goodStartPage	+ "\">\n"
			+ "<meta name=\"bepress_citation_pdf_url\""	+ " content=\""	+ goodPdfUrl + "\">\n"
			+ "<meta name=\"bepress_citation_abstract_html_url\"" + " content=\"" + goodAbsUrl + "\">\n"
			+ "<meta name=\"bepress_citation_doi\"" + " content=\""	+ goodDOI + "\">\n"
			+"<div id=\"issn\">\n" +
            "<p>ISSN: "+goodISSN+"</p>\n" +
            "</div>";
	
			//+ "  <div id=\"issn\"><!-- FILE: /data/templates/www.example.com/bogus/issn.inc -->ISSN: " + goodISSN + " </div>\n";

	public void testExtractFromGoodContent() throws Exception {
		String url = "http://www.example.com/vol1/issue2/art3/";
		MockCachedUrl cu = new MockCachedUrl(url, bau);
		cu.setContent(goodContent);
		cu.setContentSize(goodContent.length());
		cu.setProperty(CachedUrl.PROPERTY_CONTENT_TYPE, "text/html");
		FileMetadataExtractor me = new BePressHtmlMetadataExtractorFactory.BePressHtmlMetadataExtractor();
		ArticleMetadata md = me.extract(cu);
		assertNotNull(md);
		assertEquals(goodDOI, md.getDOI());
		assertEquals(goodVolume, md.getVolume());
		assertEquals(goodIssue, md.getIssue());
		assertEquals(goodStartPage, md.getStartPage());
		assertEquals(goodISSN, md.getISSN());

		goodAuthor = goodAuthor.replaceAll(",", "");
		goodAuthor = goodAuthor.replaceAll(";", ",");

		assertEquals(goodAuthor, md.getAuthor());
		assertEquals(goodArticleTitle, md.getArticleTitle());
		assertEquals(goodJournalTitle, md.getJournalTitle());
		assertEquals(goodDate, md.getDate());
	}

	String badContent = "<HTML><HEAD><TITLE>"
			+ goodArticleTitle
			+ "</TITLE></HEAD><BODY>\n"
			+ "<meta name=\"foo\""
			+ " content=\"bar\">\n"
			+ "  <div id=\"issn\">"
			+ "<!-- FILE: /data/templates/www.example.com/bogus/issn.inc -->MUMBLE: "
			+ goodISSN + " </div>\n";

	public void testExtractFromBadContent() throws Exception {
		String url = "http://www.example.com/vol1/issue2/art3/";
		MockCachedUrl cu = new MockCachedUrl(url, bau);
		cu.setContent(badContent);
		cu.setContentSize(badContent.length());
		cu.setProperty(CachedUrl.PROPERTY_CONTENT_TYPE, "text/html");
		FileMetadataExtractor me = new BePressHtmlMetadataExtractorFactory.BePressHtmlMetadataExtractor();
		ArticleMetadata md = me.extract(cu);
		assertNotNull(md);
		assertNull(md.getDOI());
		assertNull(md.getVolume());
		assertNull(md.getIssue());
		assertNull(md.getStartPage());
		assertNull(md.getISSN());

		assertEquals(1, md.size());
		assertEquals("bar", md.getProperty("foo"));
	}	

	public static class MySimulatedPlugin extends SimulatedPlugin {
		public ArchivalUnit createAu0(Configuration auConfig)
				throws ArchivalUnit.ConfigurationException {
			ArchivalUnit au = new SimulatedArchivalUnit(this);
			au.setConfiguration(auConfig);
			return au;
		}

		public SimulatedContentGenerator getContentGenerator(Configuration cf,
				String fileRoot) {
			return new MySimulatedContentGenerator(fileRoot);
		}

	}

	public static class MySimulatedContentGenerator extends
			SimulatedContentGenerator {
		protected MySimulatedContentGenerator(String fileRoot) {
			super(fileRoot);
		}

		public String getHtmlFileContent(String filename, int fileNum,
				int depth, int branchNum, boolean isAbnormal) {
			String file_content = "<HTML><HEAD><TITLE>" + filename + "</TITLE></HEAD><BODY>\n";
			
			file_content += "  <meta name=\"lockss.filenum\" content=\""
					+ fileNum + "\">\n";
			file_content += "  <meta name=\"lockss.depth\" content=\"" + depth
					+ "\">\n";
			file_content += "  <meta name=\"lockss.branchnum\" content=\""
					+ branchNum + "\">\n";			

			file_content += getHtmlContent(fileNum, depth, branchNum,
					isAbnormal);
			file_content += "\n</BODY></HTML>";
			logger.debug2("MySimulatedContentGenerator.getHtmlFileContent: "
					+ file_content);

			return file_content;
		}
	}
}
