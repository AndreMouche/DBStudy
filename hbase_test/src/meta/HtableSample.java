package meta;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.RegexStringComparator;
import org.apache.hadoop.hbase.filter.RowFilter;
import org.apache.hadoop.hbase.util.Bytes;



public class HtableSample {

	
	public static void main(String []args) throws Exception, ZooKeeperConnectionException, IOException {
    	 Configuration config = HBaseConfiguration.create();
    	 HBaseAdmin admin = new HBaseAdmin(config);    	
    	 createTable(admin);
    	 insertData(config);
    	 listTest(config);
    	 list_range(config);
    	 deleteTable(admin);
     }
     
     public static void createTable(HBaseAdmin admin) throws Exception, ZooKeeperConnectionException, IOException {
    	 HTableDescriptor tableDescriper = new HTableDescriptor(TableName.valueOf(MetaData.TABLE_NAME));
    	 tableDescriper.addFamily(new HColumnDescriptor(MetaData.ATTRIBUTE_NAME));
    	 admin.createTable(tableDescriper);
     }
     
     public static void insertData(Configuration config) throws IOException {
    	 HTable table = new HTable(config,MetaData.TABLE_NAME);
    	 String []dirs = {"aa","bb","cc","aa/bb","aa/aa","aa/cc"};
    	 for(String dir:dirs) {
	    	 for(int year = 1000; year < 2000;year ++) {
	    		 MetaData meta = new MetaData(dir+"/" + String.valueOf(year) + "/" + (new Date()).toString());
	    		 Put curMeta = new Put(Bytes.toBytes(meta.key));
	    		 curMeta.add(Bytes.toBytes(MetaData.ATTRIBUTE_NAME),Bytes.toBytes(MetaData.ATT_TIME),Bytes.toBytes(meta.modifyTime));
	    	     table.put(curMeta);
	    	 }
    	 }
    	 table.flushCommits();
    	 table.close();
     }
     
     public static void listTest(Configuration config) throws IOException {
    	 List<String> result = list(config, "", "", "/", 5);
    	 System.out.println("list marker: ,prefix:aa/,delimiter:'/',maxKeys:5");
    	 for(String key:result) {
    		 System.out.println(key);
    	 }
    	 result = list(config, "", "aa/", "/", 5);
    	 System.out.println("list marker: ,prefix:aa/,delimiter:'/',maxKeys:5");
    	 for(String key:result) {
    		 System.out.println(key);
    	 }
    	 
    	 
     }
     public static List<String> list(Configuration config,String marker,String prefix,String delimiter,int maxKeys) throws IOException {
    	 HTable table = new HTable(config, MetaData.TABLE_NAME);
    	 String startKey = marker;
    	 if(prefix.compareTo(startKey) > 0 ) {
    		 startKey = prefix;
    	 }
    	 
    	 String endKey="";
    	 if(prefix.length() > 0) {
    		 endKey = prefix.substring(0,prefix.length()-1) + (char)(prefix.charAt(prefix.length()-1)+1);
    	 }
    	 
    	 char nextDelimiter=' ';
    	 if(delimiter.length()>0) {
    		 nextDelimiter = (char) (delimiter.charAt(0) + 1);
    	 }
    	 
    	 List<String> keys = listKeys(table, prefix, startKey, endKey, delimiter, maxKeys);
    	 if(keys.size() < maxKeys && delimiter.length() > 0 ) {
    		
    		 while(keys.size() < maxKeys) {
    			 String lastKey =  keys.get(keys.size() - 1);
    			 System.out.println("lastKey:" + lastKey);
    			 System.out.println("nextDelimiter:" + nextDelimiter);
    			 System.out.println(lastKey.substring(0,lastKey.length()-1));
    			 startKey = lastKey.substring(0,lastKey.length()-1) + nextDelimiter;
    			 List<String> curKeys = listKeys(table, prefix, startKey, endKey, delimiter, maxKeys - keys.size());
    			 if(curKeys.size() == 0) {
    				 break;
    			 }
    			 keys.addAll(curKeys);
    			 
    		 }
    	 }
    	 return keys;
     }
     
     public static List<String> listKeys(HTable table,String prefix,String startKey,String endKey,String delimiter,int maxKeys) throws IOException {
    	
    	 System.out.println("prefix:" + prefix + " startKey:" + startKey + " endKey:"+endKey + " delimiter:" + delimiter);
    	 List<String> keys = new ArrayList<String>();
    	 Scan scan = new Scan();
    	 if(startKey.length() > 0) {
    		scan.setStartRow(Bytes.toBytes(startKey)); 
    	 } 
    	 
    	 if(endKey.length() > 0) {
    		 scan.setStopRow(Bytes.toBytes(endKey));
    	 }
    	 scan.setCaching(maxKeys);
    	 scan.setSmall(true);
    	 ResultScanner results = table.getScanner(scan);
    	 
    	 for (Result result:results){
    	     String key = new String(result.getRow());
    	     int deId = key.indexOf(delimiter,prefix.length());
    	     if(deId >= 0) {
    	    	keys.add(key.substring(0,deId)+delimiter);
    	    	break;
    	     }
    	     
    	     keys.add(key);
    	     if(keys.size() >= maxKeys) {
    	    	 break;
    	     }
    	 }
    	 
    	 results.close();
    	 
    	 return keys;
     }
     
     public static void list_range(Configuration config) throws IOException {
    	 HTable table = new HTable(config, MetaData.TABLE_NAME);
    	 String start,end;
    	 start = "aa";
    	 end = "bb";
    	 Scan scan = new Scan(Bytes.toBytes(start),Bytes.toBytes(end));
    	 RowFilter rowFilter = new RowFilter(CompareOp.EQUAL, new RegexStringComparator("/aa/1001/*"));
    	 scan.setFilter(rowFilter);
    	 ResultScanner results = table.getScanner(scan);
    	 
    	 for(Result result:results) {
    		 String key = new String(result.getRow());
    		 String value = new String(result.getValue(Bytes.toBytes(MetaData.ATTRIBUTE_NAME), Bytes.toBytes(MetaData.ATT_TIME)));
    	     System.out.println(key+" " + value);
    	 }
    	 
    	 results.close();
    	 table.close();
    	 
    	 
     }
     
     public static void deleteTable(HBaseAdmin admin) throws IOException {
    	 admin.disableTable(MetaData.TABLE_NAME);
    	 admin.deleteTable(MetaData.TABLE_NAME);
     }
}
