package de.oglimmer.streamquerydsl.rest;

import de.oglimmer.streamquerydsl.service.QueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;

@RestController
@Slf4j
public class QueryJpaController {

    public static final String APPLICATION_VND_MS_EXCEL = "application/vnd.ms-excel";
    public static final String ATTACHMENT_FILENAME_DOWNLOAD_XLS = "attachment; filename=\"download.xls\"";

    @Autowired
    private QueryService queryService;

    @GetMapping("/jpa")
    public void getListJpa(HttpServletResponse response) throws Throwable {
        response.setCharacterEncoding("UTF-8");
        response.setContentType(APPLICATION_VND_MS_EXCEL);
        response.setHeader("Content-Disposition", ATTACHMENT_FILENAME_DOWNLOAD_XLS);
        try (final OutputStream outputStream = response.getOutputStream()) {
            queryService.queryByStream(outputStream);
        }
    }

    @GetMapping("/jpa-criteria")
    public void getListJpaCriteria(HttpServletResponse response) throws Throwable {
        response.setCharacterEncoding("UTF-8");
        response.setContentType(APPLICATION_VND_MS_EXCEL);
        response.setHeader("Content-Disposition", ATTACHMENT_FILENAME_DOWNLOAD_XLS);
        try (final OutputStream outputStream = response.getOutputStream()) {
            queryService.queryWithCriteria(outputStream);
        }
    }

}
