/*
 * $Id$
 */

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

package org.lockss.plugin.springer;

import java.io.*;

import org.lockss.util.*;
import org.lockss.daemon.*;
import org.lockss.extractor.*;
import org.lockss.plugin.*;
import org.lockss.plugin.associationforcomputingmachinery.ACMBooksXmlSchemaHelper;
import org.lockss.plugin.associationforcomputingmachinery.ACMXmlSchemaHelper;
import org.lockss.plugin.clockss.SourceXmlMetadataExtractorFactory;
import org.lockss.plugin.clockss.SourceXmlSchemaHelper;
import org.lockss.plugin.clockss.SourceXmlMetadataExtractorFactory.SourceXmlMetadataExtractor;
import org.lockss.plugin.clockss.markallen.MarkAllenWorksheetXmlSchemaHelper;
import org.lockss.extractor.XmlDomMetadataExtractor.NodeValue;
import org.lockss.extractor.XmlDomMetadataExtractor.XPathValue;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.map.*;
import org.apache.commons.io.FilenameUtils;

import javax.xml.xpath.XPathExpressionException;

/**
 * Implements a FileMetadataExtractor for Springer Source Content
 * 
 * Files used to write this class include:
 * ~/2010/ftp_PUB_10-05-17_06-11-02.zip/JOU=11864/VOL=2008.9/ISU=2-3/ART=2008_64/11864_2008_Article.xml.Meta
 */

