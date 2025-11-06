package org.example.server.chatroom.service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.server.chat.service.ChatService;
import org.example.server.chatroom.dto.*;
import org.example.server.chatroom.repository.ChatRoomRepository;
import org.example.server.user.repository.UserRepository;
import org.example.server.chatroom.AuthenticatedUserProvider;
import org.example.server.chatroom.entity.ChatRoom;
import org.example.server.chat.entity.Message;
import org.example.server.user.entity.User;
import org.example.server.chat.exception.UserNotFoundException;
import org.example.server.chatroom.exception.ChatRoomAccessDeniedException;
import org.example.server.chatroom.exception.ChatRoomNotFoundException;
import org.example.server.chatroom.repository.MessageRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class ChatRoomServiceImpl implements ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final MessageRepository messageRepository;
    private final ChatService chatService;

    @Override
    public Mono<ChatRoomResponse.CreateChatRoomResponse> createChatRoom(Long userId, CreateChatRoomRequest request) {

        return Mono.fromCallable(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException(userId));
            String defaultTitle = request.question();

            ChatRoom chatRoom = ChatRoom.builder()
                    .user(user)
                    .title(defaultTitle)
                    .build();

            ChatRoom saved = chatRoomRepository.save(chatRoom);

            return ChatRoomResponse.CreateChatRoomResponse.builder()
                    .chatRoomId(saved.getChatRoomId())
                    .title(saved.getTitle())
                    .build();
        }).subscribeOn(Schedulers.boundedElastic());
    }


    @Override
    public Mono<ChatRoomMessagesResponse> getChatMessages(Long chatRoomId) {
        return authenticatedUserProvider.getCurrentUserId()
                .flatMap(userId -> Mono.fromCallable(() -> loadChatRoomForUser(chatRoomId, userId))
                        .subscribeOn(Schedulers.boundedElastic()))
                .flatMap(chatRoom -> Mono.fromCallable(() -> buildChatRoomMessagesResponse(chatRoom))
                        .subscribeOn(Schedulers.boundedElastic()));
    }

    private ChatRoom loadChatRoomForUser(Long chatRoomId, Long userId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ChatRoomNotFoundException(chatRoomId));

        if (!chatRoom.getUser().getUserId().equals(userId)) {
            throw new ChatRoomAccessDeniedException(chatRoomId);
        }

        return chatRoom;
    }

    private ChatRoomMessagesResponse buildChatRoomMessagesResponse(ChatRoom chatRoom) {
        List<Message> messages = messageRepository
                .findAllByChatRoom_ChatRoomIdOrderByCreatedAtAsc(chatRoom.getChatRoomId());

        List<ChatMessageResponse> messageResponses = messages.stream()
                .map(message -> new ChatMessageResponse(
                        message.getMessageId(),
                        message.isUser(),
                        message.isUser() ? chatRoom.getUser().getUserId() : null,
                        message.getContent()
                ))
                .toList();

        return new ChatRoomMessagesResponse(
                chatRoom.getChatRoomId(),
                chatRoom.getTitle(),
                messageResponses
        );
    }


    @Override
    @Transactional
    public List<ChatRoomResponse.GetChatRoomResponse> findChatRooms(Long userId) {

        return chatRoomRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public void deleteChatRoom(Long userId, Long chatRoomId) {

        if (!chatRoomRepository.existsByUser_UserIdAndChatRoomId(userId, chatRoomId)) {
            throw new IllegalArgumentException("채팅방이 존재하지 않습니다.");
        } else {
            chatRoomRepository.deleteById(chatRoomId);
        }

    }

    @Override
    @Transactional
    public ChatRoomDto updateFavorite(Long userId, Long chatRoomId, boolean isFavorited) {

        ChatRoom chatRoom = chatRoomRepository.findByUserIdAndChatRoomId(userId, chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("해당 채팅방이 존재하지 않습니다."));

        if (!chatRoom.getIsFavorited() && isFavorited)
        {
            chatRoom.addFavorite();
        }
        else if (chatRoom.getIsFavorited() && !isFavorited)
        {
            chatRoom.removeFavorite();
        }
        else
        {
            throw new IllegalStateException("이미 즐겨찾기 상태가 요청하신 상태와 동일합니다.");
        }

        return ChatRoomDto.builder()
                .chatRoomId(chatRoom.getChatRoomId())
                .isFavorited(chatRoom.getIsFavorited())
                .build();
    }

    @Transactional // 변경 감지를 위해 트랜잭션 활성화
    public UpdateChatRoomTitleResponseDto updateChatRoomTitle(Long userId, Long chatRoomId, UpdateChatRoomTitleRequestDto requestDto)
    {
    ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
            .orElseThrow(() -> new ChatRoomNotFoundException(chatRoomId));

    // 2. 채팅방 소유권 확인 (ChatRoom 엔티티가 User 정보를 가지고 있어야 함)
    //    (ChatRoom 엔티티에 'private User user;' 필드가 있다고 가정)
        if (!Objects.equals(chatRoom.getUser().getUserId(), userId))
        {
            // 로그인한 사용자와 채팅방 소유자가 다르면 ChatRoomAccessDeniedException 발생
            throw new ChatRoomAccessDeniedException(chatRoomId);
        }

    // 3. 엔티티의 제목 변경 (엔티티에 추가한 updateTitle 메서드 사용)
        chatRoom.updateTitle(requestDto.getTitle());

    // 4. @Transactional 종료 시, 변경 감지(dirty checking)에 의해 UPDATE 쿼리 자동 실행

    // 5. 응답 DTO 반환
        return new UpdateChatRoomTitleResponseDto(
            chatRoom.getChatRoomId(),
                chatRoom.getTitle()
        );
}

}
