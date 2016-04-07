

import java.sql.*;
import java.util.*;

/*
 * Class for all database functionality
 */

public class Database {

	//The database connection.
	private Connection conn;

	/**
	 * Create the database interface object. Connection to the database is
	 * performed later.
	 */
	public Database() {
		conn = null;
	}
	
	/**
	 * Open a connection to the database, using the specified user name and
	 * password.
	 * 
	 * @param userName
	 *            The user name.
	 * @param password
	 *            The user's password.
	 * @return true if the connection succeeded, false if the supplied user name
	 *         and password were not recognized. Returns false also if the JDBC
	 *         driver isn't found.
	 */
	public boolean openConnection(String userName, String password) {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(
					"jdbc:mysql://puccini.cs.lth.se/" + userName, userName,
					password);
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * Close the connection to the database.
	 */
	public void closeConnection() {
		try {
			if (conn != null) {
				conn.close();
			}
		} catch (SQLException e) {
		}
		conn = null;
	}
	/**
	 * Check if the connection to the database has been established
	 * 
	 * @return true if the connection has been established
	 */
	public boolean isConnected() {
		return conn != null;
	}
	
	public boolean isUser(String UID){
		//Creates the statement needed to see if the user exists
		PreparedStatement prepStmt = null;
		try{
			String sql = "SELECT * FROM Users WHERE UserName = ? "; //Istället för entiteten users kanske vi har cookie workers?
			prepStmt = conn.prepareStatement(sql);
			prepStmt.setString(1, UID);
			//Checks if the result Set has 1 "next" or a first object, hence whether its empty or not
			ResultSet rs = prepStmt.executeQuery();
			if(rs.next()){
				String userN= rs.getString("userName");
				CurrentUser.instance().loginAs(userN); 	//Vad var current user nu igen?
				System.out.println(userN + "is logged in");
				return true;
			}else
				return false;
		}catch(SQLException e){
			System.out.println("Det gick inte att kolla om användaren existerar:" + " "); //English?
			e.printStackTrace();
			return false;
		}
		//If it is not possible to "log in" or find the user one should always close the statement
		finally{
			try{
				prepStmt.close();
			}catch(SQLException e){
				e.printStackTrace();
			}
		}
		
	}
	
	
	// Methods for the Krusty database
	
	public boolean createOrder(){
		return true;
	}
	
	public int nbrOfPallets(String cookieName){
		return 0;
	}
	public void updateStorage(String cookieTypeMade){
		
	}
	public int storageAmountLeft(String ingredientName){
		return 0;
	}
	//Här vill få returnerat hur MYCKET som levererades samt NÄR
	public String lastDelivery (String ingredientName) {
		return "tillsvidare";
	}
	
	public String displayAllRecipees(){
		return "tillsvidare";
	}
	
	/*
	 * Tänker mig att vi har en vektor som input.
	 * Plats 1 anger recept namn, därefter har vi ingredientsnamn1, mängd1, ingredientnamn2, mängd2, etc...??
	 */
	public boolean addRecipee(ArrayList<String> newRecipee){
		return true;
	}
	public String getPalletInfo(int palletId){
		return "Tillsvidare";
	}
	//Hitta alla pallar som bär på kaktypen cookieName
	public ArrayList<Integer> palletsContaining(String cookieName){
		ArrayList<Integer> temp = new ArrayList<Integer>();
		return temp;
	}
	public ArrayList<Integer> palletsProducedInInterval(String start, String end){
		ArrayList<Integer> temp = new ArrayList<Integer>();
		return temp;
	}
	
	//Gives an array of all cookies that are "bad" and hence being blocked at the moment
	public  ArrayList<String> cookiesCurrentlyBlocked(){
		ArrayList<String> temp = new ArrayList<String>();
		return temp;
	}
	
	//Gives an array of all palletIds containing cookies that are currently blocked
	public ArrayList<Integer> findBlocketPallets(){
		ArrayList<Integer> temp = new ArrayList<Integer>();
		return temp;
	}
	//Ingen aning om hur vi ska göra med denna metod...
	public ArrayList<Integer> deliveredPalletsForCosutmer(int costumerId){
		ArrayList<Integer> temp = new ArrayList<Integer>();
		return temp;
	}
	
	public boolean showOrdersForInterval(String start, String end){
		
	}
	
}
