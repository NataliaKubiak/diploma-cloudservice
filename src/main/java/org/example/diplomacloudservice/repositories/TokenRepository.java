package org.example.diplomacloudservice.repositories;

import org.example.diplomacloudservice.entities.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Integer> {

    Optional<Object> findFirstByToken(String token);
}
