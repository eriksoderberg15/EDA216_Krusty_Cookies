
public class Main {

	/*
	 * Main class creating the database instance and setting up the connection
	 * 
	 * Also creating and booting the GUI
	 */
	public static void main(String[] args) {
		Database db = new Database();
		db.openConnection("db70", "tintinerik");
		new ProductionGUI(db);
		
		/*
		 * 1. Create database object
		 * 2. Set up connection
		 * 3. Start GUI
		 */
	}

}
