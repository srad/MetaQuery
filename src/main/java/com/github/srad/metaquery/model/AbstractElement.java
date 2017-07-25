package com.github.srad.metaquery.model;

abstract public class AbstractElement {
    public String getId() {
        return id;
    }

    public String getBegin() {
        return begin;
    }

    public String getEnd() {
        return end;
    }

    public String getText() {
        return text;
    }

    private String id;
    private String begin;
    private String end;
    private String text;

    protected AbstractElement(String id, String begin, String end, String text) {
        this.id = id;
        this.begin = begin;
        this.end = end;
        this.text = text;
    }
}
