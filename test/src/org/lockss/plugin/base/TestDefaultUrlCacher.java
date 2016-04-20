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

package org.lockss.plugin.base;

import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;

import org.lockss.plugin.*;
import org.lockss.daemon.*;
import org.lockss.state.AuState;
import org.lockss.test.*;
import org.lockss.app.*;
import org.lockss.alert.*;
import org.lockss.util.*;
import org.lockss.util.urlconn.*;
import org.lockss.repository.*;
import org.lockss.crawler.*;
import org.lockss.config.*;

import static org.lockss.util.DateTimeUtil.GMT_DATE_FORMATTER;

/**
 * This is the test class for org.lockss.plugin.simulated.GenericFileUrlCacher
 *
 * @author Emil Aalto
 * @version 0.0
 */
public class TestDefaultUrlCacher extends LockssTestCase {

  protected static Logger logger = Logger.getLogger("TestDefaultUrlCacher");

  MyDefaultUrlCacher cacher;
  MockCachedUrlSet mcus;
  MockPlugin plugin;

  private MyMockArchivalUnit mau;
  private MockLockssDaemon theDaemon;
  private LockssRepository repo;
  private MockAlertManager alertMgr;
  private int pauseBeforeFetchCounter;
  private UrlData ud;
  private MockNodeManager nodeMgr = new MockNodeManager();
  private MockAuState maus;


  private static final String TEST_URL = "http://www.example.com/testDir/leaf1";
  private boolean saveDefaultSuppressStackTrace;

  public void setUp() throws Exception {
    super.setUp();

    String tempDirPath = getTempDir().getAbsolutePath() + File.separator;
    CIProperties props = new CIProperties();
    props.setProperty(LockssRepositoryImpl.PARAM_CACHE_LOCATION, tempDirPath);
    ConfigurationUtil.setCurrentConfigFromProps(props);

    theDaemon = getMockLockssDaemon();
    theDaemon.getHashService();

    theDaemon.getRepositoryManager();
    mau = new MyMockArchivalUnit();

    mau.setConfiguration(ConfigManager.newConfiguration());

    plugin = new MockPlugin();
    plugin.initPlugin(theDaemon);
    mau.setPlugin(plugin);

    repo =
      (LockssRepository)theDaemon.newAuManager(LockssDaemon.LOCKSS_REPOSITORY,
                                               mau);
    theDaemon.setLockssRepository(repo, mau);
    repo.startService();

    theDaemon.setNodeManager(nodeMgr, mau);

    mcus = new MockCachedUrlSet(TEST_URL);
    mcus.setArchivalUnit(mau);
    mau.setAuCachedUrlSet(mcus);
    saveDefaultSuppressStackTrace =
      CacheException.setDefaultSuppressStackTrace(false);
    alertMgr = new MockAlertManager();
    getMockLockssDaemon().setAlertManager(alertMgr);
    
    theDaemon.setNodeManager(nodeMgr, mau);
    maus = new MockAuState(mau);
    nodeMgr.setAuState(maus);
  }

  public void tearDown() throws Exception {
    TimeBase.setReal();
    CacheException.setDefaultSuppressStackTrace(saveDefaultSuppressStackTrace);
    super.tearDown();
  }
  
  public void testCacheStartUrl() throws IOException {
    ud = new UrlData(new StringInputStream("test stream"), 
                     new CIProperties(), TEST_URL);
    mau.setStartUrls(ListUtil.list(TEST_URL));
    long origChange = maus.getLastContentChange();
    cacher = new MyDefaultUrlCacher(mau, ud);
    cacher.storeContent();
    long finalChange = maus.getLastContentChange();
    assertEquals(origChange, finalChange);
  }

