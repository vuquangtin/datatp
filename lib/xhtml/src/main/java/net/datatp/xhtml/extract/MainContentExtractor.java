package net.datatp.xhtml.extract;

import java.util.List;

import net.datatp.xhtml.xpath.XPath;
import net.datatp.xhtml.xpath.XPathRepetion;
import net.datatp.xhtml.xpath.XPathRepetions;
import net.datatp.xhtml.xpath.XPathStructure;
import net.datatp.xhtml.xpath.XPathTree;

public class MainContentExtractor implements WDataExtractor {
  private String type =  "content"; 
  
  public MainContentExtractor() { }
  
  public MainContentExtractor(String type) { this.type = type; }
  
  @Override
  public WDataExtract extract(WDataContext context) {
    XPathStructure structure = context.getXpathStructure();
    XPath titleXPath = structure.findTitleHeaderCandidate();
    
    XPathRepetions xpathRepetitions = structure.getXPathRepetions();
    List<XPathRepetion> foundTextRepetions = 
        xpathRepetitions.findXPathRepetionWithTag("tag:text", "text:small", "text:medium", "text:small");
    XPath bodyXPath = findBody(structure, titleXPath, foundTextRepetions);
    if(bodyXPath == null) return null;

    XPathTree bodyXPathTree = structure.getXPathTree().subTree(bodyXPath);
    bodyXPathTree.removeXPathWithAttr("tag:repetion", new String[] { "link", "link:related" }, true);
    XPath[] bodyXPathAsArray = bodyXPathTree.getXPathAsArray();
    if(bodyXPathAsArray.length < 3 && bodyXPathTree.getText().length() < 500) return null;

    WDataExtract extract = new WDataExtract(type);
    if(titleXPath != null) {
      XPathTree titleXPathTree = structure.getXPathTree().subTree(titleXPath);
      extract.add(new XPathExtract("title", titleXPathTree.getXPathAsArray()));
    }

    extract.add(new XPathExtract("content", bodyXPathAsArray));

    return extract;
  }
  
  public ExtractEntity extractEntity(WDataContext context) {
    XPathStructure structure = context.getXpathStructure();
    XPath titleXPath = structure.findTitleHeaderCandidate();
    
    XPathRepetions xpathRepetitions = structure.getXPathRepetions();
    List<XPathRepetion> foundTextRepetions = 
        xpathRepetitions.findXPathRepetionWithTag("tag:text", "text:small", "text:medium", "text:small");
    XPath bodyXPath = findBody(structure, titleXPath, foundTextRepetions);
    if(bodyXPath == null) return null;
    
    XPathTree bodyXPathTree = structure.getXPathTree().subTree(bodyXPath);
    bodyXPathTree.removeXPathWithAttr("tag:repetion", new String[] { "link", "link:related" }, true);
    XPath[] bodyXPathAsArray = bodyXPathTree.getXPathAsArray();
    if(bodyXPathAsArray.length < 3 &&  bodyXPathTree.getText().length() < 500) return null;
    
    ExtractEntity entity = new ExtractEntity(type, type);

    if(titleXPath != null) {
      XPathTree titleXPathTree = structure.getXPathTree().subTree(titleXPath);
      entity.withTitle(titleXPathTree.getText());
    }

    entity.withContent(bodyXPathTree.getText());
    return entity;
  }

  XPath findBody(XPathStructure structure, XPath titleXPath, List<XPathRepetion> xpathRepetions) {
    if(titleXPath != null) {
      XPath bestCandidateXPath = null;
      for(int i = 0; i < xpathRepetions.size(); i++) {
        XPathRepetion xpathRepetion = xpathRepetions.get(i);
        XPath blockXPath = xpathRepetion.getParentXPath();
        XPath candidateXPath = structure.findClosestAncestor(titleXPath, blockXPath);
        if(bestCandidateXPath == null) {
          bestCandidateXPath = candidateXPath;
        } else {
          if(candidateXPath.getFragment().length > bestCandidateXPath.getFragment().length) {
            bestCandidateXPath = candidateXPath;
          }
        }
      }
      return bestCandidateXPath;
    } else {
      XPathRepetion bestXPathRepetion = null;
      for(int i = 0; i < xpathRepetions.size(); i++) {
        XPathRepetion xpathRepetion = xpathRepetions.get(i);
        if(bestXPathRepetion == null) {
          bestXPathRepetion = xpathRepetion;
        } else if(xpathRepetion.getInfo().getTotalTextLength() > bestXPathRepetion.getInfo().getTotalTextLength()) {
          bestXPathRepetion = xpathRepetion;
        }
      }
      if(bestXPathRepetion == null) return null;
      XPath xpathBody = bestXPathRepetion.getParentXPath();
      return xpathBody;
    }
  }
}
