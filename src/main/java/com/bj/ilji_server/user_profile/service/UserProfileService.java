package com.bj.ilji_server.user_profile.service;

import com.bj.ilji_server.firebase.FirebaseService;
import com.bj.ilji_server.user.entity.User;
import com.bj.ilji_server.user.repository.UserRepository;
import com.bj.ilji_server.user_profile.dto.UserProfileResponse;
import com.bj.ilji_server.user_profile.dto.UserProfileUpdateRequest;
import com.bj.ilji_server.user_profile.entity.UserProfile;
import com.bj.ilji_server.user_profile.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;
    private final FirebaseService firebaseService;

    private boolean isFirebaseUrl(String url) {
        return url != null && url.startsWith("https://firebasestorage.googleapis.com/");
    }

    @Transactional
    public UserProfileResponse getUserProfile(Long userId) {
        UserProfile profile = findOrCreateProfile(userId);
        return UserProfileResponse.from(profile);
    }

    @Transactional
    public void updateUserProfile(Long userId,
                                  UserProfileUpdateRequest request,
                                  MultipartFile profileImage,
                                  MultipartFile bannerImage,
                                  String profileImageUrl,
                                  String bannerImageUrl
    ) throws IOException {
        System.out.println("[Service] updateUserProfile 진입");
        UserProfile userProfile = findOrCreateProfile(userId);

        // === 1. 프로필 이미지 처리 ===
        String finalProfileUrl = userProfile.getProfileImage();
        if (profileImage != null && !profileImage.isEmpty()) {
            if (isFirebaseUrl(finalProfileUrl)) {
                firebaseService.deleteFile(finalProfileUrl);
            }
            finalProfileUrl = firebaseService.uploadFile(profileImage, "profile_images");
            System.out.println("[Service] 새 프로필 이미지 업로드: " + finalProfileUrl);
        } else if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
            if (isFirebaseUrl(finalProfileUrl)) {
                firebaseService.deleteFile(finalProfileUrl);
            }
            finalProfileUrl = profileImageUrl; // 기본 이미지 URL로 교체
            System.out.println("[Service] 기본 프로필 이미지로 복원: " + finalProfileUrl);
        }

        // === 2. 배너 이미지 처리 ===
        String finalBannerUrl = userProfile.getBannerImage();
        if (bannerImage != null && !bannerImage.isEmpty()) {
            if (isFirebaseUrl(finalBannerUrl)) {
                firebaseService.deleteFile(finalBannerUrl);
            }
            finalBannerUrl = firebaseService.uploadFile(bannerImage, "banner_images");
            System.out.println("[Service] 새 배너 이미지 업로드: " + finalBannerUrl);
        } else if (bannerImageUrl != null && !bannerImageUrl.isEmpty()) {
            if (isFirebaseUrl(finalBannerUrl)) {
                firebaseService.deleteFile(finalBannerUrl);
            }
            finalBannerUrl = bannerImageUrl; // 기본 배너 URL
            System.out.println("[Service] 기본 배너 이미지로 복원: " + finalBannerUrl);
        }

        // === 3. DTO 업데이트 및 DB 저장 ===
        request.setProfileImage(finalProfileUrl);
        request.setBannerImage(finalBannerUrl);

        userProfile.update(request);
        System.out.println("[Service] DB 업데이트 완료");
    }

    private UserProfile findOrCreateProfile(Long userId) {
        return userProfileRepository.findById(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. id=" + userId));
                    UserProfile newProfile = UserProfile.builder().user(user).build();
                    return userProfileRepository.save(newProfile);
                });
    }
}
