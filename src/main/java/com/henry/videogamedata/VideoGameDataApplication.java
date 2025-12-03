package com.henry.videogamedata;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableCaching
public class VideoGameDataApplication {

    public static void main(String[] args) {
        SpringApplication.run(VideoGameDataApplication.class, args);
    }

    // 🤖 NEW: The "Startup Script" Bean
    @Bean
    public CommandLineRunner createFullTextIndex(JdbcTemplate jdbcTemplate) {
        return args -> {
            try {
                System.out.println("🛠️ Attempting to create FULLTEXT index...");

                // This raw SQL command builds the special index for fast searching
                String sql = "ALTER TABLE video_game ADD FULLTEXT INDEX idx_fulltext_name (name)";

                jdbcTemplate.execute(sql);

                System.out.println("✅ FULLTEXT index created successfully!");
            } catch (Exception e) {
                // If it fails, it likely already exists. We can ignore this error safely.
                System.out.println("ℹ️ Index likely already exists (or creation skipped): " + e.getMessage());
            }
        };
    }
}