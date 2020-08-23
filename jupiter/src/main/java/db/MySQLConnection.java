package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import entity.Item;
import entity.Item.ItemBuilder;


public class MySQLConnection {
	private Connection conn;

	// connect MySQL
	public MySQLConnection() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver").getConstructor().newInstance();
			conn = DriverManager.getConnection(MySQLDBUtil.URL);
			// getConnection will throw SQLException
			// SQLException belongs to java.lang.Exception 
		} catch (Exception e) {
			e.printStackTrace(); // print for easy to debug
		}
	}
	
	// close the connection
	public void close() {
		if (conn != null) {
			try {
				conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	// MySQL index start with 1
	// 在进行数据沟通是null check， 之前null check没有必要
	public void setFavoriteItems(String userId, Item item) {
		if (conn == null) {
			// err will print in console with red color, so not use out
			//更加直观出现问题了
			System.err.println("DB connection failed"); 
			return;
		}
		saveItem(item);
		// 声明item id
		String sql = "INSERT INTO history (user_id, item_id) VALUES (?, ?)";
		try {
			// PrepareStatement: 可以在问号的地方把它更新
			// Statement (used in MySQLTableCreation): statement是死的，没有更改
			PreparedStatement statement = conn.prepareStatement(sql);
			// 如果像GitHubClient那使用了%s， 这个会把不必要的也加去，算入逻辑了，
			// 类似 INSET INTO history.. VALUES (INSERT...,?) 这样就不安全了
			//用userID替换 了问号，并且给userID加了引号，告诉系统这是个string，，这里没有任何逻辑，这样比较安全
			statement.setString(1, userId); 
			statement.setString(2, item.getItemId());
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// 传了userId和itemId，而不是userId和item， 因为delete不用保存
	public void unsetFavoriteItems(String userId, String itemId) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return;
		}
		String sql = "DELETE FROM history WHERE user_id = ? AND item_id = ?";
		try {
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			statement.setString(2, itemId);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void saveItem(Item item) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return;
		}
		// 假设item已经存在了，可能会报错，加了IGNORE后就可以略过
		String sql = "INSERT IGNORE INTO items VALUES (?, ?, ?, ?, ?)";
		try {
			PreparedStatement statement = conn.prepareStatement(sql);
			//几个问号就set几个string
			statement.setString(1, item.getItemId());
			statement.setString(2, item.getName());
			statement.setString(3, item.getAddress());
			statement.setString(4, item.getImageUrl());
			statement.setString(5, item.getUrl());
			// 找database插进数据
			statement.executeUpdate();
			
			// 在保存item的同时保存keywords
			sql = "INSERT IGNORE INTO keywords VALUES (?, ?)";
                    statement = conn.prepareStatement(sql);
			statement.setString(1, item.getItemId());
			for (String keyword : item.getKeywords()) {
				statement.setString(2, keyword);
				statement.executeUpdate();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public Set<String> getFavoriteItemIds(String userId) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return new HashSet<>();
		}

		Set<String> favoriteItems = new HashSet<>();

		try {
			String sql = "SELECT item_id FROM history WHERE user_id = ?";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			// 读取数据 所以用了executeQuery
			// exexuteUpdate只更新数据不能读取
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				String itemId = rs.getString("item_id");
				favoriteItems.add(itemId);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return favoriteItems;
	}
	
	public Set<Item> getFavoriteItems(String userId) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return new HashSet<>();
		}
		Set<Item> favoriteItems = new HashSet<>();
		Set<String> favoriteItemIds = getFavoriteItemIds(userId);

		String sql = "SELECT * FROM items WHERE item_id = ?";
		try {
			PreparedStatement statement = conn.prepareStatement(sql);
			for (String itemId : favoriteItemIds) {
				statement.setString(1, itemId);
				ResultSet rs = statement.executeQuery();

				ItemBuilder builder = new ItemBuilder();
				if (rs.next()) {
					builder.setItemId(rs.getString("item_id"));
					builder.setName(rs.getString("name"));
					builder.setAddress(rs.getString("address"));
					builder.setImageUrl(rs.getString("image_url"));
					builder.setUrl(rs.getString("url"));
					builder.setKeywords(getKeywords(itemId)); 
					// getKeywords 是个单独的method，因为keyword是个单独的table，是个set
					favoriteItems.add(builder.build());
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return favoriteItems;
	}

	public Set<String> getKeywords(String itemId) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return null;
		}
		Set<String> keywords = new HashSet<>();
		String sql = "SELECT keyword from keywords WHERE item_id = ? ";
		try {
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, itemId);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				String keyword = rs.getString("keyword");
				keywords.add(keyword);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return keywords;
	}
	// to check where this method used: right-click on the method name->Reference->Workspace
	
	public String getFullname(String userId) {
		// check valid connection
		if (conn == null) {
			System.err.println("DB connection failed");
			return "";
		}
		String name = "";
		String sql = "SELECT first_name, last_name FROM users WHERE user_id = ? ";
		try {
			// paepare sql
			PreparedStatement statement = conn.prepareStatement(sql);
			// 1: parameter index; the statement index start with 1
			// userId: argument which added into the statement
			statement.setString(1, userId);
			ResultSet rs = statement.executeQuery();
			// check the user to get the full name
			if (rs.next()) {
				name = rs.getString("first_name") + " " + rs.getString("last_name");
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		return name;
	}

	public boolean verifyLogin(String userId, String password) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return false;
		}
		String sql = "SELECT user_id FROM users WHERE user_id = ? AND password = ?";
		try {
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			statement.setString(2, password);
			ResultSet rs = statement.executeQuery();
			//check if the userId exist
			if (rs.next()) {
				return true;
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		return false;
	}

	public boolean addUser(String userId, String password, String firstname, String lastname) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return false;
		}
		// ignore: if duplicate, just ignore
		String sql = "INSERT IGNORE INTO users VALUES (?, ?, ?, ?)";
		try {
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			statement.setString(2, password);
			statement.setString(3, firstname);
			statement.setString(4, lastname);
			
			// executeUpdate() == 1: success then update one column, return 1;
			// 						 fail then not update
			return statement.executeUpdate() == 1;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
}