  public void testCache() throws IOException {
    ud = new UrlData(new StringInputStream("test stream"), 
        new CIProperties(), TEST_URL);
    long origChange = maus.getLastContentChange();
    cacher = new MyDefaultUrlCacher(mau, ud);
    // should cache
    cacher.storeContent();
    long finalChange = maus.getLastContentChange();
    assertTrue(cacher.wasStored);
    assertNotEquals(origChange, finalChange);
  }

  public void testCacheEmpty() throws IOException {
    ud = new UrlData(new StringInputStream(""), 
        new CIProperties(), TEST_URL);
    cacher = new MyDefaultUrlCacher(mau, ud);
    // should cache
    cacher.storeContent();
    assertTrue(cacher.wasStored);
    assertClass(CacheException.WarningOnly.class,
		cacher.getInfoException());
    assertEquals("Empty file stored",
		 cacher.getInfoException().getMessage());
  }

  public void testCacheEmptyPluginDoesntCare() throws IOException {
    HttpResultMap resultMap = (HttpResultMap)plugin.getCacheResultMap();
    resultMap.storeMapEntry(ContentValidationException.EmptyFile.class,
			    CacheSuccess.class);
    ud = new UrlData(new StringInputStream(""), 
        new CIProperties(), TEST_URL);
    cacher = new MyDefaultUrlCacher(mau, ud);
    // should cache
    cacher.storeContent();
    assertTrue(cacher.wasStored);
    assertNull(cacher.getInfoException());
  }

  public void testCacheEmptyRetry() throws IOException {
    HttpResultMap resultMap = (HttpResultMap)plugin.getCacheResultMap();
    resultMap.storeMapEntry(ContentValidationException.EmptyFile.class,
			    CacheException.RetryableNetworkException_2.class);
    ud = new UrlData(new StringInputStream(""), 
        new CIProperties(), TEST_URL);
    cacher = new MyDefaultUrlCacher(mau, ud);
    try {
      cacher.storeContent();
      fail("Should have thrown CacheException.RetryableNetworkException_2");
    } catch (CacheException.RetryableNetworkException_2 e) {
      // expected
    }
    assertFalse(cacher.wasStored);
    assertNull(cacher.getInfoException());
  }

  void setSuppressValidation(UrlCacher uc) {
    BitSet fetchFlags = new BitSet();
    fetchFlags.set(UrlCacher.SUPPRESS_CONTENT_VALIDATION);
    uc.setFetchFlags(fetchFlags);
  }

  public void testCacheEmptySuppressValidation() throws IOException {
    HttpResultMap resultMap = (HttpResultMap)plugin.getCacheResultMap();
    resultMap.storeMapEntry(ContentValidationException.EmptyFile.class,
			    CacheException.RetryableNetworkException_2.class);
    ud = new UrlData(new StringInputStream(""), 
        new CIProperties(), TEST_URL);
    cacher = new MyDefaultUrlCacher(mau, ud);
    setSuppressValidation(cacher);
    cacher.storeContent();
    assertTrue(cacher.wasStored);
    assertNull(cacher.getInfoException());
  }

  public void testCacheExceptions() throws IOException {
    ud = new UrlData(new StringInputStream("test stream"), 
        null, TEST_URL);
    try {
      cacher = new MyDefaultUrlCacher(mau, ud);
      fail("Should have thrown NullPointerException.");
    } catch (NullPointerException npe) {
    }
    assertNull(cacher);

    // no exceptions from null inputstream
    ud = new UrlData(null, new CIProperties(), TEST_URL);
    cacher = new MyDefaultUrlCacher(mau, ud);
    cacher.storeContent();
    // should simply skip
    assertFalse(cacher.wasStored);

    ud = new UrlData(new StringInputStream("test stream"),
        new CIProperties(), TEST_URL);
    cacher = new MyDefaultUrlCacher(mau, ud);
    cacher.storeContent();
    assertTrue(cacher.wasStored);
  }

