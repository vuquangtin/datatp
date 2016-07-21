package net.datatp.webcrawler.scheduler;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import net.datatp.channel.ChannelGateway;
import net.datatp.http.crawler.scheduler.URLPreFetchScheduler;
import net.datatp.http.crawler.scheduler.URLSchedulerPluginManager;
import net.datatp.http.crawler.urldb.URLDatum;
import net.datatp.http.crawler.urldb.URLDatumDBIterator;
import net.datatp.http.crawler.urldb.URLDatumDBWriter;
import net.datatp.webcrawler.site.WebCrawlerSiteContextManager;
import net.datatp.webcrawler.urldb.URLDatumRecordDB;
import net.datatp.webcrawler.urldb.URLDatumRecordDBIterator;
import net.datatp.webcrawler.urldb.URLDatumRecordDBWriter;
/**
 * Author : Tuan Nguyen
 *          tuan08@gmail.com
 * Apr 29, 2010  
 */
@ManagedResource(
  objectName="net.datatp.webcrawler.fetcher:name=WebCrawlerURLPreFetchScheduler", 
  description="This bean is responsible to schedule the urls"
)
@Component
public class WebCrawlerURLPreFetchScheduler extends URLPreFetchScheduler {
  @Autowired
  protected URLDatumRecordDB urlDatumDB ;

  @Autowired
  @Qualifier("URLFetchGateway")
  private ChannelGateway  urldatumFetchGateway ;

  
  @Autowired
  public void setSiteContextManager(WebCrawlerSiteContextManager siteContextManager) {
    this.siteContextManager = siteContextManager;
  }

  @Autowired
  public void setURLSchedulerPluginManager(URLSchedulerPluginManager manager) {
    this.schedulerPluginManager = manager;
  }
  
  @ManagedAttribute(
    description = "Maximum frequency of url can schedule per site. " + 
                  "The real max frequency is the frequency of connection multiply by this frequency"
  )
  public int  getMaxSchedulePerSite() { return this.maxSchedulePerSite ; }
  
  @ManagedAttribute(
    description="set the maximum frequency of url can schedule per site.",
    defaultValue="5", currencyTimeLimit=20
  )
  @Value("${crawler.master.scheduler.prefetch.max-per-site}")
  public void setMaxSchedulePerSite(int max) { maxSchedulePerSite = max ; }

  @ManagedAttribute(description="The frequency of time this bean is invoked")
  public int getScheduleCounter() { return this.scheduleCounter ; }

  protected URLDatumDBIterator createURLDatumDBIterator() throws Exception {
    return new URLDatumRecordDBIterator(urlDatumDB);
  }
  
  protected URLDatumDBWriter createURLDatumDBWriter() throws Exception {
    return new URLDatumRecordDBWriter(urlDatumDB);
  }
  
  protected void onSchedule(ArrayList<URLDatum> holder) {
    urldatumFetchGateway.send(holder);
  }
}