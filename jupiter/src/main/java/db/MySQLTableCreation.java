package db;

import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.Connection;

public class MySQLTableCreation {
	// Run this as Java application to reset the database.
	public static void main(String[] args) {
		try {
			// Step 1 Connect to MySQL.
			System.out.println("Connecting to " + MySQLDBUtil.URL);
			// same as create new instance: Driver driver = new Driver(); driver: used to connect the database
			// don't need to save this instance, it will be connected by the driverManager automatically. 
			Class.forName("com.mysql.cj.jdbc.Driver").getConstructor().newInstance();   
			Connection conn = DriverManager.getConnection(MySQLDBUtil.URL);
			
			// connection null check
			if (conn == null) {
				System.out.print("connect failed");
				return;
			}

			// Step 2 Drop tables in case they exist.
			Statement statement = conn.createStatement();
			String sql = "DROP TABLE IF EXISTS keywords";
			statement.executeUpdate(sql);

			sql = "DROP TABLE IF EXISTS history";
			statement.executeUpdate(sql);

			sql = "DROP TABLE IF EXISTS items";
			statement.executeUpdate(sql);

			sql = "DROP TABLE IF EXISTS users";
			statement.executeUpdate(sql);

			// Step 3 Create new tables
			sql = "CREATE TABLE items (" 
					+ "item_id VARCHAR(255) NOT NULL," 
					+ "name VARCHAR(255),"
					+ "address VARCHAR(255)," 
					+ "image_url VARCHAR(255)," 
					+ "url VARCHAR(255),"
					+ "PRIMARY KEY (item_id)" 
					+ ")";
			statement.executeUpdate(sql);

			sql = "CREATE TABLE users (" 
					+ "user_id VARCHAR(255) NOT NULL," 
					+ "password VARCHAR(255) NOT NULL,"
					+ "first_name VARCHAR(255)," 
					+ "last_name VARCHAR(255)," 
					+ "PRIMARY KEY (user_id)" 
					+ ")";
			statement.executeUpdate(sql);

			sql = "CREATE TABLE keywords (" 
					+ "item_id VARCHAR(255) NOT NULL," 
					+ "keyword VARCHAR(255) NOT NULL,"
					+ "PRIMARY KEY (item_id, keyword)," 
					+ "FOREIGN KEY (item_id) REFERENCES items(item_id)" 
					+ ")";
			statement.executeUpdate(sql);

			sql = "CREATE TABLE history (" 
					+ "user_id VARCHAR(255) NOT NULL," 
					+ "item_id VARCHAR(255) NOT NULL,"
					+ "last_favor_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," 
					+ "PRIMARY KEY (user_id, item_id)," 
					+ "FOREIGN KEY (user_id) REFERENCES users(user_id),"
					+ "FOREIGN KEY (item_id) REFERENCES items(item_id)" 
					+ ")";
			statement.executeUpdate(sql);

			// Step 4: insert fake user 1111/3229c1097c00d497a0fd282d586be050
			// just for test because this is no sign-up 
			sql = "INSERT INTO users VALUES('1111', '3229c1097c00d497a0fd282d586be050', 'John', 'Smith')";
			statement.executeUpdate(sql);

			conn.close(); // best to put in finally, but it will be more complex. 
			// if put here, then it will be not disconnect when there is some error, waste space.
			// if put in finally, even there is error, it still can be disconnect.
			System.out.println("Import done successfully");

		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

}
