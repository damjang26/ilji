package com.bj.ilji_server.user_profile.service;

import com.bj.ilji_server.firebase.FirebaseService;
import com.bj.ilji_server.user.constant.UserProfileConstant;
import com.bj.ilji_server.user.dto.UserSearchResponse;
import com.bj.ilji_server.user.entity.User;
import com.bj.ilji_server.user.repository.UserRepository;
import com.bj.ilji_server.user_profile.dto.UserProfileResponse;
import com.bj.ilji_server.user_profile.dto.UserProfileUpdateRequest;
import com.bj.ilji_server.user_profile.entity.UserProfile;
import com.bj.ilji_server.user_profile.repository.UserProfileRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;
    private final FirebaseService firebaseService;

    private boolean isFirebaseUrl(String url) {
        return url != null && url.startsWith("https://firebasestorage.googleapis.com/");
    }

    @Transactional(readOnly = true) // 읽기 전용 트랜잭션으로 성능 최적화
    public UserProfileResponse getUserProfile(User incompleteUser) {
        // 컨트롤러에서 받은 불완전한 User 객체의 email을 사용하여 DB에서 완전한 User 정보를 다시 조회합니다.
        User user = userRepository.findByEmail(incompleteUser.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. email=" + incompleteUser.getEmail()));

        UserProfile profile = userProfileRepository.findById(user.getId())
                .orElseGet(() -> createNewProfile(user)); // 프로필이 없으면 새로 생성

        return UserProfileResponse.from(profile);
    }

    @Transactional
    public UserProfile updateUserProfile(User incompleteUser,
                                         UserProfileUpdateRequest request,
                                         MultipartFile profileImage,
                                         MultipartFile bannerImage,
                                         boolean revertProfileImage,
                                         boolean revertBannerImage)
            throws IOException {

        // 프로필 조회와 마찬가지로, 완전한 User 정보를 먼저 조회합니다.
        User user = userRepository.findByEmail(incompleteUser.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. email=" + incompleteUser.getEmail()));

        // 1. 쓰기 트랜잭션 내에서 엔티티를 조회하거나 새로 생성합니다.
        UserProfile userProfile = userProfileRepository.findById(user.getId())
                .orElseGet(() -> createNewProfile(user));

        // 2. 프로필 이미지 업데이트 로직
        if (revertProfileImage) {
            // 기본 이미지로 되돌리기
            deleteImageFromFirebase(userProfile.getProfileImage());
            userProfile.setProfileImage(UserProfileConstant.DEFAULT_PROFILE_IMAGE_URL);
        } else if (profileImage != null && !profileImage.isEmpty()) {
            // 새 이미지로 교체
            deleteImageFromFirebase(userProfile.getProfileImage());
            String newProfileImageUrl = firebaseService.uploadFile(profileImage, "profile_images");
            userProfile.setProfileImage(newProfileImageUrl);
        }

        // 3. 배너 이미지 업데이트 로직
        if (revertBannerImage) {
            // 기본 이미지로 되돌리기
            deleteImageFromFirebase(userProfile.getBannerImage());
            userProfile.setBannerImage(UserProfileConstant.DEFAULT_BANNER_IMAGE_URL);
        } else if (bannerImage != null && !bannerImage.isEmpty()) {
            // 새 이미지로 교체
            deleteImageFromFirebase(userProfile.getBannerImage());
            String newBannerImageUrl = firebaseService.uploadFile(bannerImage, "banner_images");
            userProfile.setBannerImage(newBannerImageUrl);
        }

        // [추가] 배너 이미지는 변경하지 않고 위치만 변경하는 경우
        // request에 bannerPositionY 값이 있고, 새로운 배너 이미지 파일이 없을 때
        if (request != null && request.getBannerPositionY() != null && (bannerImage == null || bannerImage.isEmpty())) {
            // UserProfile 엔티티의 bannerPositionY 필드를 직접 업데이트합니다.
            userProfile.setBannerPositionY(request.getBannerPositionY());
        }

        // 4. 텍스트 정보 업데이트 (request가 null이 아닐 경우에만)
        if (request != null) {
            userProfile.update(request);
        }

        // 5. 변경 사항을 DB에 반영 (JPA Dirty-checking에 의해 트랜잭션 커밋 시 자동 반영)
        // 명시적으로 save를 호출할 필요는 없지만, 가독성이나 즉시 반영을 위해 saveAndFlush를 사용할 수 있습니다.
        return userProfileRepository.save(userProfile);
    }

    /**
     * Firebase Storage에 저장된 이미지를 삭제합니다.
     * URL이 유효한 Firebase URL인 경우에만 삭제를 시도합니다.
     * @param fileUrl 삭제할 파일의 URL
     */
    private void deleteImageFromFirebase(String fileUrl) throws IOException {
        if (isFirebaseUrl(fileUrl)) {
            firebaseService.deleteFile(fileUrl);
        }
    }

    // 새로운 프로필을 생성하는 로직
    private UserProfile createNewProfile(User user) {
        UserProfile newProfile = UserProfile.builder()
                .user(user)
                .profileImage(UserProfileConstant.DEFAULT_PROFILE_IMAGE_URL)
                .bannerImage(UserProfileConstant.DEFAULT_BANNER_IMAGE_URL)
                .build();
        return userProfileRepository.save(newProfile);
    }

    @Transactional(readOnly = true)
    public List<UserSearchResponse> searchUsers(String query, User currentUser) {
        List<UserProfile> userProfiles = userProfileRepository.searchByEmailOrNickname(query, currentUser.getId());
        return userProfiles.stream()
                .map(UserSearchResponse::from)
                .collect(Collectors.toList());
    }

    // User ID로 프로필을 찾거나 생성하는 헬퍼 메서드
    private UserProfile findOrCreateProfile(Long userId) {
        return userProfileRepository.findById(userId)
                .orElseGet(() -> createNewProfile(userRepository.findById(userId)
                        .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId))));
    }

    /**
     * [추가] 사용자 ID로 프로필 정보를 조회합니다.
     * @param userId 조회할 사용자의 ID
     * @return UserProfileResponse
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getProfileByUserId(Long userId) {
        UserProfile userProfile = findOrCreateProfile(userId);
        return UserProfileResponse.from(userProfile);
    }

    /**
     * [추가] 닉네임이 이미 사용 중인지 확인합니다.
     * @param nickname 확인할 닉네임
     * @return 중복이면 true, 아니면 false
     */
    @Transactional(readOnly = true)
    public boolean isNicknameDuplicate(String nickname) {
        // UserProfileRepository에 추가했던 existsByNickname 메서드를 사용합니다.
        return userProfileRepository.existsByNickname(nickname);
    }

    /**
     * [추가] 사용자의 닉네임을 설정(업데이트)합니다.
     * @param incompleteUser 컨트롤러에서 받은 불완전한 User 정보
     * @param nickname 설정할 새로운 닉네임
     */
    @Transactional
    public void setNickname(User incompleteUser, String nickname) {
        // updateUserProfile과 마찬가지로, 완전한 User 정보를 먼저 조회합니다.
        User user = userRepository.findByEmail(incompleteUser.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. email=" + incompleteUser.getEmail()));

        // 사용자의 프로필을 찾거나, 없으면 새로 생성합니다.
        UserProfile userProfile = findOrCreateProfile(user.getId());
        userProfile.updateNickname(nickname); // UserProfile 엔티티의 닉네임 업데이트 메서드 호출
    }
}