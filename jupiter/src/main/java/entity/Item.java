package entity;

import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

public class Item {
	private String itemId;
	private String name;
	private String address;
	private Set<String> keywords;
	private String imageUrl;
	private String url;
	
	private Item(ItemBuilder builder) {
		this.itemId = builder.itemId;
		this.name = builder.name;
		this.address = builder.address;
		this.imageUrl = builder.imageUrl;
		this.url = builder.url;
		this.keywords = builder.keywords;
	}

	
	public String getItemId() {
		return itemId;
	}
	public String getName() {
		return name;
	}
	public String getAddress() {
		return address;
	}
	public Set<String> getKeywords() {
		return keywords;
	}
	public String getImageUrl() {
		return imageUrl;
	}
	public String getUrl() {
		return url;
	}
	
	// JSONString --> JSONArray/JSONObject --> Item -->JSONArray/JSONObject -->JSONString 
	//从github里拿到了JSONString，然后要转成JSONArray  --- 这是在githubclient里完成的
	// 然后要Item 
	// 要Item 是因为方便存到数据库，方便使用
	// 但是Item --> JSONObject 是为了传到前端，但前端还不能直接使用JSONObject，前端要JSONString
	// JSONObject --> JSONString 方便因为Java有自带的method, 在RpcHelper里实现
	// Item --> JSONObject-->JSONString 更方便
	// 如果Item --> JSONString 可以但是要自己写，比较麻烦，而且如果写错就不好了
	public JSONObject toJSONObject() {
		JSONObject obj = new JSONObject();
		obj.put("item_id", itemId);
		obj.put("name", name);
		obj.put("address", address);
		obj.put("keywords", new JSONArray(keywords));
		obj.put("image_url", imageUrl);
		obj.put("url", url);
		return obj;
	}
	
	// ItemBuider 可以增强封装性，而且比较简单易读
	// 需要ItemBuilder去创建Item class
	// 如果不用static，那就需要创建Item class去调用，这样就是鸡生蛋，蛋生鸡的问题了
	public static class ItemBuilder {
		private String itemId;
		private String name;
		private String address;
		private String imageUrl;
		private String url;
		private Set<String> keywords;
		
		public void setItemId(String itemId) {
			this.itemId = itemId;
		}

		public void setName(String name) {
			this.name = name;
		}

		public void setAddress(String address) {
			this.address = address;
		}

		public void setImageUrl(String imageUrl) {
			this.imageUrl = imageUrl;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public void setKeywords(Set<String> keywords) {
			this.keywords = keywords;
		}
		
		public Item build() {
			return new Item(this);
		}
	}

}
