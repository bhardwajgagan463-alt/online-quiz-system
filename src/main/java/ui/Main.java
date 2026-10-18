package ui;

import dao.DBConnection;
import util.DatabaseException;
import util.Theme;

import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Theme.install();                                   // FlatLaf + palette + fonts
            try {
                DBConnection.initializeDatabase();             // creates quiz.db on first run
            } catch (DatabaseException e) {
                UIHelper.error(null, "Database error:\n" + e.getMessage());
                System.exit(1);
            }
            new MainFrame().setVisible(true);
        });
    }
}