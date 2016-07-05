package net.datatp.webcrawler.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.datatp.util.URLParser;
import net.datatp.webcrawler.urldb.URLContext;
import net.datatp.webcrawler.urldb.URLDatum;
import net.datatp.xhtml.XhtmlLink;
import net.datatp.xhtml.dom.TDocument;
import net.datatp.xhtml.dom.TNodeUtil;
import net.datatp.xhtml.dom.visitor.ExtractLinkVisitor;
import net.datatp.xhtml.util.URLRewriter;
import net.datatp.xhtml.util.URLSessionIdCleaner;
/**
 * Author : Tuan Nguyen tuan.nguyen@headvances.com Jun 23, 2010
 */
public class URLExtractor {
  final static URLSessionIdCleaner URL_CLEANER = new URLSessionIdCleaner() ;
  private static final Logger logger = LoggerFactory.getLogger(URLExtractor.class);

  private List<String> excludePatterns;

  private Pattern[]    excludePatternMatchers         ;
  private URLRewriter  urlRewriter = new URLRewriter();

  public URLExtractor() {
  }

  public void onInit() {
    if (excludePatterns != null && excludePatterns.size() > 0) {
      excludePatternMatchers = new Pattern[excludePatterns.size()];
      for (int i = 0; i < excludePatternMatchers.length; i++) {
        excludePatternMatchers[i] = Pattern.compile(excludePatterns.get(i));
      }
    }
  }

  public void addExcludePattern(String string) {
    if (excludePatterns == null)
      excludePatterns = new ArrayList<String>();
    excludePatterns.add(string);
  }

  public void setExcludePatterns(List<String> list) {
    this.excludePatterns = list;
  }

  public Map<String, URLDatum> extract(URLDatum urldatum, URLContext context, TDocument doc) {
    Map<String, URLDatum> urls = new HashMap<String, URLDatum>();
    try {
      if(context == null) return urls ;
      String siteURL = context.getUrlNormalizer().getSiteURL();
      String baseURL = TNodeUtil.getBase(doc.getRoot());
      if (baseURL == null || baseURL.length() == 0) {
        baseURL = context.getUrlNormalizer().getBaseURL();
      }
      ExtractLinkVisitor linkSelector = new ExtractLinkVisitor();
      doc.getRoot().visit(linkSelector) ;
      List<XhtmlLink> links = linkSelector.getLinks();

      if(urldatum.getDeep() == 1) {
        String refreshUrl = TNodeUtil.findRefreshMetaNodeUrl(doc.getRoot()) ;
        if(refreshUrl != null) {
          links.add(new XhtmlLink("refresh url", refreshUrl)) ;
        }
      }

      for (int i = 0; i < links.size(); i++) {
        XhtmlLink link = links.get(i);
        String anchorText = link.getAnchorText();
        if(link.getDeep() > 1 && (anchorText == null || anchorText.length() == 0)) {
          continue;
        }
        String newURL = link.getURL();
        newURL = urlRewriter.rewrite(siteURL, baseURL, newURL);
        if (!isAllowProtocol(newURL)) continue;
        URLParser newURLNorm = new URLParser(newURL);
        URL_CLEANER.process(newURLNorm) ;
        String newNormalizeURL = newURLNorm.getNormalizeURL();

        if (newURLNorm.getRef() != null) continue;

        if (!context.getSiteContext().allowURL(newURLNorm)) {
          continue; // ignore the external link
        }

        if (isExclude(newURLNorm.getPathWithParams())) {
          continue;
        }

        // CONTROL DEEP LIMIT
        int maxCrawlDeep = context.getSiteContext().getSiteConfig().getCrawlDeep();

        URLDatum newURLDatum = createURLDatum(urldatum, newNormalizeURL, newURLNorm, anchorText);
        if (!isInDeepRange(newURLDatum, maxCrawlDeep)) {
          continue;
        }
        addURL(urls, newNormalizeURL, newURLDatum);
      }
    } catch (Throwable t) {
      logger.error("Cannot extract url for " + urldatum.getFetchUrl(), t);
    }
    return urls;
  }

  private void addURL(Map<String, URLDatum> urls, String url, URLDatum datum) {
    URLDatum exist = urls.get(url) ;
    if(exist == null) {
      urls.put(url, datum);
    } else {
      if(datum.getAnchorText().getLength() > exist.getAnchorText().getLength()) {
        urls.put(url, datum);
      }
    }
  }

  private boolean isAllowProtocol(String url) {
    if (url.startsWith("http://")) return true ;
    if (url.startsWith("https://")) return true;
    return false;
  }

  private boolean isExclude(String path) {
    if (excludePatternMatchers == null) return false;
    for (Pattern sel : excludePatternMatchers) {
      if (sel.matcher(path).matches()) return true;
    }
    return false;
  }

  private boolean isInDeepRange(URLDatum datum, int maxDeep) {
    // if(datum.getPageType() == URLDatum.PAGE_TYPE_DETAIL) return
    // datum.getDeep() <= maxDeep ;
    // else return datum.getDeep() < maxDeep ;
    return datum.getDeep() <= maxDeep;
  }

  private URLDatum createURLDatum(URLDatum parent, String origUrl, URLParser urlNorm, String anchorText) {
    URLDatum urlDatum = new URLDatum(System.currentTimeMillis());
    urlDatum.setOriginalUrl(origUrl, urlNorm);
    byte deep = (byte) (1 + parent.getDeep());
    urlDatum.setDeep(deep);
    urlDatum.setAnchorText(anchorText);
    return urlDatum;
  }
}