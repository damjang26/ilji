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
import java.util.UUID;

@Service
public class FirebaseService {

    @Value("${firebase.bucket-name}")
    private String bucketName;

    @Value("${firebase.config-path}")
    private String configPath;

    private Storage storage;

    // @PostConstruct는 애플리케이션 시작 시 한 번만 실행되어 Storage 객체를 초기화합니다.
    // 매 요청마다 비싼 객체를 생성하는 비효율을 방지합니다.
    @PostConstruct
    public void init() throws IOException {
        FileInputStream serviceAccount = new FileInputStream(configPath);
        this.storage = StorageOptions.newBuilder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build()
                .getService();
    }

    /**
     * Firebase Storage에 파일을 업로드하고 공개 URL을 반환합니다.
     * @param file 업로드할 파일
     * @param path 저장할 경로 (예: "ilog-images", "profile-images")
     * @return 업로드된 파일의 공개 URL
     */
    public String uploadFile(MultipartFile file, String path) throws IOException {
        // 파일 이름 중복을 피하기 위해 UUID를 사용하고, 지정된 경로에 저장합니다.
        String fileName = path + "/" + UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, fileName)
                .setContentType(file.getContentType())
                .build();

        storage.create(blobInfo, file.getBytes());

        // 업로드된 파일의 공개 URL을 생성하여 반환합니다.
        return "https://firebasestorage.googleapis.com/v0/b/" + bucketName + "/o/" + fileName.replaceAll("/", "%2F") + "?alt=media";
    }

    public boolean deleteFile(String fileUrl) {
        try {
            // 예: https://firebasestorage.googleapis.com/v0/b/[bucket]/o/ilog-images%2Fabcd_uuid.png?alt=media
            // → filePath = "ilog-images/abcd_uuid.png"

            String prefix = "https://firebasestorage.googleapis.com/v0/b/" + bucketName + "/o/";
            String suffix = "?alt=media";

            if (fileUrl.startsWith(prefix) && fileUrl.endsWith(suffix)) {
                String filePathEncoded = fileUrl.substring(prefix.length(), fileUrl.length() - suffix.length());
                // URL 인코딩된 경로를 다시 디코딩
                String filePath = java.net.URLDecoder.decode(filePathEncoded, java.nio.charset.StandardCharsets.UTF_8);

                // Firebase Storage에서 파일 삭제
                return storage.delete(BlobId.of(bucketName, filePath));
            } else {
                throw new IllegalArgumentException("잘못된 Firebase Storage URL 형식입니다: " + fileUrl);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}