/*
 * $Id$
 */

/*

Copyright (c) 2000-2016 Board of Trustees of Leland Stanford Jr. University,
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

package org.lockss.plugin.springer.link;

import java.io.*;
import java.util.*;

import org.lockss.daemon.*;
import org.lockss.daemon.Crawler.CrawlerFacade;
import org.lockss.plugin.ArchivalUnit.ConfigurationException;
import org.lockss.util.*;

/**
 * <p>
 * A crawl seed that queries Springer's Meta API to enumerate article metadata
 * and synthesize start URLs for crawls.
 * </p>
 * <p>
 * Note that this is the newer Meta API, not the older Metadata API.
 * </p>
 * 
 * @since 1.67.5
 * @see https://dev.springer.com/
 */
public class SpringerLinkJournalsCrawlSeed extends BaseSpringerLinkCrawlSeed {

  /**
   * <p>
   * A logger for this class.
   * </p>
   * 
   * @since 1.67.5
   */
  private static final Logger log = Logger.getLogger(SpringerLinkJournalsCrawlSeed.class);
  
  /**
   * <p>
   * The journal ISSN (<code>journal_issn</code>) of this crawl seed's AU.
   * </p>
   * 
   * @since 1.67.5
   */
  protected String eissn;
  
  public static final String JOURNAL_EISSN_KEY = "journal_eissn";
  /**
   * <p>
   * The volume name (<code>volume_name</code>) of this crawl seed's AU.
   * </p>
   * 
   * @since 1.67.5
   */
  protected String volume;

  /**
   * <p>
   * Builds a new crawl seed with the given crawler façade.
   * </p>
   * 
   * @param facade
   *          A crawler façade for this crawl seed.
   * @since 1.67.5
   */
  public SpringerLinkJournalsCrawlSeed(CrawlerFacade facade) {
    super(facade);
  }

  @Override
  protected void initialize() 
      throws ConfigurationException ,PluginException ,IOException {
    super.initialize();
    this.eissn = au.getConfiguration().get(JOURNAL_EISSN_KEY);
    this.volume = au.getConfiguration().get(ConfigParamDescr.VOLUME_NAME.getKey());
    this.urlList = null;
  }

  /**
   * <p>
   * Assembles the query URL for a given starting index.
   * </p>
   * 
   * @param startingIndex
   *          A starting index (starts at 1).
   * @return The query URL for the given starting index.
   * @since 1.67.5
   */
  protected String makeApiUrl(int startingIndex) {
    String url = String.format("%smeta/v1/pam?q=issn:%s%%20volume:%s&api_key=%s&p=%d&s=%d",
                               API_URL,
                               eissn,
                               volume,
                               API_KEY,
                               EXPECTED_RECORDS_PER_RESPONSE,
                               startingIndex);
    return url;
  }
/*
 * (non-Javadoc)
 * encode the doi so that ":" and similar in the doi portion of the URL are
 * consitent with the href links within the article pages
 */
  @Override
  protected List<String> convertDoisToUrls(Collection<String> dois) {
    List<String> urls = new ArrayList<String>();
    for(String doi:dois) {
      // Encode the doi, then revert the FIRST %2F (slash) back to a "/":
      // 10.1023/A%3A1026541510549, not
      // 10.1023%2FA%3A1026541510549
      String url = String.format("%sarticle/%s", baseUrl, encodeDoi(doi).replaceFirst("%2F","/"));
      urls.add(url);
    }
    return urls;
  }
}
