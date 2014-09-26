package sample;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.filter.RegexStringComparator;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.util.GenericOptionsParser;

public class SearchByEmail {
  public static void main(String[] args) throws IOException {
    Configuration config = HBaseConfiguration.create();

    // Use GenericOptionsParser to get only the parameters to the class
    // and not all the parameters passed (when using WebHCat for example)
    String[] otherArgs = new GenericOptionsParser(config, args).getRemainingArgs();
    if (otherArgs.length != 1) {
      System.out.println("usage: [regular expression]");
      System.exit(-1);
    }

    // Open the table
    HTable table = new HTable(config, "people");

    // Define the family and qualifiers to be used
    byte[] contactFamily = Bytes.toBytes("contactinfo");
    byte[] emailQualifier = Bytes.toBytes("email");
    byte[] nameFamily = Bytes.toBytes("name");
    byte[] firstNameQualifier = Bytes.toBytes("first");
    byte[] lastNameQualifier = Bytes.toBytes("last");

    // Create a new regex filter
    RegexStringComparator emailFilter = new RegexStringComparator(otherArgs[0]);
    // Attach the regex filter to a filter
    //   for the email column
    SingleColumnValueFilter filter = new SingleColumnValueFilter(
      contactFamily,
      emailQualifier,
      CompareOp.EQUAL,
      emailFilter
    );

    // Create a scan and set the filter
    Scan scan = new Scan();
    scan.setFilter(filter);

    // Get the results
    ResultScanner results = table.getScanner(scan);
    // Iterate over results and print  values
    for (Result result : results ) {
      String id = new String(result.getRow());
      byte[] firstNameObj = result.getValue(nameFamily, firstNameQualifier);
      String firstName = new String(firstNameObj);
      byte[] lastNameObj = result.getValue(nameFamily, lastNameQualifier);
      String lastName = new String(lastNameObj);
      System.out.println(firstName + " " + lastName + " - ID: " + id);
      byte[] emailObj = result.getValue(contactFamily, emailQualifier);
      String email = new String(emailObj);
      System.out.println(firstName + " " + lastName + " - " + email + " - ID: " + id);
    }
    results.close();
    table.close();
  }
}

