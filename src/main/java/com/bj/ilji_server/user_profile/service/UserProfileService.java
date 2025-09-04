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


    @Transactional
    public UserProfileResponse getUserProfile(Long userId) {
        UserProfile userProfile = findOrCreateProfile(userId);
        return UserProfileResponse.from(userProfile);
    }

    @Transactional
    public void updateUserProfile(Long userId, UserProfileUpdateRequest request, MultipartFile profileImage, MultipartFile bannerImage) throws IOException {
        System.out.println("[Service] 2. updateUserProfile 메서드 진입");
        UserProfile userProfile = findOrCreateProfile(userId);


        // 1. 프로필 이미지 처리
        String profileImageUrl = userProfile.getProfileImage(); // 기존 이미지 URL 유지
        if (profileImage != null && !profileImage.isEmpty()) {
            // TODO: 기존 이미지가 있다면 Firebase에서 삭제하는 로직 추가 가능
            // if (profileImageUrl != null) firebaseService.deleteFile(profileImageUrl);

            System.out.println("[Service]    - 프로필 이미지 Firebase 업로드 시도...");
            profileImageUrl = firebaseService.uploadFile(profileImage, "profile_images");
            System.out.println("[Service]    - 프로필 이미지 업로드 완료. URL: " + profileImageUrl);
        }

        // 2. 배너 이미지 처리
        String bannerImageUrl = userProfile.getBannerImage(); // 기존 이미지 URL 유지
        if (bannerImage != null && !bannerImage.isEmpty()) {
            // TODO: 기존 이미지가 있다면 Firebase에서 삭제하는 로직 추가 가능
            // if (bannerImageUrl != null) firebaseService.deleteFile(bannerImageUrl);
            System.out.println("[Service]    - 배너 이미지 Firebase 업로드 시도...");
            bannerImageUrl = firebaseService.uploadFile(bannerImage, "banner_images");
            System.out.println("[Service]    - 배너 이미지 업로드 완료. URL: " + bannerImageUrl);
        }

        // 3. DTO에 이미지 URL 설정
        request.setProfileImage(profileImageUrl);
        request.setBannerImage(bannerImageUrl);
        System.out.println("[Service] 3. DB 저장을 위해 DTO에 이미지 URL 설정 완료");
        System.out.println("[Service]    - DTO 최종 상태: " + request);


        // 4. 프로필 정보 및 이미지 URL 업데이트
        System.out.println("[Service] 4. DB 업데이트 시도...");
        userProfile.update(request);
        System.out.println("[Service] 5. DB 업데이트 완료");


    }

    private UserProfile findOrCreateProfile(Long userId) {
        return userProfileRepository.findById(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다. id=" + userId));
                    UserProfile newProfile = UserProfile.builder().user(user).build();
                    return userProfileRepository.save(newProfile);
                });
    }
}