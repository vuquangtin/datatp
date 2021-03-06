package net.datatp.crawler.scheduler;

import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.datatp.crawler.scheduler.metric.URLCommitMetric;
import net.datatp.crawler.scheduler.metric.URLScheduleMetric;
import net.datatp.crawler.site.SiteConfig;
import net.datatp.crawler.site.SiteContext;
import net.datatp.crawler.site.SiteContextManager;
import net.datatp.crawler.urldb.URLDatum;
import net.datatp.crawler.urldb.URLDatumDBWriter;
import net.datatp.util.URLInfo;
/**
 * $Author: Tuan Nguyen$ 
 **/
public class URLScheduler {
  private static final Logger     logger = LoggerFactory.getLogger(URLScheduler.class);

  protected URLPreFetchScheduler  preFetchScheduler;

  protected URLPostFetchScheduler postFetchScheduler;

  protected URLSchedulerReporter  reporter;

  protected boolean               exist  = false;
  protected ManageThread          manageThread;
  protected URLSchedulerStatus    status = URLSchedulerStatus.INIT;

  public URLScheduler() { }
  
  public URLScheduler(URLPreFetchScheduler preFetchScheduler, 
                      URLPostFetchScheduler postFetchScheduler, 
                      URLSchedulerReporter reporter) { 
    this.preFetchScheduler = preFetchScheduler;
    this.postFetchScheduler = postFetchScheduler;
    this.reporter = reporter;
  }
  
  public URLSchedulerReporter getSchedulerReporter() { return reporter; }
  
  public URLSchedulerStatus getStatus() { return this.status ; }

  synchronized public void start() {
    logger.info("start CrawlerURLScheduler!!!!!!!!!!!!!!!!!!!!!!!") ;
    exist = false;
    if(manageThread != null && manageThread.isAlive()) {
      exist = false;
      return ;
    }
    manageThread = new ManageThread() ;
    manageThread.setName("crawler.master.fetch-manager") ;
    status = URLSchedulerStatus.STARTING ;
    manageThread.start() ;
    logger.info("Create a new thread and start CrawlerURLScheduler!") ;
  }

  synchronized public void stop() {
    logger.info("stop CrawlerURLScheduler!!!!!!!!!!!!!!!!!!!!!!!") ;
    status = URLSchedulerStatus.STOP ;
    if(manageThread == null) return ;
    if(manageThread.isAlive()) {
      exist = true ;
      logger.info("set manage thread to exist for CrawlerURLScheduler") ;
    }
  }

  public void run() {
    try {
      long lastUpdateDB = 0l ;
      long updatePeriod =  1 * 24 * 3600 * 1000l ;
      URLCommitMetric commitInfo = null ;
      URLScheduleMetric lastScheduleInfo = null;
      while(!exist) {
        status = URLSchedulerStatus.SCHEDULING ;
        URLScheduleMetric sheduleInfo = preFetchScheduler.schedule() ;
        boolean scheduleInfoIsChanged = sheduleInfo.isChangedCompareTo(lastScheduleInfo);
        if(scheduleInfoIsChanged) {
          reporter.report(sheduleInfo);
          reporter.report(preFetchScheduler.getSiteContextManager().getSiteStatistics());
          lastScheduleInfo = sheduleInfo;
        }
        
        status = URLSchedulerStatus.COMMITTING ;
        commitInfo = postFetchScheduler.process() ;
        reporter.report(commitInfo);
        
        //        if(lastUpdateDB + updatePeriod < System.currentTimeMillis()) {
        //        	URLDatumRecordDB urldatumDB = postFetchScheduler.getURLDatumDB() ;
        //        	URLDatumDBUpdater updater = new URLDatumDBUpdater(postFetchScheduler.getSiteConfigManager()) ;
        //        	urldatumDB.update(updater) ;
        //        	logger.info("\n" + updater.getUpdateInfo()) ;
        //        	lastUpdateDB = System.currentTimeMillis() ;
        //        }
        status = URLSchedulerStatus.IDLE;
        if(scheduleInfoIsChanged) {
          waitForDBChange(5000);
        } else {
          waitForDBChange(60000);
        }
      }
    } catch(Throwable ex) {
      logger.error("URLDatumgFetchScheduler Error", ex) ;
    }
  }
  
  synchronized void notifyDBChange() {
    notifyAll();
  }
  
  synchronized void waitForDBChange(long waitTime) throws InterruptedException {
    wait(waitTime);
  }
  
  public void injectURL() throws Exception {
    URLDatumDBWriter writer = postFetchScheduler.getURLDatumDB().createURLDatumDBWriter();
    
    long currentTime = System.currentTimeMillis() ;
    int count = 0 ;

    SiteContextManager siteContextManager = postFetchScheduler.getSiteContextManager() ;
    Iterator<Map.Entry<String, SiteContext>> i = siteContextManager.getSiteConfigContexts().entrySet().iterator() ;
    while(i.hasNext()) {
      Map.Entry<String, SiteContext> entry = i.next() ;
      SiteContext context = entry.getValue() ;
      SiteConfig.Status status = context.getSiteConfig().getStatus() ;
      if(status != SiteConfig.Status.Ok) continue ;
      String[] url = context.getSiteConfig().getInjectUrl() ;
      for(String selUrl : url) {
        selUrl = selUrl.trim() ;
        if(selUrl.length() == 0) continue ;
        URLInfo newURLParser = new URLInfo(selUrl) ;
        URLDatum datum = writer.createURLDatumInstance(currentTime) ;
        datum.setDeep((byte) 1) ;
        datum.setOriginalUrl(selUrl, newURLParser) ;
        datum.setPageType(URLDatum.PAGE_TYPE_LIST) ;
        writer.write(datum);
        count++ ;
      }
    }
    writer.close() ;
    writer.optimize();
    notifyDBChange();
    logger.info("inject/update " + count + " urls") ;
  }

  public class ManageThread extends Thread {
    public void run() { URLScheduler.this.run() ; }
  }
}