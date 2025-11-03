package org.example.server.chatRoom;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.example.server.chat.ChatService;
import org.example.server.chat.dto.AskRequest;
import org.example.server.chat.dto.AskResponse;
import org.example.server.chatRoom.dto.ChatRoomResponse;
import org.example.server.chatRoom.dto.CreateChatRoomRequest;
import org.example.server.chat.entity.ChatRoom;
import org.example.server.chat.entity.User;
import org.example.server.chat.exception.UserNotFoundException;
import org.example.server.chatRoom.repository.ChatRoomRepository;
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
        ChatRoom chatRoom = ChatRoom.builder()
                .user(owner)
                .title(question)
                .createdAt(LocalDateTime.now())
                .updateAt(LocalDateTime.now())
                .isDeleted(Boolean.FALSE)
                .build();

        ChatRoom saved = chatRoomRepository.save(chatRoom);

        return new ChatRoomResponse(saved.getChatRoomId(), saved.getTitle(), answer);
    }
}
