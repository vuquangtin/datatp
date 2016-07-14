package net.datatp.webcrawler.master;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.hadoop.io.Writable;

import net.datatp.webcrawler.master.model.URLCommitInfo;
import net.datatp.webcrawler.master.model.URLScheduleInfo;

public class CrawlerMasterInfo implements Writable {
  final static public int MAX_INFO_SIZE =  100 ;

  final static public int STOP_STATUS = 0 ;
  final static public int RUNNING_STATUS = 1 ;

  private long startTime ;
  private int  status ;
  private LinkedList<URLScheduleInfo> sheduleInfo = new LinkedList<URLScheduleInfo>() ;
  private LinkedList<URLCommitInfo> fetchDataProcessInfo = new LinkedList<URLCommitInfo>() ;

  public CrawlerMasterInfo() {

  }

  public CrawlerMasterInfo(boolean loadJVMInfo) {
    if(loadJVMInfo) {
    }
  }

  public long getStartTime() { return startTime; }
  public void setStartTime(long startTime) { this.startTime = startTime ; }

  public int getStatus() { return status; }
  public void setStatus(int status) { this.status = status; }

  public String getStatusInfo() {
    if(status == RUNNING_STATUS) return "RUNNING" ;
    return "STOP" ;
  }

  public URLScheduleInfo[] getSheduleInfo() { 
    return sheduleInfo.toArray(new URLScheduleInfo[sheduleInfo.size()]);
  }

  public void addSheduleInfo(URLScheduleInfo info) {
    sheduleInfo.addFirst(info) ;
    if(this.sheduleInfo.size() == MAX_INFO_SIZE) {
      this.sheduleInfo.removeLast() ;
    }
  }

  public URLCommitInfo[] getFetchDataProcessInfo() {
    return fetchDataProcessInfo.toArray(new URLCommitInfo[fetchDataProcessInfo.size()]);
  }

  public void addFetchDataProcessInfo(URLCommitInfo info) {
    fetchDataProcessInfo.addFirst(info);
    if(fetchDataProcessInfo.size() == MAX_INFO_SIZE) {
      fetchDataProcessInfo.removeLast() ;
    }
  }

  public void readFields(DataInput in) throws IOException {
    this.startTime = in.readLong() ;
    this.status = in.readInt() ;
    int size = in.readInt() ;
    for(int i = 0; i < size; i++) {
      URLScheduleInfo info = new URLScheduleInfo();
      info.readFields(in);
      sheduleInfo.add(info) ;
    }
    size = in.readInt() ;
    for(int i = 0; i < size; i++) {
      URLCommitInfo info = new URLCommitInfo();
      info.readFields(in);
      fetchDataProcessInfo.add(info) ;
    }
  }

  public void write(DataOutput out) throws IOException {
    out.writeLong(startTime) ;
    out.writeInt(status) ;

    out.writeInt(this.sheduleInfo.size()) ;
    Iterator<URLScheduleInfo>  scheduleInfoItr = sheduleInfo.iterator() ;
    while(scheduleInfoItr.hasNext()) {
      scheduleInfoItr.next().write(out) ;
    }

    out.writeInt(this.fetchDataProcessInfo.size()) ;
    Iterator<URLCommitInfo> fetchDataProcessInfoItr = fetchDataProcessInfo.iterator() ;
    while(fetchDataProcessInfoItr.hasNext()) {
      fetchDataProcessInfoItr.next().write(out) ;
    }
  }

  public void report(PrintStream out) {
    out.println("############SCHEDULE INFO##################################") ;
    String[] scheduleHeader   =  { "url", "schedule", "delay", "pending", "waiting"	} ;
    int[] scheduleHeaderWidth =  {    10,         10,      10,        10,        10 } ;
    printRow(out, scheduleHeader, scheduleHeaderWidth) ;
    for(URLScheduleInfo sel :  this.sheduleInfo) {
      String[] value = { 
          Integer.toString(sel.getUrlCount()),
          Integer.toString(sel.getScheduleCount()),
          Integer.toString(sel.getDelayScheduleCount()),
          Integer.toString(sel.getPendingCount()),
          Integer.toString(sel.getWaitingCount()),
      } ;
      printRow(out, value, scheduleHeaderWidth) ;
    }
    out.println("############PROCESS INFO##################################") ;
    String[] processHeader   =  { "url", "new url", "new url list", "new url detail"} ;
    int[] processHeaderWidth =  {    10,        10,             15,              15} ;
    printRow(out, processHeader, processHeaderWidth) ;
    for(URLCommitInfo sel : fetchDataProcessInfo) {
      String[] value = { 
          Integer.toString(sel.getCommitURLCount()),
          Integer.toString(sel.getNewURLFoundCount()),
          Integer.toString(sel.getNewURLTypeList()),
          Integer.toString(sel.getNewURLTypeDetail()),
      } ;
      printRow(out, value, processHeaderWidth) ;
    }
  }

  private void printRow(PrintStream out, String[] cell, int[] width) {
    for(int i = 0; i < cell.length; i++) {
      out.print(cell[i]) ;
      for(int j = cell[i].length(); j < width[i]; j++) out.print(' ') ;
    }
    out.println() ;
  }
}