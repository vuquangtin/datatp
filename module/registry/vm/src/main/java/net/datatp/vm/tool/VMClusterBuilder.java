package net.datatp.vm.tool;

import java.util.List;

import net.datatp.util.text.TabularFormater;
import net.datatp.vm.VMDescriptor;
import net.datatp.vm.VMStatus;
import net.datatp.vm.client.VMClient;
import net.datatp.vm.command.VMCommand;
import net.datatp.vm.service.VMService;
import net.datatp.registry.SequenceIdTracker;
import net.datatp.registry.event.WaitingNodeEventListener;
import net.datatp.registry.event.WaitingRandomNodeEventListener;

public class VMClusterBuilder {
  protected VMClient vmClient ;
  protected String   localAppHome; 
  
  public VMClusterBuilder(String localAppHome, VMClient vmClient) {
    this.localAppHome = localAppHome;
    this.vmClient = vmClient ;
  }
  
  public VMClient getVMClient() { return this.vmClient ; }
  
  protected void init(VMClient vmClient) {
    this.vmClient = vmClient;
  }
  
  public void clean() throws Exception {
  }
  
  public void start() throws Exception {
    if(!vmClient.getRegistry().isConnect()) {
      vmClient.getRegistry().connect() ;
    }
    
    WaitingNodeEventListener waitingListener = createVMMaster(localAppHome);
    waitingListener.waitForEvents(30000);
    TabularFormater info = waitingListener.getTabularFormaterEventLogInfo();
    info.setTitle("Waiting for vm-master events to make sure it is launched properly");
    System.out.println(info.getFormatText()); 
  }
  
  public void shutdown() throws Exception {
    List<VMDescriptor> list = vmClient.getActiveVMDescriptors() ;
    for(VMDescriptor vmDescriptor : list) {
      vmClient.execute(vmDescriptor, new VMCommand.Shutdown());
    }
  }
  
  public WaitingNodeEventListener createVMMaster(String localAppHome) throws Exception {
    if(!vmClient.getRegistry().isConnect()) {
      vmClient.getRegistry().connect() ;
    }
    
    SequenceIdTracker masterIdTracker= new SequenceIdTracker(vmClient.getRegistry(), VMService.MASTER_ID_TRACKER_PATH, true);
    String vmId = "vm-master-" + masterIdTracker.nextInt();
    
    WaitingNodeEventListener waitingListener = new WaitingRandomNodeEventListener(vmClient.getRegistry()) ;
    String vmStatusPath = VMService.getVMStatusPath(vmId);
    waitingListener.add(vmStatusPath, VMStatus.RUNNING, "Wait for RUNNING status for vm " + vmId, true);
    String vmHeartbeatPath = VMService.getVMHeartbeatPath(vmId);
    waitingListener.addCreate(vmHeartbeatPath, format("Expect %s has connected heartbeat", vmId), true);
    String vmServiceStatusPath = VMService.MASTER_PATH + "/status";
    waitingListener.add(vmServiceStatusPath, VMService.Status.RUNNING, "Wait for VMService RUNNING status ", true);
    h1(format("Create VM master %s", vmId));
    vmClient.createVMMaster(localAppHome, vmId);
    return waitingListener;
  }
  
  static public void h1(String title) {
    System.out.println("\n\n");
    System.out.println("------------------------------------------------------------------------");
    System.out.println(title);
    System.out.println("------------------------------------------------------------------------");
  }
  
  static public void h2(String title) {
    System.out.println(title);
    StringBuilder b = new StringBuilder() ; 
    for(int i = 0; i < title.length(); i++) {
      b.append("-");
    }
    System.out.println(b) ;
  }
  
  static public String format(String tmpl, Object ... args) {
    return String.format(tmpl, args) ;
  }
}
