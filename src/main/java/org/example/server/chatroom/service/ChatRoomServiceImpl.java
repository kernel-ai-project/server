package org.example.server.chatroom.service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.server.chat.service.ChatService;
import org.example.server.chat.dto.AskRequest;
import org.example.server.chat.dto.AskResponse;
import org.example.server.chatroom.dto.*;
import org.example.server.chatroom.repository.ChatRoomRepository;
import org.example.server.user.respository.UserRepository;
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
    public Mono<ChatRoomResponse.CreateChatRoomResponse> createChatRoom(CreateChatRoomRequest request) {
        String question = request.question().trim();

        Mono<User> ownerMono = authenticatedUserProvider.getCurrentUserId()
                .flatMap(userId -> Mono.fromCallable(() -> loadOwner(userId))
                        .subscribeOn(Schedulers.boundedElastic()));

        Mono<AskResponse> answerMono = chatService.ask(new AskRequest(question));

        return ownerMono
                .zipWith(answerMono)
                .flatMap(tuple -> Mono.fromCallable(() ->
                                saveChatRoomAndBuildResponse(tuple.getT1(), question, tuple.getT2().answer()))
                        .subscribeOn(Schedulers.boundedElastic()));
    }

    private User loadOwner(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private ChatRoomResponse.CreateChatRoomResponse saveChatRoomAndBuildResponse(User owner, String question, String answer) {
        LocalDateTime now = LocalDateTime.now();

        ChatRoom chatRoom = ChatRoom.builder()
                .user(owner)
                .title(question)
//                .createdAt(now)
//                .updateAt(now)
                .isDeleted(Boolean.FALSE)
                .build();

        ChatRoom saved = chatRoomRepository.save(chatRoom);
        persistInitialMessages(saved, question, answer, now);

        return ChatRoomResponse.CreateChatRoomResponse.builder()
                .chatRoomId(saved.getChatRoomId())
                .title(saved.getTitle())
                .answer(answer)
                .build();
    }

    private void persistInitialMessages(ChatRoom chatRoom, String question, String answer, LocalDateTime createdAt) {
        Message questionMessage = Message.builder()
                .chatRoom(chatRoom)
                .isUser(true)
                .content(question)
//                .createdAt(createdAt)
                .build();

        Message answerMessage = Message.builder()
                .chatRoom(chatRoom)
                .isUser(false)
                .content(answer)
//                .createdAt(LocalDateTime.now())
                .build();

        messageRepository.saveAll(List.of(questionMessage, answerMessage));
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
                        message.getContent(),
                        toOffsetDateTime(message.getCreatedAt())
                ))
                .toList();

        return new ChatRoomMessagesResponse(
                chatRoom.getChatRoomId(),
                chatRoom.getTitle(),
                messageResponses
        );
    }

    private OffsetDateTime toOffsetDateTime(LocalDateTime createdAt) {
        return createdAt != null ? createdAt.atOffset(ZoneOffset.UTC) : null;
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
    public ChatRoomResponse.GetChatRoomFavorite updateFavorite(Long userId, Long chatRoomId, boolean isFavorited) {

        ChatRoom chatRoom = chatRoomRepository.findByUserIdAndChatRoomId(userId, chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("해당 채팅방이 존재하지 않습니다."));

        if (!chatRoom.getIsFavorited() && isFavorited) {
            chatRoom.addFavorite();
        } else if (chatRoom.getIsFavorited() && !isFavorited) {
            chatRoom.removeFavorite();
        } else {
            throw new IllegalStateException("이미 즐겨찾기 상태가 요청하신 상태와 동일합니다.");
        }

        return ChatRoomResponse.GetChatRoomFavorite.builder()
                .chatRoomId(chatRoom.getChatRoomId())
                .isFavorited(chatRoom.getIsFavorited())
                .build();
    }

    @Override
    @Transactional
    public ChatRoomResponse.GetChatRoomTitle updateChatRoomTitle(Long userId, Long chatRoomId, String title) {

        ChatRoom chatRoom = chatRoomRepository.findByUserIdAndChatRoomId(userId, chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("해당 채팅방이 존재하지 않습니다."));

        chatRoom.editTitle(title);

        return ChatRoomResponse.GetChatRoomTitle.builder()
                .chatRoomId(chatRoom.getChatRoomId())
                .title(chatRoom.getTitle())
                .build();
    }

    @Override
    @Transactional
    public String findGreeting(Long userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));

        return user.getNickname() + "님 안녕하세요";
    }
}
