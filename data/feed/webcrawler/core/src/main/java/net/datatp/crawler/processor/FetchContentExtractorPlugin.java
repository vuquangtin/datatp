package net.datatp.crawler.processor;

import java.util.List;

import net.datatp.crawler.fetcher.FetchContext;
import net.datatp.crawler.site.SiteContext;
import net.datatp.crawler.site.SiteExtractor;
import net.datatp.xhtml.extract.WDataExtractContext;
import net.datatp.xhtml.extract.entity.ExtractEntity;
/**
 * $Author: Tuan Nguyen$ 
 **/
public class FetchContentExtractorPlugin implements FetchProcessorPlugin {
  @Override
  public void process(FetchContext fetchCtx, WDataExtractContext wDataCtx) {
    if(wDataCtx == null) return;
    SiteContext siteContext      = fetchCtx.getURLContext().getSiteContext();
    SiteExtractor siteExtractor  = siteContext.getSiteExtractor();
    List<ExtractEntity> extracts = siteExtractor.extract(wDataCtx);
    fetchCtx.getXDocMapper().setPageType("list");
    if(extracts == null) return;
    
    for(ExtractEntity sel : extracts) {
      fetchCtx.getXDocMapper().addEntity(sel);
    }
    fetchCtx.getXDocMapper().setPageType("detail");
  }
}