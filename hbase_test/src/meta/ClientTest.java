package meta;

import java.io.IOException;
import java.io.InterruptedIOException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import junit.framework.TestCase;

public class ClientTest extends TestCase {
	private HtableClient client;
	public static final String Spechars = "特殊   字符`-=[]\\;',./ ~!@#$%^&*()_+{}|:\"<>?";
	public static final String SpecharsWithoutBackslash = "特殊字符`-=[];',./ ~!@#$%^&*()_+{}|:\"<>?";

	int objectCount = 1010;
	String[] objectNamePrefix = { "etc%admin%" + Spechars,
			"home#qa#__%%%__" + Spechars };
	String[] objectNamePrefixForDelimiter = {
			"abcdefg/x/%%%___%____" + Spechars,
			"abcdefg/xy/%%%___%____" + Spechars,
			"abcdefg/xz/%%%___%____" + Spechars,
			"abcdefg/y/%%%___%____" + Spechars,
			"abcdefg/z/%%%___%____" + Spechars };
	String[] objectNames = null;
	String[] objectNamesForDelimiter = null;

	@Before
	public void before() throws Exception {
		System.out.println("Before");
		client = new HtableClient();
	}

	@After
	public void after() throws IOException {
		System.out.println("After");
		client.Destroy();
	}

	/**
	 * <strong>默认参数listObjects.</strong><br>
	 * <p>
	 * 测试除桶名外其它参数为空，进行listObjects操作<br>
	 * <p>
	 * <b>测试数据：</b><br>
	 * <p>
	 * <b>测试步骤：</b><br>
	 * 1、向桶内上传1010个对象<br>
	 * 2、除桶名外其它参数为空，进行listObjects操作<br>
	 * 3、判断结果对象个数是否为100<br>
	 * <p>
	 * <b>期望结果：</b><br>
	 * 结果对象个数为100
	 * <p>
	 * <b>备注：</b><br>
	 * 不指定MaxKeys，最多返回100个对象
	 * 
	 * @throws Exception
	 * 
	 * @throws IOException
	 * @throws InterruptedIOException
	 */

	@Test
	public void testListObjectDefault() throws Exception {

		try {
			before();
			for (int id = 0; id < objectCount; id++) {

				client.putData(String.format("%s/%d", objectNamePrefix, id));

			}

			ListResult resultData = client.list("", "", "", 100);
			Assert.assertEquals(100, resultData.size());
		} catch (InterruptedIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			after();
		}
	}

	/**
	 * <strong>利用NextMarker遍历对象.</strong><br>
	 * <p>
	 * 测试指定Maker，利用NextMarker遍历对象<br>
	 * <p>
	 * <b>测试数据：</b><br>
	 * <p>
	 * <b>测试步骤：</b><br>
	 * 1、向桶内上传1010个对象，其中505个对象名以"ect"开头，505个以"home"开头<br>
	 * 2、指定桶名，指定Maker为"home"，其他参数为空，listObjects<br>
	 * 3、在while循环中利用NextMarker作为新的listobjects的Maker参数遍历对象，将获取的对象保存在listResult中
	 * <br>
	 * <p>
	 * <b>期望结果：</b><br>
	 * listResult中对象数目等于505
	 * <p>
	 * <b>备注：</b><br>
	 * bug: http://jira.hz.netease.com/browse/CLOUD-1991
	 * @throws Exception 
	 */
	 @Test
	 public void testListObjectWithNextMaker() throws Exception{
	  try{
		  before();
		  for(int id = 0; id < objectCount; id ++) {
			  String key = String.format("%s%d",objectNamePrefix[id%objectNamePrefix.length],id );
			  client.putData(key);
		  }
		  
		  ListResult listResult = client.list("home", "", "", objectCount);
		  Assert.assertEquals(objectCount/2, listResult.size());
		  
		  int num = 0;
		  int maxKeys = 20;
		  String marker = "home";
		  do {
			 listResult = client.list(marker, "", "", maxKeys);
			 num += listResult.size();
			 marker = listResult.getNextMarker();
		  }while(listResult.getIsTrancated());
		  
		  Assert.assertEquals(objectCount/2, num);
		  
	  }finally {
		  after();
	  }
	 }
	 /**
	 * <strong>listObjects，prefix不存在.</strong><br>
	 * <p>测试listObjects，prefix不存在，list结果为空<br>
	 * <p><b>测试数据：</b><br>
	 * <p><b>测试步骤：</b><br>
	 * 1、向桶内上传1010个对象，其中505个对象名以"ect"开头，505个以"home"开头<br>
	 * 2、指定桶名，指定prefix为"no"，其他参数为空，listObjects<br>
	 * <p><b>期望结果：</b><br>
	 * 结果对象数目为0
	 * @throws Exception 
	 */
	
