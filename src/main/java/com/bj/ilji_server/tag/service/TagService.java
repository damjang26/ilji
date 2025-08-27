package com.bj.ilji_server.tag.service;

import com.bj.ilji_server.tag.dto.TagCreateRequest;
import com.bj.ilji_server.tag.dto.TagResponse;
import com.bj.ilji_server.tag.entity.Tag;
import com.bj.ilji_server.tag.repository.TagRepository;
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

    public List<TagResponse> getUserTags(User user) {
        return tagRepository.findByUserId(user.getId()).stream()
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
}
