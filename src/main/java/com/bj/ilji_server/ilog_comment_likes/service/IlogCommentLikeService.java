package com.bj.ilji_server.ilog_comment_likes.service;

import com.bj.ilji_server.ilog_comment_likes.entity.IlogCommentLike;
import com.bj.ilji_server.ilog_comment_likes.repository.IlogCommentLikeRepository;
import com.bj.ilji_server.ilog_comments.entity.IlogComment;
import com.bj.ilji_server.ilog_comments.repository.IlogCommentRepository;
import com.bj.ilji_server.likes.dto.LikerInfoDTO;
import com.bj.ilji_server.user_profile.entity.UserProfile;
import com.bj.ilji_server.user_profile.repository.UserProfileRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IlogCommentLikeService {

    private final IlogCommentLikeRepository ilogCommentLikeRepository;
    private final UserProfileRepository userProfileRepository;
    private final IlogCommentRepository ilogCommentRepository;

    /**
     * 댓글에 대한 '좋아요'를 추가하거나 취소합니다. (토글 방식)
     * @param commentId '좋아요'를 누를 댓글의 ID
     * @param userId '좋아요'를 누르는 사용자의 ID
     * @return '좋아요'가 추가되면 true, 취소되면 false를 반환합니다.
     */
    @Transactional
    public boolean toggleCommentLike(Long commentId, Long userId) {
        // 1. 사용자(UserProfile)와 댓글(IlogComment) 엔티티를 조회합니다.
        UserProfile userProfile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + userId));
        IlogComment ilogComment = ilogCommentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("댓글을 찾을 수 없습니다: " + commentId));

        // 2. 해당 사용자가 해당 댓글에 '좋아요'를 눌렀는지 확인합니다.
        Optional<IlogCommentLike> existingLike = ilogCommentLikeRepository.findByUserProfileAndIlogComment(userProfile, ilogComment);

        if (existingLike.isPresent()) {
            // 3-1. '좋아요'가 이미 존재하면 삭제하고 false를 반환합니다. (좋아요 취소)
            ilogCommentLikeRepository.delete(existingLike.get());
            ilogComment.decreaseLikeCount(); // 댓글 엔티티의 좋아요 수 감소
            return false;
        } else {
            // 3-2. '좋아요'가 없으면 새로 생성하고 true를 반환합니다. (좋아요 추가)
            ilogCommentLikeRepository.save(new IlogCommentLike(ilogComment, userProfile));
            ilogComment.increaseLikeCount(); // 댓글 엔티티의 좋아요 수 증가
            return true;
        }
    }

    /**
     * 특정 댓글에 '좋아요'를 누른 사용자 목록을 조회합니다.
     * @param commentId 사용자 목록을 조회할 댓글의 ID
     * @return LikerInfoDTO 리스트
     */
    @Transactional(readOnly = true) // 읽기 전용 트랜잭션으로 성능 최적화
    public List<LikerInfoDTO> getCommentLikers(Long commentId) {
        List<IlogCommentLike> likes = ilogCommentLikeRepository.findAllByIlogComment_Id(commentId);
        return likes.stream()
                .map(IlogCommentLike::getUserProfile) // IlogCommentLike에서 UserProfile을 추출합니다.
                .filter(userProfile -> !userProfile.isAccountPrivate()) // 비공개 계정은 제외합니다.
                .map(LikerInfoDTO::new) // UserProfile을 LikerInfoDTO로 변환합니다.
                .collect(Collectors.toList());
    }
}