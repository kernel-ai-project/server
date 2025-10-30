package org.example.server.echo;

import org.example.server.echo.dto.EchoRequest;
import org.example.server.echo.dto.EchoResponse;
import org.example.server.echo.dto.HealthResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class EchoService {

    private final WebClient fastapiClient;

    public Mono<HealthResponse> health() {
        return fastapiClient.get()
                .uri("/health")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(HealthResponse.class);
    }

    public Mono<EchoResponse> echo(EchoRequest req) {
        return fastapiClient.post()
                .uri("/echo")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .retrieve()
                .bodyToMono(EchoResponse.class);
    }
}
