package com.practise.batch.springbatch.config;

import com.practise.batch.springbatch.entity.Student;
import com.practise.batch.springbatch.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;


@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    private final StudentRepository studentRepository;
    private final PlatformTransactionManager platformTransactionManager;
    private final JobRepository jobRepository;

    @Bean
    public FlatFileItemReader<Student> itemReader(){
        FlatFileItemReader<Student> itemReader = new FlatFileItemReader<Student>();
        itemReader.setResource(new FileSystemResource("src/main/resources/student.csv"));
        itemReader.setName("csvReader");
        itemReader.setLinesToSkip(1);
        itemReader.setLineMapper(lineMapper());
        return itemReader;
    }

    @Bean
    public RepositoryItemWriter<Student> writer(){
        RepositoryItemWriter<Student> itemWriter = new RepositoryItemWriter<>();
        itemWriter.setMethodName("save");
        itemWriter.setRepository(studentRepository);
        return itemWriter;
    }

    @Bean
    public StudentProcessor processor(){
        return new StudentProcessor();
    }

    @Bean
    public Step importStep(){
        return new StepBuilder("csvImport",jobRepository)
                .<Student,Student>chunk(10,platformTransactionManager)
                .reader(itemReader())
                .processor(processor())
                .writer(writer())
                .build();

    }

    @Bean
    public Job runJob(){
        return new JobBuilder("importStudents",jobRepository)
                .start(importStep())
                .build();
    }

    private LineMapper<Student> lineMapper(){
        DefaultLineMapper<Student> lineMapper = new DefaultLineMapper<>();
        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(DelimitedLineTokenizer.DELIMITER_COMMA);
        lineTokenizer.setStrict(false);
        lineTokenizer.setNames("id","firstName","lastName","age");

        BeanWrapperFieldSetMapper<Student> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(Student.class);

        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);
        return lineMapper;
    }
}
