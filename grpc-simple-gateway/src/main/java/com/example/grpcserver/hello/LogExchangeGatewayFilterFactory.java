package com.example.grpcserver.hello;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractNameValueGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.cloud.gateway.support.GatewayToStringStyler.filterToStringCreator;

@Component
public class LogExchangeGatewayFilterFactory extends AbstractNameValueGatewayFilterFactory {

    private static final Logger log = LoggerFactory.getLogger(LogExchangeGatewayFilterFactory.class);

    @Override
    public GatewayFilter apply(NameValueConfig config) {
        return new GatewayFilter() {
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
                ServerHttpRequest request = exchange.getRequest();
                log.info("REQUEST:\n{} {}\n\n{}\n", request.getMethod(), request.getURI(), formatHeaders(request.getHeaders()));
                return chain
                        .filter(exchange)
                        .then(Mono.fromRunnable(() -> {
                            ServerHttpResponse response = exchange.getResponse();
                            log.info("RESPONSE:\nHTTP/2 {}\n\n{}\n", response.getStatusCode(), formatHeaders(response.getHeaders()));
                        }));
            }

            @Override
            public String toString() {
                return filterToStringCreator(LogExchangeGatewayFilterFactory.this)
                        .toString();
            }

            private String formatHeaders(HttpHeaders headers) {
                return headers
                        .entrySet()
                        .stream()
                        .flatMap(e -> e.getValue().stream().map(v -> Map.entry(e.getKey(), v)))
                        .map(e -> e.getKey() + ":\"" + e.getValue() + "\"")
                        .collect(Collectors.joining("\n"));
            }
        };
    }

}
