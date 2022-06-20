package com.hhwyz.md5;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * @author erniu.wzh
 * @date 2022/6/20 11:36
 */
@Controller
public class MyController {
    @Autowired
    private MyComponent myComponent;
    @Value("${all.file.path}")
    private String allFilePath;

    private Long allFileSize = null;
    private Instant now = Instant.now();

    @GetMapping(value = "/stat", produces = {MediaType.TEXT_HTML_VALUE})
    @ResponseBody
    public String stat() throws IOException {
        if (allFileSize == null) {
            allFileSize = Files.lines(Paths.get(allFilePath)).count();
        }
        long all = allFileSize;
        long done = myComponent.getCount();

        if (done != all) {
            now = Instant.now();
        }
        Instant start = myComponent.getStart();
        Duration d = Duration.between(now, start).abs();
        double lastSeconds = (double) d.getSeconds() / done * (all - done);
        Duration lastD = Duration.of((long) lastSeconds, ChronoUnit.SECONDS);
        return String.format("已完成：%.2f%% 已耗时：%s 剩余时间：%s", (double) done / all * 100, d, lastD.toString());
    }
}
