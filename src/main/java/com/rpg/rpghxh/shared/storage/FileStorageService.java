package com.rpg.rpghxh.shared.storage;

import com.rpg.rpghxh.shared.exceptions.FileStorageException;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
public class FileStorageService {

    private final MinioClient minioClient;
    private final String bucket;

    public FileStorageService(MinioClient minioClient, @Value("${minio.bucket}") String bucket) {
        this.minioClient = minioClient;
        this.bucket = bucket;
    }

    public void upload(String objectKey, InputStream stream, long size, String contentType) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .stream(stream, size, -1)
                    .contentType(contentType)
                    .build());
        } catch (Exception ex) {
            throw new FileStorageException("Falha ao enviar arquivo para o storage", ex);
        }
    }

    public InputStream download(String objectKey) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .build());
        } catch (Exception ex) {
            throw new FileStorageException("Falha ao baixar arquivo do storage", ex);
        }
    }

    public ObjectStat stat(String objectKey) {
        try {
            StatObjectResponse stat = minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .build());
            return new ObjectStat(stat.contentType(), stat.size());
        } catch (Exception ex) {
            throw new FileStorageException("Falha ao obter metadados do arquivo no storage", ex);
        }
    }

    public void delete(String objectKey) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .build());
        } catch (Exception ex) {
            throw new FileStorageException("Falha ao remover arquivo do storage", ex);
        }
    }

    public record ObjectStat(String contentType, long size) {
    }
}