package org.example.diplomacloudservice.repositories;

import org.example.diplomacloudservice.entities.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<File, Integer> {

    Optional<File> findByFileNameAndUserId(String fileName, int userId);

}
