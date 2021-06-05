package de.oglimmer.streamquerydsl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class CreationService {

    private final String[] BEGINNING = {"Kr", "Ca", "Ra", "Mrok", "Cru",
            "Ray", "Bre", "Zed", "Drak", "Mor", "Jag", "Mer", "Jar", "Mjol",
            "Zork", "Mad", "Cry", "Zur", "Creo", "Azak", "Azur", "Rei", "Cro",
            "Mar", "Luk"};
    private final String[] MIDDLE = {"air", "ir", "mi", "sor", "mee", "clo",
            "red", "cra", "ark", "arc", "miri", "lori", "cres", "mur", "zer",
            "marac", "zoir", "slamar", "salmar", "urak"};
    private final String[] END = {"d", "ed", "ark", "arc", "es", "er", "der",
            "tron", "med", "ure", "zur", "cred", "mur"};

    private Random rand = new Random();

    @Autowired
    private PersonRepository personRepository;
    @Autowired
    private DogRepository dogRepository;

    private String generateName() {
        return BEGINNING[rand.nextInt(BEGINNING.length)] +
                MIDDLE[rand.nextInt(MIDDLE.length)] +
                END[rand.nextInt(END.length)];

    }

    @Transactional
    public void create() {
        List<Person> list = new ArrayList<>(100);
        for (int i = 0; i < 100; i++) {
            Person person = new Person();
            person.setFirstname(generateName());
            person.setName(generateName());
            person.setNote(generateName());
            list.add(person);
        }
        personRepository.saveAll(list);
        List<Dog> dogs = new ArrayList<>(100 * 8);
        for (int i = 0; i < 100; i++) {
            Person person = list.get(i);
            for (int j = 0; j < Math.random() * 10; j++) {
                Dog dog = new Dog();
                dog.setName(generateName());
                dog.setNote("this dog was born in " + generateName());
                dog.setOwner(person);
                dogs.add(dog);
            }
        }
        dogRepository.saveAll(dogs);
    }

}
