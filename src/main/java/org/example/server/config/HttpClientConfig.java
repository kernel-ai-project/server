package org.example.server.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class HttpClientConfig {

    @Value("${fastapi.base-url}")
    private String fastapiBaseUrl;

    @Bean
    public WebClient fastapiClient(WebClient.Builder builder) {
        HttpClient http = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .responseTimeout(Duration.ofSeconds(10))
                .doOnConnected(conn -> {
                    conn.addHandlerLast(new ReadTimeoutHandler(15, TimeUnit.SECONDS));
                    conn.addHandlerLast(new WriteTimeoutHandler(15, TimeUnit.SECONDS));
                });

        return builder
                .baseUrl(fastapiBaseUrl)
                .clientConnector(new ReactorClientHttpConnector(http))
                .build();
    }
}
