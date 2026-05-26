package com.renthouse.userservice.service;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class ImageStorageService {
    private static final List<String> ALLOWED_TYPES = List.of("image/jpeg", "image/png", "image/webp");

    @Autowired
    private GridFsTemplate gridFsTemplate;
    @Autowired
    private MongoDatabaseFactory mongoDatabaseFactory;

    public String replaceUserAvatar(Integer userId, String oldFileId, MultipartFile file) throws IOException {
        validateImage(file);
        if (oldFileId != null && !oldFileId.isBlank()) {
            delete(oldFileId);
        }
        Document metadata = new Document()
                .append("ownerType", "USER")
                .append("ownerId", userId)
                .append("usage", "AVATAR")
                .append("contentType", file.getContentType());
        ObjectId id = gridFsTemplate.store(file.getInputStream(), file.getOriginalFilename(), file.getContentType(), metadata);
        return id.toHexString();
    }

    public StoredImage read(String fileId) throws IOException {
        if (!ObjectId.isValid(fileId)) {
            return null;
        }
        ObjectId objectId = new ObjectId(fileId);
        GridFSFile file = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(objectId)
                .and("metadata.ownerType").is("USER")
                .and("metadata.usage").is("AVATAR")));
        if (file == null) {
            return null;
        }
        GridFSBucket bucket = GridFSBuckets.create(mongoDatabaseFactory.getMongoDatabase());
        byte[] data = bucket.openDownloadStream(objectId).readAllBytes();
        String contentType = file.getMetadata() == null ? "application/octet-stream" : file.getMetadata().getString("contentType");
        return new StoredImage(data, contentType);
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("图片不能为空");
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("仅支持 JPG、PNG、WEBP 图片");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("单张图片不能超过5MB");
        }
    }

    private void delete(String fileId) {
        if (ObjectId.isValid(fileId)) {
            gridFsTemplate.delete(new Query(Criteria.where("_id").is(new ObjectId(fileId))));
        }
    }

    public record StoredImage(byte[] data, String contentType) {}
}
