package com.bj.ilji_server.likes.service;

import com.bj.ilji_server.likes.dto.LikerInfoDTO;
import com.bj.ilji_server.likes.entity.Likes;
import com.bj.ilji_server.likes.repository.LikesRepository;
import com.bj.ilji_server.ilog.entity.ILog;
import com.bj.ilji_server.ilog.repository.ILogRepository;
import com.bj.ilji_server.user_profile.entity.UserProfile;
import com.bj.ilji_server.user_profile.repository.UserProfileRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LikesService {

    private final LikesRepository likesRepository;
    private final UserProfileRepository userProfileRepository;
    private final ILogRepository iLogRepository;

    @Transactional
    public boolean toggleLike(Long ilogId, Long userId) {
        // 1. 사용자(UserProfile)와 일기(ILog) 엔티티를 조회합니다.
        UserProfile userProfile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + userId));
        ILog iLog = iLogRepository.findById(ilogId)
                .orElseThrow(() -> new EntityNotFoundException("일기를 찾을 수 없습니다: " + ilogId));

        // 2. 해당 사용자가 해당 일기에 '좋아요'를 눌렀는지 확인합니다.
        Optional<Likes> existingLike = likesRepository.findByUserProfileAndILog(userProfile, iLog);

        if (existingLike.isPresent()) {
            // 3-1. '좋아요'가 이미 존재하면 삭제하고 false를 반환합니다. (좋아요 취소)
            likesRepository.delete(existingLike.get());
            iLog.decrementLikeCount();
            return false;
        } else {
            // 3-2. '좋아요'가 없으면 새로 생성하고 true를 반환합니다. (좋아요 추가)
            likesRepository.save(new Likes(userProfile, iLog));
            iLog.incrementLikeCount();
            return true;
        }
    }

    @Transactional(readOnly = true) // 읽기 전용 트랜잭션으로 성능 최적화
    public List<LikerInfoDTO> getLikers(Long ilogId) {
        List<Likes> likes = likesRepository.findAllByiLog_Id(ilogId);
        return likes.stream()
                .map(Likes::getUserProfile) // Likes 엔티티에서 UserProfile 엔티티를 추출합니다.
                // ✅ [수정] 프로필이 비공개가 아닌 사용자만 필터링합니다.
                .filter(userProfile -> !userProfile.isAccountPrivate())
                .map(LikerInfoDTO::new) // 필터링된 UserProfile을 DTO로 변환합니다.
                .collect(Collectors.toList());
    }
}