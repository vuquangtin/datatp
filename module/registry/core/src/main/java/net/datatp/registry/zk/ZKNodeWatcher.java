package net.datatp.registry.zk;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import net.datatp.registry.event.NodeEvent;
import net.datatp.registry.event.NodeWatcher;

public class ZKNodeWatcher implements Watcher {
  private String basePath ;
  private NodeWatcher nodeWatcher;
  
  public ZKNodeWatcher(String basePath, NodeWatcher nodeWatcher) {
    this.basePath = basePath ;
    this.nodeWatcher = nodeWatcher;
  }
  
  public void process(WatchedEvent event) {
    if(nodeWatcher.isComplete()) return ;
    String path = event.getPath();
    if(path == null) return;
    NodeEvent nEvent = new NodeEvent(realPath(path), eventType(event)) ;
    try {
      nodeWatcher.onEvent(nEvent);
    } catch (Throwable e) {
      System.err.println("bassePath = " + basePath + ", event path = " + event.getPath());
      e.printStackTrace();
    }
  }
  
  private String realPath(String path) {
    if(path.length() == basePath.length()) return "/" ;
    return path.substring(basePath.length()) ;
  }
  
  private NodeEvent.Type eventType(WatchedEvent event) {
    if(event.getType()      == Watcher.Event.EventType.NodeCreated) return NodeEvent.Type.CREATE ;
    else if(event.getType() == Watcher.Event.EventType.NodeDataChanged) return NodeEvent.Type.MODIFY ;
    else if(event.getType() == Watcher.Event.EventType.NodeDeleted) return NodeEvent.Type.DELETE ;
    else if(event.getType() == Watcher.Event.EventType.NodeChildrenChanged) return NodeEvent.Type.CHILDREN_CHANGED ;
    return NodeEvent.Type.OTHER ;
  }
}