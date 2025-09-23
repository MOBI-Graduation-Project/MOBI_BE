package com.mobi.mobi.chat.dto;


import com.mobi.mobi.chat.entity.enums.ChatType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class CreateGroupRoomRequest {
    @Schema(description = "생성할 채팅방의 이름", example = "삼성전자 주주 모임")
    private String roomName;

    @Schema(description = "채팅방 타입", example = "GROUP")
    private ChatType chatType;
}