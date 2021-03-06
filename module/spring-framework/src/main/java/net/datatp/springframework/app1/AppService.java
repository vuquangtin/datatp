package net.datatp.springframework.app1;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

//@Service configure in META-INF/application1-config.xml
public class AppService {
  @PostConstruct
  public void onInit() throws Exception {
    System.out.println("AppService1: onInit()");
    System.out.println("AppService1: classloader = " + Thread.currentThread().getContextClassLoader().hashCode());
  }
  
  @PreDestroy
  public void onDestroy() throws Exception {
    System.out.println("AppService1: onDestroy()");
  }
}
