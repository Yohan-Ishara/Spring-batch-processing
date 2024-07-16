package com.practise.batch.springbatch.utility;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.AbstractResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

public class InMemoryMultipartFileResource extends AbstractResource {

    private final MultipartFile multipartFile;

    public InMemoryMultipartFileResource(MultipartFile multipartFile) {
        this.multipartFile = multipartFile;
    }

    @Override
    public String getDescription() {
        return multipartFile.getOriginalFilename();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return multipartFile.getInputStream();
    }
}
