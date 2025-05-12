package com.example.quizcards.initializer;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class FullTextIndexInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public FullTextIndexInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        // Creating full-text index for folders title
        checkAndCreateFullTextIndex("folders", "title");
        checkAndCreateFullTextIndex("set_flashcards", "title");
        checkAndCreateFullTextIndex("category_set_flashcards", "category_name");
    }

    private void checkAndCreateFullTextIndex(String tableName, String columnName) {
        String checkIndexSql = String.format(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS " +
                        "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = '%s' AND INDEX_NAME = '%s'",
                tableName, columnName
        );
        Integer count = jdbcTemplate.queryForObject(checkIndexSql, Integer.class);

        if (count != null && count > 0) {
            System.out.printf("FULLTEXT index for column '%s' in table '%s' already exists.%n", columnName, tableName);
        } else {
            try {
                String createIndexSql = String.format("ALTER TABLE %s ADD FULLTEXT(%s)", tableName, columnName);
                jdbcTemplate.execute(createIndexSql);
                System.out.printf("FULLTEXT index for column '%s' in table '%s' has been created.%n", columnName, tableName);
            } catch (Exception e) {
                System.err.printf("Error creating FULLTEXT index for column '%s' in table '%s': %s%n", columnName, tableName, e.getMessage());
            }
        }
    }
}
