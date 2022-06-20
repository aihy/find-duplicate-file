package com.hhwyz.md5;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
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
    private Instant start;

    @Override
    public void run(String... args) throws Exception {
        start = Instant.now();
        BlockingQueue<String> workQueue = new ArrayBlockingQueue<>(poolSize * 2);
        ExecutorService md5Pool = Executors.newFixedThreadPool(poolSize);

        BufferedReader reader = new BufferedReader(Files.newBufferedReader(Paths.get(allFilePath)));
        while (true) {
            String line = reader.readLine();
            if (line == null) {
                break;
            }
            workQueue.put(line);
            md5Pool.submit(() -> {
                try {
                    processPath(workQueue.take());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            count += 1;
        }
    }

    private void processPath(String line) {
        try {
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
        } catch (Exception e) {
            log.error("process error, line={}", line, e);
        }
    }

    public Long getCount() {
        return count;
    }

    public Instant getStart() {
        return start;
    }
}
