# ChatRoom 생성 흐름 (현재 구현 기준)

## 1. 요청 개요
1. 클라이언트가 `POST /api/chatrooms` 로 `{"question": "..."}` JSON을 전송한다.
2. 요청 바디는 `CreateChatRoomRequest` 로 역직렬화되고 `@NotBlank` 검증이 적용된다.
3. `ChatRoomController.create` 가 `ChatRoomService.createChatRoom` 을 호출한다.
4. 서비스는 사용자 조회 → AI 질문 전송 → 채팅방 저장 순으로 작업을 수행한 뒤, 최종 `ChatRoomResponse` 를 반환한다.

## 2. `ChatRoomController` (`src/main/java/org/example/server/chat/ChatRoomController.java`)
- `@RestController` 와 `@RequestMapping("/api/chatrooms")` 로 WebFlux 엔드포인트를 정의한다.
- `create` 메서드는 `@PostMapping` 으로 매핑되며, `@Valid @RequestBody` 로 요청 파라미터를 검증한다.
- 서비스가 돌려준 `Mono<ChatRoomResponse>` 에 `map(ResponseEntity::ok)` 을 적용해 HTTP 200 응답으로 감싼다.

## 3. `ChatRoomService` (`src/main/java/org/example/server/chat/ChatRoomService.java`)

### `createChatRoom(CreateChatRoomRequest request)`
1. `question` 문자열의 양 끝 공백을 제거한다.
2. `authenticatedUserProvider.getCurrentUserId()` 로 현재 사용자 ID 를 `Mono<Long>` 형태로 구한다.
3. `Mono.fromCallable(() -> loadOwner(userId)).subscribeOn(boundedElastic())` 조합으로 blocking JPA 조회를 Reactor 전용 스레드풀에서 실행한다.
4. 동시에 `chatService.ask(new AskRequest(question))` 로 FastAPI 서버에 질문을 전달해 `Mono<AskResponse>` 를 만든다.
5. `ownerMono.zipWith(answerMono)` 로 두 비동기 작업을 병렬 처리하면서 결과를 묶는다.
6. 묶인 결과를 `Mono.fromCallable(() -> saveChatRoomAndBuildResponse(...)).subscribeOn(boundedElastic())` 로 감싸 채팅방 저장과 DTO 생성까지 JPA 친화적인 스레드에서 처리한다.

### `loadOwner(Long userId)`
- `userRepository.findById(userId)` 로 사용자를 조회하고, 없으면 `UserNotFoundException` 을 던져 404 상황을 표현한다.

### `saveChatRoomAndBuildResponse(User owner, String question, String answer)`
1. `ChatRoom.builder()` 로 새 엔티티를 생성하고 소유자, 제목, 생성/수정 시각, 삭제 여부를 채운다.
2. `chatRoomRepository.save(chatRoom)` 으로 DB에 저장해 생성된 식별자를 확보한다.
3. 저장된 엔티티에서 ID·제목을 꺼내고 AI 응답 문자열을 더해 `ChatRoomResponse` 를 반환한다.
