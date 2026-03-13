package com.sh.Ram.aws.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    private final AmazonS3 amazonS3;

    public String imageUpload(MultipartFile image) throws IOException {

        String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();

        ObjectMetadata data = new ObjectMetadata();
        data.setContentType(image.getContentType());
        data.setContentLength(image.getSize());

        amazonS3.putObject(bucket, fileName, image.getInputStream(), data);

        return amazonS3.getUrl(bucket, fileName).toString();

    }
}