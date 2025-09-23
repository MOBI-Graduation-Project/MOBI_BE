package com.mobi.mobi.chat.service;

import com.mobi.mobi.apiPayload.handler.GeneralException;
import com.mobi.mobi.apiPayload.status.ErrorStatus;
import com.mobi.mobi.chat.dto.ChatMessageDTO;
import com.mobi.mobi.chat.entity.ChatMessage;
import com.mobi.mobi.chat.entity.ChatRoom;
import com.mobi.mobi.chat.entity.ChatRoomMember;
import com.mobi.mobi.chat.entity.enums.ChatType;
import com.mobi.mobi.chat.repository.ChatMessageRepository;
import com.mobi.mobi.chat.repository.ChatRoomMemberRepository;
import com.mobi.mobi.chat.repository.ChatRoomRepository;
import com.mobi.mobi.member.entity.Member;
import com.mobi.mobi.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final MemberRepository memberRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;

    @Transactional
    public Long getOrCreateRoom(Long myId, Long otherId) {
        if (myId.equals(otherId)) {
            throw new GeneralException(ErrorStatus.INVALID_FRIEND_REQUEST); // 자기 자신과는 채팅 불가
        }
        Member me = findMemberById(myId);
        Member other = findMemberById(otherId);

        // 두 사용자 ID로 기존 1:1 채팅방이 있는지 조회
        return chatRoomRepository.findFriendChatRoomByMemberIds(myId, otherId)
                .map(ChatRoom::getId) // 있으면 기존 방 ID 반환
                .orElseGet(() -> { // 없으면 새로 생성
                    ChatRoom newRoom = ChatRoom.builder()
                            .roomName(me.getNickname() + "님과 " + other.getNickname() + "님의 채팅방")
                            .chatType(ChatType.FRIEND)
                            .build();
                    chatRoomRepository.save(newRoom);
                    // 채팅방에 두 명의 참가자를 추가
                    chatRoomMemberRepository.save(ChatRoomMember.builder().chatRoom(newRoom).member(me).build());
                    chatRoomMemberRepository.save(ChatRoomMember.builder().chatRoom(newRoom).member(other).build());
                    return newRoom.getId();
                });
    }

    @Transactional
    public Long createGroupRoom(String roomName, ChatType chatType, Long creatorId) {
        Member creator = findMemberById(creatorId);
        ChatRoom newRoom = ChatRoom.builder()
                .roomName(roomName)
                .chatType(chatType) // GROUP 또는 LOCATION
                .build();
        chatRoomRepository.save(newRoom);
        // 생성자를 첫 참가자로 추가
        chatRoomMemberRepository.save(ChatRoomMember.builder().chatRoom(newRoom).member(creator).build());
        return newRoom.getId();
    }

    @Transactional
    public void joinGroupRoom(Long roomId, Long memberId) {
        Member member = findMemberById(memberId);
        ChatRoom room = findRoomById(roomId);

        // 이미 참여하고 있는지 확인
        chatRoomMemberRepository.findByChatRoomAndMember(room, member).ifPresent(m -> {
            throw new GeneralException(ErrorStatus.ALREADY_IN_CHAT_ROOM); // ErrorStatus에 추가 필요
        });

        chatRoomMemberRepository.save(ChatRoomMember.builder().chatRoom(room).member(member).build());
    }

    @Transactional
    public ChatMessage saveMessage(ChatMessageDTO messageDTO) {
        Member sender = findMemberById(messageDTO.getSenderId());
        ChatRoom room = findRoomById(messageDTO.getRoomId());

        ChatMessage message = ChatMessage.builder()
                .chatRoom(room)
                .sender(sender)
                .content(messageDTO.getContent())
                .build();
        return chatMessageRepository.save(message);
    }

    @Transactional(readOnly = true)
    public List<ChatMessageDTO> getChatHistory(Long roomId) {
        ChatRoom room = findRoomById(roomId);
        return chatMessageRepository.findByChatRoomOrderByCreatedAtAsc(room)
                .stream()
                .map(ChatMessageDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // Helper 메서드
    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
    }
    private ChatRoom findRoomById(Long roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.CHAT_ROOM_NOT_FOUND));
    }
}


