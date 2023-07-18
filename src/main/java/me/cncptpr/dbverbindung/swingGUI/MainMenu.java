package me.cncptpr.dbverbindung.swingGUI;

import me.cncptpr.console.Console;
import me.cncptpr.dbverbindung.core.ColumnInfo;
import me.cncptpr.dbverbindung.core.ResultTable;
import me.cncptpr.dbverbindung.core.TableInfo;
import me.cncptpr.dbverbindung.core.dbconnection.DBConnection;
import me.cncptpr.dbverbindung.core.events.sqlErrorEvent.SQLErrorEvent;
import me.cncptpr.dbverbindung.core.events.sqlRunEvent.SQLRunEvent;
import me.cncptpr.dbverbindung.core.handler.HistoryHandler;
import me.cncptpr.dbverbindung.core.handler.InfoHandler;
import me.cncptpr.dbverbindung.core.handler.SQLHandler;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;

import static me.cncptpr.dbverbindung.core.events.EventHandlers.*;

import static me.cncptpr.dbverbindung.Main.SETTINGS;


/**
 * The main menu is a tabbed panel where all the SQL is done.
 * This class handles switching between tabs and handles all user input.
 * Almost every action is triggered from this class.
 * The results of these action get passed up from the core over events (The core doesn't know about the ui).
 */
public class MainMenu {

    public  enum Tab {
        SQLResult(0), SQLEditor(1), History(2), Info(3), DBChooser(4);
        final int i;

        static Tab from(int index) {
            return switch (index) {
                case 0 -> SQLResult;
                case 1 -> SQLEditor;
                case 2 -> History;
                case 3 -> Info;
                case 4 -> DBChooser;
                default -> null;
            };
        }
        Tab(int i)  {
            this.i = i;
        }

        public int index() {
            return i;
        }
    }

    //SQL Result
    public JTextField SQLResult_Input;
    public JTable SQLResult_Table;
    public JButton SQLResult_SendButton;

    //SQL Editor
    public JPanel SQLEditor_history;
    public JTextArea SQLEditor_Input;
    public JButton SQLEditor_SendButton;

    //Info
    public JScrollPane Info_Tables;
    public JScrollPane Info_Columns;
    private String selectedTable;

    //Database Chooser
    public JComboBox<String> DBChooser_DropDown;
    public JButton DBChooser_ChooseButton;
    public JButton DBChooser_LogoutButton;

    //Menu

    public JTabbedPane Menu_TabbedPane;
    public JPanel MainPanel;

    //History
    public JScrollPane History_ScrollPane;
    private final JTextArea History_TextArea = new JTextArea();

    public MainMenu() {

        //=========================================== register to listeners ==========================================//
        SQL_EDIT_EVENT.register(this::editSQL);
        SQL_RUN_EVENT.register(this::showSQL);
        SQL_ERROR_EVENT.register(this::showSQLError);

        //================================================= Run SQL ==================================================//
        SQLResult_SendButton.addActionListener(e -> SQLHandler.runSQL(SQLResult_Input.getText()));
        SQLEditor_SendButton.addActionListener(e -> SQLHandler.runSQL(SQLEditor_Input.getText()));
        SQLResult_Input.addActionListener(e -> SQLHandler.runSQL(SQLResult_Input.getText()));


        //========================================= Fill DBChooser Drop Down =========================================//
        DBChooser_DropDown.removeAllItems();
        try (DBConnection connection = new DBConnection()) {
            for (String database : connection.getAllDatabases()) {
                DBChooser_DropDown.addItem(database);
            }
            DBChooser_DropDown.setSelectedItem(SETTINGS.getString("database_current"));
            DBChooser_DropDown.addActionListener(e -> {
                SETTINGS.set("database_current", DBChooser_DropDown.getSelectedItem());
                changeTab(Tab.SQLEditor);
            });
        } catch (SQLException e) {
            Console.error(e);
        }

        //============================================== Logout Listener =============================================//
        DBChooser_LogoutButton.addActionListener(e -> LOGOUT_EVENT.call());

        //============================================== Tab Listener ================================================//
        Menu_TabbedPane.addChangeListener(this::tabChanged);

        //============================================== Configure the History =======================================//
        HistoryHandler.init();
        History_TextArea.setEditable(false);
        History_ScrollPane.setViewportView(History_TextArea);

        //============================================== Initial Tab =================================================//
        changeTab(Tab.SQLEditor);
    }

