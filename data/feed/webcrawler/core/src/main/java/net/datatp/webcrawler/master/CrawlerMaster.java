package net.datatp.webcrawler.master;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.datatp.webcrawler.site.SiteContextManager;
/**
 * Author : Tuan Nguyen
 *          tuan@gmail.com
 * Apr 21, 2010  
 */
@Service("CrawlerMaster")
public class CrawlerMaster {
  private static final Logger logger = LoggerFactory.getLogger(CrawlerMaster.class);

  @Autowired
  private URLFetchScheduler urlFetchScheduler ;
  @Autowired
  private SiteContextManager siteConfigManager ;
  private CrawlerMasterInfo crawlerMasterInfo ;
  private boolean startOnInit = false ; 

  public SiteContextManager getSiteConfigManager() { return this.siteConfigManager ; }

  public URLFetchScheduler getURLFetchScheduler() { return this.urlFetchScheduler ; }

  public CrawlerMasterInfo getCrawlerMasterInfo()  { return this.crawlerMasterInfo ; }

  public void setStartOnInit(boolean b) { this.startOnInit = b ;  }

  @PostConstruct
  public void onInit() throws Exception {
    this.crawlerMasterInfo = new CrawlerMasterInfo(true) ;
    this.crawlerMasterInfo.setStartTime(System.currentTimeMillis()) ;
    if(startOnInit) start();
    logger.info("onInit(), initialize the CrawlerService environment");
  }

  @PreDestroy
  public void onDestroy() throws Exception {
    stop() ;
    logger.info("onDestroy(), destroy the CrawlerService environment");
  }

  synchronized public void start() {
    logger.info("Start the CrawlerService!!!!!!!!!!!!!!!!!!!!!!!") ;
    this.urlFetchScheduler.start() ;
    this.crawlerMasterInfo.setStatus(CrawlerMasterInfo.RUNNING_STATUS) ;
  }

  synchronized public void stop() {
    this.urlFetchScheduler.stop() ;
    logger.info("Stop the CrawlerService!!!!!!!!!!!!!!!!!!!!!!!") ;
  }
}