

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import com.mysql.jdbc.Connection;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import java.util.Date;



/**
 * Servlet implementation class ChatServlet
 */
public class ChatServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	Connection conn;

    /**
     * Default constructor. 
     */
    public ChatServlet() {
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		// TODO Auto-generated method stub
		MysqlDataSource dataSource = new MysqlDataSource();
		dataSource.setUser("");
		dataSource.setPassword("");
		dataSource.setPort(3306);
		dataSource.setDatabaseName("ChatroomDB");
		dataSource.setServerName("chatroom.cm4hjrblo4cc.us-east-1.rds.amazonaws.com");
		try {
			conn = (Connection) dataSource.getConnection();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		//add access control header
		response.addHeader("Access-Control-Allow-Origin", "*");
		
		try {
			String reqType = request.getParameter("req");
			PrintWriter out  = response.getWriter();
			
			if(reqType!=null && reqType.equals("signup")) {
				String userId = DbMethods.getNewUserId(conn);
				out.write(userId);
			}
			
			out.flush();
			out.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @throws ParserConfigurationException 
	 * @throws SAXException 
	 * @throws XPathExpressionException 
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */

	@SuppressWarnings("unchecked")
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	// TODO Auto-generated method stub
			//Do taxonoly analysis on the received url:

			//String taxonomy = TaxonomyAnalysis.analysis(url);
			//String taxonomyTable = taxonomy.replaceAll("\\", "");
			//check if 
			
		//add access control header
		response.addHeader("Access-Control-Allow-Origin", "*");
		
		try {
			PrintWriter out  = response.getWriter();
			JSONObject responseObject = new JSONObject();
			
			String type = request.getParameter("type");
			
			String userId = request.getParameter("userId");
			String url = request.getParameter("url");
			System.out.println(url);
			String message = null;
			String chatroom = null;
			
			Date date= new java.util.Date();
			long currentTimeStamp = System.currentTimeMillis() / 1000L;
			
			if(type.equals("sendMessage")) {
				message = request.getParameter("message");
				chatroom = request.getParameter("chatroom");
				
				//save message with userId, url, message, currentTimestamp
				boolean saved = DbMethods.saveMessage(conn, userId, chatroom, currentTimeStamp, message, url);
				
				//Response to client
				if(saved) {
					responseObject.put("status", "ok");
				}
				else {
					responseObject.put("status", "failed_to_send_message");
				}
			}
			else if(type.equals("getMessages")) {
				chatroom = request.getParameter("chatroom");
				
				//get messages with userId, url, currentTimestamp
				JSONArray messages = DbMethods.getMessages(conn, userId, chatroom);
				System.out.println(messages);
				
				if(messages!=null) {
					responseObject.put("status", "ok");
					responseObject.put("messages", messages);
				}
				else {
					responseObject.put("status", "failed_to_get_messages");
				}
			}
			else if(type.equals("loginToChatRoom")) {
				
				responseObject = DbMethods.loginToChatroom(conn, Integer.parseInt(userId), url);
				responseObject.put("status", "ok");
			}
			
			out.print(responseObject);
			out.flush();
			out.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public void destroy() {
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
