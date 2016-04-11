/**
 * Created by christineboghammar on 09/04/16.
 */
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;


public class ProducePane extends BasicPane {
            private static final long serialVersionUID = 1;
            /**
             * A label to show a cookielist.
             */
            private JLabel cookieNameLabel;

            /**
             * The list model for the cookiename list.
             */
            private DefaultListModel<String> cookieListModel;

            /**
             * The cookiename list.
             */
            private JList<String> cookieNameList;

            /**
             * The text fields where the cookie data is shown.
             */
            private JTextField[] fields;

            /**
             * The number of the cookie name field.
             */
            private static final int COOKIE_NAME = 0;

            /**
             * The number of the date the pallet was produced field.
             */
            private static final int PALLET_DATE = 1;


            /**
             * The total number of fields.
             */
            private static final int NBR_FIELDS = 2;

            /**
             * Create the Produce Pallets pane.
             *
             * @param db
             *            The database object.
             */
            public ProducePane(Database db) {
                super(db);
            }

            /**
             * Create the left panel, containing the movie name list and the performance
             * date list.
             *
             * @return The left panel.
             */
        public JComponent createLeftPanel() {
            cookieListModel = new DefaultListModel<String>();

            cookieNameList = new JList<String>(cookieListModel);
            cookieNameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            cookieNameList.setPrototypeCellValue("123456789012");
            cookieNameList.addListSelectionListener(new CookieSelectionListener());
            JScrollPane p1 = new JScrollPane(cookieNameList);
            JPanel p = new JPanel();
            p.setLayout(new GridLayout(1, 2));
            p.add(p1);
            return p;
        }

        /**
         * Create the top panel, containing the fields with the cookie data.
         *
         * @return The top panel.
         */
        public JComponent createTopPanel() {
            String[] texts = new String[NBR_FIELDS];
            texts[COOKIE_NAME] = "Cookie";
            texts[PALLET_DATE] = "Date";


            fields = new JTextField[NBR_FIELDS];
            for (int i = 0; i < fields.length; i++) {
                fields[i] = new JTextField(20);
                fields[i].setEditable(false);
            }

            JPanel input = new InputPanel(texts, fields);

            JPanel p1 = new JPanel();
            p1.setLayout(new FlowLayout(FlowLayout.CENTER));
            p1.add(new JLabel("Chosen cookie: "));
            cookieNameLabel = new JLabel("");
            p1.add(cookieNameLabel);

            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.add(p1);
            p.add(input);
            return p;
        }

        /**
         * Create the bottom panel, containing the produce pallet-button and the
         * message line.
         *
         * @return The bottom panel.
         */
        public JComponent createBottomPanel() {
            JButton[] buttons = new JButton[1];
            buttons[0] = new JButton("Produce Pallet");
            return new ButtonAndMessagePanel(buttons, messageLabel,
                    new ActionHandler());
        }

        /**
         * Perform the entry actions of this pane: clear all fields, fetch the cookie
         * names from the database and display them in the name list.
         */
        public void entryActions() {
            clearMessage();
            fillNameList();
            clearFields();
        }

        /**
         * Fetch cookie names from the database and display them in the name list.
         */
        public void fillNameList() {
            cookieListModel.removeAllElements();
            ArrayList<String> cookies = db.showCreatableCookies();

            for(String c : cookies){
                cookieListModel.addElement(c);
            }
            cookieNameList.setModel(cookieListModel);
        }

    /**
     * Fills the fields with information regarding a produced pallet.
     * @param pallet, list with info for a pallet.
     */
    private void fillFields(ArrayList<String> pallet) {
            fields[COOKIE_NAME].setText(pallet.get(0));
            fields[PALLET_DATE].setText(pallet.get(1));

        }

        /**
         * Clear all text fields.
         */
        private void clearFields() {
            for (int i = 0; i < fields.length; i++) {
                fields[i].setText("");
            }
        }

        /**
         * A class that listens for clicks in the cookie list.
         */
        class CookieSelectionListener implements ListSelectionListener {
            /**
             * Called when the user selects a cookie in the name list. Fetches
             * performance dates from the database and displays them in the date
             * list.
             *
             * @param e
             *            The selected list item.
             */
            public void valueChanged(ListSelectionEvent e) {
                if (cookieNameList.isSelectionEmpty()) {
                    return;
                }
                clearFields();
                clearMessage();
                String cookieName = cookieNameList.getSelectedValue();
                cookieNameLabel.setText(cookieName);
            }
        }

        /**
         * A class that listens for button clicks.
         */
        class ActionHandler implements ActionListener {
            /**
             * Called when the user clicks the produce pallet button. One pallet is
             * produced for the chosen cookie.
             * @param e
             *            The event object (not used).
             */
            public void actionPerformed(ActionEvent e) {
                if (cookieNameList.isSelectionEmpty() ){
                    return;
                }
                String cookieName = cookieNameList.getSelectedValue();
                ArrayList<String> pallet = db.createPallet(cookieName);
                if(!pallet.isEmpty()) {
                    fillFields(pallet);
                    displayMessage("One pallet of " + cookieName + " was successfully produced!");
                }else{
                    displayMessage("The pallet could not be produced, not enough ingredients.");
                }

                }
            }
    }
