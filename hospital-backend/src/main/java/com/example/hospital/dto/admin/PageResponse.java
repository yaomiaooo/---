package com.example.hospital.dto.admin;

import java.util.List;

public class PageResponse<T> {
    private List<T> records;
    private long total;

    public PageResponse() {}

    public PageResponse(List<T> records, long total) {
        this.records = records;
        this.total = total;
    }

    public static <T> PageResponse<T> of(List<T> records, long total) {
        return new PageResponse<>(records, total);
    }

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
}
