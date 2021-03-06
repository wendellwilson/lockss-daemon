/*
 * $Id$
 */

/*

Copyright (c) 2000-2003 Board of Trustees of Leland Stanford Jr. University,
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

package org.lockss.plugin.iop;

import java.io.*;
import java.util.*;
import org.lockss.util.*;
import org.lockss.filter.*;
import org.lockss.plugin.FilterRule;

/**
 * <p>This URL normalizer goes with the ancient plugin
 * org.lockss.plugin.iop.IOPPlugin, not with current plugins for the
 * IOPscience platform.</p>
 */
public class IOPFilterRule implements FilterRule {
  public Reader createFilteredReader(Reader reader) {
    List tagList = ListUtil.list(
	//cruft in the TOC
        new HtmlTagFilter.TagPair("<td class=\"toc_left\">", "</td>", true),

        new HtmlTagFilter.TagPair("&nbsp;|&nbsp; <a title=\"Citing articles: ", "Citing articles</a>", true),

	//content box
        new HtmlTagFilter.TagPair("<div id=\"art-opts\">", "</div>", true),
        new HtmlTagFilter.TagPair("<ul class=\"art-opts-mm\">", "</ul>", true),
	//hackish, but this will remove the links to the refs, which change
	//over time
        new HtmlTagFilter.TagPair("<span class=\"smltxt\">", "</span>", true),
        new HtmlTagFilter.TagPair("<!--", "-->", true),
        new HtmlTagFilter.TagPair("<script", "</script>", true),
        new HtmlTagFilter.TagPair("<", ">")
        );
    Reader filteredReader = HtmlTagFilter.makeNestedFilter(reader, tagList);
    return new WhiteSpaceFilter(filteredReader);
  }
}