  public void testValidate() throws IOException {
    HttpResultMap resultMap = (HttpResultMap)plugin.getCacheResultMap();
    resultMap.storeMapEntry(MyContentValidationException1.class,
			    CacheSuccess.class);
    resultMap.storeMapEntry(MyContentValidationException2.class,
			    CacheException.WarningOnly.class);
    resultMap.storeMapEntry(MyContentValidationException3.class,
			    CacheException.RetryableNetworkException_2.class);
    mau.setContentValidatorFactory(new MyContentValidatorFactory());
    MockCachedUrl mcu = mau.addUrl(TEST_URL, "invalid_1");

    doStore("not invalid", null);
    assertNull(cacher.getInfoException());
    // Success
    doStore("invalid_1", null);
    assertNull(cacher.getInfoException());
    // Warning
    doStore("invalid_2", null);
    assertEquals("v ex 2",
		 cacher.getInfoException().getMessage());

    try {
      doStore("invalid_3", null);
      fail("Should have thrown CacheException.RetryableNetworkException_2");
    } catch (CacheException.RetryableNetworkException_2 e) {
      // expected
      assertEquals("v ex 3", e.getMessage());
      assertNull(cacher.getInfoException());
    }
    
    // Not explicitly mapped, maps to ContentValidationException default in
    // HttpResultMap

    try {
      doStore("invalid_4", null);
      fail("Should have thrown CacheException.UnretryableException");
    } catch (CacheException.UnretryableException e) {
      // expected
      assertEquals("v ex 4", e.getMessage());
      assertNull(cacher.getInfoException());
    }

    doStore("valid", null);
    assertNull(cacher.getInfoException());

    // Unmapped
    try {
      doStore("valid", "plug_err");
      fail("Should have thrown CacheException.UnknownExceptionException");
    } catch (CacheException.UnknownExceptionException e) {
      // expected
      assertEquals("Unmapped exception: org.lockss.daemon.PluginException: random plugin exception",
		   e.getMessage());
      assertNull(cacher.getInfoException());
    }

    try {
      doStore("IOException", null);
      fail("Should have thrown CacheException.UnknownExceptionException");
    } catch (CacheException.UnknownExceptionException e) {
      // expected
      assertEquals("Unmapped exception: java.io.IOException: EIEIO",
		   e.getMessage());
      assertNull(cacher.getInfoException());
    }

    try {
      doStore("PluginException", null);
      fail("Should have thrown CacheException.UnknownExceptionException");
    } catch (CacheException.UnknownExceptionException e) {
      // expected
      assertEquals("Unmapped exception: org.lockss.daemon.PluginException: nickel",
		   e.getMessage());
      assertNull(cacher.getInfoException());
    }


    // Empty combined with validation failure - exception thrown by plugin
    // validator should take precedence

    doStore("", "invalid_1");
    assertMatchesRE("WarningOnly: Empty file stored",
		    cacher.getInfoException().toString());

    doStore("", "invalid_2");
    assertMatchesRE("WarningOnly: v ex 2",
		    cacher.getInfoException().toString());
    try {
      doStore("", "invalid_3");
      fail("Should have thrown CacheException.RetryableNetworkException_2");
    } catch (CacheException.RetryableNetworkException_2 e) {
      // expected
      assertEquals("v ex 3", e.getMessage());
      assertNull(cacher.getInfoException());
    }

    doStore("", null);
    assertMatchesRE("WarningOnly: Empty file stored",
		    cacher.getInfoException().toString());
  }

  void doStore(String content, String prop)
      throws IOException {
    ud = new UrlData(new StringInputStream(content), 
		     new CIProperties(), TEST_URL);
    cacher = new MyDefaultUrlCacher(mau, ud);
    MockCachedUrl mcu = mau.addUrl(TEST_URL, content);
    if (prop != null) {
      mcu.addProperty("prop_name", prop);
    }
    mcu.addProperty("other_prop_name", "foo");
    cacher = new MyDefaultUrlCacher(mau, ud);
    cacher.storeContent();
  }

