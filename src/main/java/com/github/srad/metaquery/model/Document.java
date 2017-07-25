package com.github.srad.metaquery.model;

import java.util.Map;

public class Document {
    private String id;
    private String title;
    private String text;

    public Document(Map<String, String> data) {
        setId(data.get("id"));
        setTitle(data.get("title"));
        setText(data.getOrDefault("text", null));
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
