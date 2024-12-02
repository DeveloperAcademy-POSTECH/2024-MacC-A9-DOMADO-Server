package com.onemorethink.domadosever.global.demo;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DatabaseInitializeService {

    private final EntityManager entityManager;
    private final ResourceLoader resourceLoader;
    private final DataSource dataSource;

    public void initializeDatabase() {
        try {
            // DB 타입 확인
            String dbName = dataSource.getConnection().getMetaData().getDatabaseProductName().toLowerCase();
            boolean isMySql = dbName.contains("mysql");

            // 1. 참조 무결성 비활성화
            if (isMySql) {
                entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
            } else {
                entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE").executeUpdate();
            }

            // 2. roles 테이블을 제외한 모든 테이블 조회
            String schemaQuery = isMySql
                    ? "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME != 'roles'"
                    : "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC' AND TABLE_NAME != 'ROLES'";

            List<String> tableNames = entityManager.createNativeQuery(schemaQuery)
                    .getResultList();

            // 3. roles 제외한 테이블만 초기화
            for (String tableName : tableNames) {
                entityManager.createNativeQuery("TRUNCATE TABLE " + tableName).executeUpdate();
            }

            // 4. 참조 무결성 다시 활성화
            if (isMySql) {
                entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
            } else {
                entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE").executeUpdate();
            }

            // 5. data.sql 실행
            Resource resource = resourceLoader.getResource("classpath:data.sql");
            String[] sqlStatements = new String(resource.getInputStream().readAllBytes())
                    .split(";");

            for (String sql : sqlStatements) {
                if (!sql.trim().isEmpty()) {
                    entityManager.createNativeQuery(sql.trim()).executeUpdate();
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Database reset failed: " + e.getMessage(), e);
        }
    }
}
