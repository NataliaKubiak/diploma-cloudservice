package org.example.diplomacloudservice.repositories;

import org.example.diplomacloudservice.entities.File;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<File, Integer> {

    Optional<File> findByFileNameAndUserId(String fileName, int userId);

    @Query("SELECT f FROM File f WHERE f.user.id = :userId")
    List<File> findFilesByUserId(@Param("userId") int userId, Pageable pageable);

}
