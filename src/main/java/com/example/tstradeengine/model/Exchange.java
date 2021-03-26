package com.example.tstradeengine.model;

public class Exchange {
    private Integer id;
    private String url;
    private boolean isEnable;

    public Exchange() {
    }

    public Exchange(int id, String url, boolean isEnable) {
        this.id = id;
        this.url = url;
        this.isEnable = isEnable;
    }

    public Integer getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isEnable() {
        return isEnable;
    }

    public void setEnable(boolean enable) {
        isEnable = enable;
    }
}
