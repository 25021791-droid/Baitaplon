package com.auction.server.service;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;


public class TestConnection {
    public static void main(String[] args) {

        try {
            System.out.println("-- Connecting to Aiven...");
            Connection conn = DatabaseService.getConnection();
            if (conn != null) {
                System.out.println("--> Connection successful.");
                
                DatabaseMetaData metaData = conn.getMetaData();
                try (ResultSet tables = metaData.getTables(null, null, "%", new String[] { "TABLE" })) {
                    while (tables.next()) {
                        String tableName = tables.getString("TABLE_NAME");
                        System.out.println("\nTable: " + tableName);
                        
                        try (Statement stmt = conn.createStatement();
                             ResultSet columns = stmt.executeQuery("SELECT * FROM " + tableName + " LIMIT 1")) {
                            ResultSetMetaData colMeta = columns.getMetaData();
                            int colCount = colMeta.getColumnCount();
                            for (int i = 1; i <= colCount; i++) {
                                System.out.println("  - " + colMeta.getColumnName(i) + " (" + colMeta.getColumnTypeName(i) + ")");
                            }
                        } catch (Exception ex) {
                            System.out.println("  (Could not read columns: " + ex.getMessage() + ")");
                        }
                    }
                }
                
                conn.close();
            }
        } catch (Exception e) {
            System.err.println("--> Connection Failed.");
            e.printStackTrace();
        }

    }
}
