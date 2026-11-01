package com.withyourhand.ymy;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiIntegrationTest {
    @Value("${local.server.port}")
    private int port;

    private HttpResponse<String> get(String path) throws Exception {
        try (var client = HttpClient.newHttpClient()) {
            return client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path)).GET().build(),
                    HttpResponse.BodyHandlers.ofString());
        }
    }

    @Test
    void exposesHealthAndServiceIdentity() throws Exception {
        var health = get("/actuator/health");
        assertThat(health.statusCode()).isEqualTo(200);
        assertThat(health.body()).contains("\"status\":\"UP\"");
        var info = get("/api/ymy/info");
        assertThat(info.statusCode()).isEqualTo(200);
        assertThat(info.body()).contains("\"service\":\"WithYourHandYmy\"", "\"owner\":\"YMY\"");
    }

    @Test
    void greetsTheRequestedNameAndProvidesADefault() throws Exception {
        var greeting = get("/api/ymy/hello?name=YMY");
        assertThat(greeting.statusCode()).isEqualTo(200);
        assertThat(greeting.body()).contains("你好，YMY！");
        assertThat(get("/api/ymy/hello").body()).contains("你好，朋友！");
    }

    @Test
    void rejectsWhitespaceAndOverlongNames() throws Exception {
        for (var name : new String[] {"%20%20", "x".repeat(51)}) {
            var response = get("/api/ymy/hello?name=" + name);
            assertThat(response.statusCode()).isEqualTo(400);
            assertThat(response.body()).contains("\"code\":\"INVALID_ARGUMENT\"");
        }
    }
}
