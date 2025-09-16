package com.bj.ilji_server.ilog_comments.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class IlogCommentCreateRequest {

    private String content;

    private Long parentCommentId; // 대댓글일 경우 부모 댓글의 ID
}