package com.renthouse.houseservice.model;

public class HouseImage {
    private String fileId;
    private String url;
    private Integer sortOrder;
    private Boolean cover;

    public HouseImage() {}

    public HouseImage(String fileId, Integer sortOrder, Boolean cover) {
        this.fileId = fileId;
        this.url = "/api/house/images/" + fileId;
        this.sortOrder = sortOrder;
        this.cover = cover;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Boolean getCover() {
        return cover;
    }

    public void setCover(Boolean cover) {
        this.cover = cover;
    }
}
