package com.example.demo.repository;

import com.example.demo.dto.Account;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

public interface AccountRepository extends MongoRepository<Account, ObjectId>, UserDetailsService {

    Optional<Account> findByEmail(String email);

    @Override default UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return findByEmail(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found by email: " + username));
    }
}
