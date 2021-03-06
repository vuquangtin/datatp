package net.datatp.registry.task.switchable;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.datatp.registry.BatchOperations;
import net.datatp.registry.Node;
import net.datatp.registry.NodeCreateMode;
import net.datatp.registry.RefNode;
import net.datatp.registry.Registry;
import net.datatp.registry.RegistryException;
import net.datatp.registry.Transaction;
import net.datatp.registry.lock.Lock;
import net.datatp.registry.notification.Notifier;
import net.datatp.registry.task.TaskExecutorDescriptor;
import net.datatp.registry.task.TaskStatus;
import net.datatp.registry.task.TaskTransactionId;

public class SwitchableTaskRegistry<T> {
  static public Comparator<String> TASK_ID_SEQ_COMPARATOR = new Comparator<String>() {
    @Override
    public int compare(String taskId_1, String taskId_2) {
      int taskIdSeq1 = Integer.parseInt(taskId_1.substring(taskId_1.lastIndexOf('-') + 1));
      int taskIdSeq2 = Integer.parseInt(taskId_2.substring(taskId_2.lastIndexOf('-') + 1));
      return taskIdSeq1 - taskIdSeq2;
    }
  };
  
  final static public String TASK_LIST_PATH          = "task-list";
  final static public String EXECUTIONS_PATH         = "executions";
  final static public String AVAILABLE_PATH          = EXECUTIONS_PATH + "/available";
  final static public String ASSIGNED_PATH           = EXECUTIONS_PATH + "/assigned";
  final static public String ASSIGNED_TASK_ID_PATH   = ASSIGNED_PATH   + "/task-ids";
  final static public String ASSIGNED_HEARTBEAT_PATH = ASSIGNED_PATH   + "/task-heartbeats";
  final static public String FINISHED_PATH           = EXECUTIONS_PATH + "/finished";
  final static public String LOCK_PATH               = EXECUTIONS_PATH + "/lock";
  final static public String NOTIFICATIONS_PATH      = "notifications";
  
  final static public String TASK_STATUS_PATH        = "status";

  private Registry registry ;
  private String   path ;
  private Class<T> taskDescriptorType;
  
  private Node     tasksRootNode ;
  private Node     tasksListNode ;
  
  private Node     executionsNode ;
  private Node     tasksAvailableNode ;
  private Node     tasksAssignedNode ;
  private Node     tasksAssignedIdNode ;
  private Node     tasksAssignedHeartbeatNode ;
  private Node     tasksFinishedNode ;
  private Node     tasksLockNode ; 
  
  private Notifier taskExecutionNotifier ;
  private Notifier taskCoordinationNotifier ;

  public SwitchableTaskRegistry() { }
  
  public SwitchableTaskRegistry(Registry registry, String path, Class<T> taskDescriptorType) throws RegistryException {
    init(registry, path, taskDescriptorType) ;
  }
  
  
  protected void init(Registry registry, String path, Class<T> taskDescriptorType) throws RegistryException {
    this.registry = registry;
    this.path     = path;
    this.taskDescriptorType = taskDescriptorType;
    
    tasksRootNode = registry.get(path) ;
    tasksListNode = tasksRootNode.getDescendant(TASK_LIST_PATH); 
    
    executionsNode = tasksRootNode.getDescendant(EXECUTIONS_PATH);
    
    tasksAvailableNode = tasksRootNode.getDescendant(AVAILABLE_PATH);
    
    tasksAssignedNode = tasksRootNode.getDescendant(ASSIGNED_PATH);
    tasksAssignedIdNode = tasksRootNode.getDescendant(ASSIGNED_TASK_ID_PATH);
    tasksAssignedHeartbeatNode = tasksRootNode.getDescendant(ASSIGNED_HEARTBEAT_PATH);
    
    tasksFinishedNode = tasksRootNode.getDescendant(FINISHED_PATH);
    tasksLockNode = tasksRootNode.getDescendant(LOCK_PATH);
    
    taskExecutionNotifier = new Notifier(registry, path + "/" + NOTIFICATIONS_PATH, "task-execution");
    taskCoordinationNotifier = new Notifier(registry, path + "/" + NOTIFICATIONS_PATH, "task-coordination");
  }
  
  public void initRegistry() throws RegistryException {
    tasksRootNode.createIfNotExists() ;
    tasksListNode.createIfNotExists(); 
    tasksAvailableNode.createIfNotExists();
    tasksAssignedIdNode.createIfNotExists();
    tasksAssignedHeartbeatNode.createIfNotExists();
    tasksFinishedNode.createIfNotExists();
    tasksLockNode.createIfNotExists();
    
    taskExecutionNotifier.initRegistry();
    taskCoordinationNotifier.initRegistry();
  }
  
