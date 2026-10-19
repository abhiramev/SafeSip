package safesip.ui;

import safesip.db.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginFrame extends JFrame {

    private final JTextField txtUser = new JTextField(15);
    private final JPasswordField txtPass = new JPasswordField(15);

    public LoginFrame() {
        setTitle("SafeSip - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(25, 40, 25, 40));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("SafeSip", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 30));
        JLabel sub = new JLabel("Drink smart. Stay safe.", SwingConstants.CENTER);

        c.gridx = 0; c.gridy = 0; c.gridwidth = 2; p.add(title, c);
        c.gridy = 1; p.add(sub, c);

        c.gridwidth = 1;
        c.gridy = 2; c.gridx = 0; p.add(new JLabel("Username"), c);
        c.gridx = 1; p.add(txtUser, c);
        c.gridy = 3; c.gridx = 0; p.add(new JLabel("Password"), c);
        c.gridx = 1; p.add(txtPass, c);

        JButton btnLogin = new JButton("Login");
        c.gridy = 4; c.gridx = 0; c.gridwidth = 2; p.add(btnLogin, c);

        btnLogin.addActionListener(e -> login());
        txtPass.addActionListener(e -> login());   // press Enter to login

        add(p);
        pack();
        setLocationRelativeTo(null);
    }

    private void login() {
        String sql = "SELECT user_id, name, role FROM users WHERE username=? AND password=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, txtUser.getText().trim());
            ps.setString(2, new String(txtPass.getPassword()));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    new MainFrame(rs.getInt("user_id"), rs.getString("name"), rs.getString("role"))
                            .setVisible(true);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid username or password",
                            "Login failed", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) { }
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
