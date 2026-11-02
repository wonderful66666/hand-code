package com.withyourhand.ymy.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@RestController
@RequestMapping("/api/ymy/notes")
public class CodingNoteController {

    public final JdbcTemplate jdbcTemplate;

    public CodingNoteController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    @GetMapping
    public List<CodingNote> findAll() {
        String sql = "SELECT id, content from coding_note order by id";

        List<CodingNote> rs_note = jdbcTemplate.query(sql, (rs, rsNum) -> {
            return new CodingNote(
                rs.getLong("id"),
                rs.getString("content")
            );
        });

        return rs_note;
    }

    // POST /api/ymy/notes: 新增一条笔记
    //
    // consumes: 生命这个接口接收 application/json 请求正文。
    // @ResponseStatus: 成功返回时， 使用 201 Created 状态码
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public CodingNote create(
        @Validated @RequestBody CreateNoteRequest request
    ) {
        //@RequestBody
        //把Spring收到的JSON转化成 CreateNoteRequest 对象
        String sql = "INSERT INTO coding_note (content) VALUES (?)";

        //用来接收数据库为本次插入生成的主键
        KeyHolder keyHolder = new GeneratedKeyHolder();

        //jdbctemplate的一个update重载
        //参数1 创建statment的conn
        //参数2 接收数据库产生的ID
        jdbcTemplate.update(connection -> {
            //RETURN_GENERATED_KEYS
            //告诉JDBC, 执行插入后需要返回数据库生成的ID
            PreparedStatement preparedStatement = connection.prepareStatement(
                sql,
                Statement.RETURN_GENERATED_KEYS
            );

            //参数编号从1开始
            //content你只需要拼接，不需要去处理SQL或者单双引号
            preparedStatement.setString(1,request.content());

            //jdbc会在回调完成后执行语句
            return preparedStatement;
        }, keyHolder);

        //从keyHolder取得生成的ID
        Number generatedId = keyHolder.getKey();
        //正常情况下会拿到一个ID,因为H2会返回，但是如果报错你是拿不到。
        if (generatedId == null) {
            throw new IllegalStateException("新增笔记失败");
        }

        return new CodingNote(
            generatedId.longValue(),
            request.content()
        );
    }




    //请求对象，客户端新增的时候，只需要commit一个content
    //
    // @NotBlank ... @Size 限制字符串长度，和当前的content VARCHAR(255) 的限制对应
    public record CreateNoteRequest(
        @NotBlank(message = "content 不能为空或者全部为空白")
        @Size(max = 255, message = "content最长不能超过255")
        String content
    ) {}

    //响应对象
    //定义CodingNote结构
    public record CodingNote(long id, String content) {}
}


