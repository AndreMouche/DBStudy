import java.util.ArrayList;
import java.util.List;

/*
 * 使用list接口实现类似目录的功能，其中目录分隔符delimiter为任意字符。
 * 支持ListResult list(String marker,String prefix,String delimiter,int maxKeys
 * 其中：
 * 1.marker：相当于startKey，返回的所有key字典序大于等于marker,以此实现分页功能。
 * 2.prefix：返回值的所有key以prefix为前缀 
 * 3.delimiter: 返回Key以delimiter作为分隔符分组，同组元素仅返回相同前缀，
 *   以此实现目录功能。即所有{$prefix}AAAA{$delimiter} [*] 的key,
 *   只返回该组元素的相同前缀{$prefix}AAAA{$delimiter} 
 * 4.返回列表按字典序排序，若结果大于maxKeys条，则返回前maxKeys条。 
 * */
public class Main {

	public static List<String> listKeys(String[] input, String marker,
			String prefix, String delimiter, int maxKeys) {
		List<String> answer = new ArrayList<String>();
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

		int curIndex = BinarySearch(input, startKey);
		if (curIndex >= input.length) {
			return answer;
		}

		while (answer.size() < maxKeys && curIndex < input.length) {
			if (endKey.length() > 0 && input[curIndex].compareTo(endKey) >= 0) {
				break;
			}
			if (delimiter.length() > 0) {
				int deId = input[curIndex].indexOf(delimiter, prefix.length());
				if (deId >= 0) {
					answer.add(input[curIndex].substring(0, deId) + delimiter);
					startKey = input[curIndex].substring(0, deId)
							+ nextDelimiter;
					curIndex = BinarySearch(input, startKey);
					continue;
				}

			}
			answer.add(input[curIndex]);
			curIndex++;

		}

		return answer;
	}

	/**
	 * Get smalleast index whose input[index]>=key
	 * 
	 * @param input
	 * @param key
	 * @return
	 */
	public static int BinarySearch(String[] input, String key) {
		int left, right;
		left = 0;
		right = input.length;
		while (left < right) {
			int mid = (left + right) >> 1;
			if (input[mid].compareTo(key) < 0) {
				left = mid + 1;
			} else if (input[mid].compareTo(key) == 0) {
				return mid;
			} else {
				right = mid;
			}
		}
		return right;
	}

	public static void main(String[] args) {
		String[] input = { "aaaaaaa", "abaaa", "ac/1/1", "ac/2/1", "ad/1",
				"ad/1/2", "ad/1/2/3", "bbbbb", "bc/1", "bc/1/2/3", "bddd" };

		List<String> result = listKeys(input, "", "", "", 100);
		System.out.println("test1");
		System.out.println(result.size());
		for (String key : result) {
			System.out.println(key);
		}

		result = listKeys(input, "ab", "", "/", 5);
		System.out.println("test2");
		System.out.println(result.size());
		for (String key : result) { // [abaaa, ac/, ad/, bbbbb, bc/]
			System.out.println(key);
		}

		result = listKeys(input, "", "ac/", "/", 100);
		System.out.println("test3");
		System.out.println(result.size());
		for (String key : result) { // [ac/1/,ac/2/]
			System.out.println(key);
		}
		// int id = BinarySearch(input, "bddda");
		// if(id >= input.length) {
		// System.out.println("None");
		// } else {
		// System.out.println(input[id]);
		// }

	}
}
