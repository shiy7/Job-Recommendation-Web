package rpc;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import entity.Item;
import entity.Item.ItemBuilder;

public class RpcHelper {
	
	// private RpcHelper() {} 可以写上去避免其他人new RpcHelper（）
	
	// Writes a JSONArray to http response.
		public static void writeJsonArray(HttpServletResponse response, JSONArray array) throws IOException{
			response.setContentType("application/json");
			response.getWriter().print(array);
		}

	              // Writes a JSONObject to http response.
		public static void writeJsonObject(HttpServletResponse response, JSONObject obj) throws IOException {		
			response.setContentType("application/json");
			response.getWriter().print(obj);
		}
		
		// Parses a JSONObject from http request.
		// 把从http的body读出来成string再转成JSONObject
		public static JSONObject readJSONObject(HttpServletRequest request) throws IOException {
			BufferedReader reader = new BufferedReader(request.getReader());
			StringBuilder requestBody = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				requestBody.append(line);
			}
			// 上面拿到了JOSON string， 下面return 把string转成JOSONObject
			return new JSONObject(requestBody.toString());
		}


	      // Convert a JSON object to Item object
		// 为了以后项目的extend，所有没有把readJSONObject 和parseFavoriteItem 放在一个method里
		// readJSONObject 是可以针对所有的，如果之后有其他的method要把JSONObject转成其他的，这样会更方便
		public static Item parseFavoriteItem(JSONObject favoriteItem) {
			ItemBuilder builder = new ItemBuilder();
			builder.setItemId(favoriteItem.getString("item_id"));
			builder.setName(favoriteItem.getString("name"));
			builder.setAddress(favoriteItem.getString("address"));
			builder.setUrl(favoriteItem.getString("url"));
			builder.setImageUrl(favoriteItem.getString("image_url"));
			
			Set<String> keywords = new HashSet<>();
			JSONArray array = favoriteItem.getJSONArray("keywords");
			for (int i = 0; i < array.length(); ++i) {
				keywords.add(array.getString(i));
			}
			builder.setKeywords(keywords);
			return builder.build();
		}

}
