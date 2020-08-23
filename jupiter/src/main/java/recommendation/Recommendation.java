package recommendation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import db.MySQLConnection;
import entity.Item;
import external.GitHubClient;


public class Recommendation {
	
	public List<Item> recommendItems(String userId, double lat, double lon) {
		// recommend result
		List<Item> recommendedItems = new ArrayList<>();

		// Step 1, get all favorited itemids
		MySQLConnection connection = new MySQLConnection(); // 创建connection
		Set<String> favoritedItemIds = connection.getFavoriteItemIds(userId);

		// Step 2, get all keywords, sort by count
		// {"software engineer": 6, "backend": 4, "san francisco": 3, "remote": 1}
		
		Map<String, Integer> allKeywords = new HashMap<>();
		// look through IDs, count the keyword,对一个单独用户操作
		// map： key: keywords; value: count
		for (String itemId : favoritedItemIds) {
			Set<String> keywords = connection.getKeywords(itemId);
			for (String keyword : keywords) {
				// 统计count,如果第一次出现就count=0+1
				allKeywords.put(keyword, allKeywords.getOrDefault(keyword, 0) + 1);
			}
		}
		// close the connection
		connection.close();
		
		// 创建空的list, hashMap cannot be sorted,so convert it to list for sorting
		// Entry<> 是对于hashMap， entrySet() 是hashMap里所有的（key，count）pairs
		List<Entry<String, Integer>> keywordList = new ArrayList<>(allKeywords.entrySet());
		// sort keyword list, use lambada comparator 
		Collections.sort(keywordList, (Entry<String, Integer> e1, Entry<String, Integer> e2) -> {
			return Integer.compare(e2.getValue(), e1.getValue());
		});

		// Cut down search list only top 3
		if (keywordList.size() > 3) {
			keywordList = keywordList.subList(0, 3);
		}
		
		// Step 3, search based on keywords, filter out favorite items
		//创建新的hashMap
		Set<String> visitedItemIds = new HashSet<>();
		// 创建新的client
		GitHubClient client = new GitHubClient();
		
		
		for (Entry<String, Integer> keyword : keywordList) {
			List<Item> items = client.search(lat, lon, keyword.getKey());

			for (Item item : items) {
				// look all item, if visited or favorited, then skip; otherwise, add into the recommend
				//确保数据库里没有出现过
				if (!favoritedItemIds.contains(item.getItemId()) && !visitedItemIds.contains(item.getItemId())) {
					recommendedItems.add(item);
					visitedItemIds.add(item.getItemId());
				}
			}
		}
		return recommendedItems;
	}


}
