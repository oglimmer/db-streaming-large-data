package de.oglimmer.streamquerydsl.rest;

import de.oglimmer.streamquerydsl.service.CreationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@Slf4j
public class CreateController {

    @Autowired
    private CreationService creationService;

    private ExecutorService executorService = Executors.newFixedThreadPool(10);

    @PostMapping()
    public void create(@RequestBody Long number) {
        for (int i = 0; i < number; i++) {
            executorService.submit(() -> creationService.create());
        }
    }

}
