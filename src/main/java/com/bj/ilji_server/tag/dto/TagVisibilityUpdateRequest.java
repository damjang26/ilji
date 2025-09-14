package com.bj.ilji_server.tag.dto;

import com.bj.ilji_server.tag.entity.TagVisibility;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TagVisibilityUpdateRequest {
    private TagVisibility visibility;
}