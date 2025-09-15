package com.bj.ilji_server.tag.service;

import com.bj.ilji_server.friend.dto.FriendshipStatus;
import com.bj.ilji_server.friend.service.FriendService;
import com.bj.ilji_server.schedule.repository.ScheduleRepository;
import com.bj.ilji_server.tag.dto.TagCreateRequest;
import com.bj.ilji_server.tag.dto.TagResponse;
import com.bj.ilji_server.tag.dto.TagUpdateRequest;
import com.bj.ilji_server.tag.entity.Tag;
import com.bj.ilji_server.tag.entity.TagVisibility;
import com.bj.ilji_server.tag.repository.TagRepository;
import com.bj.ilji_server.user.repository.UserRepository;
import com.bj.ilji_server.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagService {

    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;
    private final FriendService friendService;

    public List<TagResponse> getUserTags(User user) {
        // [수정] NPE 방지를 위해 null 체크를 추가하고, 더 명확한 오류 메시지를 제공합니다.
        // 이 오류는 보통 인증 토큰 없이 API를 호출했을 때 발생합니다.
        if (user == null) {
            throw new IllegalArgumentException("User 객체가 null입니다. 요청에 인증 정보가 누락되었을 수 있습니다.");
        }
        // [수정] 사용자의 태그를 가져오기 위해 findById가 아닌 findByUserId를 사용해야 합니다.
        return tagRepository.findByUserId(user.getId()).stream()
                .map(TagResponse::new)
                .collect(Collectors.toList());
    }

    // [dev용 추가] ID로 사용자의 태그 목록을 조회하는 서비스 메서드
    public List<TagResponse> getUserTagsById(Long userId) {
        // [수정] 사용자의 태그를 가져오기 위해 findById가 아닌 findByUserId를 사용해야 합니다.
        return tagRepository.findByUserId(userId).stream()
                .map(TagResponse::new)
                .collect(Collectors.toList());
    }

    public List<TagResponse> getVisibleTags(Long ownerId, User viewer) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Owner not found"));

        // If viewer is the owner, show all tags
        if (owner.getId().equals(viewer.getId())) {
            return tagRepository.findByUser(owner).stream()
                    .map(TagResponse::new)
                    .collect(Collectors.toList());
        }

        // Check friendship status from the viewer's perspective
        FriendshipStatus status = friendService.checkFriendshipStatus(viewer, owner);
        List<TagVisibility> visibleScopes;

        switch (status) {
            case MUTUAL:
                visibleScopes = List.of(TagVisibility.PUBLIC, TagVisibility.MUTUAL_FRIENDS);
                break;
            case FOLLOWING:
            case NONE:
            case FOLLOWED_BY:
            default:
                visibleScopes = List.of(TagVisibility.PUBLIC);
                break;
        }

        return tagRepository.findByUserAndVisibilityIn(owner, visibleScopes).stream()
                .map(TagResponse::new)
                .collect(Collectors.toList());
    }
    @Transactional
    public TagResponse createTag(User user, TagCreateRequest request) {
        Tag newTag = Tag.builder()
                .user(user)
                .label(request.getLabel())
                .color(request.getColor())
                .build();
        Tag savedTag = tagRepository.save(newTag);
        return new TagResponse(savedTag);
    }

    // [dev용 추가] ID로 태그를 생성하는 서비스 메서드
    @Transactional
    public TagResponse createTagForDev(TagCreateRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + request.getUserId()));

        Tag newTag = Tag.builder()
                .user(user)
                .label(request.getLabel())
                .color(request.getColor())
                .build();
        Tag savedTag = tagRepository.save(newTag);
        return new TagResponse(savedTag);
    }

    @Transactional
    public void deleteTag(User user, Long tagId) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new IllegalArgumentException("태그를 찾을 수 없습니다."));

        if (!tag.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("이 태그를 삭제할 권한이 없습니다.");
        }

        // [수정] 태그를 삭제하기 전에, 해당 태그를 사용하는 모든 스케줄과의 연결을 끊습니다.
        // 이는 데이터베이스의 ON DELETE SET NULL 규칙이 적용되기 전에
        // JPA가 잠재적으로 발생시킬 수 있는 제약 조건 위반 예외를 방지합니다.
        scheduleRepository.disassociateTagFromSchedules(tag);
        tagRepository.delete(tag);
    }

    @Transactional
    public TagResponse updateTag(User user, Long tagId, TagUpdateRequest request) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new IllegalArgumentException("태그를 찾을 수 없습니다."));

        if (!tag.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("이 태그를 수정할 권한이 없습니다.");
        }

        tag.update(request);

        return new TagResponse(tag);
    }
}
