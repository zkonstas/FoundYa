import java.io.IOException;
import java.sql.SQLException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.xml.sax.SAXException;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;


public class TestDbMethodsLocally {

	public static void main(String[] args) throws SQLException, XPathExpressionException, IOException, SAXException, ParserConfigurationException, InterruptedException {
		// TODO Auto-generated method stub
		MysqlDataSource dataSource = new MysqlDataSource();
		dataSource.setUser("");
		dataSource.setPassword("");
		dataSource.setPort(3306);
		dataSource.setDatabaseName("ChatroomDB");
		dataSource.setServerName("chatroom.cm4hjrblo4cc.us-east-1.rds.amazonaws.com");
		Connection conn = null;
		try {
			conn = (Connection) dataSource.getConnection();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("==============Adding user1==============");
		String user1 = DbMethods.getNewUserId(conn); //test user is created!
		System.out.println("User is user"+ user1);
		System.out.println("==============Adding user2==============");
		String user2 = DbMethods.getNewUserId(conn); //test user is created!
		System.out.println("User is user"+ user2);
		
		System.out.println("==============Logging user1 to a chatroom==============");
		JSONObject json = DbMethods.loginToChatroom(conn, Integer.parseInt(user1), "http://www.bbc.com/news/world-us-canada-30538154");
		String chatroom = (String) json.get("table");
		String taxonomy = (String) json.get("taxonomy");
		System.out.println("User "+user1+" logged in into chatroom "+chatroom+" and the taxonomy of the chatrrom is: "+taxonomy);
		System.out.println("==============Logging user2 to the same chatroom as user1==============");
		JSONObject json2 = DbMethods.loginToChatroom(conn, Integer.parseInt(user2), "http://www.bbc.com/news/world-us-canada-30538154");
		String chatroom2 = (String) json.get("table");
		String taxonomy2 = (String) json.get("taxonomy");
		System.out.println("User "+user2+" logged in into chatroom "+chatroom2+" and the taxonomy of the chatrrom is: "+taxonomy2);
		
		System.out.println("==============Saving a message from user1 to the chatroom's table==============");
		boolean bool1 = DbMethods.saveMessage(conn, user1, chatroom, System.currentTimeMillis() / 1000L, "this is msg1",  "http://www.bbc.com/news/world-us-canada-30538154");
		System.out.println("result of saving for user1 was "+bool1);
		System.out.println("==============Saving a message from user2 to the chatroom's table==============");
		Thread.currentThread().sleep(3000);
		DbMethods.saveMessage(conn, user2, chatroom2, System.currentTimeMillis() / 1000L, "this is msg2",  "http://www.bbc.com/news/world-us-canada-30538154");
		System.out.println("result of saving for user2 was "+bool1);
		
		System.out.println("==============Retreiving messages for user1 from the chatroom's table==============");
		if(DbMethods.getMessages(conn, user1, chatroom )!= null){
			System.out.println("There are messages to be printed!");
		}
		

		
		

	}

}
