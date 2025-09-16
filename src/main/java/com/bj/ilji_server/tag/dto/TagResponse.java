package com.bj.ilji_server.tag.dto;

import com.bj.ilji_server.tag.entity.Tag;
import com.bj.ilji_server.tag.entity.TagVisibility;
import lombok.Getter;

@Getter
public class TagResponse {
    private Long id;
    private String label;
    private String color;
    private TagVisibility visibility;
    private Long position;

    public TagResponse(Tag tag) {
        this.id = tag.getId();
        this.label = tag.getLabel();
        this.color = tag.getColor();
        this.visibility = tag.getVisibility();
        this.position = tag.getPosition();
    }
}
