package com.bj.ilji_server.tag.dto;

import com.bj.ilji_server.tag.entity.Tag;
import lombok.Getter;

@Getter
public class TagResponse {
    private Long id;
    private String label;
    private String color;

    public TagResponse(Tag tag) {
        this.id = tag.getId();
        this.label = tag.getLabel();
        this.color = tag.getColor();
    }
}
