package com.bj.ilji_server.tag.repository;

import com.bj.ilji_server.tag.entity.Tag;
import com.bj.ilji_server.tag.entity.TagVisibility;
import com.bj.ilji_server.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Long> {
    List<Tag> findByUserId(Long userId);

    List<Tag> findByUserOrderByPositionDesc(User user);

    List<Tag> findByUserAndVisibilityInOrderByPositionDesc(User user, List<TagVisibility> visibilities);

}
