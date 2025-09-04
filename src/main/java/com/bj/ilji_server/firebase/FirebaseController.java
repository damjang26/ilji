package com.bj.ilji_server.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.util.UUID;

@RequestMapping("/api")
@RestController
public class FirebaseController {

    @PostMapping("/firebase")
    public ResponseEntity<String> handleFileUpload(@RequestParam("file") MultipartFile file) {
        try {
            System.out.println("--- 업로드 진임..---");
            // 저장 경로 지정
//            String uploadDir = "C:\\mzz\\springboot_intellij\\upload\\";
            FileInputStream serviceAccount = new FileInputStream("src/main/resources/firebase-config.json");

            Storage storage = StorageOptions.newBuilder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build()
                    .getService();


            String fileName = "memory/" + UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            BlobInfo blobInfo = BlobInfo.newBuilder("mz-test-46f03.appspot.com", fileName)
                    .setContentType(file.getContentType())
                    .build();

            storage.create(blobInfo, file.getBytes());

            String fileUrl = "https://firebasestorage.googleapis.com/v0/b/" +
                    "mz-test-46f03.appspot.com/o/" +
                    fileName.replaceAll("/", "%2F") +
                    "?alt=media";
            System.out.println(fileUrl);
            return ResponseEntity.ok(fileUrl);


//            String filePath = uploadDir + file.getOriginalFilename();
//            file.transferTo(new File(filePath));
//            return ResponseEntity.ok("파일 업로드 성공: " + file.getOriginalFilename());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("파일 업로드 실패");
        }
    }
}
