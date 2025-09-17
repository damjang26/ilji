package com.bj.ilji_server.ilog.mobile;

import com.bj.ilji_server.ilog.dto.ILogCreateRequest;
import com.bj.ilji_server.ilog.dto.ILogResponse;
import com.bj.ilji_server.ilog.service.ILogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/mobile/i-log") // New endpoint for mobile
@RequiredArgsConstructor
public class MobileILogController {

    private final ILogService ilogService;
    private final ObjectMapper objectMapper;

    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ILogResponse> createIlog(
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestPart("request") String requestJson // Receive JSON as String
    ) throws IOException {
        System.out.println("[MobileILogController] createIlog 메서드 진입.");
        // Manually parse the JSON string into ILogCreateRequest
        ILogCreateRequest request = objectMapper.readValue(requestJson, ILogCreateRequest.class);
        System.out.println("[MobileILogController] requestJson 파싱 성공: " + request.toString());

        // Call the existing service method
        return ResponseEntity.ok(ilogService.createIlog(request, images));
    }
}
