package de.oglimmer.streamquerydsl;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

@RestController
@Slf4j
public class QueryController {

    public static final String APPLICATION_VND_MS_EXCEL = "application/vnd.ms-excel";
    public static final String ATTACHMENT_FILENAME_DOWNLOAD_XLS = "attachment; filename=\"download.xls\"";
    @Autowired
    private CreationService creationService;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private DataSource dataSource;

    private ExecutorService executorService = Executors.newFixedThreadPool(10);

    @GetMapping("/plain-file")
    public void getListPlainFile(@QuerydslPredicate(root = Person.class) Predicate predicate,
                                 HttpServletResponse response) throws Throwable {

        try (final Connection connection = dataSource.getConnection()) {
            try (final Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY,
                    ResultSet.CONCUR_READ_ONLY)) {
                statement.setFetchSize(Integer.MIN_VALUE);

                final File tempFile = File.createTempFile("sql-", ".data");

                long startTime = System.currentTimeMillis();
                try (final ResultSet resultSet = statement.executeQuery("select * from person join dog on person" +
                        ".id=dog.owner_id")) {
                    try (final Writer writer = new BufferedWriter(new FileWriter(tempFile))) {
                        while (resultSet.next()) {
                            writer.append(resultSet.getString("firstname") + "," + resultSet.getString("name") + "," + resultSet.getString("note") + "\r\n");
                        }
                    }
                    log.debug("File created in {}", (System.currentTimeMillis() - startTime));

                    response.setContentType(APPLICATION_VND_MS_EXCEL);
                    response.setContentLengthLong(tempFile.length());
                    response.setHeader("Content-Disposition", ATTACHMENT_FILENAME_DOWNLOAD_XLS);
                    response.setCharacterEncoding("UTF-8");

                    startTime = System.currentTimeMillis();
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
                } finally {
                    tempFile.delete();
                }
            }
        }
    }

    @GetMapping("/plain-direct")
    public void getListPlainDirect(@QuerydslPredicate(root = Person.class) Predicate predicate,
                                   HttpServletResponse response) throws Throwable {

        try (final Connection connection = dataSource.getConnection()) {
            try (final Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY,
                    ResultSet.CONCUR_READ_ONLY)) {
                statement.setFetchSize(Integer.MIN_VALUE);

                response.setContentType(APPLICATION_VND_MS_EXCEL);
                response.setHeader("Content-Disposition", ATTACHMENT_FILENAME_DOWNLOAD_XLS);
                response.setCharacterEncoding("UTF-8");

                long startTime = System.currentTimeMillis();
                try (final ResultSet resultSet = statement.executeQuery("select * from person join dog on person" +
                        ".id=dog.owner_id")) {
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
        }
    }

    @GetMapping("/dsl")
    public void getListQueryDsl(@QuerydslPredicate(root = Person.class) Predicate predicate,
                                HttpServletResponse response) throws Throwable {

        JPAQueryFactory jpaFctory = new JPAQueryFactory(entityManager);

        response.setContentType(APPLICATION_VND_MS_EXCEL);
        response.setHeader("Content-Disposition", ATTACHMENT_FILENAME_DOWNLOAD_XLS);
        response.setCharacterEncoding("UTF-8");

        Expression<Person> select = QPerson.person;
        EntityPath<Person> entityPath = QPerson.person;

        EntityPath<Dog> dogPath = QDog.dog;

        try (Stream<Person> stream = jpaFctory
                .select(select)
                .from(entityPath)
//                .join(dogPath)
//                .on(QPerson.person.eq(QDog.dog.owner))
                .createQuery()
//                .setHint(QueryHints.HINT_FETCH_SIZE, Integer.MIN_VALUE)
//    int resultSetType   ResultSet.TYPE_FORWARD_ONLY, int resultSetConcurrency  ResultSet.CONCUR_READ_ONLY ?!?!?!?
                .getResultStream()) {

            try (final PrintWriter writer = response.getWriter()) {
                stream.forEach(row -> {
                    row.getDogs().stream().forEach(subrow -> {
                        writer.println(row.getFirstname() + "," + row.getName() + "," + row.getNote());
                    });
                    writer.flush();
                });
            }
        }
    }

    @PostMapping()
    public void create(@RequestBody Long number) {
        for (int i = 0; i < number; i++) {
            executorService.submit(() -> creationService.create());
        }
    }

}
