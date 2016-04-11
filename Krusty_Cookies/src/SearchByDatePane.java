import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

/**
 * Created by christineboghammar on 10/04/16.
 */
public class SearchByDatePane extends BasicPane {
    private static final long serialVersionUID = 1;

    /**
     * A label to show a cookielist.
     */
    /**
     * The list model for the cookiename list.
     */
    private DefaultListModel<String> cookieListModel;

    /**
     * The cookiename list.
     */
    private JList<String> cookieNameList;

    private JTextField fromInput;
    private JTextField toInput;
    /**
     * Create a BasicPane object.
     *
     * @param db The database object.
     */

    private DefaultListModel<String> palletResultListModel;
    private JList<String> palletResultList;

    public SearchByDatePane(Database db) {
        super(db);
    }

    public JComponent createTopPanel() {
        return new JPanel();
    }

    public JComponent createMiddlePanel() {
        JPanel p = new JPanel();
        p.setLayout(new GridLayout(1, 1));

        palletResultListModel = new DefaultListModel<String>();
        palletResultList = new JList<String>(palletResultListModel);
        JScrollPane p1 = new JScrollPane(palletResultList);
        p.add(p1);
        return p;
    }

    public JComponent createBottomPanel() {
        JButton[] buttons = new JButton[1];
        buttons[0] = new JButton("Search");
        return new ButtonAndMessagePanel(buttons, messageLabel, new ActionHandler());
    }

    public JComponent createLeftPanel() {
        JPanel p = new JPanel();
        p.setLayout(new GridLayout(2, 1));

        cookieListModel = new DefaultListModel<String>();
        cookieNameList = new JList<String>(cookieListModel);
        cookieNameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cookieNameList.setPrototypeCellValue("123456789012");
        cookieNameList.addListSelectionListener(new CookieSelectionListener());
        JScrollPane p1 = new JScrollPane(cookieNameList);
        p.add(p1);

        JPanel date = new JPanel();
        date.setLayout(new GridLayout(2, 2)); //ViewportLayout()??
        JTextField from = new JTextField("Start date (yyyy-mm-dd):");
        from.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        from.setEditable(false);
        fromInput = new JTextField("", 10);
        JTextField to = new JTextField("End date (yyyy-mm-dd):");
        to.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        to.setEditable(false);
        toInput = new JTextField("", 10);
        date.add(from);
        date.add(fromInput);
        date.add(to);
        date.add(toInput);
        p.add(date);
        return p;
    }

    /**
     * Perform the entry actions of this pane: clear all fields, fetch the cookie
     * names from the database and display them in the name list.
     */
    public void entryActions() {
        clearMessage();
        fillNameList();
        fromInput.setText("");
        toInput.setText("");
        palletResultListModel.removeAllElements();
    }

    /**
     * Fetch cookie names from the database and display them in the name list.
     */
    private void fillNameList() {
        cookieListModel.removeAllElements();
        ArrayList<String> cookies = db.showCreatableCookies();

        for (String c : cookies) {
            cookieListModel.addElement(c);
        }
        cookieNameList.setModel(cookieListModel);
    }

    private void clearLists() {
        clearMessage();
        palletResultListModel.removeAllElements();
    }

    /**
     * A class that listens for clicks in the cookie list.
     */
    private class CookieSelectionListener implements ListSelectionListener {
        /**
         * Called when the user selects a cookie in the name list. Fetches
         * performance dates from the database and displays them in the date
         * list.
         *
         * @param e The selected list item.
         */
        public void valueChanged(ListSelectionEvent e) {
            if (cookieNameList.isSelectionEmpty()) {
                return;
            }
            clearMessage();
//            String cookieName = cookieNameList.getSelectedValue();
        }
    }

    /**
     * Actionhandler listens to Search button..
     */
    private class ActionHandler implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            clearLists();
            if (cookieNameList.isSelectionEmpty()) {
                displayMessage("Please select a cookie!");
                return;
            }
                String toDate = toInput.getText();
                String fromDate = fromInput.getText();
            if(toInput.equals("")){
                displayMessage("Type in a start date with format (yyyy-mm-dd)!");
                return;
            }if(fromInput.equals("")){
                displayMessage("Type in an end date with format (yyyy-mm-dd)!");
                return;
            }
            String cookieName = cookieNameList.getSelectedValue();
            ArrayList<String> pallet = db.palletInfoForIntervall(cookieName, fromDate, toDate);
            System.out.println(pallet.isEmpty());
            if (!pallet.isEmpty()) {
                for (String p : pallet) {
                    palletResultListModel.addElement(p);
                }
                displayMessage("Pallets for " + cookieName + " is displayed");
                palletResultList.setModel(palletResultListModel);
            }
        }
    }

}


