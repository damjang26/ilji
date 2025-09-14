package com.bj.ilji_server.tag.dto;

import com.bj.ilji_server.tag.entity.TagVisibility;
import lombok.Data;

@Data
public class TagUpdateRequest {
    private String label;
    private String color;
    private TagVisibility visibility;
}