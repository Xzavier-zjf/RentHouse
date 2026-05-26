package com.renthouse.houseservice.service;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.renthouse.houseservice.model.HouseImage;
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
import java.util.Comparator;
import java.util.List;
import java.util.stream.StreamSupport;

@Service
public class HouseImageStorageService {
    private static final int MAX_HOUSE_IMAGES = 8;
    private static final List<String> ALLOWED_TYPES = List.of("image/jpeg", "image/png", "image/webp");

    @Autowired
    private GridFsTemplate gridFsTemplate;
    @Autowired
    private MongoDatabaseFactory mongoDatabaseFactory;

    public HouseImage addHouseImage(Integer houseId, MultipartFile file) throws IOException {
        validateImage(file);
        List<HouseImage> existing = listHouseImages(houseId);
        if (existing.size() >= MAX_HOUSE_IMAGES) {
            throw new IllegalArgumentException("每套房源最多上传8张图片");
        }
        int sortOrder = existing.stream().map(HouseImage::getSortOrder).filter(value -> value != null).max(Integer::compareTo).orElse(0) + 1;
        boolean cover = existing.isEmpty();
        Document metadata = new Document()
                .append("ownerType", "HOUSE")
                .append("ownerId", houseId)
                .append("usage", "HOUSE_IMAGE")
                .append("contentType", file.getContentType())
                .append("sortOrder", sortOrder)
                .append("isCover", cover);
        ObjectId id = gridFsTemplate.store(file.getInputStream(), file.getOriginalFilename(), file.getContentType(), metadata);
        return new HouseImage(id.toHexString(), sortOrder, cover);
    }

    public List<HouseImage> listHouseImages(Integer houseId) {
        return StreamSupport.stream(gridFsTemplate.find(houseQuery(houseId)).spliterator(), false)
                .map(this::toHouseImage)
                .sorted(Comparator.comparing(HouseImage::getSortOrder, Comparator.nullsLast(Integer::compareTo)))
                .toList();
    }

    public String coverImageUrl(Integer houseId) {
        List<HouseImage> images = listHouseImages(houseId);
        return images.stream()
                .filter(image -> Boolean.TRUE.equals(image.getCover()))
                .findFirst()
                .or(() -> images.stream().findFirst())
                .map(HouseImage::getUrl)
                .orElse(null);
    }

    public void updateHouseImage(Integer houseId, String fileId, Integer sortOrder, Boolean cover) {
        GridFSFile file = requireHouseImage(houseId, fileId);
        Document metadata = file.getMetadata() == null ? new Document() : new Document(file.getMetadata());
        if (sortOrder != null) {
            metadata.put("sortOrder", sortOrder);
        }
        if (Boolean.TRUE.equals(cover)) {
            clearCover(houseId);
            metadata.put("isCover", true);
        }
        metadata.put("ownerType", "HOUSE");
        metadata.put("ownerId", houseId);
        metadata.put("usage", "HOUSE_IMAGE");
        updateMetadata(fileId, metadata);
    }

    public void deleteHouseImage(Integer houseId, String fileId) {
        GridFSFile file = requireHouseImage(houseId, fileId);
        boolean wasCover = file.getMetadata() != null && Boolean.TRUE.equals(file.getMetadata().getBoolean("isCover"));
        gridFsTemplate.delete(new Query(Criteria.where("_id").is(new ObjectId(fileId))));
        if (wasCover) {
            listHouseImages(houseId).stream().findFirst().ifPresent(next -> updateHouseImage(houseId, next.getFileId(), next.getSortOrder(), true));
        }
    }

    public StoredImage read(String fileId) throws IOException {
        ObjectId objectId = new ObjectId(fileId);
        GridFSFile file = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(objectId)));
        if (file == null) {
            return null;
        }
        GridFSBucket bucket = GridFSBuckets.create(mongoDatabaseFactory.getMongoDatabase());
        byte[] data = bucket.openDownloadStream(objectId).readAllBytes();
        String contentType = file.getMetadata() == null ? "application/octet-stream" : file.getMetadata().getString("contentType");
        return new StoredImage(data, contentType);
    }

    private HouseImage toHouseImage(GridFSFile file) {
        Document metadata = file.getMetadata() == null ? new Document() : file.getMetadata();
        return new HouseImage(file.getObjectId().toHexString(), metadata.getInteger("sortOrder", 0), metadata.getBoolean("isCover", false));
    }

    private Query houseQuery(Integer houseId) {
        return new Query(Criteria.where("metadata.ownerType").is("HOUSE").and("metadata.ownerId").is(houseId));
    }

    private GridFSFile requireHouseImage(Integer houseId, String fileId) {
        if (!ObjectId.isValid(fileId)) {
            throw new IllegalArgumentException("图片ID无效");
        }
        GridFSFile file = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(new ObjectId(fileId))
                .and("metadata.ownerType").is("HOUSE")
                .and("metadata.ownerId").is(houseId)));
        if (file == null) {
            throw new IllegalArgumentException("图片不存在");
        }
        return file;
    }

    private void clearCover(Integer houseId) {
        gridFsTemplate.find(houseQuery(houseId)).forEach(file -> {
            Document metadata = file.getMetadata() == null ? new Document() : new Document(file.getMetadata());
            metadata.put("isCover", false);
            updateMetadata(file.getObjectId().toHexString(), metadata);
        });
    }

    private void updateMetadata(String fileId, Document metadata) {
        mongoDatabaseFactory.getMongoDatabase().getCollection("fs.files")
                .updateOne(new Document("_id", new ObjectId(fileId)), new Document("$set", new Document("metadata", metadata)));
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

    public record StoredImage(byte[] data, String contentType) {}
}
