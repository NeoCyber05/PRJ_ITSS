import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class AlterTable {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://aws-1-ap-southeast-2.pooler.supabase.com:6543/postgres";
        String user = "postgres.cnhmzykdxfnwzccfiynl";
        String pass = "itss_2025.2";
        try (Connection conn = DriverManager.getConnection(url, user, pass);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("ALTER TABLE request ADD COLUMN IF NOT EXISTS note VARCHAR(255)");
            System.out.println("Column 'note' added successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
