package com.interview.moneyForward.repository;

import com.interview.moneyForward.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
}
