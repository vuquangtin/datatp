package net.datatp.registry.zk;

import java.util.List;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.OpResult;

import net.datatp.registry.Node;
import net.datatp.registry.NodeCreateMode;
import net.datatp.registry.PathFilter;
import net.datatp.registry.RegistryException;
import net.datatp.registry.Transaction;
import net.datatp.util.dataformat.DataSerializer;

public class TransactionImpl implements Transaction {
  private RegistryImpl registry;
  private org.apache.zookeeper.Transaction zkTransaction ;
  private int opCount ;
  
  public TransactionImpl(RegistryImpl registry, org.apache.zookeeper.Transaction zkTransaction) {
    this.zkTransaction = zkTransaction;
    this.registry = registry;
  }
  
  @Override
  public Transaction create(String path, byte[] data, NodeCreateMode mode) {
    zkTransaction.create(registry.realPath(path), data, RegistryImpl.DEFAULT_ACL, RegistryImpl.toCreateMode(mode));
    opCount++;
    return this;
  }

  @Override
  public <T> Transaction create(String path, T obj, NodeCreateMode mode) {
    byte[] data = null; ;
    if(obj != null) {
      data = DataSerializer.JSON.toBytes(obj);
    } else {
      data = new byte[0] ;
    }
    zkTransaction.create(registry.realPath(path), data, RegistryImpl.DEFAULT_ACL, RegistryImpl.toCreateMode(mode));
    opCount++;
    return this;
  }
  
  @Override
  public <T> Transaction create(Node node, T obj, NodeCreateMode mode) {
    return create(node.getPath(), obj, mode) ;
  }
  
  @Override
  public Transaction createChild(Node node, String name, NodeCreateMode mode) {
    return createChild(node, name, new byte[0], mode);
  }
  
  @Override
  public Transaction createChild(Node node, String name, byte data[], NodeCreateMode mode) {
    return create(node.getPath() + "/" + name, data, mode);
  }
  
  @Override
  public <T> Transaction createChild(Node node, String name, T obj, NodeCreateMode mode) {
    return createChild(node, name, DataSerializer.JSON.toBytes(obj), mode);
  }
  
  @Override
  public Transaction createDescendant(Node node, String relativePath, NodeCreateMode mode) {
    return createDescendant(node, relativePath, new byte[0], mode);
  }
  @Override
  public Transaction createDescendant(Node node, String relativePath, byte data[], NodeCreateMode mode) {
    return create(node.getPath() + "/" + relativePath, data, mode);
  }

  @Override
  public <T> Transaction createDescendant(Node node, String relativePath, T obj , NodeCreateMode mode) {
    return createDescendant(node, relativePath, DataSerializer.JSON.toBytes(obj), mode);
  }
  
  @Override
  public Transaction delete(String path) {
    zkTransaction.delete(registry.realPath(path), -1);
    opCount++;
    return this;
  }

  @Override
  public Transaction rdelete(String path) throws RegistryException {
    List<String> tree = registry.findDencendantRealPaths(path, null);
    for (int i = tree.size() - 1; i >= 0 ; --i) {
      //Delete the leaves first and eventually get rid of the root
      zkTransaction.delete(tree.get(i), -1);
      opCount++;
    }
    return this;
  }
  
  public void rcopy(String path, String toPath) throws RegistryException {
    rcopy(path, toPath, null);
  }
  
  public void rcopy(String path, String toPath, PathFilter filter) throws RegistryException {
    List<String> tree = registry.findDencendantPaths(path, filter);
    for (int i = 0; i < tree.size(); i++) {
      String selPath = tree.get(i);
      String selToPath = selPath.replace(path, toPath);
      byte[] data = registry.getData(selPath) ;
      create(selToPath, data, NodeCreateMode.PERSISTENT) ;
      opCount++;
    }
  }
  
  @Override
  public Transaction check(String path) {
    zkTransaction.check(registry.realPath(path), -1);
    opCount++;
    return this;
  }

  @Override
  public Transaction setData(String path, byte[] data) {
    zkTransaction.setData(registry.realPath(path), data, -1);
    opCount++;
    return this;
  }
  
  @Override
  public <T> Transaction setData(final String path, T obj) {
    zkTransaction.setData(registry.realPath(path), DataSerializer.JSON.toBytes(obj), -1);
    opCount++;
    return this ;
  }
  
  @Override
  public <T> Transaction setData(Node node, byte[] data) {
    setData(node.getPath(), data);
    return this;
  }
  
  @Override
  public <T> Transaction setData(Node node, T obj) {
    setData(node.getPath(), obj);
    return this;
  }

  public Transaction deleteChild(Node node, String name) {
    delete(node.getPath() + "/" + name) ;
    return this ;
  }
  
  public Transaction deleteDescendant(Node node, String relativePath) {
    delete(node.getPath() + "/" + relativePath) ;
    return this ;
  }
  
  @Override
  public void commit() throws RegistryException {
    if(opCount == 0) return ;
    try {
      List<OpResult> results = zkTransaction.commit();
    } catch (InterruptedException  | KeeperException e) {
      throw RegistryImpl.toRegistryException("Commit Error", e);
    }
  }
}
