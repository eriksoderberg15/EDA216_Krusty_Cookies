import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * Created by christineboghammar on 10/04/16.
 */
public class SearchPalletNbrPane extends BasicPane {
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
//
//            /**
//             * The list model for the performance date list.
//             */
//            private DefaultListModel<String> dateListModel;
//
//            /**
//             * The performance date list.
//             */
//            private JList<String> dateList;

    /**
     * The text fields where the cookie data is shown.
     */
    private JTextField[] fields;

    /**
     * The number of the palletNbr field to show produced pallet.
     */
    private static final int PALLET_NBR = 0;

    /**
     * The number of the cookie name field.
     */
    private static final int COOKIE_NAME = 1;

    /**
     * The number of the date the pallet was produced field.
     */
    private static final int PALLET_DATE = 2;

    /**
     * The number of the time the pallet was produced field.
     */
    private static final int PALLET_TIME = 3;

    /**
     * The total number of fields.
     */
    private static final int NBR_FIELDS = 4;

    /**
     * Create the Produce Pallets pane.
     *
     * @param db
     *            The database object.
     */
    public SearchPalletNbrPane(Database db) {
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
        cookieNameList.addListSelectionListener(new SearchPalletNbrPane.CookieSelectionListener());
        JScrollPane p1 = new JScrollPane(cookieNameList);

//            dateListModel = new DefaultListModel<String>();

//            dateList = new JList<String>(dateListModel);
//            dateList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//            dateList.setPrototypeCellValue("123456789012");
//            dateList.addListSelectionListener(new DateSelectionListener());
//            JScrollPane p2 = new JScrollPane(dateList);

        JPanel p = new JPanel();
        p.setLayout(new GridLayout(1, 2)); //ViewportLayout()??
        p.add(p1);
//            p.add(p2);
        return p;
    }

    /**
     * Create the top panel, containing the fields with the performance data.
     *
     * @return The top panel.
     */
    public JComponent createTopPanel() {
        String[] texts = new String[NBR_FIELDS];
        texts[COOKIE_NAME] = "Cookie";
        texts[PALLET_NBR] = "Pallet Number";
        texts[PALLET_DATE] = "Date";
        texts[PALLET_TIME] = "Time";

        fields = new JTextField[NBR_FIELDS];
        for (int i = 0; i < fields.length; i++) {
            fields[i] = new JTextField(20);
            fields[i].setEditable(false);
        }

        JPanel input = new InputPanel(texts, fields);

        JPanel p1 = new JPanel();
        p1.setLayout(new FlowLayout(FlowLayout.LEFT));
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
                new SearchPalletNbrPane.ActionHandler());
    }

    /**
     * Perform the entry actions of this pane: clear all fields, fetch the cookie
     * names from the database and display them in the name list.
     */
    public void entryActions() {
        clearMessage();
        fillNameList();
    }

    /**
     * Fetch cookie names from the database and display them in the name list.
     */
    private void fillNameList() {
        cookieListModel.removeAllElements();
        ArrayList<String> cookies = db.showCreatableCookies();

        for(String c : cookies){
            cookieListModel.addElement(c);
        }
        //db.getCookieNames(cookieListModel();
        //cookieNameList.setModel(cookieListModel);
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
            clearMessage();
            String cookieName = cookieNameList.getSelectedValue();
            System.out.println("Vi har tryckt på cookie: " + cookieName);
            cookieNameLabel.setText(cookieName);
        }
    }

    /**
     * A class that listens for Produce Pallet button clicks.
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
//                fillFields(pallet);
                displayMessage("One pallet of " + cookieName + " was successfully produced!");
            }else{
                displayMessage("The pallet could not be produced, not enough ingredients.");
            }

        }
    }
}

