package com.practise.batch.springbatch.controller;

import com.practise.batch.springbatch.utility.InMemoryMultipartFileResource;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
public class StudentController {


    private final JobLauncher launcher;
    private final Job job;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please upload a valid CSV file.");
        }

        try {
            // Save the file locally
//            String filePath = "/Users/yohan/Desktop/Code/spring-batch/src/main/resources/" + file.getOriginalFilename();
//            File localFile = new File(filePath);
//            file.transferTo(localFile);

            // Start the batch job
            JobParameters jobParameters = new JobParametersBuilder()
                   // .addString("filePath", filePath)
                    .addLong("startAt", System.currentTimeMillis())
                    .toJobParameters();

            launcher.run(job, jobParameters);
            return ResponseEntity.status(HttpStatus.OK).body("File uploaded successfully.");

        } catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException |
                 JobParametersInvalidException e) {
            throw new RuntimeException(e);
        }
    }
}