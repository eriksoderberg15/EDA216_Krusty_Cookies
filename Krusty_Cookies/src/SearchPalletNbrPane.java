import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by christineboghammar on 10/04/16.
 */
public class SearchPalletNbrPane extends BasicPane {
    private static final long serialVersionUID = 1;
    /**
     * A label to show a cookielist.
     */
    private JLabel palletNumberLabel;

    /**
     * The text fields where the Pallet number is written.
     */
    private JTextField input;
    /**
     * Create the Produce Pallets pane.
     *
     * @param db
     *            The database object.
     */
    public SearchPalletNbrPane(Database db) {
        super(db);
    }
    private DefaultListModel<String> palletResultListModel;
    private JList<String> palletResultList;
    /**
     * Create the left panel, containing the movie name list and the performance
     * date list.
     *
     * @return The left panel.
     */
    public JComponent createLeftPanel() {
        JPanel p = new JPanel();
        p.setName("Nbr");
        p.setLayout(new GridLayout(4, 2));
        JPanel empty = new JPanel();
        p.add(empty);
        JTextField text = new JTextField("Enter pallet number below:");
        text.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        text.setEditable(false);
        p.add(text);
        input = new JTextField("", 20);
        p.add(input);

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

    /**
     * Create the top panel, containing the fields with the performance data.
     *
     * @return The top panel.
     */
    public JComponent createTopPanel() {

        JPanel p1 = new JPanel();
        p1.setLayout(new FlowLayout(FlowLayout.CENTER));
        p1.add(new JLabel("Pallet number: "));
        palletNumberLabel = new JLabel("");
        p1.add(palletNumberLabel);
        return p1;
    }

    /**
     * Create the bottom panel, containing the produce pallet-button and the
     * message line.
     *
     * @return The bottom panel.
     */
    public JComponent createBottomPanel() {
        JButton[] buttons = new JButton[1];
        buttons[0] = new JButton("Search");
        return new ButtonAndMessagePanel(buttons, messageLabel,
                new SearchPalletNbrPane.ActionHandler());
    }

    /**
     * Perform the entry actions of this pane: clear all fields, fetch the cookie
     * names from the database and display them in the name list.
     */
    public void entryActions() {
        clearMessage();
        palletResultListModel.removeAllElements();
        input.setText("");
    }

    /**
     * A class that listens for Search button clicks.
     */
    private class ActionHandler implements ActionListener {
        /**
         * Called when the user clicks the produce pallet button. One pallet is
         * produced for the chosen cookie.
         * @param e
         *            The event object (not used).
         */
        public void actionPerformed(ActionEvent e) {
            String nbr = input.getText();
            entryActions();
            if (nbr.equals("")){
                displayMessage("Please type in a number");
                return;
            }
            int palletNbr = Integer.parseInt(nbr);
            String palletInfo = db.getPalletInfo(palletNbr);
            if(palletInfo.equals("")){
                displayMessage("No pallet with number: " + palletNbr + " was found");
                input.setText("");
                return;
            }
            palletNumberLabel.setText(palletInfo);
            palletResultListModel.addElement(palletInfo);

        }
    }
}

