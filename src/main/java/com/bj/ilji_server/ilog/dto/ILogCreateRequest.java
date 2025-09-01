package com.bj.ilji_server.ilog.dto;

import com.bj.ilji_server.ilog.entity.ILog;
import com.bj.ilji_server.user.entity.User;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ILogCreateRequest {

    private Long userId;
    private LocalDate iLogDate;
    private String content;
    private String imgUrl;
    private ILog.Visibility visibility = ILog.Visibility.PUBLIC;
    private String friendTags;
    private String tags;

    // Entity로 변환하는 편의 메서드
    public ILog toEntity(User user) {
        return ILog.builder()
                .user(user)
                .iLogDate(this.iLogDate)
                .content(this.content)
                .imgUrl(this.imgUrl)
                .visibility(this.visibility)
                .friendTags(this.friendTags)
                .tags(this.tags)
                .build();
    }
}
