/*
 * $Id: CachedUrlSet.java,v 1.3 2003-03-18 02:27:41 aalto Exp $
 */

/*

Copyright (c) 2002 Board of Trustees of Leland Stanford Jr. University,
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

package org.lockss.plugin;
import java.util.Iterator;
import java.security.MessageDigest;

import org.lockss.daemon.*;

/**
 * This interface is implemented by plug-ins for the LOCKSS daemons.  The
 * generic daemon uses this interface to perform I/O on the files
 * representing the preserved content.  The generic daemon treats
 * <code>CachedUrlSet</code> objects as containing a set of files whose
 * URLs match a list of {@link CachedUrlSetSpec}s (<code>[url-prefix,
 * regular-expression]</code> pairs).
 *
 * @author  David S. H. Rosenthal
 * @version 0.0
 */
public interface CachedUrlSet extends CachedUrlSetNode {
  /**
   * Returns the owning ArchivalUnit.
   * @return the {@link ArchivalUnit} to which this CachedUrlSet belongs
   */
  public ArchivalUnit getArchivalUnit();

  /**
   * Return the {@link CachedUrlSetSpec}
   * describing the set of URLs that are members of this
   * <code>CachedUrlSet</code>.
   * @return the {@link CachedUrlSetSpec}
   */
  public CachedUrlSetSpec getSpec();

  /**
   * Return true if the url matches an entry in the
   * <code>CachedUrlSet</code> object's list.
   * @param url    url to be matched
   * @return <code>true</code> if the url matches an entry in the list,
   *         i.e. is in the file set, <code>false</code> otherwise
   */
  public boolean containsUrl(String url);

  // Methods used by the poller

  public CachedUrlSetHasher getContentHasher(MessageDigest hasher);

  /**
   * Return an object that can be used to hash the names of cached urls
   * that match the list of {@link CachedUrlSetSpec}
   * entries.
   * @param hasher a {@link MessageDigest} object to which the
   *               names will be supplied.
   * @return a {@link CachedUrlSetHasher} object that will
   *         hash the names of cached urls matching this
   *         <code>CachedUrlSet</code>.
   */
  public CachedUrlSetHasher getNameHasher(MessageDigest hasher);

  /**
   * Return an {@link Iterator} of {@link CachedUrlSetNode}
   * objects representing the direct descendants of this
   * <code>CachedUrlSet</code>.  These are CachedUrlSets for internal nodes
   * and CachedUrls for leaf nodes.
   * @return an {@link Iterator} of the {@link CachedUrlSetNode}s
   *         matching the members of the
   *         {@link CachedUrlSetSpec} list.
   */
  public Iterator flatSetIterator();

  /**
   * Return an {@link Iterator} of {@link CachedUrlSetNode}
   * objects representing all the nodes of the tree rooted at this
   * <code>CachedUrlSet</code>.  These are CachedUrlSets for internal nodes
   * and CachedUrls for leaf nodes.
   * @return an {@link Iterator} of {@link CachedUrlSetNode}s
   *         for all the nodes matching the members of the
   *         {@link CachedUrlSetSpec} list.
   */
  public Iterator treeIterator();

  /**
   * Return an estimate of the time required to hash the content.
   * @return an estimate of the time required to hash the content.
   */
  public long estimatedHashDuration();

  /**
   * Provide the measured duration of a hash attempt and an
   * indication of success or failure.
   * @param elapsed the measured duration of a hash attempt.
   * @param err the exception that terminated the hash, or null if it
   * succeeded
   */
  public void storeActualHashDuration(long elapsed, Exception err);

  // Methods used by readers

  /**
   * Create a {@link CachedUrl} object within the set.
   * @param url the url of interest
   * @return a {@link CachedUrl} object representing the url.
   */
  public CachedUrl makeCachedUrl(String url);

  // Methods used by writers

  /**
   * Create a {@link UrlCacher} object within the set.
   * @param url the url of interest
   * @return a {@link UrlCacher} object representing the url.
   */
  public UrlCacher makeUrlCacher(String url);

  /**
   * Needs to be overridden to hash CachedUrlSets properly.
   * @return the hashcode
   */
  public int hashCode();

  /**
   * Needs to be overridden to hash CachedUrlSets properly.
   * @param obj the object to compare to
   * @return true if equal
   */
  public boolean equals(Object obj);
}
