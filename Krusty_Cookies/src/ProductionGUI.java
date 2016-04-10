
import javax.swing.*;
import javax.swing.event.*;

import java.awt.*;
import java.awt.event.*;

/**
 * ProductionGUI is the user interface to the production database. It sets up the main
 * window and connects to the database.
 */
public class ProductionGUI {
    /**
     * db is the database object
     */
    private Database db;

    /**
     * tabbedPane is the contents of the window. It consists of two panes, Produce pallets
     * and Search pallets.
     */
    private JTabbedPane tabbedPane;

    /**
     * Create a GUI object and connect to the database.
     *
     * @param db
     *            The database.
     */
    public ProductionGUI(Database db) {
        this.db = db;

        JFrame frame = new JFrame("Production Database");
        tabbedPane = new JTabbedPane();

        ProducePane producePane = new ProducePane(db);
        tabbedPane.addTab("Produce Pallets", null, producePane,
                "for producing pallets of cookies");

        SearchAllPane searchPane = new SearchAllPane(db);
        tabbedPane.addTab("Search Pallets", null, searchPane, "search for produced pallets");

        SearchCookiePane searchCookiePane = new SearchCookiePane(db);
        tabbedPane.addTab("Search Pallets", null, searchCookiePane, "search for produced pallets");

        SearchByDatePane searchByDatePane = new SearchByDatePane(db);
        tabbedPane.addTab("Search Pallets", null, searchByDatePane, "search for produced pallets");


        tabbedPane.setSelectedIndex(0);

        frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);

        tabbedPane.addChangeListener(new ChangeHandler());
        frame.addWindowListener(new WindowHandler());

        frame.setSize(500, 400);
        frame.setVisible(true);

        producePane.displayMessage("Connecting to database ...");

        if (db.openConnection("db70", "eriktintin")) {
            producePane.displayMessage("Connected to database");
        } else {
            producePane.displayMessage("Could not connect to database, please try again");
        }
    }

    /**
     * ChangeHandler is a listener class, called when the user switches panes.
     */
    class ChangeHandler implements ChangeListener {
        /**
         * Called when the user switches panes. The entry actions of the new
         * pane are performed.
         *
         * @param e
         *            The change event (not used).
         */
        public void stateChanged(ChangeEvent e) {
            BasicPane selectedPane = (BasicPane) tabbedPane
                    .getSelectedComponent();
            selectedPane.entryActions();
        }
    }

    /**
     * WindowHandler is a listener class, called when the user exits the
     * application.
     */
    class WindowHandler extends WindowAdapter {
        /**
         * Called when the user exits the application. Closes the connection to
         * the database.
         *
         * @param e
         *            The window event (not used).
         */
        public void windowClosing(WindowEvent e) {
            db.closeConnection();
            System.exit(0);
        }
    }
}
