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
			e.printStackTrace();
		}
		conn = null;
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

	/**
	 * 
	 * @param cookieName
	 * @return List of PalletInfo:   
	 */
	public ArrayList<String> createPallet(String cookieName){
		ArrayList<String> palletInfo = new ArrayList<String>();

		if(updateStorage(cookieName)){
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			String date = df.format(new Date());
			String createPallet = "INSERT INTO Pallets(cookieName, prodDate, orderId) values(?,?,?)";
			PreparedStatement ps = null;
			try {
				conn.setAutoCommit(false);
				ps = conn.prepareStatement(createPallet);	

				ps.setString(1, cookieName);	
				ps.setString(2, date);	
				ps.setInt(3, 3);	

				ps.executeUpdate();
				palletInfo.add(cookieName);
				palletInfo.add(date);
			} catch (SQLException e) {
				e.printStackTrace();
			}finally {
				try {
					ps.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			return palletInfo;
		}else{
			return palletInfo; 
		}
	}

	public boolean updateStorage(String cookieTypeMade){		
		HashMap<String, Integer> map = new HashMap<String, Integer>();	

		PreparedStatement ps = null;
		String sqlFetchRecipe = "SELECT * FROM IngredientsInCookies where cookieName = ?";	

		try{
			conn.setAutoCommit(false);
			ps = conn.prepareStatement(sqlFetchRecipe);
			ps.setString(1, cookieTypeMade);
			ResultSet rs = ps.executeQuery();

			while(rs.next()){
				String ingredientName= rs.getString("ingredientName");
				String amountString = rs.getString("amount");
				int amount = Integer.parseInt(amountString);
				map.put(ingredientName, amount);
			}
			/*Nu har vi läst hur mycket som vi kommer behöva av varje ingrediens
			 * Så nu vill vi kolla om vi kan subtrahera detta från råvarulagret.
			 */

			//För varje ingrediens i receptet går vi in i råvarulagret och subtraherar ner värdet för varje ingrediens
			for(String tempIngName: map.keySet()) {
				int amountNeeded = map.get(tempIngName); //Mängd som behövs av ingrediensen
				int stockAmount = readStockAmount(tempIngName);
				
				if(stockAmount<amountNeeded){
					//I något av ingredienserna fanns det inte tillräckligt
					conn.rollback();
					return false;
				}else{
					subtractStock(amountNeeded, tempIngName);
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
		return true;
	}

	public void subtractStock(int amountToSubtract, String ingName){
		String sqlSubtract = "UPDATE Ingredients SET stockAmount = stockAmount - ? WHERE ingredientName = ?";
		PreparedStatement prepStmt = null;
		try{
			prepStmt = conn.prepareStatement(sqlSubtract);
			prepStmt.setInt(1, amountToSubtract);
			prepStmt.setString(2, ingName);
			int useless = prepStmt.executeUpdate();
		}catch(SQLException e){
			e.printStackTrace();
		}
	}
	private int readStockAmount(String ingredient){		//method that reads the integer stockamount of an ingredient
		String sqlIngrStockAmount = "SELECT stockAmount FROM Ingredients where ingredientName = ?";
		PreparedStatement prepStmt = null;

		int stockAmount = 0;
		try{
			prepStmt = conn.prepareStatement(sqlIngrStockAmount);
			prepStmt.setString(1, ingredient);	//

			ResultSet res = prepStmt.executeQuery();
			res.next();
			stockAmount = res.getInt("stockAmount");
		}catch(SQLException e){
			e.printStackTrace();
		}
		return stockAmount;
	}

	public HashMap<String, ArrayList<String>> blockAllPallets(String cookieType){
		String blockPallets = "UPDATE Pallets SET isBlocked = 'true' where isBlocked = 'false' and cookieName = ?";
		PreparedStatement ps = null;
		try{
			ps = conn.prepareStatement(blockPallets);
			ps.setString(1, cookieType);
			ps.executeUpdate();
		}catch(SQLException e){
			e.printStackTrace();
		}
		return findBlockedPallets(cookieType);
	}

	private HashMap<String, ArrayList<String>> findBlockedPallets(String cookieName){
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

	public String getPalletInfo(int palletId){
		StringBuilder sb = new StringBuilder();
		String palletInfo = "";
		String getPalletInfo = "SELECT * FROM Pallets where palletNbr = ?";

		PreparedStatement ps = null;

		try{
			ps = conn.prepareStatement(getPalletInfo);
			ps.setInt(1, palletId);

			ResultSet rs = ps.executeQuery();
			rs.next();
			String tempKey = Integer.toString(rs.getInt("palletNbr"));
			sb.append(tempKey + " | ");
			sb.append(rs.getString("cookieName") + " | ");
			sb.append(rs.getString("prodDate") + " | ");
			sb.append(rs.getString("location") + " | ");
			String blocked = rs.getString("isBlocked");
			if(blocked.equals("true"))
				sb.append("Blocked");
			palletInfo = sb.toString();

		}catch(SQLException e){
			e.printStackTrace();
		}
		return palletInfo;
	}
	
	public ArrayList<String> palletInfoForIntervall(String cookieType, String dateStart, String dateEnd){
		ArrayList<String> palletInfoForInterval = new ArrayList<String>();
		String showPalletsForIntervall = "SELECT * FROM Pallets where cookieName = ? and prodDate >= ? and prodDate <= ?";
		PreparedStatement ps = null;
		try{
			ps = conn.prepareStatement(showPalletsForIntervall);
			ps.setString(1, cookieType);
			ps.setString(2, dateStart);
			ps.setString(3, dateEnd);

			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				StringBuilder sb = new StringBuilder();
				String tempKey = Integer.toString(rs.getInt("palletNbr"));
				sb.append(tempKey + " | ");
				sb.append(rs.getString("cookieName") + " | ");
				sb.append(rs.getString("prodDate") + " | ");
				sb.append(rs.getString("location") + " | ");
				String blocked = rs.getString("isBlocked");	
				if(blocked.equals("true")) {
					sb.append("Blocked");
				}
				String pallet = sb.toString();
				palletInfoForInterval.add(pallet);
			}	
		}catch(SQLException e){
			e.printStackTrace();
		}
		return palletInfoForInterval;
	}

	public ArrayList<String> findPalletsContainingCookieList(String cookieToFind){
		ArrayList<String> palletList = new ArrayList<String>();
		String findPallets = "SELECT * FROM Pallets where cookieName = ?";
		PreparedStatement ps = null;
		try{
			ps = conn.prepareStatement(findPallets);
			ps.setString(1, cookieToFind);
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				StringBuilder sb = new StringBuilder();
				String tempKey = Integer.toString(rs.getInt("palletNbr"));
				sb.append(tempKey + " | ");
				sb.append(rs.getString("cookieName")).append(" | ");
				sb.append(rs.getString("prodDate")).append(" | ");
				sb.append(rs.getString("location")).append(" | ");
				String blocked = rs.getString("isBlocked");
				if(blocked.equals("true")) {
					sb.append("Blocked");
				}
				String pallet = sb.toString();
				palletList.add(pallet);
			}
		}catch(SQLException e){
			e.printStackTrace();
		}
		return palletList;
	}
}
