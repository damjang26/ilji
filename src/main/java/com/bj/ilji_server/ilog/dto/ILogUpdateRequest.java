package com.bj.ilji_server.ilog.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ILogUpdateRequest {
    private String content;
    private int visibility;
    private List<String> existingImageUrls;
}