package com.withyourhand.ymy.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ymy")
public class HelloController {
    @GetMapping("/info")
    public ServiceInfo info() {
        return new ServiceInfo("WithYourHandYmy", "YMY", "Java", Runtime.version().toString());
    }

    @GetMapping("/hello")
    public Greeting hello(@RequestParam(defaultValue = "朋友") @NotBlank @Size(max = 50) String name) {
        return new Greeting("WithYourHandYmy", "你好，" + name.strip() + "！今天也动手写点代码。");
    }

    public record ServiceInfo(String service, String owner, String language, String version) {}

    public record Greeting(String service, String message) {}
}
