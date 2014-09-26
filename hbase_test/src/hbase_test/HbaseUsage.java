package hbase_test;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;


public class HbaseUsage {
	public static void main(String[] args) throws IOException {
        Configuration conf = HBaseConfiguration.create();
        HTable table = new HTable(conf, "myHBaseTable");
        Put p = new Put(Bytes.toBytes("myRow"));
        p.add(Bytes.toBytes("myFamily"), Bytes.toBytes("someQualifier"),
                Bytes.toBytes("Some Value"));
        table.put(p);
        Get g = new Get(Bytes.toBytes("myRow"));
        Result r = table.get(g);
        byte[] value = r.getValue(Bytes.toBytes("myFamily"),
                Bytes.toBytes("someQualifier"));
        String valueStr = Bytes.toString(value);
        System.out.println("GET: " + valueStr);
 
        Scan s = new Scan();
        s.addColumn(Bytes.toBytes("myFamily"), Bytes.toBytes("someQualifier"));
        ResultScanner scanner = table.getScanner(s);
        try {
            for (Result rr : scanner) {
                System.out.println("Found row: " + rr);
            }
        } finally {
            scanner.close();
        }
        
        table.close();
    }

}
