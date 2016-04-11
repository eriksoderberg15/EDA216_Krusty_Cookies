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
public class SearchCookiePane extends BasicPane {
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

    private boolean blockcheck = false;

    private static String COOKIEPANEL = "Search by cookies";
    private DefaultListModel<String> palletResultListModel;
    private JList<String> palletResultList;
    private JComboBox<String> cookieChoice;

    public SearchCookiePane(Database db) {
        super(db);
    }

    public JComponent createTopPanel() {
        JPanel p = new JPanel();
        return p;
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

        cookieListModel = new DefaultListModel<String>();

        cookieNameList = new JList<String>(cookieListModel);
        cookieNameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cookieNameList.setPrototypeCellValue("123456789012");
        cookieNameList.addListSelectionListener(new CookieSelectionListener());
        JScrollPane p1 = new JScrollPane(cookieNameList);

        JCheckBox block = new JCheckBox("Block the chosen cookie");
        block.addItemListener(new BlockItemListener());

        JPanel p = new JPanel();
        p.setLayout(new GridLayout(2, 1));
        p.add(p1);
        p.add(block);
        return p;
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

        for (String c : cookies) {
            cookieListModel.addElement(c);
        }
        cookieNameList.setModel(cookieListModel);
    }

    public void clearLists() {
        clearMessage();
        palletResultListModel.removeAllElements();
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
         * @param e The selected list item.
         */
        public void valueChanged(ListSelectionEvent e) {
            if (cookieNameList.isSelectionEmpty()) {
                return;
            }
            clearMessage();
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
            String cookieName = cookieNameList.getSelectedValue();
            if(blockcheck){
                System.out.println("BLOCK");
                db.blockAllPallets(cookieName);
            }
            ArrayList<String> pallet = db.findPalletsContainingCookieList(cookieName);
            System.out.println(pallet.isEmpty());
            if (!pallet.isEmpty()) {
                for (String p : pallet) {
                    palletResultListModel.addElement(p);
                }
                displayMessage("Pallets for " + cookieName + " is displayed");
                palletResultList.setModel(palletResultListModel);
            }


//            if(!(cookieChoice.getSelectedIndex() == 0)){
//                String cookie = cookieChoice.getSelectedItem().toString();
//               ArrayList<String> palletList = db.findPalletsContainingCookieList(cookie);
//                for(String pallet : palletList){
//                    palletResultListModel.addElement(pallet);
//                }
//                displayMessage("The list is displaying all pallets for cookie: "+ cookie);
//                palletResultList.setModel(palletResultListModel);
//            }else{
//                displayMessage("Choose a cookie and search again");
//                return;
//            }
        }
    }

    private class BlockItemListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            int state = e.getStateChange();
            if (state == ItemEvent.SELECTED) {
                blockcheck = true;
            }else if(state == ItemEvent.DESELECTED){
                blockcheck = false;
        }
    }

}}
