package meta;

import java.io.IOException;
import java.io.InterruptedIOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableExistsException;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;

public class HtableClient {
	private Configuration config;
	private HBaseAdmin admin;
	private HTable table;

	public HtableClient() throws Exception {
		config = HBaseConfiguration.create();
		admin = new HBaseAdmin(config);
		createTable(admin);
	}

	public void createTable(HBaseAdmin admin) throws Exception,
			ZooKeeperConnectionException, IOException {
		try {
			HTableDescriptor tableDescriper = new HTableDescriptor(
					TableName.valueOf(MetaData.TABLE_NAME));
			tableDescriper.addFamily(new HColumnDescriptor(
					MetaData.ATTRIBUTE_NAME));
			admin.createTable(tableDescriper);
		} catch (TableExistsException e) {
			System.out.println("System already exists");
		}
		table = new HTable(config, MetaData.TABLE_NAME);
	}

	public void putData(String key) throws IOException, InterruptedIOException {
		MetaData meta = new MetaData(key);
		Put curMeta = new Put(Bytes.toBytes(meta.key));
		curMeta.add(Bytes.toBytes(MetaData.ATTRIBUTE_NAME),
				Bytes.toBytes(MetaData.ATT_TIME),
				Bytes.toBytes(meta.modifyTime));
		table.put(curMeta);
	}

	public void delete(String key) throws IOException {
		Delete dI = new Delete(Bytes.toBytes(key));
		table.delete(dI);
	}

	public void Destroy() throws IOException {
		table.close();
		admin.disableTable(MetaData.TABLE_NAME);
		admin.deleteTable(MetaData.TABLE_NAME);
	}

	public ListResult list(String marker, String prefix, String delimiter,
			int maxKeys) throws IOException {
		delimiter = delimiter.trim();
		ListResult listResult = new ListResult(prefix, marker, delimiter,
				maxKeys);
		
		String startKey = marker;
		if (prefix.compareTo(startKey) > 0) {
			startKey = prefix;
		}

		String endKey = "";
		if (prefix.length() > 0) {
			endKey = prefix.substring(0, prefix.length() - 1)
					+ (char) (prefix.charAt(prefix.length() - 1) + 1);
		}

		char nextDelimiter = ' ';
		if (delimiter.length() > 0) {
			nextDelimiter = (char) (delimiter.charAt(0) + 1);
		}

		String lastKey = listKeys(startKey, endKey, listResult);
		if (listResult.size() > 0 && listResult.size() < maxKeys
				&& delimiter.length() > 0) {

			while (listResult.size() < maxKeys) {
				int deId = lastKey.indexOf(delimiter, prefix.length());
				if(deId >  0) {
					startKey = lastKey.substring(0,deId) + nextDelimiter;
				} else {
					startKey = lastKey + nextDelimiter;
				}
				lastKey = listKeys(startKey, endKey, listResult);
				if(lastKey.length() == 0 ) {
					break;
				}
			}
			
		}
		
		
		return listResult;
	}

	public String listKeys(String startKey, String endKey, ListResult listResult)
			throws IOException {

		System.out.println(listResult);
		System.out.println("prefix:" + listResult.getPrefix() + " startKey:"
				+ startKey + " endKey:" + endKey + " delimiter:"
				+ listResult.getDelimiter());

		Scan scan = new Scan();
		if (startKey.length() > 0) {
			scan.setStartRow(Bytes.toBytes(startKey));
		}

		if (endKey.length() > 0) {
			scan.setStopRow(Bytes.toBytes(endKey));
		}
		scan.setCaching(listResult.getMaxKeys() - listResult.size()+1);
		scan.setSmall(true);
		ResultScanner results = table.getScanner(scan);

		String lastKey = "";
		for (Result result : results) {
			
			String key = new String(result.getRow());
			if(listResult.size() >= listResult.getMaxKeys()) {
				listResult.setIsTrancated(true);
			    listResult.setNextMarker(key);	
			    break;
			}
			
			if (listResult.getDelimiter().length() == 0) {
				listResult.addKey(key);
			} else {
				int deId = key.indexOf(listResult.getDelimiter(), listResult
						.getPrefix().length());
				if (deId >= 0) {
					String commonPrefix = key.substring(0, deId) + listResult.getDelimiter();
					listResult.addCommonPrefix(commonPrefix);
					lastKey = key;
					break;
				}else {
					listResult.addKey(key);
				}
			}

			lastKey = key;
		}
		

		results.close();

		return lastKey;
	}

}