	 public void testlistObjectSpecifiedPrefixNoResult() throws Exception{
		 try{
			  before();
			  for(int id = 0; id < objectCount; id ++) {
				  String key = String.format("%s%d",objectNamePrefix[id%objectNamePrefix.length],id );
				  client.putData(key);
			  }
			  ListResult listResult = client.list("", "no", "", objectCount);
			  Assert.assertEquals(0,listResult.size());
			  
		  }finally {
			  after();
		  }
	 }
	 /**
	 * <strong>listObjects，符合prefix的对象数目小于Maxkeys.</strong><br>
	 * <p>测试listObjects，符合prefix的对象数目小于Maxkeys，list出来的结果数=实际符合条件的对象数<br>
	 * <p><b>测试数据：</b><br>
	 * <p><b>测试步骤：</b><br>
	 * 1、向桶内上传1010个对象，其中505个对象名以"ect"开头，505个以"home"开头<br>
	 * 2、指定桶名，指定prefix为"ect"，MaxKeys为800，listObjects<br>
	 * <p><b>期望结果：</b><br>
	 * 结果对象数目为505，isTruncated=false
	 * @throws Exception 
	 */
	 public void testListObjectSpecifiedPrefixSmallerThanMaxkey( ) throws Exception{
		 try{
			  before();
			  for(int id = 0; id < objectCount; id ++) {
				  String key = String.format("%s%d",objectNamePrefix[id%objectNamePrefix.length],id );
				  client.putData(key);
			  }
			  ListResult listResult = client.list("", "etc", "", 800);
			  Assert.assertEquals(505,listResult.size());
			  Assert.assertEquals(Boolean.FALSE, listResult.getIsTrancated());
			  
		  }finally {
			  after();
		  }
	 }
	 /**
	 * <strong>listObjects，符合prefix的对象数目大于Maxkeys.</strong><br>
	 * <p>测试listObjects，符合prefix的对象数目大于Maxkeys，list出来的结果数=Maxkeys<br>
	 * <p><b>测试数据：</b><br>
	 * <p><b>测试步骤：</b><br>
	 * 1、向桶内上传1010个对象，其中505个对象名以"ect"开头，505个以"home"开头<br>
	 * 2、指定桶名，指定prefix为"ect"，MaxKeys为200，listObjects<br>
	 * <p><b>期望结果：</b><br>
	 * 结果对象数目为200，isTruncated=true
	 * @throws Exception 
	 */
	@Test
	 public void testListObjectSpecifiedPrefixLargerThanMaxkey() throws Exception{
		 try{
			  before();
			  for(int id = 0; id < objectCount; id ++) {
				  String key = String.format("%s%d",objectNamePrefix[id%objectNamePrefix.length],id );
				  client.putData(key);
			  }
			  ListResult listResult = client.list("", "etc", "", 200);
			  Assert.assertEquals(200,listResult.size());
			  Assert.assertEquals(Boolean.TRUE, listResult.getIsTrancated());
			  
		  }finally {
			  after();
		  }
	 }
	
