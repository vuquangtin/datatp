package net.datatp.crawler.distributed.registry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.datatp.crawler.distributed.registry.event.CrawlerEventContext;
import net.datatp.crawler.fetcher.metric.URLFetcherMetric;
import net.datatp.zk.registry.RegistryClient;
import net.datatp.zk.registry.event.EventBroadcaster;
import net.datatp.zk.registry.event.EventListener;

public class FetcherRegistry {
  final static public String FETCHER  = "fetcher";
  final static public String EVENTS   = "/events";
  final static public String REPORTS  = "/reports";

  private RegistryClient                     registryClient;
  private EventBroadcaster                   eventBroadcaster;
  private EventListener<CrawlerEventContext> eventListener;

  public FetcherRegistry(RegistryClient client) throws Exception {
    registryClient = client.useNamespace(FETCHER);
    initRegistry();
    eventBroadcaster = new EventBroadcaster(registryClient, EVENTS);
  }
  
  public void initRegistry() throws Exception {
    registryClient.createIfNotExists(EVENTS);
  }
  
  public void onDestroy() throws IOException {
    eventListener.onDestroy();
  }
  
  public EventBroadcaster getEventBroadcaster() { return eventBroadcaster; }

  public void listenToEvent(CrawlerEventContext context) throws Exception {
    eventListener = new EventListener<>(context, registryClient, EVENTS);
  }
  
  public void report(String machine, URLFetcherMetric metric) throws Exception {
    String path = REPORTS + "/" + machine + "/fetcher/" + metric.getName();
    if(!registryClient.exists(path)) {
      registryClient.createIfNotExists(path);
    }
    registryClient.setData(path, metric);
  }
  
  public void initReport(String vmName, List<URLFetcherMetric> metrics) throws Exception {
    String reportPath = REPORTS + "/" + vmName + "/fetcher";
    registryClient.createIfNotExists(reportPath);
    for(URLFetcherMetric metric : metrics) {
      String path = reportPath + "/" + metric.getName();
      if(!registryClient.exists(path)) {
        registryClient.create(path, metric);
      }
    }
  }
  
  public void report(String vmName, List<URLFetcherMetric> metrics) throws Exception {
    String reportPath = REPORTS + "/" + vmName + "/fetcher";
    List<String> names = new ArrayList<>();
    for(URLFetcherMetric sel : metrics) {
      names.add(sel.getName());
    }
    registryClient.createChildren(reportPath, names, metrics);
  }
}
