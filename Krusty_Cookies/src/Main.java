
public class Main {

	/*
	 * Main class creating the database instance and setting up the connection
	 * 
	 * Also creating and booting the GUI
	 */
	public static void main(String[] args) {
		Database db = new Database();
		new ProductionGUI(db);
		
	}

}