  public void initRegistry(Transaction transaction) throws RegistryException {
    transaction.create(tasksRootNode, null, NodeCreateMode.PERSISTENT);
    transaction.create(tasksListNode, null, NodeCreateMode.PERSISTENT);
    
    transaction.create(executionsNode, null, NodeCreateMode.PERSISTENT);
    
    transaction.create(tasksAvailableNode, null, NodeCreateMode.PERSISTENT);

    transaction.create(tasksAssignedNode, null, NodeCreateMode.PERSISTENT);
    transaction.create(tasksAssignedIdNode, null, NodeCreateMode.PERSISTENT);
    transaction.create(tasksAssignedHeartbeatNode, null, NodeCreateMode.PERSISTENT);
    
    transaction.create(tasksFinishedNode, null, NodeCreateMode.PERSISTENT);
    transaction.create(tasksLockNode, null, NodeCreateMode.PERSISTENT);
    
    transaction.create(path + "/" + NOTIFICATIONS_PATH, null, NodeCreateMode.PERSISTENT);
    taskExecutionNotifier.initRegistry(transaction);
    taskCoordinationNotifier.initRegistry(transaction);
  }
  
  public Registry getRegistry() { return registry; }

  public String getPath() { return path; }
  
  public Node getTasksRootNode() { return tasksRootNode; }
  
  public Node getTasksListNode() { return tasksListNode; }

  public Node getTasksAvailableNode() { return tasksAvailableNode; }

  public Node getTasksAssignedNode() { return tasksAssignedIdNode; }
  
  public Node getTasksAssignedHeartbeatNode() { return tasksAssignedHeartbeatNode; }
  
  public Node getTasksFinishedNode() { return tasksFinishedNode; }

  public Notifier getTaskExecutionNotifier() { return this.taskExecutionNotifier ; }
  
  public Notifier getTaskCoordinationNotifier() { return this.taskCoordinationNotifier ; }
  
  public T getTaskDescriptor(String taskId) throws RegistryException {
    return tasksListNode.getChild(taskId).getDataAs(taskDescriptorType) ;
  }
  
  public TaskStatus getTaskStatus(String taskId) throws RegistryException {
    return tasksListNode.getChild(taskId).getChild(TASK_STATUS_PATH).getDataAs(TaskStatus.class) ;
  }
  
  public void offer(String taskId, T taskDescriptor) throws RegistryException {
    Transaction transaction = registry.getTransaction() ;
    transaction.createChild(tasksListNode, taskId, taskDescriptor, NodeCreateMode.PERSISTENT);
    transaction.createDescendant(tasksListNode, taskId + "/" + TASK_STATUS_PATH, TaskStatus.INIT, NodeCreateMode.PERSISTENT);
    transaction.createChild(tasksAvailableNode, taskId + "-", NodeCreateMode.PERSISTENT_SEQUENTIAL);
    transaction.commit();
  }
  
  public SwitchableTaskContext<T> take(final TaskExecutorDescriptor executor) throws RegistryException {
    BatchOperations<SwitchableTaskContext<T>> takeOp = new BatchOperations<SwitchableTaskContext<T>>() {
      @Override
      public SwitchableTaskContext<T> execute(Registry registry) throws RegistryException {
        List<String> availableTasks = tasksAvailableNode.getChildren() ;
        if(availableTasks.size() == 0) return null ;
        Collections.sort(availableTasks, TASK_ID_SEQ_COMPARATOR);
        String taskIdSeq = availableTasks.get(0) ;
        String taskId = taskIdSeq.substring(0, taskIdSeq.lastIndexOf('-'));
        try {
          Node taskNode = tasksListNode.getChild(taskId) ;
          Transaction transaction = registry.getTransaction();
          TaskTransactionId taskTransactionID = new TaskTransactionId(taskId, Math.abs(transaction.hashCode()) + "");
          
          transaction.setData(taskNode.getChild(TASK_STATUS_PATH), TaskStatus.PROCESSING);
          transaction.createChild(tasksAssignedIdNode, taskTransactionID.getTaskTransactionId(), NodeCreateMode.PERSISTENT);
          transaction.createChild(tasksAssignedHeartbeatNode, taskTransactionID.getTaskTransactionId(), executor, NodeCreateMode.EPHEMERAL);
          transaction.deleteChild(tasksAvailableNode, taskIdSeq);
          transaction.commit();
          SwitchableTaskContext<T> taskContext = createTaskContext(taskTransactionID, null) ;
          return taskContext;
        } catch(Exception ex) {
          String errorMessage = "Fail to grab task " + taskId + " for the executor " + executor.getId();
          StringBuilder registryDump = new StringBuilder() ;
          try {
            tasksAssignedIdNode.getParentNode().dump(registryDump);
          } catch (IOException e) {
          }
          errorMessage += "\n" + registryDump.toString();
          taskExecutionNotifier.warn("fail-to-grab-a-task ", errorMessage, ex);
          throw ex;
        }
      }
    };
    try {
      Lock lock = tasksLockNode.getLock("write", "Lock to grab a task for the executor " + executor.getId()) ;
      return lock.execute(takeOp, 3, 3000);
    } catch(RegistryException ex) {
      String errorMessage = "Fail to assign the task after 3 tries";
      taskExecutionNotifier.error("fail-to-grab-a-task", errorMessage, ex);
      throw ex;
    }
  }
  
