package com.interview.moneyForward.init;

import com.interview.moneyForward.model.User;
import com.interview.moneyForward.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer {

    private final UserRepository repository;

    public DataInitializer(UserRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    public void init() {
        if (!repository.existsById("TaroYamada")) {
            User user = new User();
            user.setUserId("TaroYamada");
            user.setPassword("PaSSwd4TY");
            user.setNickName("Taro");
            user.setComment("I'm happy.");
            repository.save(user);
        }
    }
}