  static class MyContentValidatorFactory implements ContentValidatorFactory {
    public ContentValidator createContentValidator(ArchivalUnit au,
						   String contentType) {
    return new MyContentValidator();
    }
  }
    
  static class MyContentValidator implements ContentValidator {
    public void validate(CachedUrl cu)
	throws ContentValidationException, PluginException, IOException {
      CIProperties props = cu.getProperties();
      String prop = props.getProperty("prop_name");
      if (prop != null) {
	switch (prop) {
	case "plug_err":
	  throw new PluginException("random plugin exception");
	case "invalid_1":
	  throw new MyContentValidationException1("v ex 1");
	case "invalid_2":
	  throw new MyContentValidationException2("v ex 2");
	case "invalid_3":
	  throw new MyContentValidationException3("v ex 3");
	case "invalid_4":
	  throw new MyContentValidationException4("v ex 4");
	}
      }
      String cont = StringUtil.fromInputStream(cu.getUnfilteredInputStream());
      switch (cont) {
      case "invalid_1":
	throw new MyContentValidationException1("v ex 1");
      case "invalid_2":
	throw new MyContentValidationException2("v ex 2");
      case "invalid_3":
	throw new MyContentValidationException3("v ex 3");
      case "invalid_4":
	throw new MyContentValidationException4("v ex 4");
      case "IOException":
	throw new IOException("EIEIO");
      case "PluginException":
	throw new PluginException("nickel");
      }
    }
  }

  static class MyContentValidationException1
    extends ContentValidationException {
    MyContentValidationException1(String msg) {
      super(msg);
    }
  }

  static class MyContentValidationException2
    extends ContentValidationException {
    MyContentValidationException2(String msg) {
      super(msg);
    }
  }

  static class MyContentValidationException3
    extends ContentValidationException {
    MyContentValidationException3(String msg) {
      super(msg);
    }
  }

  static class MyContentValidationException4
    extends ContentValidationException {
    MyContentValidationException4(String msg) {
      super(msg);
    }
  }



  enum DisagreeTest {Default, Ignore, Warning, Error};

  public void testCacheSizeDisagreesAlert(DisagreeTest mode)
      throws IOException {
    HttpResultMap resultMap = (HttpResultMap)plugin.getCacheResultMap();
    switch (mode) {
    case Ignore:
      resultMap.storeMapEntry(ContentValidationException.WrongLength.class,
			      CacheSuccess.class);
      break;
    case Warning:
      resultMap.storeMapEntry(ContentValidationException.WrongLength.class,
			      CacheException.WarningOnly.class);
      break;
    case Error:
      resultMap.storeMapEntry(ContentValidationException.WrongLength.class,
			      CacheException.UnretryableException.class);
      break;
    case Default:
      break;
    }

    CIProperties props = new CIProperties();
    props.setProperty("Content-Length", "8");
    ud = new UrlData(new StringInputStream("123456789"), props, TEST_URL);
    cacher = new MyDefaultUrlCacher(mau, ud);
    try {
      cacher.storeContent();
      switch (mode) {
      case Ignore:
      case Warning:
      case Default:
	break;
      case Error:
	fail("storeContent() should have thrown WrongLength");
      }
    } catch (CacheException e) {
      switch (mode) {
      case Ignore:
	fail("storeContent() shouldn't have thrown", e);
      case Warning:
	assertClass(CacheException.WarningOnly.class, e);
	break;
      case Error:
	assertClass(CacheException.UnretryableException.class, e);
	break;
      case Default:
	assertClass(CacheException.RetryableNetworkException_3_10S.class, e);
	break;
      }
      assertMatchesRE("File size \\(9\\) differs from Content-Length header \\(8\\): http://www.example.com/testDir/leaf1",
		      e.getMessage());
    }
    switch (mode) {
    case Ignore:
    case Warning:
      assertTrue(cacher.wasStored);
      break;
    case Default:
    case Error:
      assertFalse(cacher.wasStored);
      break;
    }

    assertEquals(1, alertMgr.getAlerts().size());
    Alert alert = alertMgr.getAlerts().get(0);
    assertEquals("FileVerification", alert.getAttribute(Alert.ATTR_NAME));
    assertEquals(mau.getAuId(), alert.getAttribute(Alert.ATTR_AUID));
    assertEquals(TEST_URL, alert.getAttribute(Alert.ATTR_URL));
    assertEquals(Alert.SEVERITY_WARNING,
		 alert.getAttribute(Alert.ATTR_SEVERITY));
    assertEquals("File size (9) differs from Content-Length header (8): " + TEST_URL,
		 alert.getAttribute(Alert.ATTR_TEXT));
  }