	 /**
	 * <strong>listObjects，Maker不存在.</strong><br>
	 * <p>测试listObjects，Maker不存在，list结果为空<br>
	 * <p><b>测试数据：</b><br>
	 * <p><b>测试步骤：</b><br>
	 * 1、向桶内上传1010个对象，其中505个对象名以"ect"开头，505个以"home"开头<br>
	 * 2、指定桶名，指定Maker为"zzz"，其他参数为空，listObjects<br>
	 * <p><b>期望结果：</b><br>
	 * 结果对象数目为0，isTruncated=false
	 * @throws Exception 
	 */
	 @Test
	 public void testListObjectSpecifiedMarkerNoResult() throws Exception{
		 try{
			  before();
			  for(int id = 0; id < objectCount; id ++) {
				  String key = String.format("%s%d",objectNamePrefix[id%objectNamePrefix.length],id );
				  client.putData(key);
			  }
			  ListResult listResult = client.list("", "zzz", "", 200);
			  Assert.assertEquals(0,listResult.size());
			  Assert.assertFalse(listResult.getIsTrancated());
			  
		  }finally {
			  after();
		  }
	
	 }
	 /**
	 * <strong>listObjects，符合Maker的对象数目小于Maxkeys.</strong><br>
	 * <p>测试listObjects，符合Maker的对象数目小于Maxkeys，list结果数目=符合Maker的对象数目<br>
	 * <p><b>测试数据：</b><br>
	 * <p><b>测试步骤：</b><br>
	 * 1、向桶内上传1010个对象，其中505个对象名以"ect"开头，505个以"home"开头<br>
	 * 2、指定桶名，指定Maker为"home"，Maxkeys=800，listObjects<br>
	 * <p><b>期望结果：</b><br>
	 * 结果对象数目为505，isTruncated=false
	 */
	 @Test
	 public void testListObjectSpecifiedMarkersmallerThanMaxkey()throws Exception{
		 try{
			  before();
			  for(int id = 0; id < objectCount; id ++) {
				  String key = String.format("%s%d",objectNamePrefix[id%objectNamePrefix.length],id );
				  client.putData(key);
			  }
			  ListResult listResult = client.list("", "home", "", 800);
			  Assert.assertEquals(505,listResult.size());
			  Assert.assertFalse(listResult.getIsTrancated());
			  
		  }finally {
			  after();
		  }
	
	 }
	 /**
	 * <strong>listObjects，符合Maker的对象数目大于Maxkeys.</strong><br>
	 * <p>测试listObjects，符合Maker的对象数目大于Maxkeys，list结果数目=Maxkeys<br>
	 * <p><b>测试数据：</b><br>
	 * <p><b>测试步骤：</b><br>
	 * 1、向桶内上传1010个对象，其中505个对象名以"ect"开头，505个以"home"开头<br>
	 * 2、指定桶名，指定Maker为"home"，Maxkeys=200，listObjects<br>
	 * <p><b>期望结果：</b><br>
	 * 结果对象数目为200，isTruncated=true
	 */
	 @Test
	 public void
	 testListObjectSpecifiedMarkerLargerThanMaxkey()throws Exception{
		 try{
			  before();
			  for(int id = 0; id < objectCount; id ++) {
				  String key = String.format("%s%d",objectNamePrefix[id%objectNamePrefix.length],id );
				  client.putData(key);
			  }
			  ListResult listResult = client.list("", "home", "", 200);
			  Assert.assertEquals(200,listResult.size());
			  Assert.assertTrue(listResult.getIsTrancated());
			  
		  }finally {
			  after();
		  }
	
	 }
	 /**
	 * <strong>listObjects，指定Delimiter.</strong><br>
	 * <p>测试listObjects，指定Delimiter，包含Delimiter的对象在返回结果中被折叠<br>
	 * <p><b>测试数据：</b><br>
	 * <p><b>测试步骤：</b><br>
	 *
	 1、向桶内上传500个对象，"abcdefg/x/","abcdefg/xy/","abcdefg/xz/","abcdefg/y/","abcdefg/z/"前缀的各100个<br>
	 * 2、指定桶名，指定Prefix为"abcdefg/"，指定Delimiter为"/"，Maxkeys=100，listObjects<br>
	 * <p><b>期望结果：</b><br>
	 *
	 结果对象数目为0，所有返回对象均被折叠，返回的commonPrefixes为"abcdefg/x/","abcdefg/xy/","abcdefg/xz/","abcdefg/y/","abcdefg/z/"
	 * <p><b>备注：</b><br>
	 *bug fixed: http://jira.hz.netease.com/browse/CLOUD-2314
	 */
	 @Test
	 public void TestListObjectSpecifiedDelimiter()throws Exception{
		 try{
			  before();
			  for(String dir:objectNamePrefixForDelimiter) {
				  for(int id = 0; id < 100; id ++) {
					  String key = String.format("%s%s%d",dir,objectNamePrefix[id%objectNamePrefix.length],id );
					  client.putData(key);
				  }
			  }
			  ListResult listResult = client.list("", "abcdefg/", "/", 100);
			  Assert.assertEquals(objectNamePrefixForDelimiter.length,listResult.size());
			  Assert.assertFalse(listResult.getIsTrancated());
			  
		  }finally {
			  after();
		  }
	
	 }
	 /**
	 *
	 <strong>listObjects，指定Delimiter,Prefix，对象都不能同时满足Delimiter,Prefix.</strong><br>
	 *
	 <p>测试listObjects，指定Delimiter,Prefix，对象都不能同时满足Delimiter,Prefix，返回对象数目为0<br>
	 * <p><b>测试数据：</b><br>
	 * <p><b>测试步骤：</b><br>
	 * 1、向桶内上传1010个对象，其中505个命名形式为"etc%admin%"开头，505个命名形式为"home#q#"开头<br>
	 * 2、指定桶名，指定Delimiter为"q"，Prefix="etc"，Maxkeys=1000，listObjects<br>
	 * <p><b>期望结果：</b><br>
	 * 结果对象CommonPrefixs数目为0
	 * @throws Exception 
	 */
	 @Test
	 public void testListObjectSpecifiedDelimiterAndWrongPrefix() throws Exception {
		 try{
			  before();
			  String []delimiterPrefx = {"etc%admin%","home#q#"};
			  
			  for(int id = 0; id < 1010; id ++) {
				  String key = String.format("%s%d",delimiterPrefx[id%delimiterPrefx.length],id );
				  client.putData(key);
			  }
			  
			  ListResult listResult = client.list("", "etc", "q", 1000);
			  Assert.assertEquals(505,listResult.size());
			  Assert.assertFalse(listResult.getIsTrancated());
			  Assert.assertEquals(0, listResult.getCommonPrefixs().size());
			  
		  }finally {
			  after();
		  }
	 }
	 /**
	 * <strong>listObjects，指定Delimiter,Prefix.</strong><br>
	 * <p>测试listObjects，指定Delimiter,Prefix<br>
	 * <p><b>测试数据：</b><br>
	 * <p><b>测试步骤：</b><br>
	 * 1、向桶内上传1010个对象，其中505个命名形式为"etc%admin%"开头，505个命名形式为"home#q#"开头<br>
	 * 2、指定桶名，指定Delimiter为"q"，Prefix="home"，listObjects<br>
	 * <p><b>期望结果：</b><br>
	 * 结果对象数目为0，commonPrefixes = "home#q",所以满足条件的对象均被折叠
	 */
	
