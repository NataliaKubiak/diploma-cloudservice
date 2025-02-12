package org.example.diplomacloudservice.entities;

import jakarta.persistence.*;
import lombok.*;

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

    // TODO: 12/02/2025 подумать что там с on delete cascade или что там?
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "size")
    private Long size;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
