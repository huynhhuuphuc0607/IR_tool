package com.example.irtools;

public class Result {
    private String url;
    private String docId;
    private double tf;
    private double tf_idf;

    public Result(String url, String docId, double tf, double tf_idf) {
        this.url = url;
        this.docId = docId;
        this.tf = tf;
        this.tf_idf = tf_idf;
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
