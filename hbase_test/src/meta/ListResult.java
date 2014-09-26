package meta;

import java.util.ArrayList;
import java.util.List;

public class ListResult {

	private String prefix;
	private String delimiter;
	private String marker;
	private int maxKeys;

	private List<String> keys;
	private List<String> commonPrefixs;
	private Boolean isTrancated;

	private String nextMarker;

	public ListResult(String prefix, String marker, String delimiter,
			int maxkeys) {
		this.prefix = prefix;
		this.marker = marker;
		this.delimiter = delimiter;
		this.maxKeys = maxkeys;
		keys = new ArrayList<String>();
		commonPrefixs = new ArrayList<String>();
		isTrancated = Boolean.FALSE;

	}

	public void addCommonPrefix(String commonPrefix){
		this.commonPrefixs.add(commonPrefix);
	}
	
	public void addKey(String key) {
		this.keys.add(key);
	}
	
	public String getPrefix() {
		return prefix;
	}

	public String getDelimiter() {
		return delimiter;
	}

	public String getMarker() {
		return marker;
	}

	public int getMaxKeys() {
		return maxKeys;
	}

	public Boolean getIsTrancated() {
		return isTrancated;
	}

	public void setIsTrancated(Boolean isTrancated) {
		this.isTrancated = isTrancated;
	}

	public String getNextMarker() {
		return nextMarker;
	}

	public void setNextMarker(String nextMarker) {
		this.nextMarker = nextMarker;
	}

	public int size() {
		return keys.size() + commonPrefixs.size();
	}
	
	public List<String> getCommonPrefixs(){
		return this.commonPrefixs;
	}
	public String toString(){
		StringBuffer result = new StringBuffer();
		result.append("Prefix:"+prefix + "\n");
		result.append("Marker:" + marker + "\n");
		result.append("Delimiter:" + delimiter + "\nMaxKeys:");
		result.append(maxKeys);
		result.append("\n");
		return result.toString();
	}
}
