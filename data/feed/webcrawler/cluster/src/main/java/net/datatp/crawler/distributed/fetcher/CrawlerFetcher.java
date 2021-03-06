package net.datatp.crawler.distributed.fetcher;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
/**
 * Author : Tuan Nguyen
 *          tuan08@gmail.com
 * Jul 7, 2010  
 */
@Service("CrawlerFetcher")
public class CrawlerFetcher {
  private static final Logger logger = LoggerFactory.getLogger(CrawlerFetcher.class);

  @Autowired
  private DistributedFetcher fetcherManager ;

  private boolean startOnInit = false;

  public DistributedFetcher getFetcherManager() { return fetcherManager ; }

  public void setStartOnInit(boolean b) { this.startOnInit = b ; }
  
  @PostConstruct
  public void onInit() { 
    if(startOnInit) start() ;
  }

  public void start() {
    fetcherManager.start() ;
    logger.info("CrawlerFetcher Start!") ;
  }

  public void stop() {
    fetcherManager.stop() ;
    logger.info("CrawlerFetcher Stop!") ;
  }
}