package de.oglimmer.streamquerydsl.rest;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@RestController
@Slf4j
public class QueryController {

    public static final String APPLICATION_VND_OPENXMLFORMATS_OFFICEDOCUMENT_SPREADSHEETML_SHEET = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    public static final String APPLICATION_VND_MS_EXCEL = "application/vnd.ms-excel";
    public static final String TEXT_CSV = "text/csv";
    public static final String ATTACHMENT_FILENAME_DOWNLOAD_XLS = "attachment; filename=\"download.xls\"";
    public static final String ATTACHMENT_FILENAME_DOWNLOAD_XLSX = "attachment; filename=\"download.xlsx\"";
    public static final String ATTACHMENT_FILENAME_DOWNLOAD_CSV = "attachment; filename=\"download.csv\"";
    public static final int EXCEL_MAX_ROWS = 1048575;
    public static final String SQL_GET_ALL_PERSONS_WITH_DOGS = "select * from person join dog on person" +
            ".id=dog.owner_id";
    public static final String SQL_GET_COUNT_OF_ALL_PERSONS_WITH_DOGS = "select count(*) from person join dog on " +
            "person.id=dog.owner_id";

    @Autowired
    private DataSource dataSource;

    @GetMapping("/plain-file")
    public void getListPlainFile(HttpServletResponse response) throws Exception {

        final File tempFile = File.createTempFile("sql-", ".data");

        try (final Connection connection = dataSource.getConnection()) {
            try (final Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY,
                    ResultSet.CONCUR_READ_ONLY)) {
                statement.setFetchSize(Integer.MIN_VALUE);

                long startTime = System.currentTimeMillis();
                try (final Writer writer = new BufferedWriter(new FileWriter(tempFile))) {
                    try (final ResultSet resultSet = statement.executeQuery(SQL_GET_ALL_PERSONS_WITH_DOGS)) {
                        while (resultSet.next()) {
                            writer.append(resultSet.getString("firstname") + "," + resultSet.getString("name") + "," + resultSet.getString("note") + "\r\n");
                        }
                    }
                    log.debug("File created in {}", (System.currentTimeMillis() - startTime));
                }
            }
        }

        response.setContentType(APPLICATION_VND_MS_EXCEL);
        response.setContentLengthLong(tempFile.length());
        response.setHeader("Content-Disposition", ATTACHMENT_FILENAME_DOWNLOAD_XLS);
        response.setCharacterEncoding("UTF-8");

        long startTime = System.currentTimeMillis();
        try (final ServletOutputStream outputStream = response.getOutputStream()) {
            try (final InputStream fis = new BufferedInputStream(new FileInputStream(tempFile))) {
                byte[] buff = new byte[10240];
                int len;
                while ((len = fis.read(buff, 0, buff.length)) > -1) {
                    outputStream.write(buff, 0, len);
                }
            }
        }
        log.debug("Streamed in {}", (System.currentTimeMillis() - startTime));
        tempFile.delete();
    }

    @GetMapping("/plain-direct")
    public void getListPlainDirect(HttpServletResponse response) throws Exception {

        try (final Connection connection = dataSource.getConnection()) {
            try (final Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY,
                    ResultSet.CONCUR_READ_ONLY)) {
                statement.setFetchSize(Integer.MIN_VALUE);

                int totalRows = getTotalRows(statement);

                processDirectAsCSV(response, statement, totalRows);
            }
        }
    }

    @GetMapping("/plain-direct-poi")
    public void getListPlainDirectToPoi(HttpServletResponse response) throws Exception {

        try (final Connection connection = dataSource.getConnection()) {
            try (final Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY,
                    ResultSet.CONCUR_READ_ONLY)) {
                statement.setFetchSize(Integer.MIN_VALUE);

                int totalRows = getTotalRows(statement);

                if (totalRows > EXCEL_MAX_ROWS) {
                    processDirectAsCSV(response, statement, totalRows);
                } else {
                    response.setCharacterEncoding("UTF-8");
                    response.setContentType(APPLICATION_VND_OPENXMLFORMATS_OFFICEDOCUMENT_SPREADSHEETML_SHEET);
                    response.setHeader("Content-Disposition", ATTACHMENT_FILENAME_DOWNLOAD_XLSX);

                    SXSSFWorkbook wb = new SXSSFWorkbook(100);
                    Sheet s = wb.createSheet();
                    int rownum = 0;
                    long startTime = System.currentTimeMillis();
                    try (final ResultSet resultSet = statement.executeQuery(SQL_GET_ALL_PERSONS_WITH_DOGS)) {
                        while (resultSet.next()) {
                            if (rownum > EXCEL_MAX_ROWS) {
                                break;
                            }
                            Row r = s.createRow(rownum++);
                            Cell c = r.createCell(0);
                            c.setCellValue(resultSet.getString("firstname"));
                            c = r.createCell(1);
                            c.setCellValue(resultSet.getString("name"));
                            c = r.createCell(2);
                            c.setCellValue(resultSet.getString("note"));
                        }
                    }
                    log.debug("File created in {}", (System.currentTimeMillis() - startTime));
                    startTime = System.currentTimeMillis();
                    try (ServletOutputStream outputStream = response.getOutputStream()) {
                        wb.write(outputStream);
                    }
                    wb.dispose();
                    log.debug("File streamed in {}", (System.currentTimeMillis() - startTime));
                }
            }
        }
    }

    private void processDirectAsCSV(HttpServletResponse response, Statement statement, int totalRows) throws SQLException, IOException {
        response.setCharacterEncoding("UTF-8");
        if (totalRows > EXCEL_MAX_ROWS) {
            response.setContentType(TEXT_CSV);
            response.setHeader("Content-Disposition", ATTACHMENT_FILENAME_DOWNLOAD_CSV);
        } else {
            response.setContentType(APPLICATION_VND_MS_EXCEL);
            response.setHeader("Content-Disposition", ATTACHMENT_FILENAME_DOWNLOAD_XLS);
        }

        long startTime = System.currentTimeMillis();
        try (final ResultSet resultSet = statement.executeQuery(SQL_GET_ALL_PERSONS_WITH_DOGS)) {
            try (final ServletOutputStream outputStream = response.getOutputStream()) {
                while (resultSet.next()) {
                    String str =
                            resultSet.getString("firstname") + "," + resultSet.getString("name") + "," + resultSet.getString("note") + "\r\n";
                    outputStream.write(str.getBytes(StandardCharsets.UTF_8));
                }
            }
            log.debug("File created & streamed in {}", (System.currentTimeMillis() - startTime));
        }
    }

    private int getTotalRows(Statement statement) throws SQLException {
        int totalRows;
        long startTime = System.currentTimeMillis();
        try (final ResultSet resultSet = statement.executeQuery(SQL_GET_COUNT_OF_ALL_PERSONS_WITH_DOGS)) {
            resultSet.next();
            totalRows = resultSet.getInt(1);
        }
        log.debug("Get total-rows ({}) in {}", totalRows, (System.currentTimeMillis() - startTime));
        return totalRows;
    }

}
