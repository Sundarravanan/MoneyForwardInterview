package com.interview.moneyForward.controller;

import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.interview.moneyForward.model.User;
import com.interview.moneyForward.repository.UserRepository;
import com.interview.moneyForward.util.AuthUtil;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class UserController {
    private final UserRepository repository;

    public UserController(UserRepository repository) {
        this.repository = repository;
    }

    private static final Pattern USER_ID_PATTERN = Pattern.compile("^[A-Za-z0-9]{6,20}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^[\\x21-\\x7E]{8,20}$");

    @GetMapping("/hello")
    public ResponseEntity<?> hello() {
        return ResponseEntity.ok(Map.of("message", "Hello World!"));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Map<String, String> body) {
        String userId = body.get("user_id");
        String password = body.get("password");

        if (userId == null || password == null) {
            return bad("Account creation failed", "required user_id and password");
        }

        if (!USER_ID_PATTERN.matcher(userId).matches()
                || !PASSWORD_PATTERN.matcher(password).matches()) {
            return bad("Account creation failed", "invalid user_id or password");
        }

        if (repository.existsById(userId)) {
            return bad("Account creation failed", "already same user_id is used");
        }

        User user = new User();
        user.setUserId(userId);
        user.setPassword(password);
        user.setNickName(userId);
        repository.save(user);

        return ResponseEntity.ok(Map.of(
                "message", "Account successfully created",
                "user", Map.of(
                        "user_id", userId,
                        "nickname", userId
                )
        ));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getUser(@PathVariable String userId, HttpServletRequest req) {
        User auth = authenticate(req);
        if (auth == null) return authFail();

        return repository.findById(userId)
                .map(u -> ResponseEntity.ok(Map.of(
                        "message", "User details by user_id",
                        "user", Map.of(
                                "user_id", u.getUserId(),
                                "nickname", u.getNickName() != null ? u.getNickName() : "",
                                "comment", u.getComment() != null ? u.getComment() : ""
                        )
                )))
                .orElse(ResponseEntity.status(404)
                        .body(Map.of("message", "No User found")));
    }

    @PatchMapping("/users/{userId}")
    public ResponseEntity<?> update(
            @PathVariable String userId,
            @RequestBody Map<String, String> body,
            HttpServletRequest req
    ) {
        User auth = authenticate(req);
        if (auth == null) return authFail();
        if (!auth.getUserId().equals(userId)) {
            return ResponseEntity.status(403)
                    .body(Map.of("message", "No Permission for Update"));
        }

        User user = repository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404)
                    .body(Map.of("message", "No User found"));
        }

        if (body.containsKey("user_id") || body.containsKey("password")) {
            return bad("User update failed", "not updatable user_id and password");
        }

        if (!body.containsKey("nickname") && !body.containsKey("comment")) {
            return bad("User update failed", "required nickname or comment");
        }

        if (body.get("nickname") != null) {
            user.setNickName(body.get("nickname"));
        }
        if (body.get("comment") != null) {
            user.setComment(body.get("comment"));
        }
        repository.save(user);

        return ResponseEntity.ok(Map.of(
                "message", "User successfully updated",
                "recipe", new Object[]{
                        Map.of(
                                "nickname", user.getNickName(),
                                "comment", user.getComment()
                        )
                }
        ));
    }

    @PostMapping("/close")
    public ResponseEntity<?> close(HttpServletRequest req) {
        User auth = authenticate(req);
        if (auth == null) return authFail();

        repository.deleteById(auth.getUserId());

        return ResponseEntity.ok(
                Map.of("message", "Account and user successfully removed")
        );
    }

    private User authenticate(HttpServletRequest req) {
        String[] creds = AuthUtil.extractCredentials(req);
        if (creds == null) return null;

        User user = repository.findById(creds[0]).orElse(null);
        if (user == null || !user.getPassword().equals(creds[1])) {
            return null;
        }
        return user;
    }

    private ResponseEntity<?> authFail() {
        return ResponseEntity.status(401)
                .body(Map.of("message", "Authentication Failed"));
    }

    private ResponseEntity<?> bad(String msg, String cause) {
        return ResponseEntity.badRequest()
                .body(Map.of("message", msg, "cause", cause));
    }
}
