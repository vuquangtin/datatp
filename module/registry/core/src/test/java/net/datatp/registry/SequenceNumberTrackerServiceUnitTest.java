package net.datatp.registry;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import net.datatp.registry.RegistryConfig;
import net.datatp.registry.SequenceNumberTrackerService;
import net.datatp.registry.zk.RegistryImpl;
import net.datatp.util.io.FileUtil;
import net.datatp.zk.tool.server.EmbededZKServer;

public class SequenceNumberTrackerServiceUnitTest {
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
  public void testSequenceNumberTrackerService() throws Exception {
    final RegistryImpl registry = newRegistry();
    registry.connect();
    SequenceNumberTrackerService service = new SequenceNumberTrackerService(registry);
    service.createIntTrackerIfNotExist("test");
    Assert.assertEquals(1, service.nextInt("test"));
    Assert.assertEquals(2, service.nextInt("test"));
    registry.shutdown();
  }
  
  private RegistryImpl newRegistry() {
    return new RegistryImpl(RegistryConfig.getDefault()) ;
  }
}
