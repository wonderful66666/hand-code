package com.withyourhand.ymy;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class H2ConnectionTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void canExecuteSql() throws SQLException {
        try (
         Connection connection = dataSource.getConnection();
         Statement statement = connection.createStatement();
        ResultSet executeQuery = statement.executeQuery("SELECT 1 AS RESULT");
        ) {
            //assertThat是
            assertThat(executeQuery.next()).isTrue();  //移动到第一行看是否有数据
            assertThat(executeQuery.getInt("result")).isEqualTo(1);
        }
    }
}
