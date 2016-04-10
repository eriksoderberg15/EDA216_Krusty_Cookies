import java.sql.*;
import java.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/*
 * Class for all database functionality
 */

public class Database {
	private Connection conn;

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
			e.printStackTrace();
		}finally {
			try {
				ps.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return cookieNames;

	}

	//Allt visas som strängar i GUIt, därav returnerar vi här en lista med strängar

	/**
	 * 
	 * @param cookieName
	 * @return List of PalletInfo:   
	 */
	public ArrayList<String> createPallet(String cookieName){
		ArrayList<String> palletInfo = new ArrayList<String>();
		if(updateStorage(cookieName)){
			//Om det går: skapa då palletten
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			String date = df.format(new Date());
			String createPallet = "INSERT INTO Pallets(palletNbr, cookieName, prodDate, location, isBlocked, orderId) values(?,?,?,?,?,?)";
			PreparedStatement ps = null;
			try {
				conn.setAutoCommit(false);

				ps = conn.prepareStatement(createPallet);	

				ps.setString(1, "pallettNummer");	//Cookie namn
				ps.setString(2, cookieName);	//Cookie namn
				ps.setString(3, date);	//pallet nummer
				ps.setString(4, "location");	//datum
				ps.setString(5, "false");	//tid
				ps.setString(6, "orderIdSpelaringenroll");	//tid

				ps.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return palletInfo;
		}else{
			System.out.println("Det gick inte att skapa palletten för det fanns inte tillräckligt med ingredienser");
			return palletInfo; 
		}
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
		String sqlFetchRecipe = "SELECT * FROM IngredientsInCookies where cookieName = ?";	
		String sqlIngrStockAmount = "SELECT stockAmount FROM Ingredients where ingredientName = ?";
		String sqlSubtract = "UPDATE Ingredients SET stockAmount = stockAmount - ? WHERE ingredientName = ?";

		try{
			ps = conn.prepareStatement(sqlFetchRecipe);
			ps.setString(1, cookieTypeMade);
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
			//För varje ingrediens i receptet går vi in i råvarulagret och subtraherar ner värdet för varje ingrediens
			for(String key: map.keySet()) {
				String tempIngName = key;
				int amountNeeded = map.get(key); //Mängd som behövs av ingrediensen

				prepStmt = conn.prepareStatement(sqlIngrStockAmount);
				prepStmt.setString(1, tempIngName);	//
				ResultSet res = prepStmt.executeQuery();

				String ingredientAmount = res.getString("stockAmount");	
				int stockAmount = Integer.parseInt(ingredientAmount);//Håller på en int som säger hur mycket vi har i lagret av ingrediensen

				if(stockAmount<amountNeeded){
					//I något av ingredienserna fanns det inte tillräckligt
					conn.rollback();
					System.out.println("Det fanns inte tillräckligt med ingredienser i råvarulagret för att baka kakan");
					return false;
				}else{
					prepStmt = conn.prepareStatement(sqlSubtract);
					prepStmt.setInt(1, amountNeeded);
					prepStmt.setString(2, tempIngName);
					ResultSet noNeedOf = prepStmt.executeQuery();
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
		}finally{
			try {
				ps.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return true;	//Om vi inte har kommit in i if:en för stockAmount<amountNeeded så har ju allt gått prima
	}


	/*
	 * Search-metoderna
	 */

	public HashMap<String, ArrayList<String>>findPalletsContainingCookie(String cookieToFind){
		ArrayList<String> tempPalletInfo = new ArrayList<String>();//1: content, 2: prodDate, 3: location, 4: isBlocked
		HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();//Mappar palletId till all info för palleten

		String findPallets = "SELECT * FROM Pallets where cookieName = cookieToFind";
		PreparedStatement ps = null;
		try{
			ps = conn.prepareStatement(findPallets);
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				String tempKey = Integer.toString(rs.getInt("palletNbr"));

				tempPalletInfo.add(rs.getString("cookieName"));
				tempPalletInfo.add(rs.getString("prodDate"));
				tempPalletInfo.add(rs.getString("location"));
				tempPalletInfo.add(rs.getString("isBlocked"));

				map.put(tempKey, tempPalletInfo);
			}	
		}catch(SQLException e){
			e.printStackTrace();
			System.out.println("troligtvis fanns det inga pallets inglagda i systemet därav nullpointer typ");
		}
		return map;
	}

	public HashMap<String, ArrayList<String>> blockAllPallets(String cookieType){
		String blockPallets = "UPDATE Pallets SET isBlocked = true where isBlocked = false and cookieName = ?";
		PreparedStatement ps = null;
		try{
			ps = conn.prepareStatement(blockPallets);
			ps.setString(1, cookieType);
			ResultSet rs = ps.executeQuery();
		}catch(SQLException e){
			e.printStackTrace();
		}
		return findBlockedPallets(cookieType);
	}

	//1: content, 2: prodDate, 3: location, 4: isBlocked
	public HashMap<String, ArrayList<String>> findBlockedPallets(String cookieName){
		HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
		ArrayList<String> tempPalletInfo = new ArrayList<String>(); 
		String findBlocked = "SELECT * FROM Pallets where isBlocked = true";
		PreparedStatement ps = null;
		try{
			ps = conn.prepareStatement(findBlocked);
			ResultSet rs = ps.executeQuery();

			while(rs.next()){
				String tempKey = Integer.toString(rs.getInt("palletNbr"));

				tempPalletInfo.add(rs.getString("cookieName"));
				tempPalletInfo.add(rs.getString("prodDate"));
				tempPalletInfo.add(rs.getString("location"));
				tempPalletInfo.add(rs.getString("isBlocked"));

				map.put(tempKey, tempPalletInfo);
			}
		}catch(SQLException e){
			e.printStackTrace();
		}
		return map;
	}

	public ArrayList<Integer> findBlockedPallets(){
		ArrayList<Integer> blockedPallets = new ArrayList<Integer>(); 
		String findBlocked = "SELECT distinct palletNbr FROM Pallets where isBlocked = true";
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
	public ArrayList<String> getPalletInfo(int palletId){
		ArrayList<String> palletInfo = new ArrayList<String>();
		String getPalletInfo = "SELECT * FROM Pallets where palletNbr = ?";

		PreparedStatement ps = null;

		try{
			ps = conn.prepareStatement(getPalletInfo);
			ps.setInt(1, palletId);

			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				String tempKey = Integer.toString(rs.getInt("palletNbr"));

				palletInfo.add(rs.getString("cookieName"));
				palletInfo.add(rs.getString("prodDate"));
				palletInfo.add(rs.getString("location"));
				palletInfo.add(rs.getString("isBlocked"));

			}
		}catch(SQLException e){
			e.printStackTrace();
		}

		return palletInfo;
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


	public String[] toPalletArray(HashMap<String, ArrayList<String>> palletsContainingCookie) {
		String[] palletList = new String[palletsContainingCookie.size()];
		int index = 0;
		String pallet;
		for(String key : palletsContainingCookie.keySet()){
			StringBuilder sb = new StringBuilder();
			sb.append(key);
			ArrayList<String> palletInfo = palletsContainingCookie.get(key);
			for (int i =0; i<palletInfo.size(); i++){
				sb.append(palletInfo.get(i));
			}
			palletList[index] = sb.toString();
		}
		return palletList;
	}

	public void palletInfoForIntervall(String cookieType, String dateStart, String dateEnd, boolean shouldBlock){
		//antingen kommer man bara vilja displaya pallets för det givna intervallet, eller så kommer man ha velat blocka de först 
		//och sedan displaya. 
		String blockPallets = "UPDATE Pallets SET isBlocked = true where isBlocked = false and cookieName = ? and prodDate >= ? and prodDate <= ?";
		if(shouldBlock){

		}

		//		public void searchByDate(String startDateFormatted, String endDateFormatted, String chosenCookie, String chosenIngr,
		//				String onlyBlocked, DefaultListModel<Pallet> cookieListModel) {
		//			ArrayList<String> input = new ArrayList<String>();
		//			input.add(startDateFormatted + " 00:00:00");
		//			input.add(endDateFormatted + " 23:59:59");
		//			input.add(chosenCookie);
		//			input.add(chosenIngr);
		//			String q = "SELECT * FROM PALLET WHERE bakedate >= ? AND bakedate <=? AND cookieName LIKE ? AND cookieName IN (SELECT cookieName from recipe WHERE ingrname LIKE ?) "
		//					+ onlyBlocked + " ORDER BY bakedate;";
		//			execPrepPalSearchQuery(q, cookieListModel, input, input.size());
		//		}

	}

	public ArrayList<String> findPalletsContainingCookieList(String cookieToFind){
		StringBuilder sb = new StringBuilder();
		ArrayList<String> palletList = new ArrayList<String>();
		String findPallets = "SELECT * FROM Pallets where cookieName = cookieToFind";
		PreparedStatement ps = null;
		try{
			ps = conn.prepareStatement(findPallets);
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				String tempKey = Integer.toString(rs.getInt("palletNbr"));
				sb.append(tempKey + " | ");
				sb.append(rs.getString("cookieName") + " | ");
				sb.append(rs.getString("prodDate") + " | ");
				sb.append(rs.getString("location") + " | ");
				String blocked = rs.getString("isBlocked");
				if(blocked.equals(true)) {
					sb.append("Blocked");
				}
				palletList.add(sb.toString());
				sb.setLength(0);
			}
		}catch(SQLException e){
			e.printStackTrace();
			System.out.println("något hände med databasen");
		}
		return palletList;
	}
}
