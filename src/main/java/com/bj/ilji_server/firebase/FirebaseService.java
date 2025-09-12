package com.bj.ilji_server.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
public class FirebaseService {

    @Value("${firebase.bucket-name}")
    private String bucketName;

    @Value("${firebase.config-path}")
    private String configPath;

    private Storage storage;

    // 애플리케이션 시작 시 Storage 객체 초기화
    @PostConstruct
    public void init() throws IOException {
        FileInputStream serviceAccount = new FileInputStream(configPath);
        this.storage = StorageOptions.newBuilder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build()
                .getService();
    }

    /**
     * Firebase Storage에 파일 업로드 후 공개 URL 반환
     * @param file 업로드할 파일
     * @param path 저장할 경로 (예: "profile-images", "banner-images")
     * @return 업로드된 파일의 공개 URL
     */
    public String uploadFile(MultipartFile file, String path) throws IOException {
        String fileName = path + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

        System.out.println("[Firebase] Storage 객체 존재 여부: " + (this.storage != null));
        System.out.println("[Firebase] 파일 업로드 실행. 버킷: " + bucketName + ", 파일명: " + fileName);

        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, fileName)
                .setContentType(file.getContentType())
                .build();

        storage.create(blobInfo, file.getBytes());

        System.out.println("[Firebase] 파일 업로드 API 호출 성공.");

        return "https://firebasestorage.googleapis.com/v0/b/" +
                bucketName +
                "/o/" +
                fileName.replaceAll("/", "%2F") +
                "?alt=media";
    }

    /**
     * Firebase Storage에서 파일 삭제
     * @param fileUrl 삭제할 파일의 Firebase Storage URL
     */
    public void deleteFile(String fileUrl) throws IOException {
        // 1. URL 디코딩: URL에 포함된 %2F 같은 인코딩된 문자를 원래 문자로 변환합니다.
        // 예: .../o/profile-images%2Fimage.png -> .../o/profile-images/image.png
        String decodedUrl = URLDecoder.decode(fileUrl, StandardCharsets.UTF_8);

        // 2. 디코딩된 URL에서 파일 경로(object name)를 추출합니다.
        String prefix = "https://firebasestorage.googleapis.com/v0/b/" + bucketName + "/o/";
        String suffixMarker = "?alt=media";

        if (decodedUrl.startsWith(prefix) && decodedUrl.contains(suffixMarker)) {
            // URL에서 "?alt=media" 앞부분까지 잘라내어 파일 경로를 추출합니다.
            // 이렇게 하면 뒤에 "&token=..."이 붙어 있어도 정상적으로 처리됩니다.
            int suffixIndex = decodedUrl.indexOf(suffixMarker);
            String filePath = decodedUrl.substring(prefix.length(), suffixIndex);
            boolean deleted = storage.delete(BlobId.of(bucketName, filePath));
            // 💥 중요: 파일이 존재하지 않아 삭제가 실패한 경우(deleted=false), 예외를 던지는 대신 경고 로그만 남깁니다.
            // 이렇게 하면 파일이 없더라도 전체 작업이 중단되지 않습니다.
            if (!deleted) {
                System.out.println("[WARN] Firebase Storage에서 파일을 찾을 수 없어 삭제를 건너뜁니다: " + filePath);
            }
        } else {
            throw new IllegalArgumentException("잘못된 Firebase Storage URL 형식입니다: " + fileUrl);
        }
    }
}
