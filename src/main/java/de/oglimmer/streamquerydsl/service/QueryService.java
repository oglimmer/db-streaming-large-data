package de.oglimmer.streamquerydsl.service;

import de.oglimmer.streamquerydsl.db.Dog;
import de.oglimmer.streamquerydsl.db.Person;
import de.oglimmer.streamquerydsl.db.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static org.hibernate.jpa.QueryHints.HINT_FETCH_SIZE;

@Service
public class QueryService {

    @Autowired
    private EntityManager entityManager;
    @Autowired
    private PersonRepository personRepository;

    @Transactional(readOnly = true)
    public void queryByStream(OutputStream outputStream) {
        personRepository.getAll()
                .map(person -> {
                    entityManager.detach(person);
                    return person.getName() + "," + person.getFirstname() + "," + person.getName() + "\r\n";
                })
                .forEach(s -> writeToOutputStream(s, outputStream));
    }

    private void writeToOutputStream(String str, OutputStream outputStream) {
        try {
            outputStream.write(str.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void queryWithCriteria(OutputStream outputStream) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        Metamodel metamodel = entityManager.getMetamodel();
        EntityType<Person> personEntityType = metamodel.entity(Person.class);

        CriteriaQuery<Person> q = cb.createQuery(Person.class);
        Root<Person> personRoot = q.from(Person.class);
        personRoot.join(personEntityType.getSet("dogs", Dog.class), JoinType.LEFT);
        q.select(personRoot);

        entityManager.createQuery(q)
                .setHint(HINT_FETCH_SIZE, "" + Integer.MIN_VALUE)
                .getResultStream()
                .map(person -> {
                    entityManager.detach(person);
                    return person.getName() + "," + person.getFirstname() + "," + person.getName() + "\r\n";
                })
                .forEach(s -> writeToOutputStream(s, outputStream));
    }
}
