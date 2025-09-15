package com.bj.ilji_server.chat;

import lombok.Data;
import java.util.List;

@Data
public class CreateChatRoomRequest {
    private String roomName;
    private List<Long> userIds;
}
