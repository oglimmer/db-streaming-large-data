package de.oglimmer.streamquerydsl.rest;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import de.oglimmer.streamquerydsl.db.QDog;
import de.oglimmer.streamquerydsl.db.QPerson;
import de.oglimmer.streamquerydsl.db.Dog;
import de.oglimmer.streamquerydsl.db.Person;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.stream.Stream;

@RestController
@Slf4j
public class QueryDslController {

    public static final String APPLICATION_VND_MS_EXCEL = "application/vnd.ms-excel";
    public static final String ATTACHMENT_FILENAME_DOWNLOAD_XLS = "attachment; filename=\"download.xls\"";

    @Autowired
    private EntityManager entityManager;

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

}
