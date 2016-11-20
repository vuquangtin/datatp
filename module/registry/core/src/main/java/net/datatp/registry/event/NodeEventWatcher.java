package net.datatp.registry.event;

import net.datatp.registry.ErrorCode;
import net.datatp.registry.Registry;
import net.datatp.registry.RegistryException;

abstract public class NodeEventWatcher extends NodeWatcher {
  private Registry registry;
  private boolean persistent ;
  private String  watchedPath = null ;
  
  public NodeEventWatcher(Registry registry, boolean persistent) {
    this.registry   = registry;
    this.persistent = persistent;
  }
  
  public Registry getRegistry() { return this.registry; }
  
  public void watch(String path) throws RegistryException {
    if(registry.exists(path)) {
      watchModify(path);
      return;
    }
    watchExists(path);
  }
  
  public void watchModify(String path) throws RegistryException {
    if(watchedPath != null) {
      throw new RegistryException(ErrorCode.Unknown, "Already watched " + watchedPath) ;
    }
    registry.watchModify(path, this);
    watchedPath = path;
  }
  
  public void watchExists(String path) throws RegistryException {
    if(watchedPath != null) {
      throw new RegistryException(ErrorCode.Unknown, "Already watched " + watchedPath) ;
    }
    registry.watchExists(path, this);
    watchedPath = path;
  }
  
  @Override
  public void onEvent(NodeEvent event) {
    try {
      processNodeEvent(event);
    } catch(Exception ex) {
      ex.printStackTrace();
    }
    if(persistent) {
      try {
        if(isComplete()) return;
        registry.watchModify(event.getPath(), this);
      } catch(RegistryException ex) {
        if(ex.getErrorCode() != ErrorCode.NoNode) {
          System.err.println("watch " + event.getPath() + ": " + ex.getMessage());
        } else {
          System.err.println("Stop watching " + event.getPath() + " due to the error: " + ex.getMessage());
        }
      }
    }
  }
  
  abstract public void processNodeEvent(NodeEvent event) throws Exception ;
}