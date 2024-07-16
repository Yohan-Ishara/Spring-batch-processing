package com.practise.batch.springbatch.config;

import com.practise.batch.springbatch.entity.Student;
import com.practise.batch.springbatch.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.File;
import java.util.Objects;


@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    private final StudentRepository studentRepository;
    private final PlatformTransactionManager platformTransactionManager;
    private final JobRepository jobRepository;

    @Bean
    @StepScope
    public FlatFileItemReader<Student> itemReader(//@Value("#{jobExecutionContext['fileResource']}") InputStreamResource resource
                                                  @Value("#{jobParameters[fullPathFileName]}") String pathToFile){

        FlatFileItemReader<Student> itemReader = new FlatFileItemReader<Student>();
        itemReader.setResource(new FileSystemResource(new File(pathToFile)));
        //itemReader.setResource(resource);
        itemReader.setName("csvReader");
        itemReader.setLinesToSkip(1);
        itemReader.setLineMapper(lineMapper());
        return itemReader;
    }

//    @Bean
//    public FlatFileItemReader<Student> itemReader() {
//        FlatFileItemReader<Student> reader = new FlatFileItemReader<Student>() {
//            @Override
//            public void open(ExecutionContext executionContext) throws ItemStreamException {
//                String fileName = executionContext.getString("fileName");
//                Resource resource = new FileSystemResource("src/main/resources/" + fileName); // Use the stored file path
//                setResource(resource);
//                super.open(executionContext);
//            }
//        };
//        reader.setName("csvReader");
//        reader.setLinesToSkip(1);
//        reader.setLineMapper(lineMapper());
//        return reader;
//    }

    @Bean
    public RepositoryItemWriter<Student> writer(){
        RepositoryItemWriter<Student> itemWriter = new RepositoryItemWriter<>();
        itemWriter.setMethodName("save");
        itemWriter.setRepository(studentRepository);
        return itemWriter;
    }

    @Bean
    public TaskExecutor taskExecutor(){
        SimpleAsyncTaskExecutor asyncTaskExecutor = new SimpleAsyncTaskExecutor();
        asyncTaskExecutor.setConcurrencyLimit(20);
        return asyncTaskExecutor;
    }

    @Bean
    public StudentProcessor processor(){
        return new StudentProcessor();
    }

    @Bean
    public Step importStep(FlatFileItemReader<Student> itemReader){
        return new StepBuilder("csvImport",jobRepository)
                .<Student,Student>chunk(1000,platformTransactionManager)
                .reader(itemReader)
                .processor(processor())
                .writer(writer())
                .taskExecutor(taskExecutor())
                .build();

    }

    @Bean
    public Job runJob(FlatFileItemReader<Student> itemReader){
        return new JobBuilder("importStudents",jobRepository)
                .start(importStep(itemReader))
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
