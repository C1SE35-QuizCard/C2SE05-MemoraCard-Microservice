package com.example.quizcards.initializer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class CascadeDeleteSetupRunner implements CommandLineRunner {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        try {
            // Truy vấn tất cả các khóa ngoại trong cơ sở dữ liệu hiện tại
            String foreignKeysQuery = "SELECT TABLE_NAME, CONSTRAINT_NAME, COLUMN_NAME, REFERENCED_TABLE_NAME, REFERENCED_COLUMN_NAME " +
                    "FROM information_schema.KEY_COLUMN_USAGE " +
                    "WHERE TABLE_SCHEMA = DATABASE() " +
                    "AND REFERENCED_TABLE_NAME IS NOT NULL;";

            List<Map<String, Object>> foreignKeys = jdbcTemplate.queryForList(foreignKeysQuery);

            for (Map<String, Object> fk : foreignKeys) {
                String tableName = (String) fk.get("TABLE_NAME");
                String constraintName = (String) fk.get("CONSTRAINT_NAME");
                String columnName = (String) fk.get("COLUMN_NAME");
                String referencedTable = (String) fk.get("REFERENCED_TABLE_NAME");
                String referencedColumn = (String) fk.get("REFERENCED_COLUMN_NAME");

                // Kiểm tra nếu khóa ngoại đã có ON DELETE CASCADE
                String checkCascadeQuery = "SELECT DELETE_RULE " +
                        "FROM information_schema.REFERENTIAL_CONSTRAINTS " +
                        "WHERE CONSTRAINT_SCHEMA = DATABASE() " +
                        "AND CONSTRAINT_NAME = ?;";
                String deleteRule = jdbcTemplate.queryForObject(checkCascadeQuery, new Object[]{constraintName}, String.class);

                if ("CASCADE".equalsIgnoreCase(deleteRule)) {
                    // Nếu đã có ON DELETE CASCADE, bỏ qua
                    System.out.println(String.format("Khóa ngoại %s trên bảng %s đã có ON DELETE CASCADE. Bỏ qua.", constraintName, tableName));
                    continue;
                }

                // Xóa khóa ngoại hiện tại
                String dropForeignKeySql = String.format("ALTER TABLE `%s` DROP FOREIGN KEY `%s`;", tableName, constraintName);
                jdbcTemplate.execute(dropForeignKeySql);
                System.out.println(String.format("Đã xóa khóa ngoại: %s trên bảng: %s", constraintName, tableName));

                // Thêm lại khóa ngoại với ON DELETE CASCADE
                String addForeignKeySql = String.format(
                        "ALTER TABLE `%s` ADD CONSTRAINT `%s_cascade` FOREIGN KEY (`%s`) REFERENCES `%s`(`%s`) ON DELETE CASCADE;",
                        tableName,
                        constraintName, // Đặt tên mới cho khóa ngoại để tránh trùng lặp
                        columnName,
                        referencedTable,
                        referencedColumn
                );
                jdbcTemplate.execute(addForeignKeySql);
                System.out.println(String.format("Đã thêm khóa ngoại mới với ON DELETE CASCADE: %s_cascade trên bảng: %s", constraintName, tableName));
            }

            System.out.println("Đã thiết lập ON DELETE CASCADE cho tất cả các khóa ngoại thành công.");

        } catch (Exception e) {
            System.err.println("Lỗi khi thiết lập cascade delete: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
