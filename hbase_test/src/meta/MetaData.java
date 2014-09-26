package meta;



public class MetaData {
    public static String TABLE_NAME = "matadatatest";
    public static String ATTRIBUTE_NAME = "attributes";
    public static String ATT_TIME = "modifyTime";
    public String key;
    public String modifyTime;
    public MetaData(String key) {
    	this.key = key;
    	this.modifyTime = new java.util.Date().toString();
    }
}
