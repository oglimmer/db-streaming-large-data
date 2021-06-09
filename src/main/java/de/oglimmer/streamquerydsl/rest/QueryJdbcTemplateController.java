package de.oglimmer.streamquerydsl.rest;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@RestController
@Slf4j
public class QueryJdbcTemplateController {

    public static final String APPLICATION_VND_MS_EXCEL = "application/vnd.ms-excel";
    public static final String ATTACHMENT_FILENAME_DOWNLOAD_XLS = "attachment; filename=\"download.xls\"";

    public static final String SQL_GET_ALL_PERSONS_WITH_DOGS = "select person.id p_id, person.name p_name, person" +
            ".firstname p_firstname, person.note p_note, dog.id d_id, dog.name d_name, dog.note d_note" +
            " from person left join dog on person.id=dog.owner_id";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/template")
    public void getListJdbcTemplate(HttpServletResponse response) throws Throwable {
        jdbcTemplate.setFetchSize(Integer.MIN_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(APPLICATION_VND_MS_EXCEL);
        response.setHeader("Content-Disposition", ATTACHMENT_FILENAME_DOWNLOAD_XLS);
        try (final ServletOutputStream outputStream = response.getOutputStream()) {
            jdbcTemplate.queryForStream(SQL_GET_ALL_PERSONS_WITH_DOGS, new BeanPropertyRowMapper<>(Row.class))
                    .map(row -> row.getP_name() + "," + row.getP_firstname() + "," + row.getP_note() + "\r\n")
                    .forEach(s -> writeToOutputStream(s, outputStream));
        }
    }

    public void writeToOutputStream(String str, ServletOutputStream outputStream) {
        try {
            outputStream.write(str.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Getter
    @Setter
    public static class Row {

        private Long p_id;
        private String p_name;
        private String p_firstname;
        private String p_note;
        private Long d_id;
        private String d_name;
        private String d_note;

    }
}
