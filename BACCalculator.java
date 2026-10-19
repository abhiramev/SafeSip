package safesip.logic;

import safesip.db.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Estimates Blood Alcohol Content using the Widmark formula.
 * BAC% = alcoholGrams / (bodyWeightGrams * r) * 100 - 0.015 * hoursSinceFirstDrink
 * r = 0.68 (male), 0.55 (female).
 * This is only an estimate for a mini-project, not a medical tool.
 */
public class BACCalculator {

    /** Purchases are blocked at or above this BAC (%). */
    public static final double LIMIT = 0.08;

    /** Grams of pure alcohol in the given quantity of a drink. */
    public static double grams(int qty, int volumeMl, double alcoholPercent) {
        return qty * volumeMl * (alcoholPercent / 100.0) * 0.789;
    }

    /**
     * Estimated BAC (%) of a customer based on the last 24 hours of
     * consumption_records, plus optional extra alcohol (grams) about to be consumed.
     */
    public static double estimate(int customerId, double extraGrams) throws SQLException {
        String sql =
            "SELECT u.weight_kg, u.gender, " +
            "  SUM(cr.quantity * p.volume_ml * p.alcohol_percent / 100 * 0.789) AS grams, " +
            "  TIMESTAMPDIFF(MINUTE, MIN(cr.consumed_at), NOW()) AS mins " +
            "FROM users u " +
            "LEFT JOIN consumption_records cr ON cr.customer_id = u.user_id " +
            "  AND cr.consumed_at > NOW() - INTERVAL 24 HOUR " +
            "LEFT JOIN products p ON p.product_id = cr.product_id " +
            "WHERE u.user_id = ? " +
            "GROUP BY u.user_id, u.weight_kg, u.gender";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return 0.0;

                double weightKg = rs.getDouble("weight_kg");
                if (weightKg <= 0) weightKg = 70;                  // default if not set
                double r = "F".equals(rs.getString("gender")) ? 0.55 : 0.68;
                double totalGrams = rs.getDouble("grams") + extraGrams;
                double hours = rs.getDouble("mins") / 60.0;

                double bac = totalGrams / (weightKg * 1000 * r) * 100 - 0.015 * hours;
                return Math.max(0, bac);
            }
        }
    }
}