  public void suspend(final String executorRef, TaskTransactionId taskTransactionID) throws RegistryException {
    suspend(executorRef, taskTransactionID, false);
  }
  
  public void suspend(final String executorRef, final TaskTransactionId taskTransactionID, final boolean disconnectHeartbeat) throws RegistryException {
    BatchOperations<Boolean> suspendtOp = new BatchOperations<Boolean>() {
      @Override
      public Boolean execute(Registry registry) throws RegistryException {
        try {
          Node taskNode = tasksListNode.getChild(taskTransactionID.getTaskId()) ;
          Transaction transaction = registry.getTransaction();
          transaction.setData(taskNode.getChild(TASK_STATUS_PATH), TaskStatus.SUSPENDED);
          transaction.deleteChild(tasksAssignedIdNode, taskTransactionID.getTaskTransactionId()) ;
          if(!disconnectHeartbeat) {
            transaction.deleteChild(tasksAssignedHeartbeatNode, taskTransactionID.getTaskTransactionId()) ;
          }
          transaction.createChild(tasksAvailableNode, taskTransactionID.getTaskId() + "-", NodeCreateMode.PERSISTENT_SEQUENTIAL) ;
          transaction.commit();
          return true;
        } catch(RegistryException ex) {
          String errorMessage = "Fail to suspend the task " + taskTransactionID.getTaskTransactionId();
          StringBuilder registryDump = new StringBuilder() ;
          try {
            tasksAssignedIdNode.getParentNode().dump(registryDump);
          } catch (IOException e) {
          }
          errorMessage += "\n" + registryDump.toString();
          taskExecutionNotifier.warn("fail-to-suspend-dataflow-task", errorMessage, ex);
          throw ex ;
        }
      }
    };
    try {
      Lock lock = tasksLockNode.getLock("write", "Lock to move the task " + taskTransactionID.getTaskTransactionId() + " to suspend by " + executorRef) ;
      lock.execute(suspendtOp, 3, 5000);
    } catch(RegistryException ex) {
      String errorMessage = "Fail to suspend the task " + taskTransactionID.getTaskTransactionId();
      taskExecutionNotifier.error("fail-to-suspend-dataflow-task", errorMessage, ex);
      throw ex;
    }
  }
  
  public void finish(final String executorRef, final TaskTransactionId taskTransactionID) throws RegistryException {
    BatchOperations<Boolean> commitOp = new BatchOperations<Boolean>() {
      @Override
      public Boolean execute(Registry registry) throws RegistryException {
        try {
          Node taskNode = tasksListNode.getChild(taskTransactionID.getTaskId()) ;
          Transaction transaction = registry.getTransaction();
          //update the task descriptor
          transaction.setData(taskNode.getChild(TASK_STATUS_PATH), TaskStatus.TERMINATED);
          transaction.createChild(tasksFinishedNode, taskTransactionID.getTaskId(), NodeCreateMode.PERSISTENT);
          transaction.deleteChild(tasksAssignedIdNode, taskTransactionID.getTaskTransactionId());
          transaction.deleteChild(tasksAssignedHeartbeatNode, taskTransactionID.getTaskTransactionId());
          transaction.commit();
          return true;
        } catch(RegistryException ex) {
          String errorMessage = "Fail to finish the task " + taskTransactionID.getTaskTransactionId();
          taskExecutionNotifier.warn("fail-to-finish-a-task", errorMessage, ex);
          throw ex;
        }
      }
    };
    try {
      Lock lock = tasksLockNode.getLock("write", "Lock to move the task " + taskTransactionID.getTaskTransactionId() + " to finish by " + executorRef) ;
      lock.execute(commitOp, 3, 3000);
    } catch(RegistryException ex) {
      String errorMessage = "Fail to finish the task " + taskTransactionID.getTaskTransactionId();
      taskExecutionNotifier.warn("fail-to-finish-a-task", errorMessage, ex);
      throw ex;
    }
  }
  
  public SwitchableTaskContext<T> createTaskContext(String taskTransactionId) throws RegistryException {
    return createTaskContext(new TaskTransactionId(taskTransactionId), null) ;
  }
  
  public SwitchableTaskContext<T> createTaskContext(TaskTransactionId taskTransactionId, T taskDescriptor) throws RegistryException {
    SwitchableTaskContext<T> taskContext = new SwitchableTaskContext<T>(this, taskTransactionId, taskDescriptor);
    return taskContext;
  }
}