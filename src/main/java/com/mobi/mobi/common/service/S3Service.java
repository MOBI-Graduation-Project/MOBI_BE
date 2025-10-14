package com.mobi.mobi.common.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.mobi.mobi.apiPayload.handler.MemberHandler;
import com.mobi.mobi.apiPayload.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3 amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String uploadFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new MemberHandler(ErrorStatus.MEMBER_PROFILE_IMAGE_NOT_FOUND);
        }

        try {
            String originalFileName = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFileName);
            String storedFileName = UUID.randomUUID() + "." + fileExtension;

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            amazonS3Client.putObject(
                    new PutObjectRequest(bucket, storedFileName, file.getInputStream(), metadata)
                            .withCannedAcl(CannedAccessControlList.PublicRead)
            );

            return amazonS3Client.getUrl(bucket, storedFileName).toString();

        } catch (IOException e) {
            throw new MemberHandler(ErrorStatus.MEMBER_PROFILE_UPLOAD_FAILED);
        }
    }

    private String getFileExtension(String fileName) {
        try {
            return fileName.substring(fileName.lastIndexOf("."));
        } catch (StringIndexOutOfBoundsException e) {
            throw new MemberHandler(ErrorStatus.MEMBER_PROFILE_INVALID_FILE_FORMAT);
        }
    }
}
