package net.datatp.registry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import net.datatp.registry.RegistryConfig;
import net.datatp.registry.SequenceIdTracker;
import net.datatp.registry.zk.RegistryImpl;
import net.datatp.util.io.FileUtil;
import net.datatp.zk.tool.server.EmbededZKServer;

public class SequenceIdTrackerUnitTest {
  static {
    System.setProperty("log4j.configuration", "file:src/test/resources/test-log4j.properties") ;
  }
  
  private EmbededZKServer zkServerLauncher ;
  
  @Before
  public void setup() throws Exception {
    FileUtil.removeIfExist("./build/data", false);
    
    zkServerLauncher = new EmbededZKServer("./build/data/zookeeper") ;
    zkServerLauncher.start();
  }
  
  @After
  public void teardown() throws Exception {
    zkServerLauncher.shutdown();
  }
  
  @Test
  public void testSequenceIdTracker() throws Exception {
    String SEQ_ID_TRACKER_PATH = "/id-tracker";
    SequenceIdTracker tracker1 = new  SequenceIdTracker(newRegistry().connect(), SEQ_ID_TRACKER_PATH, true);
    System.out.println("tracker1: " + tracker1.nextSeqId());
    SequenceIdTracker tracker2 = new  SequenceIdTracker(newRegistry().connect(), SEQ_ID_TRACKER_PATH, true);
    System.out.println("tracker2: " + tracker2.nextSeqId());
  }
  
  private RegistryImpl newRegistry() {
    return new RegistryImpl(RegistryConfig.getDefault()) ;
  }
}
