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
	/**
	 * Method giving available cookie
	 * @return List of cookieNames
	 */
	public ArrayList<String> showCreatableCookies(){
		ArrayList<String> cookieNames = new ArrayList<String>();
		String sql = "SELECT cookieName FROM Cookies";
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				cookieNames.add(rs.getString("cookieName"));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			try {
				ps.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return cookieNames;

	}
	public int createPallet(String cookieName){
		/* 1. läs in alla kaktyper som går att skapa
		 * 2. tryck på ett kaknamn
		 * 3. fyll i hur många som ska produceras
		 * 4. Skapa en pallet i table Pallet
		 * 5. Subtrahera ner ingredienserna i lagret
		 * 6. Om det inte finns tillräckligt med ingredienser måste vi printa ut det i GUI:t
		 */
		if(updateStorage(cookieName)){
			//Om det går: skapa då palletten
		}


		return 0; //bör vara palletnumber
	}
	//	public int nbrOfPalletsInInterval(String start, String end, String cookieName){
	//		PreparedStatement prepStmt = null;
	//		try{
	//			String sql = "SELECT count(*) FROM Pallets WHERE prodDate > start and prodDate < end"
	//					+ "  and cName = cookieName";
	//			prepStmt = conn.prepareStatement(sql);
	//			
	//			ResultSet rs = prepStmt.executeQuery();
	//			if(rs.next()){
	//				String userN= rs.getString("userName");
	//				CurrentUser.instance().loginAs(userN); 	//Vad var current user nu igen?
	//				System.out.println(userN + "is logged in");
	//				return 0;
	//			}else
	//				return 0;
	//		}catch(SQLException e){
	//			System.out.println("Det gick inte att kolla om användaren existerar:" + " "); //English?
	//			e.printStackTrace();
	//			return 0;
	//		}
	//	}
	public boolean updateStorage(String cookieTypeMade){
		//Använder en hashmap för att mappa kvantitet till varje ingrediensnamn
		HashMap<String, Integer> map = new HashMap<String, Integer>();

		//Läs in receptet för kakjäveln
		PreparedStatement ps = null;
		String sqlFetchRecipe = "SELECT * FROM IngredientsInCookies where cookieName = cookieTypeMade";	
		String sqlIngrAmount = "SELECT stockAmount FROM Ingredients where ingredientName = tempIngName";
		String sqlSubtract = "UPDATE Ingredients SET stockAmount = stockAmount - amountNeeded WHERE ingredientName = tempIngName";
		try{
			ps = conn.prepareStatement(sqlFetchRecipe);
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				String ingredientName= rs.getString("ingredientName");
				String amountString = rs.getString("amount");
				int amount = Integer.parseInt(amountString);
				System.out.println(ingredientName);
				System.out.println(amount);
				map.put(ingredientName, amount);
			}
			/*Nu har vi läst hur mycket som vi kommer behöva av varje ingrediens
			 * Så nu vill vi kolla om vi kan subtrahera detta från råvarulagret.
			 */

			PreparedStatement prepStmt = null;
			for(String key: map.keySet()) {
				String tempIngName = key;
				int amountNeeded = map.get(key); //Så mycket vi behöver för att baka kakan
				//Här vill vi hämta mängd för ingrediensen i råvarulagret
				prepStmt = conn.prepareStatement(sqlIngrAmount);
				ResultSet res = prepStmt.executeQuery();
				String ingredientAmount = rs.getString("stockAmount");
				int ingAmountInt = Integer.parseInt(ingredientAmount);

				if(ingAmountInt<amountNeeded){
					//I något av ingredienserna fanns det inte tillräckligt
					conn.rollback();
					System.out.println("Det fanns inte tillräckligt med ingredienser i råvarulagret för att baka kakan");
					return false;
				}else{
					prepStmt = conn.prepareStatement(sqlSubtract);
					ResultSet noNeedOf = prepStmt.executeQuery();
					return true;
				}
			}
		}catch(SQLException e){
			try {
				conn.rollback();
				return false;
			} catch (SQLException e1) {
				e1.printStackTrace();
				return false;
			}

		}

	}


	/*
	 * Search-metoderna
	 */

	public ArrayList<Integer>findPalletsContainingCookie(String cookieToFind){
		ArrayList<Integer> thePallets = new ArrayList<Integer>();
		String findPallets = "SELECT palletNbr FROM Pallets where cookieName = cookieToFind";
		PreparedStatement ps = null;
		try{
			ps = conn.prepareStatement(findPallets);
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				thePallets.add(rs.getInt("palletNbr"));
			}	
		}catch(SQLException e){
			e.printStackTrace();
			System.out.println("troligtvis fanns det inga pallets inglagda i systemet därav nullpointer typ");
		}
		return thePallets;
	}

	public ArrayList<String> findBlockedCookies(){
		ArrayList<String> blockedCookies = new ArrayList<String>(); 
		String findBlocked = "SELECT distinct cookieName FROM Pallets where state = blocked";
		PreparedStatement ps = null;
		try{
			ps = conn.prepareStatement(findBlocked);
			ResultSet rs = ps.executeQuery();

			while(rs.next()){
				blockedCookies.add(rs.getString("cookieName")); //ingen aning om det är så här man ska göra
			}
		}catch(SQLException e){
			e.printStackTrace();
		}
		return blockedCookies;
	}

	public ArrayList<Integer> findBlockedPallets(){
		ArrayList<Integer> blockedPallets = new ArrayList<Integer>(); 
		String findBlocked = "SELECT distinct palletNbr FROM Pallets where state = blocked";
		PreparedStatement ps = null;
		try{
			ps = conn.prepareStatement(findBlocked);
			ResultSet rs = ps.executeQuery();

			while(rs.next()){
				blockedPallets.add(rs.getInt("palletNbr")); //Fel?
			}
		}catch(SQLException e){
			e.printStackTrace();
		}
		return blockedPallets;
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
		return true;
	}

}
