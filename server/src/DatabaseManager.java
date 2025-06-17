import java.sql.*;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:client_data.db3";

    public DatabaseManager() {
        try {
            Class.forName("org.sqlite.JDBC"); // 显式加载驱动程序
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC 驱动加载失败: " + e.getMessage());
        }
        createTable();
    }

    private void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS client_data (\n"
                + "    id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                + "    data TEXT NOT NULL,\n"
                + "    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP\n"
                + ");";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("创建表失败: " + e.getMessage());
        }
    }

    public void saveData(String data) {
        String sql = "INSERT INTO client_data(data) VALUES(?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, data);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("保存数据失败: " + e.getMessage());
        }
    }
}