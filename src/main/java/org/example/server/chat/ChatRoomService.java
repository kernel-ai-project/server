package org.example.server.chat;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.example.server.chat.dto.ChatRoomResponse;
import org.example.server.chat.dto.CreateChatRoomRequest;
import org.example.server.chat.entity.ChatRoom;
import org.example.server.chat.entity.User;
import org.example.server.chat.exception.UserNotFoundException;
import org.example.server.chat.repository.ChatRoomRepository;
import org.example.server.chat.repository.UserRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final FastApiChatRoomClient fastApiChatRoomClient;

    public Mono<ChatRoomResponse> createChatRoom(CreateChatRoomRequest request) {
        String question = request.query().trim();

        Mono<User> ownerMono = Mono.fromCallable(() -> {
                    Long currentUserId = authenticatedUserProvider.getCurrentUserId();
                    return loadOwner(currentUserId);
                })
                .subscribeOn(Schedulers.boundedElastic());

        return ownerMono
                .zipWith(fastApiChatRoomClient.createChatRoom(question))
                .flatMap(tuple -> Mono.fromCallable(() ->
                                saveChatRoomAndBuildResponse(tuple.getT1(), question, tuple.getT2().query()))
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

        owner.getChatRooms().add(chatRoom);

        ChatRoom saved = chatRoomRepository.save(chatRoom);

        return new ChatRoomResponse(saved.getChatRoomId(), saved.getTitle(), answer);
    }
}
