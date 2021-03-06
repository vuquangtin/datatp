package net.datatp.xhtml.extract;

import java.util.ArrayList;
import java.util.List;

public class WDataExtract {
  private String             type;
  private List<XPathExtract> xpathExtracts = new ArrayList<>();

  public WDataExtract(String name) {
    this.type = name;
  }

  public String getType() { return type; }
  public void setName(String name) { this.type = name; }

  public List<XPathExtract> getXPathExtracts() { return xpathExtracts; }
  public void setXpathExtracts(List<XPathExtract> xpathExtracts) { this.xpathExtracts = xpathExtracts; }
  
  public void add(XPathExtract xpathExtract) {
    if(xpathExtract == null) return;
    xpathExtracts.add(xpathExtract);
  }
  
  public String getFormattedText() {
    StringBuilder b = new StringBuilder();
    b.append("Type: " + type).append("\n") ;
    for(int i = 0; i < xpathExtracts.size(); i ++) {
      b.append(xpathExtracts.get(i).getFormattedText()).append("\n");
    }
    return b.toString();
  }
  
  static public String format(List<WDataExtract> holder) {
    if(holder == null) return "No Extract";
    StringBuilder b = new StringBuilder();
    for(int i = 0; i < holder.size(); i++) {
      b.append(holder.get(i).getFormattedText()).append("\n");
    }
    return b.toString();
  }
}