package de.tuda.sdm.dmdb.test;

import junit.framework.Test;
import junit.framework.TestSuite;
import de.tuda.sdm.dmdb.test.access.TestSuiteAccess;

public class TestSuiteDMDB extends TestSuite
{
  public static Test suite()
  {
    TestSuite suite = new TestSuite( "DMDB-All" );
    suite.addTest(TestSuiteAccess.suite());
    return suite;
  }
}
