package net.datatp.webcrawler.urldb;


import javax.annotation.PostConstruct;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.Text;
import org.springframework.beans.factory.annotation.Value;

import net.datatp.storage.hdfs.HDFSUtil;
import net.datatp.storage.kvdb.RecordDB;
import net.datatp.storage.kvdb.RecordMerger;
/**
 * Author : Tuan Nguyeni  
 *          tuan08@gmail.com
 * Apr 21, 2010  
 */
public class URLDatumRecordDB extends RecordDB<Text, URLDatumRecord> {
  @Value("${crawler.master.urldb.cleandb}")
  private boolean cleandb ;
  
  @Value("${crawler.master.urldb.dir}")
  private String dbLocation;
  
  public URLDatumRecordDB() {}

  public URLDatumRecordDB(String dbLocation, boolean cleanDb) throws Exception {
    this.dbLocation = dbLocation;
    this.cleandb    = cleanDb;
    onInit();
  }
  
  @PostConstruct
  public void onInit() throws Exception {
    Configuration conf = HDFSUtil.getDaultConfiguration() ;
    FileSystem fs = FileSystem.get(conf) ;
    if(cleandb) HDFSUtil.removeIfExists(fs, dbLocation) ;
    onInit(conf, dbLocation, Text.class, URLDatumRecord.class);
    reload() ;
  }

  public boolean getCleanDB() { return this.cleandb ; }
  public void setCleanDB(boolean b) { cleandb = b ; }

  public URLDatumRecordDB(Configuration configuration, String dblocation) throws Exception {
    super(configuration, dblocation, Text.class, URLDatumRecord.class);
  }

  public Text createKey() { return new Text(); }

  public URLDatumRecord createValue() { return new URLDatumRecord(); }

  protected RecordMerger<URLDatumRecord> createRecordMerger() { return new SegmentRecordMerger() ; }

  static public class SegmentRecordMerger implements RecordMerger<URLDatumRecord> {
    public URLDatumRecord merge(URLDatumRecord r1, URLDatumRecord r2) {
      if(r2.getCreatedTime() <= r1.getCreatedTime()) {
        return r2 ;
      }
      return r1 ;
    }
  }
}