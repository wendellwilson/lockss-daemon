/*
 * $Id: BaseAtyponPdfFilterFactory.java,v 1.4 2014-10-08 16:11:26 alexandraohlson Exp $
 */

/*

 Copyright (c) 2000-2014 Board of Trustees of Leland Stanford Jr. University,
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

package org.lockss.plugin.atypon;

import java.io.*;

import org.apache.pdfbox.exceptions.*;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.lockss.filter.pdf.SimplePdfFilterFactory;
import org.lockss.pdf.*;
import org.lockss.pdf.pdfbox.PdfBoxDocument;
import org.lockss.plugin.ArchivalUnit;
import org.lockss.util.IOUtil;

/**
 * A pdf filter that handles the needs of many Atypon children
 * - handles pdfbox cryptography exception and removes creation date, modification
 * and trailer id.
 * For more complicated issues (usually seen in pdfplus - use the 
 * BaseAtyponScrapingPdfFilterFactory
 * @author alexohlson
 *
 */
public class BaseAtyponPdfFilterFactory extends SimplePdfFilterFactory {

  //Until the daemon handles this, use the special document factory
  // that knows how to handle the cryptography exception
  public BaseAtyponPdfFilterFactory() {
    super(new BaseAtyponPdfDocumentFactory()); // FIXME 1.67
  }

  /*
   * Many Atypon pdf files have the CreationDate and ModDate and the two ID numbers in the trailer
   * vary from collection to collection. Filter them out to avoid incorrect hash failures.
   * A child could choose to avoid this entirely by setting it to org.lockss.util.Default
   * or they could write their own child implementation
   */
  @Override
  public void transform(ArchivalUnit au,
                        PdfDocument pdfDocument)
      throws PdfException {
    pdfDocument.unsetCreationDate();
    pdfDocument.unsetModificationDate();
    PdfUtil.normalizeTrailerId(pdfDocument);
  }

}
