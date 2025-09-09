package com.bj.ilji_server.tag.service;

import com.bj.ilji_server.friend.dto.FriendshipStatus;
import com.bj.ilji_server.friend.service.FriendService;
import com.bj.ilji_server.tag.dto.TagCreateRequest;
import com.bj.ilji_server.tag.dto.TagResponse;
import com.bj.ilji_server.tag.dto.TagUpdateRequest;
import com.bj.ilji_server.tag.entity.Tag;
import com.bj.ilji_server.tag.entity.TagVisibility;
import com.bj.ilji_server.tag.repository.TagRepository;
import com.bj.ilji_server.user.entity.User;
import com.bj.ilji_server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagService {

    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final FriendService friendService;

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
                .visibility(request.getVisibility()) // 추가
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

        // TODO: 이 태그를 사용하고 있는 스케줄들의 tag_id를 null로 변경하는 로직 추가 필요

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
