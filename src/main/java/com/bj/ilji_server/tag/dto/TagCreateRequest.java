package com.bj.ilji_server.tag.dto;

import com.bj.ilji_server.tag.entity.TagVisibility;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TagCreateRequest {
    private String label;
    private String color;
    private TagVisibility visibility; // 추가
    // [dev용 추가] DevTagController에서 사용자를 특정하기 위한 필드
    private Long userId;
}
