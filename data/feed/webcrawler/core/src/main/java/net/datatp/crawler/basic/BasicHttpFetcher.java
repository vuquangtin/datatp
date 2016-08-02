package net.datatp.crawler.basic;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import net.datatp.crawler.fetcher.HttpFetcher;
import net.datatp.crawler.fetcher.SiteSessionManager;
import net.datatp.crawler.processor.FetchDataProcessor;
import net.datatp.crawler.site.SiteContextManager;
import net.datatp.crawler.urldb.URLDatum;
import net.datatp.xhtml.XDoc;

public class BasicHttpFetcher extends HttpFetcher {
  private BlockingQueue<URLDatum>  urlFetchQueue;
  private BlockingQueue<URLDatum>  urlCommitQueue;
  private BlockingQueue<XDoc>      xDocQueue;
  private FetchDataProcessor       dataProcessor;
  
  public BasicHttpFetcher(String name, 
                          SiteContextManager manager, 
                          SiteSessionManager siteSessionManager, 
                          BlockingQueue<URLDatum>  urlFetchQueue,
                          BlockingQueue<URLDatum>  urlCommitQueue,
                          BlockingQueue<XDoc>      xDocQueue,
                          FetchDataProcessor       dataProcessor) {
    super(name, manager, siteSessionManager, dataProcessor);
    this.urlFetchQueue  = urlFetchQueue;
    this.urlCommitQueue = urlCommitQueue;
    this.xDocQueue      = xDocQueue;
    this.dataProcessor  = dataProcessor;
  }

  protected void onCommit(ArrayList<URLDatum> holder) throws Exception {
    for(int i = 0; i < holder.size(); i++) {
      urlCommitQueue.offer(holder.get(i), 5, TimeUnit.SECONDS);
    }
  }

  protected void onCommit(XDoc xDoc) throws Exception {
    xDocQueue.offer(xDoc, 5, TimeUnit.SECONDS);
  }

  
  @Override
  protected void onDelay(URLDatum urlDatum) throws InterruptedException {
    urlFetchQueue.offer(urlDatum, 5, TimeUnit.SECONDS);
  }

  @Override
  protected URLDatum nextURLDatum(long maxWaitTime) throws Exception {
    return urlFetchQueue.poll(1, TimeUnit.SECONDS);
  }
}
