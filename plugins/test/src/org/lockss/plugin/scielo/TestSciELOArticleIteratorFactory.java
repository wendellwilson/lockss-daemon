/*
 * $Id: TestAPSDrupalArticleIteratorFactory.java 40932 2015-03-24 23:38:30Z etenbrink $
 */

/*

Copyright (c) 2000-2015 Board of Trustees of Leland Stanford Jr. University,
all rights reserved.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
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

package org.lockss.plugin.scielo;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Stack;
import java.util.regex.Pattern;

import org.lockss.config.*;
import org.lockss.daemon.*;
import org.lockss.extractor.*;
import org.lockss.plugin.*;
import org.lockss.plugin.simulated.*;
import org.lockss.test.*;
import org.lockss.util.CIProperties;
import org.lockss.util.Constants;
import org.lockss.util.ListUtil;

public class TestSciELOArticleIteratorFactory extends ArticleIteratorTestCase {
  
  private SimulatedArchivalUnit sau;	// Simulated AU to generate content
  
  private final String PLUGIN_NAME = "org.lockss.plugin.scielo.SciELOPlugin";
  static final String BASE_URL_KEY = ConfigParamDescr.BASE_URL.getKey();
  static final String JOURNAL_ISSN_KEY = ConfigParamDescr.JOURNAL_ISSN.getKey();
  static final String YEAR_KEY = ConfigParamDescr.YEAR.getKey();
  private final String BASE_URL = "http://www.scielo.br/";
  private final Configuration AU_CONFIG = ConfigurationUtil.fromArgs(
      BASE_URL_KEY, BASE_URL,
      JOURNAL_ISSN_KEY, "X090-999X",
      YEAR_KEY, "2013");
  private String BASE_AU_URL = BASE_URL;
  private static final int DEFAULT_FILESIZE = 3000;
  private final String ARTICLE_FAIL_MSG = "Article files not created properly";
  
  protected String cuRole = null;
  ArticleMetadataExtractor.Emitter emitter;
  protected boolean emitDefaultIfNone = false;
  FileMetadataExtractor me = null;
  MetadataTarget target;
  
  @Override
  public void setUp() throws Exception {
    super.setUp();
    String tempDirPath = setUpDiskSpace();
    
    au = createAu();
    sau = PluginTestUtil.createAndStartSimAu(simAuConfig(tempDirPath));
  }
  
  @Override
  public void tearDown() throws Exception {
    sau.deleteContentTree();
    super.tearDown();
  }
  
  protected ArchivalUnit createAu() throws ArchivalUnit.ConfigurationException {
    return
        PluginTestUtil.createAndStartAu(PLUGIN_NAME, AU_CONFIG);
  }
  
  Configuration simAuConfig(String rootPath) {
    Configuration conf = ConfigManager.newConfiguration();
    conf.put("root", rootPath);
    conf.put(BASE_URL_KEY, BASE_URL);
    conf.put(JOURNAL_ISSN_KEY, "X090-999X");
    conf.put(YEAR_KEY, "2013");
    conf.put("depth", "1");
    conf.put("branch", "1");
    conf.put("numFiles", "7");
    conf.put("fileTypes", "" +
        (SimulatedContentGenerator.FILE_TYPE_PDF |
            SimulatedContentGenerator.FILE_TYPE_HTML));
    conf.put("binFileSize", "" + DEFAULT_FILESIZE);
    return conf;
  }
  
  
  public void testRoots() throws Exception {
    SubTreeArticleIterator artIter = createSubTreeIter();
    assertEquals(ListUtil.list(BASE_AU_URL), getRootUrls(artIter));
  }
  
  
  //
  // We are set up to match any of "<base_url>scielo.php[?]script=sci_arttext&pid=[^&]{0,5}<issn><year>[0-9]{4,16}&lng=en"
  //
  
  public void testUrls() throws Exception {
    SubTreeArticleIterator artIter = createSubTreeIter();
    Pattern pat = getPattern(artIter);
    
    assertMatchesRE(pat, "http://www.scielo.br/scielo.php?script=sci_arttext&pid=sX090-999X2013000020022&lng=en");
    // but not to ...
    assertNotMatchesRE(pat, "http://www.scielo.br/scielo.php?script=sci_arttext&pid=sX090-999X2013000020022&lng=es");
    assertNotMatchesRE(pat, "http://www.scielo.br/scielo.php?script=sci_arttext&pid=sX090-999X2013001&lng=en");
    assertNotMatchesRE(pat, "http://www.scielo.br/scielo.php?script=sci_arttext&pid=X090-999X2013000020022");
    
    // wrong base url
    assertNotMatchesRE(pat, "http://ametsoc.org/bitstream/handle/foobar");
  }
  
  //
  // simAU was created with only one depth
  // 1 filetype (html) and 2 files of each type
  // So the total number of files of all types is 2
  // simAU file structures looks like this branch01/01file.html
  //
  private void estCreateArticleFiles() throws Exception {
    PluginTestUtil.crawlSimAu(sau);
    
    /*
     *  Go through the simulated content you just crawled and modify the results to emulate
     *  what you would find in a "real" crawl with APSDrupal:
     *  <base_url>content/<vol>/<iss>/pg.full
     */
    
    String pat0 = "(?!branch)00([2-6])file[.]html";
    // turn xxfile.html into body
    String rep0 = "/content/1/1/C$1";
    String rep0e = "/content/ajpendo/1/1/C$1";
    PluginTestUtil.copyAu(sau, au, ".*[.]html$", pat0, rep0);
    PluginTestUtil.copyAu(sau, au, ".*[.]html$", pat0, rep0e);
    
    String pat1 = "branch(\\d+)/\\d+([1356])file[.]html";
    String rep1 = "/content/1/$1/C$2.full.pdf+html";
    PluginTestUtil.copyAu(sau, au, ".*[.]html$", pat1, rep1);
    
    String pat2 = "branch(\\d+)/\\d+([237])file[.]pdf";
    String rep2 = "/content/1/$1/C$2.full.pdf";
    String rep2e = "/content/ajpendo/1/$1/C$2.full.pdf";
    PluginTestUtil.copyAu(sau, au, ".*[.]pdf$", pat2, rep2);
    PluginTestUtil.copyAu(sau, au, ".*[.]pdf$", pat2, rep2e);
    
    Iterator<ArticleFiles> it = au.getArticleIterator(MetadataTarget.Any());
    int count = 0;
    int countFullText= 0;
    int countMetadata = 0;
    int countPdf = 0;
    int countPdfLanding = 0;
    while (it.hasNext()) {
      ArticleFiles af = it.next();
      count ++;
      //log.info(af.toString());
      CachedUrl cu = af.getFullTextCu();
      if ( cu != null) {
        ++countFullText;
      }
      cu = af.getRoleCu(ArticleFiles.ROLE_ARTICLE_METADATA);
      if (cu != null) {
        ++countMetadata;
      }
      cu = af.getRoleCu(ArticleFiles.ROLE_FULL_TEXT_PDF);
      if (cu != null) {
        ++countPdf;
      }
      cu = af.getRoleCu(ArticleFiles.ROLE_FULL_TEXT_PDF_LANDING_PAGE);
      if (cu != null) {
        ++countPdfLanding;
      }
    }
    // potential article count is 7 (1 branch * 7 files each branch)
    // less the pdf only and pdf landing only
    int expCount = 5;
    
    log.debug3("Article count is " + count);
    assertEquals(expCount, count);
    
    // you will get full text for ALL articles
    assertEquals(expCount, countFullText);
    
    // you will get metadata for all but 2
    assertEquals(expCount, countMetadata); // no metadata for pdf only
    
    // you will get pdf for 2 (3 - 1 non-base article)
    assertEquals(2, countPdf);
    
    // you will get landing for 3 (4 - 1 non-base article)
    assertEquals(3, countPdfLanding);
  }
  
  /*
   * PDF Full Text: http://ajpcell.physiology.org/content/1/1/C1.full.pdf
   * PDF Landing :  http://ajpcell.physiology.org/content/1/1/C1.full.pdf+html
   * HTML/Abstract: http://ajpcell.physiology.org/content/1/1/C1
   * 
   * http://ajpcell.physiology.org/content/1/1/C1.article-info
   * http://ajpcell.physiology.org/content/1/1/C1.figures-only
   */
  private void estCreateArticleFiles2() throws Exception {
    PluginTestUtil.crawlSimAu(sau);
    String[] urls = {
        BASE_URL + "content/1/1/C1.full",
        BASE_URL + "content/1/1/C1.full.pdf",
        BASE_URL + "content/1/1/C1.full.pdf+html",
        BASE_URL + "content/1/1/C1",
        BASE_URL + "content/1/1/C1.article-info",
        BASE_URL + "content/1/1/C1.figures-only",
        
        BASE_URL + "content/1/1/C10.full.pdf",
        BASE_URL + "content/1/1/C10.full.pdf+html",
        BASE_URL + "content/1/1/C10",
        BASE_URL + "content/1/1/C10.article-info",
        
        BASE_URL + "content/1/1/C100.full.pdf+html",
        BASE_URL + "content/1/1/C100",
        
        BASE_URL + "content/1/1/C3",
        BASE_URL + "content/1/1/C3.article-info",
        
        BASE_URL + "content/1/1/C4.full.pdf",
        BASE_URL + "content/1/1/C4",
        
        
        BASE_URL + "content/1/1/C18.article-info",
        BASE_URL + "content/1/1/C19.figures-only",
        BASE_URL,
        BASE_URL + "content"
    };
    CachedUrl cuPdf = null;
    CachedUrl cuHtml = null;
    for (CachedUrl cu : AuUtil.getCuIterable(sau)) {
      if (cuPdf == null && 
	  cu.getContentType().toLowerCase().startsWith(Constants.MIME_TYPE_PDF))
	{
            cuPdf = cu;
	}
      else if (cuHtml == null && 
	       cu.getContentType().toLowerCase().startsWith(Constants.MIME_TYPE_HTML))
	{
	  cuHtml = cu;
	}
      if (cuPdf != null && cuHtml != null) {
	break;
      }
    }
      
      for (String url : urls) {
        InputStream input = null;
        CIProperties props = null;
        if (url.contains("pdf+html")) {
          input = cuHtml.getUnfilteredInputStream();
          props = cuHtml.getProperties();
        } else if (url.contains("pdf")) {
          input = cuPdf.getUnfilteredInputStream();
          props = cuPdf.getProperties();
        } else {
          input = cuHtml.getUnfilteredInputStream();
          props = cuHtml.getProperties();
        }
        UrlData ud = new UrlData(input, props, url);
        UrlCacher uc = au.makeUrlCacher(ud);
        uc.storeContent();
      }
    
    Stack<String[]> expStack = new Stack<String[]>();
    String [] af1 = {
        BASE_URL + "content/1/1/C1.full",
        BASE_URL + "content/1/1/C1.full.pdf+html",
        BASE_URL + "content/1/1/C1.full.pdf",
        BASE_URL + "content/1/1/C1.full.pdf+html"};
    
    String [] af2 = {
        BASE_URL + "content/1/1/C10",
        BASE_URL + "content/1/1/C10.full.pdf+html",
        BASE_URL + "content/1/1/C10.full.pdf",
        BASE_URL + "content/1/1/C10.full.pdf+html"};
    
    String [] af3 = {
        BASE_URL + "content/1/1/C100",
        BASE_URL + "content/1/1/C100.full.pdf+html",
        null,
        BASE_URL + "content/1/1/C100.full.pdf+html"};
    
    String [] af4 = {
        BASE_URL + "content/1/1/C3",
        null,
        null,
        BASE_URL + "content/1/1/C3"};
    
    String [] af5 = {
        BASE_URL + "content/1/1/C4",
        null,
        BASE_URL + "content/1/1/C4.full.pdf",
        BASE_URL + "content/1/1/C4"};
    
    String [] af6 = {
        null,
        null,
        null,
        null};
    
    expStack.push(af6);
    expStack.push(af5);
    expStack.push(af4);
    expStack.push(af3);
    expStack.push(af2);
    expStack.push(af1);
    String[] exp;
    
    for ( SubTreeArticleIterator artIter = createSubTreeIter(); artIter.hasNext(); ) 
    {
      // the article iterator return aspects with html first, then pdf, then nothing 
      ArticleFiles af = artIter.next();
      String[] act = {
          af.getFullTextUrl(),
          af.getRoleUrl(ArticleFiles.ROLE_FULL_TEXT_PDF_LANDING_PAGE),
          af.getRoleUrl(ArticleFiles.ROLE_FULL_TEXT_PDF),
          af.getRoleUrl(ArticleFiles.ROLE_ARTICLE_METADATA)
      };
      System.err.println(" ");
      exp = expStack.pop();
      if(act.length == exp.length){
        for(int i = 0;i< act.length; i++){
          System.err.println(" Expected: " + exp[i] + "\n   Actual: " + act[i]);
          assertEquals(ARTICLE_FAIL_MSG + " Expected: " + exp[i] + "\n   Actual: " + act[i], exp[i],act[i]);
        }
      }
      else fail(ARTICLE_FAIL_MSG + " length of expected and actual ArticleFiles content not the same:" + exp.length + "!=" + act.length);
    }
    exp = expStack.pop();
    assertEquals("Did not find null end marker", exp, af6);
  }
}
