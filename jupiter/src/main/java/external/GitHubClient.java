package external;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;

import entity.Item;
import entity.Item.ItemBuilder;


public class GitHubClient {
	private static final String URL_TEMPLATE = "https://jobs.github.com/positions.json?description=%s&lat=%s&long=%s";
	private static final String DEFAULT_KEYWORD = "developer";
	
	public List<Item> search(double lat, double lon, String keyword) {
		if (keyword == null) {
			keyword = DEFAULT_KEYWORD;
		}
		try {
			keyword = URLEncoder.encode(keyword, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String url = String.format(URL_TEMPLATE, keyword, lat, lon);
		CloseableHttpClient httpClient = HttpClients.createDefault(); // 创建客户端, 给github发请求
		try {
			// call GitHub API， 发送get请求，拿到response
			CloseableHttpResponse response = httpClient.execute(new HttpGet(url));
			//解析response，因为拿到的response的是interface
			// 检测response是不是成功了
			if (response.getStatusLine().getStatusCode() != 200) {
				return new ArrayList<>();

			}
			//从response里读出entity， 只关注content
			HttpEntity entity = response.getEntity();
			if (entity == null) {
				return new ArrayList<>();

			}
			// get content
			// InputSreamReader要么一个一个读要么就只能按指定数量读
			// BufferedReader能按行读
			BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));
			// build response
			StringBuilder responseBody = new StringBuilder();
			// 读的每一行数组
			String line = null;
			// 一行一行的读， ！= 表示一直有数据， == null就表示没有数据了那么就可以停下来了
			while ((line = reader.readLine()) != null) {
				// 每读一行就可以add into responseBody
				responseBody.append(line);
			}
			//拿到的是JSONArray的字符串，，要变成Java的object
			JSONArray array = new JSONArray(responseBody.toString());
			// 变成Item List
			return getItemList(array);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new ArrayList<>();
	}
	
	//GitHub JSONArray： {JSONObject，JSONObject...} 
	// 在GitHub里只有一层，比较简单；如果有多层，要一次一次解析
	// 把从GitHUb拿出来的JSONArray变成Item List
	private List<Item> getItemList(JSONArray array) {
		List<Item> itemList = new ArrayList<>();
		
		
		List<String> jbs = new ArrayList<>();
		for (int i = 0; i < array.length(); i++) {
			String description = getStringFieldOrEmpty(array.getJSONObject(i), "description");
			
			if (description.equals("") || description.equals("\n")) {
				jbs.add(getStringFieldOrEmpty(array.getJSONObject(i), "title"));
			} else {
				jbs.add(description);
			}
		}
		
		List<List<String>> keywords = MonkeyLearnClient.extractKeywords(jbs.toArray(new String[jbs.size()]));
		
		// 访问JSONArray里的东西
		for (int i = 0; i < array.length(); ++i) {
			JSONObject object = array.getJSONObject(i);
			// 创建ItemBuilder
			ItemBuilder builder = new ItemBuilder();
			//把Item放进ItemBuilder
			// string填什么要match API的 Key
			builder.setItemId(getStringFieldOrEmpty(object, "id"));
			builder.setName(getStringFieldOrEmpty(object, "title"));
			builder.setAddress(getStringFieldOrEmpty(object, "location"));
			builder.setUrl(getStringFieldOrEmpty(object, "url"));
			builder.setImageUrl(getStringFieldOrEmpty(object, "company_logo"));
			
			// 如果多层
			// JSONArray tem = object.getISONArray()
			// for (....)
			
			List<String> list = keywords.get(i);
			builder.setKeywords(new HashSet<String>(list));
			//创建Item
			Item item = builder.build();
			// 放进list
			itemList.add(item);
		}
		
		return itemList;
	}
	
	// check if JSONObject is null. If it is null, return " ";otherwise, return key
	private String getStringFieldOrEmpty(JSONObject obj, String field) {
		return obj.isNull(field) ? "" : obj.getString(field);
	}

}
