package net.datatp.crawler.basic;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.datatp.crawler.CrawlerApi;
import net.datatp.crawler.CrawlerStatus;
import net.datatp.crawler.fetcher.FetcherReport;
import net.datatp.crawler.fetcher.FetcherStatus;
import net.datatp.crawler.fetcher.URLFetchQueue;
import net.datatp.crawler.processor.FetchDataProcessor;
import net.datatp.crawler.processor.URLExtractor;
import net.datatp.crawler.processor.XDocProcessor;
import net.datatp.crawler.scheduler.URLScheduler;
import net.datatp.crawler.scheduler.URLSchedulerStatus;
import net.datatp.crawler.scheduler.metric.URLCommitMetric;
import net.datatp.crawler.scheduler.metric.URLScheduleMetric;
import net.datatp.crawler.site.SiteConfig;
import net.datatp.crawler.site.SiteContextManager;
import net.datatp.crawler.site.SiteStatistic;
import net.datatp.crawler.urldb.InMemURLDatumDB;
import net.datatp.crawler.urldb.URLDatum;
import net.datatp.crawler.urldb.URLDatumDB;
import net.datatp.xhtml.XDoc;

public class Crawler implements CrawlerApi {
  private Logger logger = LoggerFactory.getLogger(getClass());
  
  private CrawlerConfig           crawlerConfig;

  private URLFetchQueue           urlFetchQueue;
  private BlockingQueue<URLDatum> urlCommitQueue;
  private BlockingQueue<XDoc>     xDocQueue;

  private SiteContextManager      siteContextManager = new SiteContextManager();

  private BasicFetcher            fetcher;

  private URLDatumDB              urlDatumDB;
  private URLScheduler            urlScheduler;

  private FetchDataProcessor      dataProcessor;

  private XDocProcessor           xDocProcessor      = XDocProcessor.NONE;
  private XDocProcessorThread     xDocProcessorThread;

  public Crawler configure(CrawlerConfig config) throws Exception {
    return configure(config, new InMemURLDatumDB());
  }
  
  public Crawler configure(CrawlerConfig config, URLDatumDB urlDatumDB) throws Exception {
    logger.info("start configure()");
    crawlerConfig      = config;
    
    urlFetchQueue  = new URLFetchQueue(crawlerConfig.getMaxUrlQueueSize());
    urlCommitQueue = new LinkedBlockingQueue<>(crawlerConfig.getMaxUrlQueueSize());
    xDocQueue      = new LinkedBlockingQueue<>(crawlerConfig.getMaxXDocQueueSize());
    
    URLExtractor urlExtractor = new URLExtractor(urlDatumDB.getURLDatumFactory(), CrawlerConfig.EXCLUDE_URL_PATTERNS);
    dataProcessor = new FetchDataProcessor(urlExtractor);
    
    fetcher = new BasicFetcher(crawlerConfig, urlFetchQueue, urlCommitQueue, xDocQueue, dataProcessor, siteContextManager);
    
    this.urlDatumDB = urlDatumDB;
    urlScheduler = new InMemURLScheduler(this.urlDatumDB, siteContextManager, urlFetchQueue, urlCommitQueue);
    
    xDocProcessorThread = new XDocProcessorThread();
    xDocProcessorThread.start();
    
    logger.info("finish configure()");
    return this;
  }
  
  public void setXDocProcessor(XDocProcessor processor) { 
    xDocProcessor = processor; 
  }
  
  @Override
  public void siteCreateGroup(String group) throws Exception { }

  @Override
  public void siteAdd(SiteConfig ... configs) throws Exception {
    for(int i = 0; i < configs.length; i++) {
      SiteConfig config = configs[i];
      siteContextManager.add(config);
    }
  }
  
  @Override
  public void siteSave(SiteConfig ... configs) throws Exception {
    for(int i = 0; i < configs.length; i++) {
      SiteConfig config = configs[i];
      siteContextManager.save(config);
    }
    logger.info("Save " + configs.length + " site config objects");
  }
  
  @Override
  public String[] siteRemove(String group, String ... site) throws Exception {
    return siteContextManager.remove(group, site);
  }
  
  @Override
  public List<SiteConfig> siteGetSiteConfigs() { return siteContextManager.getSiteConfigs(); }
  
  @Override
  public void siteReload() throws Exception { }
  
  public List<SiteStatistic> siteGetSiteStatistics() throws Exception {
    return siteContextManager.getSiteStatistics();
  }
  
  public URLSchedulerStatus schedulerGetURLSchedulerStatus() {
    return urlScheduler.getStatus();
  }
  
  @Override
  public List<URLCommitMetric> schedulerGetURLCommitReport(int max) throws Exception {
    return urlScheduler.getSchedulerReporter().getURLCommitReport(max);
  }

  @Override
  public List<URLScheduleMetric> schedulerGetURLScheduleReport(int max) throws Exception {
    return urlScheduler.getSchedulerReporter().getURLScheduleReport(max);
  }

  @Override
  public void schedulerStart() throws Exception {
    urlScheduler.injectURL();
    urlScheduler.start();
  }

  @Override
  public void schedulerStop() throws Exception {
    urlScheduler.stop();
  }

  @Override
  public FetcherStatus[] getFetcherStatus() {
    return new FetcherStatus[] { fetcher.getStatus() };
  }
  
  @Override
  public FetcherReport getFetcherReport(String id) throws Exception {
    return fetcher.getFetcherReport();
  }
  
  @Override
  public void fetcherStart() throws Exception {
    fetcher.start();
  }

  @Override
  public void fetcherStop() throws Exception {
    fetcher.stop();
  }
  
  public CrawlerStatus getCrawlerStatus() {
    CrawlerStatus status = new CrawlerStatus();
    status.setUrlSchedulerStatus(schedulerGetURLSchedulerStatus());
    status.setFetcherStatus(getFetcherStatus());
    return status;
  }
  
  @Override
  public void crawlerStart() throws Exception {
    schedulerStart();
    fetcherStart();
  }

  @Override
  public void crawlerStop() throws Exception {
    fetcherStop();
    schedulerStop();
  }
  
  public class XDocProcessorThread extends Thread {
    boolean terminate ;
    
    public void run() {
      while(!terminate) {
        try {
          XDoc xdoc = xDocQueue.poll(3, TimeUnit.SECONDS);
          if(xdoc != null) xDocProcessor.process(xdoc);
        } catch(InterruptedException ex) {
        } catch(Exception ex) {
          ex.printStackTrace();
        }
      }
    }
  }
}
