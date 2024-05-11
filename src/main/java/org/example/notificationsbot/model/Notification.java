package org.example.notificationsbot.model;

import jakarta.persistence.*;
import lombok.*;
import org.example.notificationsbot.model.contract.AbstractEntity;

@Getter
@Setter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "notifications")
public class Notification extends AbstractEntity {
    @Column(name = "title")
    String title;

    @Column(name = "description")
    String description;

    @Enumerated(EnumType.STRING)
    Status status;

    @Column(name = "seconds")
    Long seconds;

    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;
}
