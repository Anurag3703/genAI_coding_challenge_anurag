package org.example.genai_coding_challenge_anurag.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import java.net.http.HttpRequest;

@Configuration
public class WebClientConfig {

    //Used for api request (I should also put a request delay)
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(1024*1024)) // 1 MB
                         .build();
    }


}
