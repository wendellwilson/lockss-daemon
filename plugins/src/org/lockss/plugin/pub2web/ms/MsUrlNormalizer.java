/*
 * $Id:$
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

package org.lockss.plugin.pub2web.ms;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.lockss.daemon.*;
import org.lockss.plugin.*;
import org.lockss.util.Logger;


public class MsUrlNormalizer implements UrlNormalizer {
  protected static Logger log = 
      Logger.getLogger(MsUrlNormalizer.class);
  protected static final String SUFFIX = "&isFastTrackArticle=";
  protected static final String EXPIRINGSUFFIX = "?expires=";
  protected static final String CONTENT_URL = "/deliver/fulltext/"; // how we identify these URLs 
  protected static final String FULLHTML_URL = "/docserver/ahah/fulltext";
  
  protected static final Pattern FULLTEXT_URL_PATTERN =
      //<base>/deliver/fulltext/<jid>/<vol>/<issue>/<stuff>.(pdf|html)?itemId="/content/journal/<jid>/...fooo...&mimeType=pdf&isFastTrackArticle=
      Pattern.compile("^(https?://[^/]+)/deliver/fulltext/[^/]+/[^?]+\\.(pdf|html)\\?itemId=(/content/journal/[^&]+)&mimeType=([^&]+)(&fmt=ahah)?(&isFastTrackArticle=)?$", Pattern.CASE_INSENSITIVE); 

   
/*
 * NEW PLAN:
 * take a PDF or HTML full text url which go through expiring 
 * URLS to get to the content
 * and normalize them to the stable crawler version
 * 
 * pdf link: 
 * http://jgv.microbiologyresearch.org/deliver/fulltext/jgv/96/2/390_vir070219.pdf?itemId=/content/journal/jgv/10.1099/vir.0.070219-0&mimeType=pdf&isFastTrackArticle=
 *           (GROUP1)/deliver/fulltext/jgv/96/10/3131_jgv000245.(GROUP2)?itemId=(GROUP3)&mimeType=(GROUP4)&isFastTrackArticle=
 *   becomes:
 *       http://jgv.microbiologyresearch.org/content/journal/jgv/10.1099/vir.0.070219-0?crawwler=true&mimetype=application/pdf
 * 
 * html link: 
 * http://jgv.microbiologyresearch.org/deliver/fulltext/jgv/96/1/183.html?itemId=/content/journal/jgv/10.1099/vir.0.064816-0&mimeType=html&fmt=ahah
 *       (GROUP1)/deliver/fulltext/jgv/96/1/183.(GROUP2)?itemId=(GROUP3)&mimeType=(GROUP4)&fmt=ahah
 *   becomes: 
 *     http://jgv.microbiologyresearch.org/content/journal/jgv/10.1099/vir.0.064816-0?crawler=true&mimetype=html
 *     
 * TOC pdf - special case this out as it doesn't have crawler version...just swallow the redirect with a consumer
 * Supplementary Data - won't get picked up by this because it is under "/content/suppdata - let it get consumed
 *    links on a page could look like this:
 *    /deliver/fulltext/jgv/96/1/67363.pdf?itemId=/content/suppdata/jgv/10.1099/vir.0.067363-0-1&amp;mimeType=pdf&amp;isFastTrackArticle=
 * Figures - won't get picked up by this because it doesn't have mimetype...it's a direct link   
 *     content/journal/jgv/10.1099/vir.0.067363-0/figures?fmt=ahah 
 *     
 */
  @Override
  public String normalizeUrl(String url, ArchivalUnit au) throws PluginException {

    Matcher ftMat = FULLTEXT_URL_PATTERN.matcher(url);
    log.debug3("about to norm: " + url);
    // the tocpdf doesn't have a crawler equivalent.  Pass it through for url consumption
    if (ftMat.matches() && (!(url.contains("/toc.pdf")))) {
      log.debug3("full text url: " + url);
      // create the crawler equivalent
      //group1 is the base_url
      // group2 is the extension...html or pdf - use to determing mimetype
      // group3 is the article identifier
      // note that group4 isn't correct mimetype. pdf must be application/pdf for fulltext
      String mt;
      if ("pdf".equals(ftMat.group(2))) {
        mt = "application/pdf";
      } else {
        mt = "html";
      }
      url = ftMat.group(1) + ftMat.group(3) + "?crawler=true&mimetype=" + mt;
      log.debug3("normalized to: " + url);
    } 
    // just remove any undesired suffixes
    // if the suffix doesn't exist the url doesn't change, no sense wasting
    // cycles checking if it exists before trying to remove
    // for the URLs that flow through - this may still be an unnecessary suffix
     url = StringUtils.substringBeforeLast(url, "&isFastTrackArticle=");
    return url;
  }

}