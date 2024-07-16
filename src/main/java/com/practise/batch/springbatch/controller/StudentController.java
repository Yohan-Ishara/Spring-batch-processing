package com.practise.batch.springbatch.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
public class StudentController {


    private final JobLauncher launcher;
    private final Job job;
    private final String TEMP_LOCATION = "/Users/yohan/Desktop/Code/spring-batch/src/main/resources";

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile multipartFile) {

        if (multipartFile.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please upload a valid CSV file.");
        }

        try {
            String originalFilename = multipartFile.getOriginalFilename();
            File fileToImport = new File(TEMP_LOCATION +originalFilename);
            multipartFile.transferTo(fileToImport);

            // Start the batch job
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("fullPathFileName", TEMP_LOCATION +originalFilename)
                    .addLong("startAt", System.currentTimeMillis())
                    .toJobParameters();

            JobExecution jobExecution = launcher.run(job, jobParameters);

              if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
                Files.deleteIfExists(Paths.get(TEMP_LOCATION +originalFilename));
            }
            return ResponseEntity.status(HttpStatus.OK).body("File uploaded successfully.");

        } catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException |
                 JobParametersInvalidException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}



//      try {
//            // Convert MultipartFile to InputStreamResource
//            InputStream inputStream = multipartFile.getInputStream();
//            InputStreamResource resource = new InputStreamResource(inputStream);
//
//            // Start the batch job
//            JobParameters jobParameters = new JobParametersBuilder()
//                    .addString("fileName", Objects.requireNonNull(multipartFile.getOriginalFilename()))
//                    .addLong("startAt", System.currentTimeMillis())
//                    .toJobParameters();
//
//            // Set the resource in the execution context
//            JobExecution jobExecution = launcher.run(job, jobParameters);
//            ExecutionContext executionContext = jobExecution.getExecutionContext();
//            executionContext.put("fileResource", resource);
//
//            if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
//                return ResponseEntity.status(HttpStatus.OK).body("File uploaded successfully and processed.");
//            } else {
//                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Batch job failed to process the file.");
//            }