package net.datatp.registry.util;

import java.io.IOException;

import net.datatp.registry.Node;
import net.datatp.registry.RegistryException;
import net.datatp.util.ExceptionUtil;

abstract public class NodeFormatter {
  String indent = "" ;
  
  public NodeFormatter setIndent(String indent) {
    this.indent = indent ;
    return this;
  }
  
  abstract public String getFormattedText() ;
  
  static public class NodeDataFormater extends NodeFormatter {
    private Node node ;
    
    public NodeDataFormater(Node node, String indent) {
      this.node = node ;
      setIndent(indent);
    }
    
    @Override
    public String getFormattedText() {
      StringBuilder b = new StringBuilder() ;
      try {
        String text = new String(node.getData());
        if(indent != null && indent.length() > 0) {
          String[] array = text.split("\n") ;
          for(int i = 0; i < array.length; i++) {
            b.append(indent + array[i]);
          }
        } else {
          b.append(text);
        }
      } catch (RegistryException e) {
        e.printStackTrace();
      }
      return b.toString();
    }
  }
  
  static public class NodeDumpFormater extends NodeFormatter {
    private Node nodeToDump = null;
      
    public NodeDumpFormater(Node nodeToDump, String indent) {
      this.nodeToDump = nodeToDump ;
      setIndent(indent);
    }
    
    @Override
    public String getFormattedText() {
      StringBuilder b = new StringBuilder() ;
      try {
        nodeToDump.dump(b, indent);
      } catch (RegistryException | IOException e) {
        b.append(ExceptionUtil.getStackTrace(e));
      }
      return b.toString();
    }
  }
}