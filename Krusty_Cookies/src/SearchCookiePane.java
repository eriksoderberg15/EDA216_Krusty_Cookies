import javax.swing.*;
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

    private static String COOKIEPANEL = "Search by cookies";
    private DefaultListModel<String> palletResultListModel;
    private JList<String> palletResultList;
    private JComboBox<String> cookieChoice;

    public SearchCookiePane(Database db){
        super(db);
    }

    public JComponent createTopPanel(){
        JPanel p1 = new JPanel();
        return p1;
    }

    public JComponent createMiddlePanel(){
        JPanel p = new JPanel();
        p.setLayout(new GridLayout(1,1));
        palletResultListModel = new DefaultListModel<String>();
        palletResultList = new JList<String>(palletResultListModel);
        JScrollPane p1 = new JScrollPane(palletResultList);
        p.add(p1);
        return p;
    }

    public JComponent createBottomPanel(){
        JButton[] buttons = new JButton[1];
        buttons[0] = new JButton("Search");
        ActionHandler ah = new ActionHandler();
        return new ButtonAndMessagePanel(buttons, messageLabel, ah);
    }

    public JComponent createLeftPanel(){
        JPanel p = new JPanel();
        p.setLayout(new GridLayout(1,1));
        JPanel comboBoxPane = new JPanel();
        comboBoxPane.setLayout(new GridLayout(3,1));

        ArrayList<String> cookies = db.showCreatableCookies();
        String cookieBox[]= (String[]) cookies.toArray();
        cookieChoice = new JComboBox<String>(cookieBox);
        cookieChoice.setEditable(false);
        cookieChoice.addItemListener(new ItemHandler());
        comboBoxPane.add(cookieChoice);

        JButton block = new JButton("Block");
        block.addActionListener(new BlockActionListener());
        comboBoxPane.add(block);

        JPanel initial = new JPanel();
        p.add(comboBoxPane, BorderLayout.CENTER);
        return p;

    }

    public void clearLists(){
        clearMessage();
        palletResultListModel.removeAllElements();
    }

    /**
     * Actionhandler listens to Search button..
     */
    private class ActionHandler implements ActionListener{
        public void actionPerformed(ActionEvent e){
            clearLists();
            if(!(cookieChoice.getSelectedIndex() == 0)){
                String cookie = cookieChoice.getSelectedItem().toString();
               ArrayList<String> palletList = db.findPalletsContainingCookieList(cookie);
                for(String pallet : palletList){
                    palletResultListModel.addElement(pallet);
                }
                displayMessage("The list is displaying all pallets for cookie: "+ cookie);
                palletResultList.setModel(palletResultListModel);
            }else{
                displayMessage("Choose a cookie and search again");
                return;
            }
        }
    }

    private class ItemHandler implements ItemListener {
        @Override
        public void itemStateChanged(ItemEvent e) {
            clearLists();
        }
    }

    private class BlockActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            clearLists();
            if(!(cookieChoice.getSelectedIndex() == 0)){
                String cookie = cookieChoice.getSelectedItem().toString();
                db.blockAllPallets(cookie);
                ArrayList<String> palletList = db.findPalletsContainingCookieList(cookie);
                for(String pallet : palletList){
                    palletResultListModel.addElement(pallet);
                }
                displayMessage("The list is displaying all (blocked) pallets for cookie: "+ cookie);
                palletResultList.setModel(palletResultListModel);
            }else{
                displayMessage("Choose a cookie and click Block again");
                return;
        }
    }

}}
