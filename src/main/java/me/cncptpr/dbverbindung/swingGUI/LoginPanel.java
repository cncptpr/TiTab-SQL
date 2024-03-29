package me.cncptpr.dbverbindung.swingGUI;

import me.cncptpr.dbverbindung.core.dbconnection.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

import static me.cncptpr.dbverbindung.core.State.state;
import static me.cncptpr.dbverbindung.core.events.EventHandlers.LOGIN_EVENT;

/**
 * This Class contains the logic for the Login Screen and reads and writes the credentials to the Settings file.
 */
public class LoginPanel {
    private JTextField usernameTextField;
    private JPasswordField passwordTextField;
    private JTextField databaseTextField;
    private JTextField ipTextField;
    private JButton loginButton;
    private JPanel mainPanel;


    public LoginPanel() {
        loginButton.addActionListener(this::OnLoginClicked);
        usernameTextField.addActionListener(e -> passwordTextField.grabFocus());
        usernameTextField.addActionListener(e -> passwordTextField.grabFocus());
        passwordTextField.addActionListener(e -> databaseTextField.grabFocus());
        databaseTextField.addActionListener(e -> ipTextField.grabFocus());
        ipTextField.addActionListener(this::OnLoginClicked);
        usernameTextField.grabFocus();
        if (!DBConnection.isJDBCDriverThere()) {
            setLoginButtonToDriverError();
        }
    }

    public void OnLoginClicked(ActionEvent ignored) {
        state().setUsername(getUsername());
        state().setPassword(getPassword());
        state().setDatabaseCurrent(getDatabase());
        state().setIP(getIP());
        resetLoginButton();
        if (DBConnection.canConnect())
            LOGIN_EVENT.call(getUsername(), getDatabase(), getIP());
        else
            setLoginButtonToError();
    }

    public Container getMainPanel() {
        return mainPanel;
    }

    private String getUsername() {
        return usernameTextField.getText();
    }
    private String getPassword() {
        return passwordTextField.getText();
    }
    private String getDatabase() {
        return databaseTextField.getText();
    }
    private String getIP() {
        return ipTextField.getText();
    }

    /**
     * Fills the forum with the information in the settings file
     */
    public void fill() {
        databaseTextField.setText(state().databaseConfig());
        ipTextField.setText(state().ip());
    }

    /**
     * When a login error occurs the look and the text of the login button is changed for visual feedback.
     * The button is automatically reset after three seconds via the {@link #resetLoginButton()} method.
     */
    private void setLoginButtonToError() {
        loginButton.setBackground(new Color(94, 7, 16));
        loginButton.setForeground(Color.WHITE);
        loginButton.setText("Fehler beim Anmelden");
        new Thread(() -> {
            try {
                Thread.sleep(3*1000);
                resetLoginButton();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    /**
     * When the JDBC Driver is missing the text of the login button is changed for visual feedback.
     * The button is locked because you can't log in without the driver.
     */
    private void setLoginButtonToDriverError() {
        loginButton.setBackground(new Color(94, 7, 16));
        loginButton.setForeground(Color.WHITE);
        loginButton.setText("Driver fehlt");
        loginButton.setEnabled(false);
    }

    /**
     * Resets the button (from e.g. it's error state) to the normal button.
     * See also {@link #setLoginButtonToError()}
     */
    private void resetLoginButton() {
        loginButton.setBackground(Color.WHITE);
        loginButton.setForeground(Color.BLACK);
        loginButton.setText("Login");
    }
}
