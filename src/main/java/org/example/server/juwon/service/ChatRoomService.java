package org.example.server.juwon.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.server.juwon.entity.ChatRoom;
import org.example.server.juwon.dto.UpdateChatRoomTitleRequestDto;
import org.example.server.juwon.dto.UpdateChatRoomTitleResponseDto;
import org.example.server.juwon.repository.ChatRoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional // 수정을 위한 트랜잭션 활성화
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;

    public UpdateChatRoomTitleResponseDto updateChatRoomTitle(Long chatRoomId, UpdateChatRoomTitleRequestDto requestDto) {
        // 1. 채팅방 조회 (없으면 EntityNotFoundException 발생)
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new EntityNotFoundException("채팅방을 찾을 수 없습니다. ID: " + chatRoomId));

        // 2. 엔티티의 제목 변경 (엔티티에 추가한 updateTitle 메서드 사용)
        chatRoom.updateTitle(requestDto.getTitle());

        // 3. @Transactional 종료 시, 변경 감지(dirty checking)에 의해
        //    별도의 save() 호출 없이 UPDATE 쿼리가 자동 실행됨.

        // 4. 응답 DTO 생성 및 반환
        return new UpdateChatRoomTitleResponseDto(
                "채팅방 이름이 성공적으로 수정되었습니다.",
                chatRoom.getChatRoomId(),
                chatRoom.getTitle()
        );
    }
}
