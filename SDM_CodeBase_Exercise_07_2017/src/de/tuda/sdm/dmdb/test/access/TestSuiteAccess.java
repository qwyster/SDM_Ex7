package de.tuda.sdm.dmdb.test.access;

import junit.framework.Test;
import junit.framework.TestSuite;

public class TestSuiteAccess extends TestSuite
{
  public static Test suite()
  {
    TestSuite suite = new TestSuite( "DMDB-Access2" );
    suite.addTestSuite( TestNaiveBitmapIndex.class );
    suite.addTestSuite( TestApproximateBitmapIndex.class );
    return suite;
  }
}
