package org.example.server.chatRoom.service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.server.chat.ChatService;
import org.example.server.chat.dto.AskRequest;
import org.example.server.chat.dto.AskResponse;
import org.example.server.chatRoom.AuthenticatedUserProvider;
import org.example.server.chatRoom.dto.ChatMessageResponse;
import org.example.server.chatRoom.dto.ChatRoomMessagesResponse;
import org.example.server.chatRoom.dto.ChatRoomResponse;
import org.example.server.chatRoom.dto.CreateChatRoomRequest;
import org.example.server.chat.entity.ChatRoom;
import org.example.server.chat.entity.Message;
import org.example.server.chat.entity.User;
import org.example.server.chat.exception.UserNotFoundException;
import org.example.server.chatRoom.exception.ChatRoomAccessDeniedException;
import org.example.server.chatRoom.exception.ChatRoomNotFoundException;
import org.example.server.chatRoom.repository.ChatRoomRepository;
import org.example.server.chatRoom.repository.MessageRepository;
import org.example.server.chatRoom.repository.UserRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final ChatService chatService;
    private final MessageRepository messageRepository;


    public Mono<ChatRoomResponse> createChatRoom(CreateChatRoomRequest request) {
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

    private ChatRoomResponse saveChatRoomAndBuildResponse(User owner, String question, String answer) {
        LocalDateTime now = LocalDateTime.now();

        ChatRoom chatRoom = ChatRoom.builder()
                .user(owner)
                .title(question)
                .createdAt(now)
                .updateAt(now)
                .isDeleted(Boolean.FALSE)
                .build();

        ChatRoom saved = chatRoomRepository.save(chatRoom);
        persistInitialMessages(saved, question, answer, now);

        return new ChatRoomResponse(saved.getChatRoomId(), saved.getTitle(), answer);
    }

    private void persistInitialMessages(ChatRoom chatRoom, String question, String answer, LocalDateTime createdAt) {
        Message questionMessage = Message.builder()
                .chatRoom(chatRoom)
                .isUser(true)
                .content(question)
                .createdAt(createdAt)
                .build();

        Message answerMessage = Message.builder()
                .chatRoom(chatRoom)
                .isUser(false)
                .content(answer)
                .createdAt(LocalDateTime.now())
                .build();

        messageRepository.saveAll(List.of(questionMessage, answerMessage));
    }

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
}
