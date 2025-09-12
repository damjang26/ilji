package com.bj.ilji_server.ilog.dto;

import com.bj.ilji_server.ilog.entity.ILog;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ILogCreateRequest {

    private Long writerId;
    private LocalDate logDate;
    private String content;
    // ✅ [제거] 이미지 URL은 서비스 레이어에서 MultipartFile을 통해 직접 생성되므로 DTO에서 제거합니다.
    // private String imgUrl;
    private ILog.Visibility visibility = ILog.Visibility.PUBLIC;
    private String friendTags;
    private String tags;
}
