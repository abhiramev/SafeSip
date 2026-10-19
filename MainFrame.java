package safesip.ui;

import safesip.db.DBConnection;
import safesip.logic.BACCalculator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class MainFrame extends JFrame {

    private final int userId;
    private final String role;

    private final DefaultTableModel productModel = new DefaultTableModel(
            new String[]{"ID", "Name", "Category", "Price (Rs)", "Alcohol %", "Volume (ml)", "Stock"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };

    /** Small helper so combo boxes can hold an id + a label. */
    static class Item {
        final int id;
        final String label;
        Item(int id, String label) { this.id = id; this.label = label; }
        @Override public String toString() { return label; }
    }

    public MainFrame(int userId, String name, String role) {
        this.userId = userId;
        this.role = role;

        setTitle("SafeSip - " + name + " (" + role + ")");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 500);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Products", buildProductsTab());
        if (role.equals("CUSTOMER")) {
            tabs.addTab("My Consumption", buildConsumptionTab());
        } else {
            tabs.addTab("Process Sale", buildSaleTab());
        }

        JButton btnLogout = new JButton("Logout");
        btnLogout.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        top.add(btnLogout);

        add(top, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        loadProducts();
    }

    // ------------------------------------------------------------------ Products tab
    private JPanel buildProductsTab() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JTable table = new JTable(productModel);
        table.setRowHeight(24);
        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> loadProducts());
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        p.add(btnRefresh, BorderLayout.SOUTH);
        return p;
    }

    private void loadProducts() {
        productModel.setRowCount(0);
        String sql = "SELECT p.product_id, p.name, p.category, p.price, p.alcohol_percent, " +
                     "p.volume_ml, COALESCE(i.quantity,0) " +
                     "FROM products p LEFT JOIN inventory i ON p.product_id = i.product_id";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                productModel.addRow(new Object[]{
                        rs.getInt(1), rs.getString(2), rs.getString(3), rs.getDouble(4),
                        rs.getDouble(5), rs.getInt(6), rs.getInt(7)});
            }
        } catch (SQLException e) {
            showError(e);
        }
    }

    // ------------------------------------------------------------------ Customer: consumption tab
    private JPanel buildConsumptionTab() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<Item> cmbProduct = new JComboBox<>();
        fillProducts(cmbProduct);
        JSpinner spnQty = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));
        JButton btnLog = new JButton("Log drink");
        JLabel lblBac = new JLabel(" ");
        lblBac.setFont(new Font("SansSerif", Font.BOLD, 18));

        c.gridx = 0; c.gridy = 0; p.add(new JLabel("Drink"), c);
        c.gridx = 1; p.add(cmbProduct, c);
        c.gridx = 0; c.gridy = 1; p.add(new JLabel("Quantity"), c);
        c.gridx = 1; p.add(spnQty, c);
        c.gridx = 0; c.gridy = 2; c.gridwidth = 2; p.add(btnLog, c);
        c.gridy = 3; p.add(lblBac, c);

        Runnable refreshBac = () -> {
            try {
                double bac = BACCalculator.estimate(userId, 0);
                boolean blocked = bac >= BACCalculator.LIMIT;
                lblBac.setText(String.format("Estimated BAC: %.3f%%  -  %s", bac,
                        blocked ? "NOT ELIGIBLE to buy" : "OK"));
                lblBac.setForeground(blocked ? new Color(180, 30, 30) : new Color(20, 120, 40));
            } catch (SQLException e) {
                showError(e);
            }
        };

        btnLog.addActionListener(e -> {
            Item prod = (Item) cmbProduct.getSelectedItem();
            if (prod == null) return;
            String sql = "INSERT INTO consumption_records (customer_id, product_id, quantity) VALUES (?,?,?)";
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, userId);
                ps.setInt(2, prod.id);
                ps.setInt(3, (Integer) spnQty.getValue());
                ps.executeUpdate();
                refreshBac.run();
            } catch (SQLException ex) {
                showError(ex);
            }
        });

        refreshBac.run();
        return p;
    }

    // ------------------------------------------------------------------ Staff/Admin: sale tab
    private JPanel buildSaleTab() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<Item> cmbCustomer = new JComboBox<>();
        fillCustomers(cmbCustomer);
        JComboBox<Item> cmbProduct = new JComboBox<>();
        fillProducts(cmbProduct);
        JSpinner spnQty = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));
        JButton btnSell = new JButton("Check BAC & Sell");
        JLabel lblResult = new JLabel(" ");
        lblResult.setFont(new Font("SansSerif", Font.BOLD, 15));

        c.gridx = 0; c.gridy = 0; p.add(new JLabel("Customer"), c);
        c.gridx = 1; p.add(cmbCustomer, c);
        c.gridx = 0; c.gridy = 1; p.add(new JLabel("Product"), c);
        c.gridx = 1; p.add(cmbProduct, c);
        c.gridx = 0; c.gridy = 2; p.add(new JLabel("Quantity"), c);
        c.gridx = 1; p.add(spnQty, c);
        c.gridx = 0; c.gridy = 3; c.gridwidth = 2; p.add(btnSell, c);
        c.gridy = 4; p.add(lblResult, c);

        btnSell.addActionListener(e -> {
            Item cust = (Item) cmbCustomer.getSelectedItem();
            Item prod = (Item) cmbProduct.getSelectedItem();
            if (cust == null || prod == null) return;
            int qty = (Integer) spnQty.getValue();
            processSale(cust.id, prod.id, qty, lblResult);
        });
        return p;
    }

    private void processSale(int customerId, int productId, int qty, JLabel lblResult) {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);

            // 1. product details + stock
            double price, pct;
            int vol, stock;
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT p.price, p.alcohol_percent, p.volume_ml, COALESCE(i.quantity,0) " +
                    "FROM products p LEFT JOIN inventory i ON p.product_id = i.product_id " +
                    "WHERE p.product_id = ?")) {
                ps.setInt(1, productId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return;
                    price = rs.getDouble(1);
                    pct = rs.getDouble(2);
                    vol = rs.getInt(3);
                    stock = rs.getInt(4);
                }
            }
            if (stock < qty) {
                setResult(lblResult, "Sale failed: only " + stock + " in stock.", false);
                return;
            }

            // 2. BAC gate (current BAC + this purchase)
            double bac = BACCalculator.estimate(customerId, BACCalculator.grams(qty, vol, pct));
            if (bac >= BACCalculator.LIMIT) {
                setResult(lblResult, String.format(
                        "SALE BLOCKED: projected BAC %.3f%% is at/above the %.2f%% limit.",
                        bac, BACCalculator.LIMIT), false);
                return;
            }

            // 3. record sale, sale item, update stock
            double total = price * qty;
            int saleId;
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO sales (customer_id, staff_id, total) VALUES (?,?,?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, customerId);
                ps.setInt(2, userId);
                ps.setDouble(3, total);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    keys.next();
                    saleId = keys.getInt(1);
                }
            }
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO sale_items (sale_id, product_id, qty, price) VALUES (?,?,?,?)")) {
                ps.setInt(1, saleId);
                ps.setInt(2, productId);
                ps.setInt(3, qty);
                ps.setDouble(4, price);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE inventory SET quantity = quantity - ? WHERE product_id = ?")) {
                ps.setInt(1, qty);
                ps.setInt(2, productId);
                ps.executeUpdate();
            }
            con.commit();

            setResult(lblResult, String.format("Sale #%d complete. Total Rs %.2f (BAC after: %.3f%%)",
                    saleId, total, bac), true);
            loadProducts();
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    // ------------------------------------------------------------------ helpers
    private void fillProducts(JComboBox<Item> combo) {
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT product_id, name FROM products")) {
            while (rs.next()) combo.addItem(new Item(rs.getInt(1), rs.getString(2)));
        } catch (SQLException e) {
            showError(e);
        }
    }

    private void fillCustomers(JComboBox<Item> combo) {
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT user_id, name FROM users WHERE role='CUSTOMER'")) {
            while (rs.next()) combo.addItem(new Item(rs.getInt(1), rs.getString(2)));
        } catch (SQLException e) {
            showError(e);
        }
    }

    private void setResult(JLabel label, String text, boolean ok) {
        label.setText(text);
        label.setForeground(ok ? new Color(20, 120, 40) : new Color(180, 30, 30));
    }

    private void showError(Exception e) {
        JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
    }
}
