package de.oglimmer.streamquerydsl;

import de.oglimmer.streamquerydsl.service.CreationService;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

//@SpringBootTest
class StreamQuerydslApplicationTests {

	public static final int BATCH_SIZE = 4000;
	public static final int PERSONS = BATCH_SIZE * 2500;
	public static final int DOGS = BATCH_SIZE * 2500;

	@Test
	void contextLoads() throws IOException {
		Writer writer = new BufferedWriter(new FileWriter("/tmp/data.sql"));
		createPerson(writer);
		createDog(writer);
		writer.close();
	}

	private void createPerson(Writer writer) throws IOException {
		int id = 1;
		while (id < PERSONS) {
			StringBuilder buff = new StringBuilder();
			buff.append("INSERT INTO person (id, name, firstname, note) VALUES ");
			int batchCount = 0;
			while (batchCount < BATCH_SIZE) {
				if (batchCount > 0) {
					buff.append(",");
				}
				final int nextId = id++;
				buff.append("(");
				buff.append(nextId);
				buff.append(",'");
				buff.append(CreationService.generateName());
				buff.append("','");
				buff.append(CreationService.generateName());
				buff.append("','");
				buff.append(CreationService.generateName());
				buff.append("')");
				batchCount++;
			}
			buff.append(";\r\n\r\n");
			writer.append(buff);
		}
	}

	private void createDog(Writer writer) throws IOException {
		int id = 1;
		while (id < DOGS) {
			StringBuilder buff = new StringBuilder();
			buff.append("INSERT INTO dog (id, name, note, owner_id) VALUES ");
			int batchCount = 0;
			while (batchCount < BATCH_SIZE) {
				if (batchCount > 0) {
					buff.append(",");
				}
				buff.append("(");
				buff.append(id++);
				buff.append(",'");
				buff.append(CreationService.generateName());
				buff.append("','");
				buff.append(CreationService.generateName());
				buff.append("',");
				buff.append((int) (Math.random() * PERSONS) + 1);
				buff.append(")");
				batchCount++;
			}
			buff.append(";\r\n\r\n");
			writer.append(buff);
		}
	}

}
