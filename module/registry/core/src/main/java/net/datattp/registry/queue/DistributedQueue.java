package net.datattp.registry.queue;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import net.datatp.util.dataformat.DataSerializer;
import net.datattp.registry.NodeCreateMode;
import net.datattp.registry.Registry;
import net.datattp.registry.RegistryException;
import net.datattp.registry.Transaction;
import net.datattp.registry.event.NodeEvent;
import net.datattp.registry.event.NodeWatcher;

/**
 * This queue implement the java.util.Queue api with the take method
 * @author Tuan
 */
public class DistributedQueue {
  private final String path;
  private Registry     registry;
  private LatchChildWatcher takeAvailableWatcher;
  private boolean shutdown = false;

  public DistributedQueue(Registry registry, String path) throws RegistryException {
    this(registry, path, true);
  }
  
  public DistributedQueue(Registry registry, String path, boolean initRegistry) throws RegistryException {
    this.path = path;
    this.registry = registry;
    if(initRegistry) {
      initRegistry();
    }
  }

  public String getPath() { return this.path ; }
  
  public Registry getRegistry() { return this.registry; }

  public void initRegistry() throws RegistryException {
    registry.createIfNotExist(path) ;
  }
  
  /**
   * Inserts the specified element into this queue if it is possible to do
   * so immediately without violating capacity restrictions.
   * When using a capacity-restricted queue, this method is generally
   * preferable to {@link #add}, which can fail to insert an element only
   * by throwing an exception.
   * @throws RegistryException 
   */
  public void offer(byte[] data) throws RegistryException {
    registry.create(path + "/", data, NodeCreateMode.PERSISTENT_SEQUENTIAL);
  }
  
  public void offer(Transaction transaction, byte[] data) throws RegistryException {
    transaction.create(path + "/", data, NodeCreateMode.PERSISTENT_SEQUENTIAL);
  }
  
  public <T> void offerAs(Transaction transaction, T object) throws RegistryException {
    byte[] data = DataSerializer.JSON.toBytes(object);
    transaction.create(path + "/", data, NodeCreateMode.PERSISTENT_SEQUENTIAL);
  }
  
  public <T> void offerAs(T object) throws RegistryException {
    byte[] data = DataSerializer.JSON.toBytes(object);
    offer(data);
  }

  /**
   * Retrieves and removes the head of this queue.  This method differs
   * from {@link #poll poll} only in that it throws an exception if this
   * queue is empty.
   */
  public byte[] remove() throws RegistryException, Exception  {
    byte[] data = poll();
    if(data == null) throw new Exception("The queue is empty") ;
    return data;
  }

  public <T> T removeAs(Class<T> type) throws RegistryException, Exception  {
    byte[] data = remove();
    return DataSerializer.JSON.fromBytes(data, type);
  }
  
  /**
   * Retrieves and removes the head of this queue, or returns <tt>null</tt> if this queue is empty.
   */
  public byte[] poll() throws RegistryException {
    List<String> orderedChildren = orderedChildren();
    if(orderedChildren.size() == 0) return null ;
    String headChild = orderedChildren.get(0) ;
    String headChildPath = path + "/" + headChild ;
    byte[] data = registry.getData(headChildPath) ;
    registry.delete(headChildPath);
    return data;
  }
  
  public <T> T pollAs(Class<T> type) throws RegistryException {
    byte[] data = poll();
    return DataSerializer.JSON.fromBytes(data, type);
  }
  
  /**
   * Retrieves, but does not remove, the head of this queue.  This method differs from {@link #peek peek} 
   * only in that it throws an exception if this queue is empty.
   */
  public byte[] element() throws RegistryException {
    byte[] data = peek() ;
    return data;
  }

  public <T> T elementAs(Class<T> type) throws RegistryException {
    byte[] data = element();
    if(data == null || data.length == 0) return null;
    return DataSerializer.JSON.fromBytes(data, type);
  }
  