    private void tabChanged(ChangeEvent changeEvent) {
        update();
    }

    /**
     * Updates the contents of the main menu to the contents of the currently active tab.
     */
    public void update() {
        int index = Menu_TabbedPane.getSelectedIndex();
        switch (Tab.from(index)) {
            case SQLResult -> updateSQLResult();
            case SQLEditor -> updateSQLEditor();
            case History -> updateHistory();
            case Info -> updateInfo();
            case DBChooser -> updateDBChooser();
        }
    }


    private void updateSQLResult() {
    }

    private void updateSQLEditor() {
    }

    private void updateHistory() {
        History_TextArea.setText(HistoryHandler.render());
    }

    private void updateInfo() {
        JPanel tablePanel = new JPanel();
        JPanel columnPanel = new JPanel();

        Info_Tables.setViewportView(tablePanel);
        Info_Columns.setViewportView(columnPanel);

        TableInfo[] info = InfoHandler.getInfo();

        tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.PAGE_AXIS));
        tablePanel.setBorder(new EmptyBorder(5, 10, 5, 10));
        columnPanel.setLayout(new BoxLayout(columnPanel, BoxLayout.PAGE_AXIS));
        columnPanel.setBorder(new EmptyBorder(5, 10, 5, 10));

        boolean tableSelected = false;
        for (TableInfo table : info){
            tablePanel.add(generateLabel(table.name(), true));
            if (table.name().equalsIgnoreCase(selectedTable)) {
                tableSelected = true;
                for (ColumnInfo columnInfo : table.columns()) {
                    columnPanel.add(generateLabel(columnInfo.name(), false));
                }
            }
        }
        if (!tableSelected) {
            for (ColumnInfo columnInfo : info[0].columns()) {
                columnPanel.add(generateLabel(columnInfo.name(), false));
            }
        }
    }

    private void updateDBChooser() {
    }

    /**
     * Shows the SQL Editor with the SQL statement to edit
     * @param sql the SQL being shown
     */
    private void editSQL(String sql) {
        SQLEditor_Input.setText(sql);
        changeTab(Tab.SQLEditor);
    }

    public void changeTab(Tab tab) {
        Menu_TabbedPane.setSelectedIndex(tab.index());
    }

    public Container getMainPanel() {
        return MainPanel;
    }

    public void showSQL(SQLRunEvent e) {
        changeTab(Tab.SQLResult);
        Console.test("Showing SQL\n");
        ResultTable result = e.resultTable();
        SQLResult_Input.setText(e.sql().replaceAll("\n", " "));
        SQLResult_Table.setModel(new DefaultTableModel(result.content(), result.titles()));
        SQLResult_Table.setBackground(UIManager.getColor("Table.background"));
    }

    /**
     * Displays an error, caused when executing a wrong sql statement, in the UI.
     */
    public void showSQLError(SQLErrorEvent e) {
        changeTab(Tab.SQLResult);
        Console.debug("Showing SQL Error\n");
        String[][] data = {{}, {"Fehler:"}, {}, {e.sql()}, {}, {e.error()}};
        String[] titles = {"Fehler:"};
        SQLResult_Table.setModel(new DefaultTableModel(data, titles));
        SQLResult_Table.setBackground(new Color(253, 63, 65));
    }

    /**
     * Generates a label as found in the info tab.
     * @param text the text of the label (the table or columns name)
     * @param addListener Whether to add a mouse listener.
     *                    The table labels require one to display their corresponding columns
     * @return The generated label. It still needs to be added to the UI.
     */
    private JLabel generateLabel(String text, boolean addListener) {
        JLabel label = new JLabel(text);
        label.setBorder(new EmptyBorder(0, 0, 5, 0));
        label.setFont(new Font(null, Font.BOLD, 15));
        label.setBackground(new Color(165, 167, 172));
        if (addListener)
            label.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1)
                        selectedTable = text;
                    updateInfo();
                }
            });
        return label;
    }
}