  public void testCacheSizeDisagreesDefault() throws IOException {
    testCacheSizeDisagreesAlert(DisagreeTest.Default);
  }

  public void testCacheSizeDisagreesIgnore() throws IOException {
    testCacheSizeDisagreesAlert(DisagreeTest.Ignore);
  }

  public void testCacheSizeDisagreesWarning() throws IOException {
    testCacheSizeDisagreesAlert(DisagreeTest.Warning);
  }

  public void testCacheSizeDisagreesError() throws IOException {
    testCacheSizeDisagreesAlert(DisagreeTest.Error);
  }

  public void testNewVersionAlert() throws IOException {
    CIProperties props = new CIProperties();
    ud = new UrlData(new StringInputStream("123456789"), props, TEST_URL);
    cacher = new MyDefaultUrlCacher(mau, ud);
    cacher.storeContent();
    assertTrue(cacher.wasStored);
    assertEquals(0, alertMgr.getAlerts().size());
    ud = new UrlData(new StringInputStream("987"), props, TEST_URL);
    cacher = new MyDefaultUrlCacher(mau, ud);
    cacher.storeContent();
    assertEquals(1, alertMgr.getAlerts().size());
    Alert alert = alertMgr.getAlerts().get(0);
    assertEquals("NewFileVersion", alert.getAttribute(Alert.ATTR_NAME));
    assertEquals(mau.getAuId(), alert.getAttribute(Alert.ATTR_AUID));
    assertEquals(TEST_URL, alert.getAttribute(Alert.ATTR_URL));
    assertEquals(Alert.SEVERITY_INFO,
		 alert.getAttribute(Alert.ATTR_SEVERITY));
    assertEquals("Collected an edditional version: " + TEST_URL,
		 alert.getAttribute(Alert.ATTR_TEXT));
  }

  public void testCacheSizeAgrees() throws IOException {
    CIProperties props = new CIProperties();
    props.setProperty("Content-Length", "9");
    ud = new UrlData(new StringInputStream("123456789"), props, TEST_URL);
    cacher = new MyDefaultUrlCacher(mau, ud);
    cacher.storeContent();
    assertTrue(cacher.wasStored);
    assertEquals(0, alertMgr.getAlerts().size());
  }

  public void testFileCache() throws IOException {
    CIProperties props = new CIProperties();
    props.setProperty("test1", "value1");
    ud = new UrlData(new StringInputStream("test content"), 
        props, TEST_URL);
    cacher = new MyDefaultUrlCacher(mau, ud);
    cacher.storeContent();

    CachedUrl url = new BaseCachedUrl(mau, TEST_URL);
    InputStream is = url.getUnfilteredInputStream();
    assertReaderMatchesString("test content", new InputStreamReader(is));

    props = url.getProperties();
    assertEquals("value1", props.getProperty("test1"));
  }

