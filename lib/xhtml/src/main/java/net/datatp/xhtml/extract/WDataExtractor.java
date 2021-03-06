package net.datatp.xhtml.extract;

import java.util.ArrayList;
import java.util.List;

public interface WDataExtractor {
  public  WDataExtract extract(WDataContext context) ;
  
  public ExtractEntity extractEntity(WDataContext context) ;
  
  static public List<WDataExtract> extract(WDataContext context, WDataExtractor ... extractor) {
    List<WDataExtract> holder = new ArrayList<>();
    for(int i = 0; i < extractor.length; i++) {
      WDataExtract extract = extractor[i].extract(context);
      if(extract != null) holder.add(extract);
    }
    if(holder.size() == 0) return null;
    return holder;
  }
  
  static public List<ExtractEntity> extractEntity(WDataContext context, WDataExtractor ... extractor) {
    List<ExtractEntity> holder = new ArrayList<>();
    for(int i = 0; i < extractor.length; i++) {
      ExtractEntity entity = extractor[i].extractEntity(context);
      if(entity != null) holder.add(entity);
    }
    if(holder.size() == 0) return null;
    return holder;
  }
}
