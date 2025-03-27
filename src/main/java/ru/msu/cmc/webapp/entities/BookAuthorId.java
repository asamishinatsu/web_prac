package ru.msu.cmc.webapp.entities;

import lombok.*;
import javax.persistence.*;
import java.io.Serializable;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class BookAuthorId implements Serializable {
    private Long bookId;
    private Long authorId;
}
