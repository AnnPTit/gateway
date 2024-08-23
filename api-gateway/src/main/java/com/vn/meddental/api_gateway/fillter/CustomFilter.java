package com.vn.meddental.api_gateway.fillter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;

@Component
public class CustomFilter extends AbstractGatewayFilterFactory<CustomFilter.Config> {

    @Autowired
    private WebClient.Builder webClientBuilder;

    public CustomFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // Lấy request ban đầu
            ServerHttpRequest request = exchange.getRequest();

            // Lấy Bearer Token từ request hiện tại
            String bearerToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
                try {
                    throw new Exception("loi r nhe");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            // Gọi service khác với Bearer Token
            return webClientBuilder.build()
                    .get()
                    .uri("http://localhost:8082/auth/test/2") // URL của service bạn muốn gọi
                    .header(HttpHeaders.AUTHORIZATION, bearerToken) // Thêm Bearer Token vào request
                    .retrieve()
                    .bodyToMono(String.class) // Giả sử response trả về là dạng String
                    .flatMap(response -> {
                        // Xử lý kết quả trả về từ service khác
                        System.out.println("Response from service: " + response);
                        if(response.equals("false")) {
                            try {
                                return reactor.core.publisher.Mono.error(new Exception("loi r nhe"));
                            } catch (Exception e) {
                                return reactor.core.publisher.Mono.error(new RuntimeException(e));
                            }
                        }
                        // Thêm kết quả từ service vào header và thêm Bearer Token
                        ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                                .header("X-Service-Response", response)
                                .header(HttpHeaders.AUTHORIZATION, bearerToken) // Thêm Bearer Token vào header
                                .build();

                        // Tạo exchange mới với request đã chỉnh sửa
                        ServerWebExchange modifiedExchange = exchange.mutate()
                                .request(modifiedRequest)
                                .build();

                        // Chuyển tiếp request
                        return chain.filter(modifiedExchange);
                    });
        };
    }

    public static class Config {
        // Cấu hình nếu cần
    }
}