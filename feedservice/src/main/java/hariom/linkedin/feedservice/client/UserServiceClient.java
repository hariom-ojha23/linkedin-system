package hariom.linkedin.feedservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;
import java.util.Map;

@FeignClient(
        name = "userservice",
        url = "${user.service.url}"
)
public interface UserServiceClient {

    @GetMapping("/api/v1/users/connections")
    List<Map<String, Object>> getAllConnection(@RequestHeader("X-User-Id") Long userId);

}
