package org.example.server.echo;

import org.example.server.chat.dto.AskRequest;
import org.example.server.chat.dto.AskResponse;
import org.example.server.echo.dto.EchoRequest;
import org.example.server.echo.dto.EchoResponse;
import org.example.server.echo.dto.HealthResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api") // 외부에 노출되는 단일 엔드포인트
@RequiredArgsConstructor
public class EchoController {

    private final EchoService echoService;

    @GetMapping("/health")
    public Mono<HealthResponse> health() {
        return echoService.health();
    }

    @PostMapping("/echo")
    public Mono<EchoResponse> echo(@Validated @RequestBody EchoRequest req) {
        return echoService.echo(req);
    }



}
