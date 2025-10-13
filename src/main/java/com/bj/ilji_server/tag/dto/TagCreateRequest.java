package com.bj.ilji_server.tag.dto;

import com.bj.ilji_server.tag.entity.TagVisibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TagCreateRequest {
    private String label;
    private String color;
    private TagVisibility visibility; // 추가
}
