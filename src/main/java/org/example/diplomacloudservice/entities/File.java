package org.example.diplomacloudservice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// TODO: 12/02/2025 что там с equals and hasnCode и с toString?
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "files")
@Entity
public class File {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    // TODO: 12/02/2025 подумать что там с on delete cascade или что там?
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "file_location", nullable = false)
    private String fileLocation;

    @Column(name = "size")
    private Long size;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}