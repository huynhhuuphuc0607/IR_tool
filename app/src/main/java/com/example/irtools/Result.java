package com.example.irtools;

public class Result {
    private String url;
    private String docId;
    private double tf;
    private double tf_idf;
    private String title;
    private String description;

    public Result(String url, String title, String description, String docId, double tf, double tf_idf) {
        this.url = url;
        this.docId = docId;
        this.tf = tf;
        this.tf_idf = tf_idf;
        this.title = title;
        this.description = description;
    }

    public Result(String url, String docId, double tf, double tf_idf) {
        this.url = url;
        this.docId = docId;
        this.tf = tf;
        this.tf_idf = tf_idf;
        this.title = "";
        this.description = "";
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public double getTf() {
        return tf;
    }

    public void setTf(double tf) {
        this.tf = tf;
    }

    public double getTf_idf() {
        return tf_idf;
    }

    public void setTf_idf(double tf_idf) {
        this.tf_idf = tf_idf;
    }
}
