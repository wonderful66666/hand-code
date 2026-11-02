package com.withyourhand.ymy;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
/**
 * application-test.yml是test环境配置，启动时，会覆盖application.yml里面的对应信息
 */
@ActiveProfiles("test")
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
            //assertThat是断言
            //看是否启动了内存数据库测试
            assertThat(connection.getMetaData().getURL()).startsWith("jdbc:h2:mem:ymy-test");
            assertThat(executeQuery.next()).isTrue();  //移动到第一行看是否有数据
            assertThat(executeQuery.getInt("result")).isEqualTo(1);
        }
    }
}