  public void testFileChecksum() throws IOException {
    ConfigurationUtil.addFromArgs(DefaultUrlCacher.PARAM_CHECKSUM_ALGORITHM,
				  "SHA-1");
    CIProperties props = new CIProperties();
    props.setProperty("test1", "value1");
    ud = new UrlData(new StringInputStream("test content"), 
        props, TEST_URL);
    cacher = new MyDefaultUrlCacher(mau, ud);
    cacher.storeContent();;

    CachedUrl url = new BaseCachedUrl(mau, TEST_URL);
    InputStream is = url.getUnfilteredInputStream();
    assertReaderMatchesString("test content", new InputStreamReader(is));

    props = url.getProperties();
    assertEquals("value1", props.getProperty("test1"));
    assertEquals("SHA-1:1EEBDF4FDC9FC7BF283031B93F9AEF3338DE9052", props.getProperty(CachedUrl.PROPERTY_CHECKSUM));
  }

  public void testSubstanceCount() throws IOException {
    ud = new UrlData(new StringInputStream("test stream"), 
		     new CIProperties(), TEST_URL);
    cacher = new MyDefaultUrlCacher(mau, ud);
    cacher.storeContent();
    CachedUrl cu = new BaseCachedUrl(mau, TEST_URL);
    mau.addCu(cu);
    AuSuspectUrlVersions asuv = repo.getSuspectUrlVersions(mau);
    assertTrue(asuv.isEmpty());
    AuState aus = AuUtil.getAuState(mau);
    assertEquals(0, aus.recomputeNumCurrentSuspectVersions());
    asuv.markAsSuspect(TEST_URL, 1);
    assertEquals(1, aus.recomputeNumCurrentSuspectVersions());
    ud = new UrlData(new StringInputStream("different content"), 
		     new CIProperties(), TEST_URL);
    cacher = new MyDefaultUrlCacher(mau, ud);
    cacher.storeContent();
    assertFalse(asuv.isEmpty());
    assertTrue(asuv.isSuspect(TEST_URL, 1));
    // suspect version no longer current
    assertEquals(0, aus.getNumCurrentSuspectVersions());

    aus.setNumCurrentSuspectVersions(4);
    assertEquals(4, aus.getNumCurrentSuspectVersions());
    ud = new UrlData(new StringInputStream("more different content"), 
		     new CIProperties(), TEST_URL);
    cacher = new MyDefaultUrlCacher(mau, ud);
    cacher.storeContent();
    // previously current version wasn't suspect, count should not decrease
    assertEquals(4, aus.getNumCurrentSuspectVersions());
  }

  // Should throw exception derived from IOException thrown by InputStream
  // in copy()
  public void testCopyInputError() throws Exception {
    InputStream input = new ThrowingInputStream(
               new StringInputStream("will throw"),
				       new IOException("Malformed chunk"),
				       null);
    ud = new UrlData(input, new CIProperties(), TEST_URL);
    cacher = new MyDefaultUrlCacher(mau, ud);
    try {
      cacher.storeContent();
      fail("Copy should have thrown");
    } catch (IOException e) {
      Throwable t = e.getCause();
      assertClass(IOException.class, t);
      assertEquals("java.io.IOException: Malformed chunk", t.getMessage());
    }
  }

  // Should throw exception derived from IOException thrown by InputStream
  // in close()
  public void testCopyInputErrorOnClose() throws Exception {
    InputStream input = new ThrowingInputStream(
               new StringInputStream("will throw"),
				       null, new IOException("CRLF expected at end of chunk: -1/-1"));
    ud = new UrlData(input, new CIProperties(), TEST_URL);
    cacher = new MyDefaultUrlCacher(mau, ud);
    try {
      cacher.storeContent();
      fail("Copy should have thrown");
    } catch (IOException e) {
      Throwable t = e.getCause();
      assertClass(IOException.class, t);
      assertEquals("java.io.IOException: CRLF expected at end of chunk: -1/-1", t.getMessage());
    }
  }

