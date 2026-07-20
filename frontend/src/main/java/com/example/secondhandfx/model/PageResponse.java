package com.example.secondhandfx.model;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PageResponse<T> {
    private List<T> content;
    private int totalPages;
    private long totalElements;
    private int number; // current page index (0-based)
    private int size;
    private boolean last;
}