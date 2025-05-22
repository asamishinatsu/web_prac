package ru.msu.cmc.webapp.entities;

import lombok.*;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class BookGenreId implements Serializable {
    private Long bookId;
    private Long genreId;
}
