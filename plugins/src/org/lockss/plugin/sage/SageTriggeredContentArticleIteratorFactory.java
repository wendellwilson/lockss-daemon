/*

Copyright (c) 2000-2010 Board of Trustees of Leland Stanford Jr. University,
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

package org.lockss.plugin.sage;

import java.util.Iterator;
import java.util.regex.*;

import org.lockss.daemon.PluginException;
import org.lockss.extractor.ArticleMetadataExtractor;
import org.lockss.extractor.ArticleMetadataExtractorFactory;
import org.lockss.extractor.BaseArticleMetadataExtractor;
import org.lockss.extractor.MetadataTarget;
import org.lockss.plugin.*;
import org.lockss.util.Logger;

public class SageTriggeredContentArticleIteratorFactory implements ArticleIteratorFactory, ArticleMetadataExtractorFactory {

  protected static Logger log = Logger.getLogger("SageTriggeredContentArticleIteratorFactory");
  
  protected static final String ROOT_TEMPLATE = "\"%s\",base_url";
  protected static final String PATTERN_TEMPLATE = "\"%s%s_[\\d]+_%s_[\\d]+/[\\d]+\\.[\\d]+_[^/]+\\.pdf$\",base_url,journal_dir,volume_name";
  
  @Override
  public Iterator<ArticleFiles> createArticleIterator(ArchivalUnit au,
                                                      MetadataTarget target)
      throws PluginException {
    return new SpringerArticleIterator(au, new SubTreeArticleIterator.Spec()
                                       .setTarget(target)
                                       .setRootTemplate(ROOT_TEMPLATE)
                                       .setPatternTemplate(PATTERN_TEMPLATE, Pattern.CASE_INSENSITIVE));
  }
  
  protected static class SpringerArticleIterator extends SubTreeArticleIterator {
	 
    protected static Pattern PATTERN = Pattern.compile("([^/]+_[\\d]+_[\\d]+_[\\d]+/[\\d]+\\.[\\d]+_[^/]+)(\\.pdf)$", Pattern.CASE_INSENSITIVE);
    
    protected SpringerArticleIterator(ArchivalUnit au,
                                  SubTreeArticleIterator.Spec spec) {
      super(au, spec);
      spec.setVisitArchiveMembers(true);
    }
    
    @Override
    protected ArticleFiles createArticleFiles(CachedUrl cu) {
      String url = cu.getUrl();
      
      Matcher mat = PATTERN.matcher(url);
      if (mat.find()) {
        return processFullText(cu, mat);
      }
      log.warning("Mismatch between article iterator factory and article iterator: " + url);
      return null;
    }

    protected ArticleFiles processFullText(CachedUrl cu, Matcher mat) {
      ArticleFiles af = new ArticleFiles();
      af.setFullTextCu(cu);
      af.setRoleCu(ArticleFiles.ROLE_FULL_TEXT_PDF, cu);
            
      //if(spec.getTarget() != MetadataTarget.Article)
		guessAdditionalFiles(af, mat);
      
      return af;
    }
    
    protected void guessAdditionalFiles(ArticleFiles af, Matcher mat) {    	
      CachedUrl htmCu = au.makeCachedUrl(mat.replaceFirst("$1.htm"));
      CachedUrl xmlCu = au.makeCachedUrl(mat.replaceFirst("$1.xml"));
	  CachedUrl txtCu = au.makeCachedUrl(mat.replaceFirst("$1.txt"));
	  
      if (htmCu != null && htmCu.hasContent()) {
    	  af.setRoleCu(ArticleFiles.ROLE_ABSTRACT, htmCu);
      }
	  
      if (xmlCu != null && xmlCu.hasContent()) {
    	  af.setRoleCu(ArticleFiles.ROLE_ARTICLE_METADATA, xmlCu);
      }
      
      if (txtCu != null && txtCu.hasContent()) {
    	  af.setRoleCu(ArticleFiles.ROLE_SUPPLEMENTARY_MATERIALS, txtCu);
      }
    }
  }
  
  @Override
  public ArticleMetadataExtractor createArticleMetadataExtractor(MetadataTarget target)
      throws PluginException {
    return new BaseArticleMetadataExtractor(ArticleFiles.ROLE_ARTICLE_METADATA);
  }
}