	 public void
	 testListObjectSpecifiedDelimiterAndRightPrefix() throws Exception {
		 try{
			  before();
			  String []delimiterPrefx = {"etc%admin%","home#q#"};
			  
			  for(int id = 0; id < 1010; id ++) {
				  String key = String.format("%s%d",delimiterPrefx[id%delimiterPrefx.length],id );
				  client.putData(key);
			  }
			  
			  ListResult listResult = client.list("", "home", "q", 1000);
			  Assert.assertEquals(1,listResult.size());
			  Assert.assertFalse(listResult.getIsTrancated());
			  Assert.assertEquals(1, listResult.getCommonPrefixs().size());
			  
		  }finally {
			  after();
		  }
	 }
	 /**
	 * <strong>listObjects，指定Delimiter不存在.</strong><br>
	 * <p>测试listObjects，指定Delimiter,Prefix<br>
	 * <p><b>测试数据：</b><br>
	 * <p><b>测试步骤：</b><br>
	 * 1、向桶内上传1010个对象，其中505个命名形式为"etc%admin%"开头，505个命名形式为"home#q#"开头<br>
	 * 2、指定桶名，指定Delimiter为"s"，其它参数不设置，listObjects<br>
	 * <p><b>期望结果：</b><br>
	 * 结果对象数目为maxKeys，相当于Delimiter未起作用
	 */
	
	 @Test
	 public void testListObjectSpecifiedWrongDelimiter() throws Exception {
		 try{
			  before();
			  String []delimiterPrefx = {"etc%admin%","home#q#"};
			  
			  for(int id = 0; id < 1010; id ++) {
				  String key = String.format("%s%d",delimiterPrefx[id%delimiterPrefx.length],id );
				  client.putData(key);
			  }
			  
			  ListResult listResult = client.list("", "", "s", 1000);
			  Assert.assertEquals(1000,listResult.size());
			  Assert.assertTrue(listResult.getIsTrancated());
			  Assert.assertEquals(0, listResult.getCommonPrefixs().size());
			  
		  }finally {
			  after();
		  }
	 }
}
