package net.datatp.vm.event;

import net.datatp.util.text.TabularFormater;
import net.datatp.vm.VMStatus;
import net.datatp.vm.service.VMService;
import net.datatp.registry.Node;
import net.datatp.registry.Registry;
import net.datatp.registry.RegistryException;
import net.datatp.registry.event.NodeEvent;
import net.datatp.registry.event.NodeEventMatcher;
import net.datatp.registry.event.WaitingOrderNodeEventListener;

public class VMWaitingEventListener {
  protected WaitingOrderNodeEventListener waitingEventListeners ;
  
  public VMWaitingEventListener(Registry registry) throws RegistryException {
    waitingEventListeners = new WaitingOrderNodeEventListener(registry);
  }

  public WaitingOrderNodeEventListener getWaitingNodeEventListener() { return waitingEventListeners; }
  
  public void waitVMServiceStatus(String desc, VMService.Status status) throws Exception {
    waitingEventListeners.add(VMService.MASTER_PATH + "/status", desc, status);
  }
  
  public void waitVMStatus(String desc, String vmName, VMStatus vmStatus) throws Exception {
    String path = VMService.getVMStatusPath(vmName);
    waitingEventListeners.add(path, desc, vmStatus);
  }
  
  public void waitHeartbeat(String desc, String vmName, boolean connected) throws Exception {
    String path = VMService.getVMHeartbeatPath(vmName);
    if(connected) {
      waitingEventListeners.add(path, NodeEvent.Type.CREATE, desc);
    } else {
      waitingEventListeners.add(path, NodeEvent.Type.DELETE, desc);
    }
  }
  
  public void waitVMMaster(String desc, final String vmName) throws Exception {
    waitingEventListeners.add(VMService.MASTER_LEADER_PATH, new VMLeaderNodeEventMatcher(vmName), desc);
  }
  
  public void waitForEvents(long timeout) throws Exception {
    waitingEventListeners.waitForEvents(timeout);
  }
  
  public TabularFormater getTabularFormaterEventLogInfo()  {
    return waitingEventListeners.getTabularFormaterEventLogInfo();
  }
  
  static public class VMLeaderNodeEventMatcher implements NodeEventMatcher {
    private String vmName  ;
    
    public VMLeaderNodeEventMatcher(String vmName) {
      this.vmName = vmName ;
    }
    
    @Override
    public boolean matches(Node node, NodeEvent event) throws Exception {
      if(event.getType() == NodeEvent.Type.MODIFY) {
        Node refNode = node.getRegistry().getRef(node.getPath());
        return refNode.getName().equals(vmName);
      }
      return false;
    }
  }
}