public class SpringerSourceMetadataExtractorFactory
                   extends SourceXmlMetadataExtractorFactory  {
  static Logger log = Logger.getLogger(SpringerSourceMetadataExtractorFactory.class);
  
  private static SourceXmlSchemaHelper journalHelper = null;
  private static SourceXmlSchemaHelper booksHelper = null;

  public FileMetadataExtractor createFileMetadataExtractor(MetadataTarget target,
							   String contentType)
      throws PluginException {
    return new SpringerSourceMetadataExtractor();
  }
  
  public static class SpringerSourceMetadataExtractor
         extends SourceXmlMetadataExtractor {
 
    /*
     * The top node for all schema will be <Publisher>
     * It's the second node that will determine the type - either
     * /Publisher/Series or /Publisher/Book for a book chapter
     *and
     * /Publisher/Journal for a journal article
     */
    @Override
    protected SourceXmlSchemaHelper setUpSchema(CachedUrl cu, Document xmlDoc) {
      //look at the top node of the Document to identify the schema
      Element top_element = xmlDoc.getDocumentElement();
      String element_name = top_element.getNodeName();
      NodeList elementChildren = top_element.getChildNodes();
      
      log.info("I'm here");

      if ("Publisher".equals(element_name)
          && elementChildren != null) { 
        // look at each child until we either run out of children or
        // find one of "Series", "Book" or "Journal"
        for (int j = 0; j < elementChildren.getLength(); j++) {
          Node cnode = elementChildren.item(j);
          String nodeName = cnode.getNodeName();
          if ("Book".equals(nodeName) || "Series".equals(nodeName)) {
            if (booksHelper == null) {
              booksHelper = new SpringerBookSourceSchemaHelper();
            }
            return booksHelper;
          } else if ("Journal".equals(nodeName)) {
            if(journalHelper == null) {
              journalHelper = new SpringerJournalSourceSchemaHelper();
            }
            return journalHelper;
          }
        }
      }
      // the only way you get here is if you could not figure out which
      // schema to use
      throw new ShouldNotHappenException("This does not match expected schema");
    }

    
    @Override
    protected SourceXmlSchemaHelper setUpSchema(CachedUrl cu) {
      throw new ShouldNotHappenException("This version of the schema setup cannot be used for this plugin");
    }
        
    
    
    private Map<String, String> journalTitleMap;
    
    // http://clockss-ingest.lockss.org/sourcefiles/springer-released/2012/ftp_PUB_11-11-17_06-38-38.zip!/JOU=00238/VOL=2011.34/ISU=6/ART=476/BodyRef/PDF/238_2010_Article_476.pdf
    private Pattern JOURNAL_ID_PATTERN = Pattern.compile("/springer-[^/]+/[0-9]{4}/[^/]+/JOU=([0-9]+)/.+");

    public SpringerSourceMetadataExtractor() {
      journalTitleMap = new HashMap<String, String>();
    }
	   
    /**
     * 
     * TODO: This is legacy and I don't think it sticks around between 
     * XML files so there wouldnt' be any way to keep track of jid-issn-title
     * information from extraction to extraction 
     * without creating an ArticleMetadataExtractor and storing it between
     * emits...
     * This may have worked back before this was partially integrated with 
     * the framework - 
     * 
     * Get the journal ID from from the article metadata. If not set, gets
     * journal id from the URL and adds it to the article metadata.
     *  
     * @param url the URL of the article
     * @param am the article metadata of the article
     * @return the journalID or null if not available
     */
    // extract journal id from cached url.
    // if not found (url is opaque), then assign a default value.
    private String getJournalId(String url, ArticleMetadata am) {
      String journalId = am.get(MetadataField.FIELD_PROPRIETARY_IDENTIFIER);
      log.debug3("getJournalId() propid journalId: " + journalId);
      if (StringUtil.isNullString(journalId)) {
        // http://clockss-ingest.lockss.org/sourcefiles/springer-released/2012/ftp_PUB_11-11-17_06-38-38.zip!/JOU=00238/VOL=2011.34/ISU=6/ART=476/BodyRef/PDF/238_2010_Article_476.pdf
        // Pattern.compile("/springer-[^/]+/[0-9]{4}/[^/]+/JOU=([0-9]+)/.+");
        Matcher mat = JOURNAL_ID_PATTERN.matcher(url);
        if (mat.find()) {
          journalId = mat.group(1);
          log.debug3("journalId: " + journalId);
          am.put(MetadataField.FIELD_PROPRIETARY_IDENTIFIER, journalId);
        }
      }
      log.debug3("getJournalIdl() journalId: " + journalId);
      return (journalId);
    }
    
    /**
     * Get the journal title for the specified ArticleMetadata. If not set,
     * looks up cached value using issn, eissn, or journalID. If not cached,
     * creates one from the issn, eissn or journalID.  Adds journalID to
     * the article metadata if not present. 
     * 
     * @param url the URL of the article
     * @param am the article metadata of the article
     * @return the journal title or null if not available
     */
    private String getJournalTitle(String url, ArticleMetadata am) {
      String journalTitle = am.get(MetadataField.FIELD_PUBLICATION_TITLE);
      String journalId = getJournalId(url, am);
      String issn = am.get(MetadataField.FIELD_ISSN);
      String eissn = am.get(MetadataField.FIELD_EISSN);

      // journal has title -- cache using issn, eissn, and journalID
      // in case journal title is missing from later records
      if (!StringUtil.isNullString(journalTitle)) {
        if (!StringUtil.isNullString(issn)) {
          journalTitleMap.put(issn, journalTitle);
        }
        if (!StringUtil.isNullString(eissn)) {
          journalTitleMap.put(eissn, journalTitle);
        }
        if (!StringUtil.isNullString(journalId)) {
          journalTitleMap.put(journalId, journalTitle);
        }
        return journalTitle;
      }
      // journal has no title -- find it using issn, eissn, and jouranalID,
      // or generate a title using one of these properties otherwise
      String genTitle = null;  // generated title fron issn, eissn or journalID
      try {
        // try ISSN as key
        if (!StringUtil.isNullString(issn)) {
          // use cached journal title for journalId
          journalTitle = journalTitleMap.get(issn);
          if (!StringUtil.isNullString(journalTitle)) {
            return journalTitle;
          }
          if (genTitle == null) {
            // generate title with issn for preference
            genTitle = "UNKNOWN_TITLE/issn=" + issn;
          }
        }
        
        // try eissn as key
        if (!StringUtil.isNullString(eissn)) {
          // use cached journal title for journalId
          journalTitle = journalTitleMap.get(eissn);
          if (!StringUtil.isNullString(journalTitle)) {
            return journalTitle;
          }
          if (genTitle == null) {
            // generate title with eissn if issn not available
          genTitle = "UNKNOWN_TITLE/eissn=" + eissn;
          }
        }
        
        // try journalId as key
        if (!StringUtil.isNullString(journalId)) {
          // use cached journal title for journalId
          journalTitle = journalTitleMap.get(journalId);
          if (!StringUtil.isNullString(journalTitle)) {
            return journalTitle;
          }
          if (genTitle == null) {
            // generate title with journalID if issn and eissn not available
            genTitle = "UNKNOWN_TITLE/journalId=" + journalId;
          }
        }
        
      } finally {
        if (StringUtil.isNullString(journalTitle)) {
          journalTitle = genTitle;
        }
        if (!StringUtil.isNullString(journalTitle)) {
          am.put(MetadataField.FIELD_PUBLICATION_TITLE, journalTitle);
        }
        log.debug3("getJournalTitle() journalTitle: " + journalTitle);
      }
      return journalTitle;
    }

    /**
     * Post-cook process after extraction and cooking....
     */
    
    @Override
    protected void postCookProcess(SourceXmlSchemaHelper schemaHelper, 
        CachedUrl cu, ArticleMetadata thisAM) {

      log.debug3("postEmitProcess for cu: " + cu);
      // hardwire publisher for board report (look at imprints later)
      thisAM.put(MetadataField.FIELD_PUBLISHER, "Springer");
      
      if (schemaHelper == journalHelper ) {
        // emit only if journal title exists, otherwise report site error
        // TODO: legacy - I'm pretty sure this is useless, but to minimize
        // instability as I add in books, I will leave it in for journals.
        String journalTitle = getJournalTitle(cu.getUrl(), thisAM);
        if (!StringUtil.isNullString(journalTitle)) {
          log.debug3("found or created a journal title");
        } else {
          log.siteError("Missing journal title: " + cu.getUrl());
        }
      } else {
        // we are a book
        String subT = thisAM.getRaw(SpringerBookSourceSchemaHelper.bookSubTitle);  
        if (subT != null) {
          StringBuilder title_br = new StringBuilder(thisAM.get(MetadataField.FIELD_PUBLICATION_TITLE));
          title_br.append(": ");
          title_br.append(subT);
          thisAM.replace(MetadataField.FIELD_PUBLICATION_TITLE,  title_br.toString());        
        }
        subT = thisAM.getRaw(SpringerBookSourceSchemaHelper.chapterSubTitle);  
        if (subT != null) {
          StringBuilder title_br = new StringBuilder(thisAM.get(MetadataField.FIELD_ARTICLE_TITLE));
          title_br.append(": ");
          title_br.append(subT);
          thisAM.replace(MetadataField.FIELD_ARTICLE_TITLE,  title_br.toString());        
        }
        
        if (thisAM.get(MetadataField.FIELD_AUTHOR) == null) {
          // too many option... try a book level info... and if not, then just move on
          String othergroup = thisAM.getRaw(SpringerBookSourceSchemaHelper.bookAuthorAu);
          if (othergroup == null) { othergroup = thisAM.getRaw(SpringerBookSourceSchemaHelper.bookAuthorEd);}
          if (othergroup == null) { othergroup = thisAM.getRaw(SpringerBookSourceSchemaHelper.bookAuthorCo);}
          if (othergroup != null) {
            thisAM.put(MetadataField.FIELD_AUTHOR, othergroup);
          }
        }
      }

    }  
    
    /* In this case, the filename is the same as the xml filename but
     * in the BodyRef/PDF/ subdirectory
     */
    @Override
    protected List<String> getFilenamesAssociatedWithRecord(SourceXmlSchemaHelper helper, CachedUrl cu,
        ArticleMetadata oneAM) {
      /*
       * Returning null indicates that we do not need to check for existence
       * of the pdf that maps to this metadata. 
       * This is because our article iterator actually started by finding the PDF
       * and from that calculated the XML. So we already know the PDF exists
       * and will get set to the ACCESS_URL and FULL_TEXT_PDF
       */
      return null;
/*
      String xml_url = cu.getUrl();
      String cuBase = FilenameUtils.getFullPath(xml_url);
      String filenameValue = FilenameUtils.getBaseName(xml_url);
      ArrayList<String> returnList = new ArrayList<String>();
      log.debug3("looking for filename of: " + cuBase + "BodyRef/PDF/" + filenameValue + ".pdf");
      returnList.add(cuBase + "BodyRef/PDF/" + filenameValue + ".pdf");
      return returnList;
*/      
    }
	    
	  
  } // SpringerSourceMetadataExtractor
  
} // SpringerSourceMetadataExtractorFactory