  /**
   * Retrieves, but does not remove, the head of this queue, or returns <tt>null</tt> if this queue is empty.
   */
  public byte[] peek() throws RegistryException {
    List<String> orderedChildren = orderedChildren();
    if(orderedChildren.size() == 0) return null ;
    String headChild = orderedChildren.get(0) ;
    String headChildPath = path + "/" + headChild ;
    byte[] data = registry.getData(headChildPath) ;
    return data;
  }
  
  /**
   * This method suppose to wait if the queue is empty and return when the queue entry is available
   * @return
   * @throws RegistryException
   * @throws InterruptedException
   */
  public byte[] take() throws RegistryException, ShutdownException, InterruptedException {
    List<String> orderedChildren = orderedChildren();
    while(orderedChildren.size() == 0){
      if(shutdown) {
        throw new ShutdownException("The queue has been shut down") ;
      }
      takeAvailableWatcher = new LatchChildWatcher();
      registry.watchChildren(path, takeAvailableWatcher);
      takeAvailableWatcher.await();
      orderedChildren = orderedChildren();
    }
    String headNode = orderedChildren.get(0);
    String headNodePath = path +"/"+headNode;
    byte[] data = registry.getData(headNodePath);
    registry.delete(headNodePath);
    return data;
  }
  
  /**
   * This method suppose to wait if the queue is empty and return when the queue entry is available
   * @return
   * @throws RegistryException
   * @throws InterruptedException
   */
  public byte[] take(long timeout) throws RegistryException, ShutdownException, InterruptedException {
    List<String> orderedChildren = orderedChildren();
    long stopTime = System.currentTimeMillis() + timeout;
    while(orderedChildren.size() == 0) {
      if(shutdown) {
        throw new ShutdownException("The queue has been shut down") ;
      }
      takeAvailableWatcher = new LatchChildWatcher();
      registry.watchChildren(path, takeAvailableWatcher);
      long waitTime = stopTime - System.currentTimeMillis() ;
      if(waitTime <= 0) return null;
      takeAvailableWatcher.await(waitTime);
      orderedChildren = orderedChildren();
    }
    
    String headNode = orderedChildren.get(0);
    String headNodePath = path +"/"+headNode;
    byte[] data = registry.getData(headNodePath);
    registry.delete(headNodePath);
    return data;
  }
  
  public <T> T takeAs(Class<T> type) throws RegistryException, InterruptedException, ShutdownException {
    byte[] data = take() ;
    return DataSerializer.JSON.fromBytes(data, type);
  }
  
  public <T> T takeAs(Class<T> type, long timeout) throws RegistryException, InterruptedException, ShutdownException {
    byte[] data = take(timeout) ;
    if(data == null) return null ;
    return DataSerializer.JSON.fromBytes(data, type);
  }
  
  public void shutdown() {
    shutdown = true;
    if(takeAvailableWatcher.latch.getCount() > 0) {
      takeAvailableWatcher.latch.countDown();
    }
  }
  
  /**
   * Returns a List of the children, ordered by id.
   */
  private List<String> orderedChildren() throws RegistryException  {
    List<String> orderedChildren = registry.getChildren(path);
    Collections.sort(orderedChildren);
    return orderedChildren;
  }
  
  private class LatchChildWatcher extends NodeWatcher {

    CountDownLatch latch;

    public LatchChildWatcher(){
      latch = new CountDownLatch(1);
    }

    @Override
    public void onEvent(NodeEvent event) {
      latch.countDown();
    }
    
    public void await() throws InterruptedException {
      latch.await();
    }
    
    public void await(long timeout) throws InterruptedException {
      latch.await(timeout, TimeUnit.MILLISECONDS);
    }
  }
  
  static public class ShutdownException extends Exception {
    private static final long serialVersionUID = 1L;

    public ShutdownException(String message) {
      super(message);
    }
  }
}