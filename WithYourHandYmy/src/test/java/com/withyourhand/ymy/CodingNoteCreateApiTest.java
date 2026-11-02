package com.withyourhand.ymy;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.stream;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.datasource.url=jdbc:h2:mem:ymy-test-create;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
    })
@ActiveProfiles("test")
public class CodingNoteCreateApiTest {

    @Value("${local.server.port}")
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private JsonMapper jsonMapper;

    @BeforeEach
    void clearNotes() {
        jdbcTemplate.update("DELETE from coding_note");
    }

    @Test
    public void createsNoteAndCanQueryIt() throws Exception {
        //一 准备请求
        //特意加入中文，换行，单双引号
        String content =
            "今天新增笔记接口\n测试 '单引号' 和 \"双引号\"";

        //Map.of来自JDK, 创建一个简单的Map
        //writeValueAsString可以把Map转换成JSON字符串,并正确处理各种字符
        String json = jsonMapper.writeValueAsString(
            Map.of("content",content)
        );

        //执行POST请求
        HttpResponse<String> response = postJson(json);

        //验证HTTP响应 --- 新增成功我们定义的是201create
        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(response.headers().firstValue("Content-Type").orElse(""))
            .startsWith("application/json");
        //新增只返回一条笔记， 所以这里应该是JSON对象
        JsonNode jsonNode = jsonMapper.readTree(response.body());
        assertThat(jsonNode.isObject()).isTrue();

        long id = jsonNode.get("id").longValue();
        //ID是由数据库生成的， 不假设每个测试都从1开始
        //因为我不能预测，但是数据库ID的共同特征是， id严格大于0
        assertThat(id).isPositive();
        assertThat(jsonNode.get("content").asString()).isEqualTo(content);

        //直接检查数据库
        String savedContent = jdbcTemplate.queryForObject(
            "SELECT content from coding_note where id = ?",
            String.class,
            id
        );
        assertThat(savedContent).isEqualTo(content);

        //直接GET
        HttpResponse<String> notes = getNotes();
        assertThat(notes.statusCode()).isEqualTo(200);

        JsonNode read_notes = jsonMapper.readTree(notes.body());
        assertThat(read_notes.isArray()).isTrue();
        assertThat(read_notes.size()).isEqualTo(1);
        //看直接查到的ID是否一样
        assertThat(read_notes.get(0).get("id").longValue()).isEqualTo(id);
        //content
        assertThat(read_notes.get(0).get("content").asString()).isEqualTo(content);
    }

    @Test
    void acceptsContentAtBoundaryLengths() throws Exception {
        //检查合法边界 1 and 255
        //不只是检测一个边界，可以防止边界错误
        for(int length : new int[] {1,255}) {
            String content = "a".repeat(length);
            //writeValueAsString 将 Map对象转成json字符串
            String json = jsonMapper.writeValueAsString(
                Map.of("content", content)
            );

            HttpResponse<String> response = postJson(json);
            assertThat(response.statusCode()).isEqualTo(201);
            JsonNode test_create = jsonMapper.readTree(response.body());
            assertThat(test_create.isObject()).isTrue();
            assertThat(test_create.get("id").longValue()).isPositive();
            assertThat(test_create.get("content").asString()).isEqualTo(content);
        }

        Long count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM coding_note",
            Long.class
        );

        assertThat(count).isEqualTo(2L);
    }

    @Test
    public void rejectMissingNullEmptyAndBlankContent() throws Exception {
        //这些语法合法的JSON
        //但是转换后的content 不满足NotBlank
        String[] invalidBodies = {
            "{}",
            "{\"content\":null}",
            "{\"content\":\"\"}",
            "{\"content\":\"    \"}",
            "{\"content\":\"\\t\\n\"}"
        };
        for (String json : invalidBodies) {
            HttpResponse<String> response = postJson(json);
            assertRejected(response);
        }
    }

    @Test
    public void rejectContentLongerThanMaxium() throws Exception {
        //255合法， 256就应该被拒绝。
        String content = "a".repeat(256);

        String json = jsonMapper.writeValueAsString(
            Map.of("content", content)
        );
        HttpResponse<String> response = postJson(json);
        assertRejected(response);
    }

    @Test
    public void rejectsUnreadableRequestBodies() throws Exception {
        String[] invalidBodies = {
            "",
            "null",
            "{\"content\":"  //不完整的JSON
        };

        for (String json : invalidBodies) {
            assertRejected(postJson(json));
        }
    }

    //多个失败场景需要检查相同的结果，提取成辅助方法
    private void assertRejected(HttpResponse<String> response) throws Exception {
        assertThat(response.statusCode()).isEqualTo(400);
        //查询到的数据返回数组，然后组成json
        JsonNode error = jsonMapper.readTree(response.body());

        assertThat(error.get("code").asString()).isEqualTo("INVALID_ARGUMENT");
        assertThat(error.get("message").asString()).isNotBlank();

        //请求失败还不够，必须确认没有错误
        Long count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM coding_note",
            Long.class
        );
        assertThat(count).isZero();
    }

    private HttpResponse<String> postJson(String json) throws Exception {
        URI uri = URI.create(
            "http://127.0.0.1:" + port + "/api/ymy/notes"
        );

        HttpRequest accept = HttpRequest.newBuilder(uri)
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))   //ofString 将参数作为请求正文
            .build();

        return send(accept);
    }

    private HttpResponse<String> send(HttpRequest accept) throws Exception {
        try (HttpClient client = HttpClient.newHttpClient()) {
            return client.send(
                accept,
                //收到的正文读取为Java String
                HttpResponse.BodyHandlers.ofString()
            );
        }
    }

    private HttpResponse<String> getNotes() throws Exception{
        URI uri = URI.create(
            "http://127.0.0.1:" + port + "/api/ymy/notes"
        );

        HttpRequest hRequest = HttpRequest.newBuilder(uri)
            .header("Accept", "application/json")
            .GET()
            .build();

        //httpClient来自JDK,不需要额外导入库
        //try-resource自动关闭流
        try (
            HttpClient httpClient = HttpClient.newHttpClient();
        ) {
            return httpClient.send(
                hRequest,
                //BodyHandlers处理响应正文的方式，这里选择提取成字符串
                HttpResponse.BodyHandlers.ofString()
            );
        }
    }
}
