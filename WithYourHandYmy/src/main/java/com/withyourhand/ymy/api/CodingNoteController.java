package com.withyourhand.ymy.api;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ymy/notes")
public class CodingNoteController {

    public final JdbcTemplate jdbcTemplate;

    public CodingNoteController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    //定义CodingNote结构
    public record CodingNote(long id, String content) {}

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
}