  // Should throw exception derived from IOException thrown by InputStream
  // in close()
  public void testIgnoreCloseException() throws Exception {
    HttpResultMap resultMap = (HttpResultMap)plugin.getCacheResultMap();
    resultMap.storeMapEntry(IOException.class,
			    CacheException.IgnoreCloseException.class);

    InputStream input = new ThrowingInputStream(
        new StringInputStream("will throw"), null, 
            new IOException("Exception should be ignored on close()"));
    ud = new UrlData(input, new CIProperties(), TEST_URL);
    cacher = new MyDefaultUrlCacher(mau, ud);
    cacher.storeContent();
  }
  
  void assertCuContents(String url, String contents) throws IOException {
    CachedUrl cu = new BaseCachedUrl(mau, url);
    InputStream is = cu.getUnfilteredInputStream();
    assertReaderMatchesString(contents, new InputStreamReader(is));
  }

  /**
   * Assert that this url has no content
   */
  void assertCuNoContent(String url) throws IOException {
    CachedUrl cu = new BaseCachedUrl(mau, url);
    assertFalse(cu.hasContent());
  }

  void assertCuProperty(String url, String expected, String key) {
    CachedUrl cu = new BaseCachedUrl(mau, url);
    CIProperties props = cu.getProperties();
    assertEquals(expected, props.getProperty(key));
  }

  void assertCuUrl(String url, String expected) {
    CachedUrl cu = new BaseCachedUrl(mau, url);
    assertEquals(expected, cu.getUrl());
  }

  // DefaultUrlCacher that remembers that it stored
  private class MyDefaultUrlCacher extends DefaultUrlCacher {
    boolean wasStored = false;

    List inputList;

    public MyDefaultUrlCacher(ArchivalUnit owner, UrlData ud) {
      super(owner, ud);
    }

    public MyDefaultUrlCacher(ArchivalUnit owner, UrlData ud, List inputList) {
      super(owner, ud);
      this.inputList = inputList;
    }

    @Override
    protected void storeContentIn(String url, InputStream input,
				  CIProperties headers)
        throws IOException {
      super.storeContentIn(url, input, headers);
      wasStored = true;
    }
  }

  private class MyMockArchivalUnit extends MockArchivalUnit {
    boolean returnRealCachedUrl = false;

    public CachedUrlSet makeCachedUrlSet(CachedUrlSetSpec cuss) {
      return new BaseCachedUrlSet(this, cuss);
    }

    public CachedUrl makeCachedUrl(String url) {
      if (returnRealCachedUrl) {
        return new BaseCachedUrl(this, url);
      } else {
        return super.makeCachedUrl(url);
      }
    }
  }

  class MyStringInputStream extends StringInputStream {
    private boolean resetWasCalled = false;
    private boolean markWasCalled = false;
    private boolean closeWasCalled = false;
    private IOException resetEx;

    private int buffSize = -1;

    public MyStringInputStream(String str) {
      super(str);
    }

    /**
     * @param str String to read from
     * @param resetEx IOException to throw when reset is called
     *
     * Same as one arg constructor, but can provide an exception that is thrown
     * when reset is called
     */
    public MyStringInputStream(String str, IOException resetEx) {
      super(str);
      this.resetEx = resetEx;
    }

    public void reset() throws IOException {
      resetWasCalled = true;
      if (resetEx != null) {
        throw resetEx;
      }
      super.reset();
    }

    public boolean resetWasCalled() {
      return resetWasCalled;
    }

    public void mark(int buffSize) {
      markWasCalled = true;
      this.buffSize = buffSize;
      super.mark(buffSize);
    }

    public boolean markWasCalled() {
      return markWasCalled;
    }

    public int getMarkBufferSize() {
      return this.buffSize;
    }

    public void close() throws IOException {
      Exception ex = new Exception("Blah");
      logger.debug3("Close called on " + this, ex);
      closeWasCalled = true;
      super.close();
    }

    public boolean closeWasCalled() {
      return closeWasCalled;
    }

  }

}
