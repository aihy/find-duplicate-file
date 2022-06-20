package com.hhwyz.md5;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author erniu.wzh
 * @date 2022/6/20 10:35
 */
@Component
@Slf4j
public class MyComponent implements CommandLineRunner {
    @Autowired
    private FileMd5Dao fileMd5Dao;
    @Value("${all.file.path}")
    private String allFilePath;
    @Value("${core.num}")
    private int poolSize;
    private Long count = 0L;
    private Instant start = null;
    private Long allFileSize = null;
    private Instant now = Instant.now();

    @PostConstruct
    public void init() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(allFilePath));
            allFileSize = 0L;
            while (reader.readLine() != null) {
                allFileSize++;
            }
            reader.close();
        } catch (Exception e) {
            log.error("allFileSize error", e);
            System.exit(-1);
        }
    }


    /**
     * with shuffle
     */
    @Override
    public void run(String... args) throws Exception {
        log.info("preparing");
        fileMd5Dao.deleteAll();
        BlockingQueue<String> workQueue = new ArrayBlockingQueue<>(poolSize * 2);
        ExecutorService md5Pool = Executors.newFixedThreadPool(poolSize);
        List<String> pathList = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(allFilePath), StandardCharsets.UTF_8));
        while (true) {
            String line = reader.readLine();
            if (line == null) {
                break;
            }
            pathList.add(line);
        }
        Collections.shuffle(pathList);
        log.info("prepare done");
        start = Instant.now();
        for (String s : pathList) {
            workQueue.put(s);
            md5Pool.submit(() -> {
                String item = null;
                try {
                    item = workQueue.take();
                    processPath(item);
                } catch (Exception e) {
                    log.error("item {} error {}", item, e.getMessage());
                }
            });
            count += 1;
        }
        System.exit(0);
    }

    private void processPath(String line) throws IOException {
        File file = new File(line);
        InputStream stream;
        stream = Files.newInputStream(file.toPath());
        String md5Hex = DigestUtils.md5Hex(stream);
        stream.close();
        FileMd5 fileMd5 = new FileMd5();
        fileMd5.setMd5(md5Hex);
        fileMd5.setPath(line);
        fileMd5.setSize(file.length());
        fileMd5Dao.insert(fileMd5);
    }

    @Scheduled(cron = "* * * * * ?")
    public void stat() {
        if (start == null) {
            return;
        }
        if (!count.equals(allFileSize)) {
            now = Instant.now();
        }
        Duration d = Duration.between(now, start).abs();
        double lastSeconds = (double) d.getSeconds() / count * (allFileSize - count);
        Duration lastD = Duration.of((long) lastSeconds, ChronoUnit.SECONDS);
        log.info(String.format("已完成：%.2f%% 已耗时：%s 剩余时间：%s", (double) count / allFileSize * 100, d, lastD.toString()));
    }
}
