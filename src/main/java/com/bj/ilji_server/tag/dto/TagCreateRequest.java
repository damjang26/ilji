package com.bj.ilji_server.tag.dto;

import com.bj.ilji_server.tag.entity.TagVisibility;
import lombok.Data;

@Data
public class TagCreateRequest {
    private String label;
    private String color;
    private TagVisibility visibility; // 추가
}
