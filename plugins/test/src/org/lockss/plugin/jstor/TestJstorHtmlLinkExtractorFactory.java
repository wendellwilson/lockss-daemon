/*
 * $Id: TestJstorHtmlLinkExtractorFactory.java,v 1.1 2014-05-21 18:05:19 alexandraohlson Exp $
 */
/*

 Copyright (c) 2000-2013 Board of Trustees of Leland Stanford Jr. University,
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

package org.lockss.plugin.jstor;

import java.util.Set;

import org.lockss.extractor.LinkExtractor;
import org.lockss.extractor.LinkExtractorFactory;
import org.lockss.plugin.jstor.JstorHtmlLinkExtractorFactory.JstorHtmlLinkExtractor;
import org.lockss.test.LockssTestCase;
import org.lockss.test.MockArchivalUnit;
import org.lockss.util.Constants;
import org.lockss.util.SetUtil;


public class TestJstorHtmlLinkExtractorFactory extends LockssTestCase {

  private JstorHtmlLinkExtractor m_extractor;
  private MyLinkExtractorCallback m_callback;
  static String ENC = Constants.DEFAULT_ENCODING;
  private MockArchivalUnit m_mau;

  private final static String JSTOR_BASE_URL = "http://www.jstor.org/";
  private final static String JSTOR_TOC_URL = JSTOR_BASE_URL + "action/showToc?journalCode=foo&issue=2&volume=11"; 
  private final static String JSTOR_ARTICLE_ABSTRACT_URL = JSTOR_BASE_URL + 
      "stable/info/41495848";

  public static final String htmltest =
      "<html><head><title>Test Title</title>" +
          "<div>" +
          "</head><body>" +
"<form name=\"frmAbs\" id=\"toc\" action=\"\">" +
          "  <input type=\"checkbox\" name=\"doi\" class=\"checkBox\" value=\"10.2307/41495848\" id=\"cite41495848\" />" +
          "</form>" +
          "</body>" +
          "</html>";
  
  public static final String fullLinkHtml =
  "<!--area:--><!--false:10.2307/41495848-->" +
  "  <li>" +
  "<div class=\"cite\">" +
  "<div class=\"subCite\">" +
  "  <input type=\"checkbox\" name=\"doi\" class=\"checkBox\" value=\"10.2307/41495848\" id=\"cite41495848\" />" +
  "</div>" +
  "<div class=\"mainCite\"><div class=\"bd langMatch\">" +
  "  <div class=\"title\">" +
  "    <label for=\"cite41495848\" class=\"hide\">Front Matter</label>" +
  "    <a class=\"title\" href=\"/stable/41495848\">Front Matter</a>" +
  "  </div>" +
  "  <div class=\"stable\">Stable URL: http://www.jstor.org/stable/41495848</div>" +
  "</div>" +
  "<div class=\"ft articleLinks\">" +
  "  <a href=\"/stable/view/41495848\">Page Scan</a>" +
  "  <a class=\"pdflink\" data-articledoi=\"10.2307/41495848\"target=\"_blank\" href=\"/stable/pdfplus/41495848.pdf\">Article PDF</a>" +
  "  <span class=\"articleLinks\"><a href=\"/stable/info/41495848\">Article Summary</a></span>" +
  "</div>" +
  "</div></li>";
  
  public static final String multiLinksHtml =
      "<div class=\"subCite\">" +
          "  <input type=\"checkbox\" name=\"doi\" class=\"checkBox\" value=\"10.2307/41495848\" id=\"cite41495848\" />" +
          "</div>" +
          "<div class=\"subCite\">" +
          "  <input type=\"checkbox\" name=\"doi\" class=\"checkBox\" value=\"10.2307/746318\" id=\"cite746318\" />" +
          "</div>" +
          "<div class=\"subCite\">" +
          "  <input type=\"checkbox\" name=\"doi\" class=\"checkBox\" value=\"10.1525/ncm.2013.36.3.toc\" id=\"10.1525/ncm.2013.36.3.toc\" />" +
          "</div>" +
          "<div class=\"subCite\">" +
          "  <input type=\"checkbox\" name=\"doi\" class=\"checkBox\" value=\"10.3764/aja.117.3.0429\" id=\"10.3764/aja.117.3.0429\" />" +
          "</div>";
  


  @Override
  public void setUp() throws Exception {
    super.setUp();
    //log.setLevel("debug3");
    m_callback = new MyLinkExtractorCallback();
    LinkExtractorFactory fact = new JstorHtmlLinkExtractorFactory();
    m_extractor = (JstorHtmlLinkExtractor) fact.createLinkExtractor("html"); 
      m_mau = new MockArchivalUnit();
  }

  public void testBasic() throws Exception {
    Set<String>expected = SetUtil.set(
        "http://www.jstor.org/action/downloadSingleCitationSec?format=refman&doi=10.2307/41495848");
    testExpectedAgainstParsedUrls(expected,htmltest,JSTOR_TOC_URL);
  }
  
  //Don't use citation form extractor except on TOC pages
  public void testNotTOC() throws Exception {
    Set<String> expected = SetUtil.set();
    testExpectedAgainstParsedUrls(expected,htmltest,JSTOR_ARTICLE_ABSTRACT_URL);
  }

  public void testFullLink() throws Exception {
    Set<String> expected = SetUtil.set(
        "http://www.jstor.org/stable/41495848",
        "http://www.jstor.org/stable/info/41495848",
        "http://www.jstor.org/stable/view/41495848",
        "http://www.jstor.org/action/downloadSingleCitationSec?format=refman&doi=10.2307/41495848",
        "http://www.jstor.org/stable/pdfplus/41495848.pdf");
    testExpectedAgainstParsedUrls(expected,fullLinkHtml, JSTOR_TOC_URL);
  }

  public void tesMultiLinks() throws Exception {
    Set<String>expected = SetUtil.set(
        "http://www.jstor.org/action/downloadSingleCitationSec?format=refman&doi=10.2307/41495848",
        "http://www.jstor.org/action/downloadSingleCitationSec?format=refman&doi=10.2307/746318",
        "http://www.jstor.org/action/downloadSingleCitationSec?format=refman&doi=10.1525/ncm.2013.36.3.toc",
        "http://www.jstor.org/action/downloadSingleCitationSec?format=refman&doi=10.3764/aja.117.3.0429");
      testExpectedAgainstParsedUrls(expected, multiLinksHtml, JSTOR_TOC_URL);
  }

  
  
  
  private void testExpectedAgainstParsedUrls(Set<String> expectedUrls, 
      String source, String srcUrl) throws Exception {

    Set<String> result_strings = parseSingleSource(source, srcUrl);
    assertEquals(expectedUrls.size(), result_strings.size());
    for (String url : result_strings) {
      log.debug3("URL: " + url);
      assertTrue(expectedUrls.contains(url));
    }
  }

  private Set<String> parseSingleSource(String source, String srcUrl)
      throws Exception {

    m_callback.reset();
    m_extractor.extractUrls(m_mau,
        new org.lockss.test.StringInputStream(source), ENC,
        srcUrl, m_callback);
    return m_callback.getFoundUrls();
  }

  private static class MyLinkExtractorCallback implements
  LinkExtractor.Callback {

    Set<String> foundUrls = new java.util.HashSet<String>();

    public void foundLink(String url) {
      foundUrls.add(url);
    }

    public Set<String> getFoundUrls() {
      return foundUrls;
    }

    public void reset() {
      foundUrls = new java.util.HashSet<String>();
    }
  }

}
