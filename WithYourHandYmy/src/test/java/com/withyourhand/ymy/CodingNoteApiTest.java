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
import static org.assertj.core.api.Assertions.assertThat;

/**
 * CodingNoteApiTest测试说明：
 *  RANDOM_PORT会自动选择端口，不会占用平时运行的8082
 *  properties是覆盖下application-test的同名配置
 *  所以我在清理内存数据库的时候，操作的是这个数据库
 */

//MAVEN测试 指令
//-f E:/WithYourHand/WithYourHandYmy/pom.xml clean test -Dtest=CodingNoteApiTest -Dmaven.repo.local=E:/WithYourHand/.cache/maven
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.datasource.url=jdbc:h2:mem:ymy-notes-api-test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
    })
@ActiveProfiles("test")
public class CodingNoteApiTest {

    //得到本次服务的启动端口
    @Value("${local.server.port}")
    public int port;

    //数据库操作
    @Autowired
    private JdbcTemplate jdbcTemplate;
    //将返回的JSON解析成对象
    @Autowired
    private JsonMapper jsonMapper;

    //每个Test执行前，先进行删库操作,不论执行顺序
    @BeforeEach
    void clearNotes() {
        jdbcTemplate.update("DELETE FROM coding_note");
    }

    @Test
    public void returnEmptyArrayWhenThereAreNoNotes() throws Exception {
        //@BeforeEach 已经清空了表

        //执行HTTP调用查询接口
        HttpResponse<String> response = getNotes();
        //验证是否请求成功
        assertThat(response.statusCode()).isEqualTo(200);

        //getNotes中定义了响应的处理方式为String
        //readTree可以把string解析成JSON数据结构
        JsonNode notes = jsonMapper.readTree(response.body());

        //验证返回的是数组,并且数组长度为0,因为HTTP返回的接口是List<CodingNote> 返回的是数组
        //所以本次返回的是一个"[]",有内容会生成JSOn数组
        assertThat(notes.isArray()).isTrue();
        assertThat(notes.size()).isZero();
    }

    @Test
    public void returnNotesOrderByIdAndPreservesContent() throws Exception {
        //准备： 创建两条笔记
        // 第一条特意包含换行，单引号，双引号，验证内容能完整返回
        String firstContent =
            "第一天：H2持久化测试\n测试 ‘单引号’ 和 \"双引号\"";
        String secondContent =
            "第二天：新增笔记接口";

        //测试时，先插入id=20 再插入 id=10
        //接口要求按id 升序返回，所以预期应该是10，20
        //
        //？是SQL的占位符
        //后面的参数依次绑定占位符，不需要手动拼接字符串或者转义引号
        jdbcTemplate.update(
            "INSERT INTO coding_note (id, content) VALUES (?,?)",
            20L,
            secondContent
        );

        jdbcTemplate.update(
            "INSERT INTO coding_note (id, content) VALUES (?,?)",
            10L,
            firstContent
        );

        //执行，调用真实HTTP接口
        HttpResponse<String> response = getNotes();
        //验证: 请求成功，响应声明使用JSON.
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(
            response.headers()
                .firstValue("Content-Type")
                .orElse("")
        ).startsWith("application/json");

        JsonNode jsonNode = jsonMapper.readTree(response.body());
        assertThat(jsonNode.isArray()).isTrue();
        assertThat(jsonNode.size()).isEqualTo(2);

        //查看里面的内容
        assertThat(jsonNode.get(0).get("id").longValue()).isEqualTo(10L);
        //asString()是用来把JSON字符串读取成Java String
        assertThat(jsonNode.get(0).get("content").asString()).isEqualTo(firstContent);
        assertThat(jsonNode.get(1).get("id").longValue()).isEqualTo(20L);
        //asString()是用来把JSON字符串读取成Java String
        assertThat(jsonNode.get(1).get("content").asString()).isEqualTo(secondContent);

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
