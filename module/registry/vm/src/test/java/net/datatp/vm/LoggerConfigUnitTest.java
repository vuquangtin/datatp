package net.datatp.vm;

import java.util.Map;

import org.junit.Test;
import org.slf4j.Logger;

import net.datatp.util.JSONSerializer;
import net.datatp.util.log.LoggerFactory;
import net.datatp.vm.LoggerConfig;

public class LoggerConfigUnitTest {
  @Test
  public void test() throws Exception {
    LoggerConfig conf = new LoggerConfig() ;
    conf.getEsAppender().setEnable(false);
    conf.getKafkaAppender().setEnable(false);
    conf.getFileAppender().setFilePath("build/test-log.log");
    conf.getConsoleAppender().setEnable(true);
    Map<String, String> log4jProps = conf.getLog4jConfiguration() ;
    System.out.println(JSONSerializer.INSTANCE.toString(log4jProps));
    
    LoggerFactory lFactory = new LoggerFactory("[TEST] ");
    LoggerFactory.log4jConfigure(log4jProps);
    Logger logger = lFactory.getLogger(getClass()) ;
    logger.info("This is a test");
  }
}