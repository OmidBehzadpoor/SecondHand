package com.example.secondhandfx.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PageResponse<T> {
    private List<T> content;
    private int totalPages;
    private long totalElements;
    private int number; // current page index (0-based)
    private int size;
    private boolean last;
}