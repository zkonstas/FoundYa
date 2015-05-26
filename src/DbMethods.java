import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.xml.sax.SAXException;

import com.alchemyapi.api.*;
import com.mysql.jdbc.Connection;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;



public class DbMethods {
	
	
	public static String getNewUserId(Connection conn) throws SQLException  {

		// Generate the SQL query.
		String query = "select count(*) AS count from Persons";
		System.out.println(conn.toString());

		// Get the query results and display them.
		Statement sqlStatement = (Statement) conn.createStatement();
		ResultSet sqlResult = sqlStatement.executeQuery(query);
		int dbSize = 0;
        while(sqlResult.next()){
            dbSize = sqlResult.getInt("count");
            System.out.println("dbsize: "+dbSize);
        }
        //getting what last ID was
        String lastID = "SELECT ID FROM Persons ORDER BY ID LIMIT 1 OFFSET " + String.valueOf(dbSize-1) ;
        ResultSet sqlResult2 = sqlStatement.executeQuery(lastID);

        int newID = 0;
        while(sqlResult2.next()){
        	newID = sqlResult2.getInt("ID");
        }
        
        newID = newID+1;
        
        //create new user and insert into table
        String newUser = "insert into Persons (UserName) values ('User_"+String.valueOf(newID) +"')" ;
        try {
			int userAdded = sqlStatement.executeUpdate(newUser);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
		return String.valueOf(newID);
	}
	
	public static boolean saveMessage(Connection conn, String userId, String chatroom, long timestamp, String message, String url) throws SQLException {
		//save the message to the db

		String query = "insert into "+ chatroom +" (UserId, Message, MessageTimestamp, Url) values ("
				+ userId
				+",'"
				+message
				+"',"
				+String.valueOf(timestamp)
				+",'"
				+url
				+"') ";
		
		Statement sqlStatement = (Statement) conn.createStatement();
		int result = sqlStatement.executeUpdate(query);
		System.out.println("save: "+result);
		return true;
	}

	@SuppressWarnings("unchecked")
	public static JSONObject loginToChatroom (Connection conn,int userId, String url) throws XPathExpressionException, IOException, SAXException, ParserConfigurationException, SQLException{
		
		JSONObject responseObject = new JSONObject();
		//long unixTime = System.currentTimeMillis() / 1000L; // get current time
		
		responseObject = getChatroom(conn, url);
		String chatroom = (String) responseObject.get("table");
		
		Statement checkExisting = (Statement) conn.createStatement();
		String checkIfUserIdExists = "select UserId from UserChatroomLogin where UserId="+
		String.valueOf(userId)+" and Chatroom='"+chatroom+"'";
		ResultSet userLogged = checkExisting.executeQuery(checkIfUserIdExists);
		
		if(userLogged.next()) {
			responseObject.put("loginType", "existingUser");
			System.out.println("existing ");
		}
		else {
			Statement sqlStatement = (Statement) conn.createStatement();
			String addUserToChatroomLoginTable = "insert into UserChatroomLogin (UserId,Chatroom,LastTimeStamp) values ("+
			String.valueOf(userId)+",'"
			+chatroom+"',"
			+String.valueOf(System.currentTimeMillis() / 1000L)+")";
			sqlStatement.executeUpdate(addUserToChatroomLoginTable);
			
			responseObject.put("loginType", "newUser");
		}

		return responseObject;
	}
	
	
	// In get messages we first get the lastTimeStamp of this user in this Chatroom from UserChatroomLogin
	// then we retrieve all the messages between the lastTimeStamp and current Timestamp from the table associated with this chatroom for this user
	// and we finally change the lastTimeStamp in update the lastTimeStamp in UserChatroomLogin to the current time.
	//public static JSONArray getMessages(Connection conn, int userId, String chatroom, int lastTimestamp, String message) {
	@SuppressWarnings("unchecked")
	public static JSONArray getMessages(Connection conn, String userId, String chatroom) throws SQLException {
		
		long currentTimeStamp = System.currentTimeMillis() / 1000L; //get current time
		JSONArray messages = new JSONArray();
		
		Statement sqlStatement = (Statement) conn.createStatement();
		String getLastTimeStamp = "select LastTimeStamp from UserChatroomLogin where UserId = "+userId+ " and Chatroom = '"+chatroom+"'";
		ResultSet sqlResult = sqlStatement.executeQuery(getLastTimeStamp);
		
		long lastTimeStamp = 0;
        while(sqlResult.next()){
        	lastTimeStamp = sqlResult.getLong("LastTimeStamp");
        }
        
        System.out.println("time: "+lastTimeStamp);
        
        //Now retrieve the messages between the lastTimeStamp and currentTimeStamp for this user in this Chatroom
        String retrievedMessages = "select * from "+chatroom+" where UserId != "+String.valueOf(userId)+" and MessageTimestamp >= "+ 
        		String.valueOf(lastTimeStamp) +
        		" and MessageTimestamp < " + String.valueOf(currentTimeStamp) +
        		" order by MessageTimestamp";
        
        ResultSet sqlResult1 = sqlStatement.executeQuery(retrievedMessages);
        while(sqlResult1.next()){
        	
            JSONObject json = new JSONObject();
            
            int senderId = sqlResult1.getInt("userId");
            String msg = sqlResult1.getString("Message");
            long msgTimeStamp = sqlResult1.getLong("MessageTimestamp");
            String msgUrl = sqlResult1.getString("Url");
            System.out.println("SenderId is "+String.valueOf(senderId)+" msg is: "+msg+" msfTimeStamp is: "+String.valueOf(msgTimeStamp) );
            json.put("userId", senderId);
            json.put("Message", msg);
            json.put("MessageTimestamp", msgTimeStamp);
            json.put("Url", msgUrl);
            if (json != null){
                messages.add(json);     

            }
        }
		
        //Now that you have retrieved and saved messages between current and last time stamp, update the lastTimeStamp for this
        //user in this chatroom in the UserChatroomLogin
        String updateLastTimeStamp = "UPDATE UserChatroomLogin SET LastTimeStamp = " + String.valueOf(currentTimeStamp)+
        		" WHERE UserId = "+String.valueOf(userId)+
        		" AND Chatroom = '"+chatroom+"'";
        int updateLastTimeStampDone = sqlStatement.executeUpdate(updateLastTimeStamp);
		return messages;
	}
	
	
	//analyze url
	//check if taxonomy table exists and if not create it and return the table name for that
	@SuppressWarnings("unchecked")
	public static JSONObject getChatroom (Connection conn, String url) throws XPathExpressionException, IOException, SAXException, ParserConfigurationException, SQLException{
		
		String taxonomy = TaxonomyAnalysis.analysis(url);
		Statement sqlStatement = (Statement) conn.createStatement();
		String query = "";
		String table = "";
		JSONObject responseObject = new JSONObject();
		
		//taxonomy is not null, check whether table for that taxonomy exists or not
		if (taxonomy != " " && taxonomy != null){

			System.out.println("taxonomy is not null!");

			query = "Select TableName from ChatroomsInfo where Taxonomy ='"+taxonomy+"'";
			ResultSet sqlResult = sqlStatement.executeQuery(query);
	        while(sqlResult.next()){
	            table = sqlResult.getString("TableName");

	            System.out.println(table.toString());

	        }
		}
		//check if table is not null, if not then the table for that taxonomy exists and we return that
		if (table != "" && table != null){
			responseObject.put("taxonomy", taxonomy);
			responseObject.put("table", table);
			return responseObject;
		}
		//if table does not exists add a new record to chatrooms with a new chatroom id, chatroom taxonomy 
		//and the associated table name for the table related to that taxonomy. Create a table for that taxonomy with
		//that new table name
		else if (taxonomy == " " || taxonomy == null){ // taxonomy is null and no taxonomy exists for the given url, 
													   //so assign the users to the general table 
			responseObject.put("taxonomy", "");
			responseObject.put("table", "generalChatroom");
			return responseObject;
		}
		else { //taxonomy is not empty but a table for that does not exist in the database, so create one table for this taxonomy
			
			String query1 = "select count(*) AS count from ChatroomsInfo";
			ResultSet sqlResult = sqlStatement.executeQuery(query1);
			int tableSize = 0;
	        while(sqlResult.next()){
	        	tableSize = sqlResult.getInt("count");
	        }
	        System.out.println("Size of ChatroomsInfo is "+tableSize);
	        String getLastTableID = "SELECT Id FROM ChatroomsInfo ORDER BY Id LIMIT 1 OFFSET " + String.valueOf(tableSize-1);
	        
	        ResultSet sqlResult3 = sqlStatement.executeQuery(getLastTableID);
	        int newID = 0;
	        while(sqlResult3.next()){
	        	newID = sqlResult3.getInt("Id");
	        }
	        newID = newID+1;
	        
	        // Add the new table information to ChatroomsInfo table
	        String addTableToChatroomsInfo = "Insert Into ChatroomsInfo (Taxonomy, TableName)Values ('"
	        +taxonomy+"','"
	        +"c_"+String.valueOf(newID)+"')";
	        int tableAddedToChatroomsInfo = sqlStatement.executeUpdate(addTableToChatroomsInfo);
	        
	        // Now create a table with the name 
	        String createTable = "create table c_"+String.valueOf(newID)+
	        		" (UserId  int, Message mediumtext, MessageTimestamp bigint, Url varchar(255))";
	        int tableCreated = sqlStatement.executeUpdate(createTable);	
	        responseObject.put("taxonomy", taxonomy);
	        responseObject.put("table", "c_"+String.valueOf(newID));
	        return responseObject;
		}
	}
}
