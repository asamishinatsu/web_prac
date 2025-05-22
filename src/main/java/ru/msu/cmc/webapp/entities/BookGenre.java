package ru.msu.cmc.webapp.entities;

import lombok.*;
import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "book_genres")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookGenre {

    @EmbeddedId
    private BookGenreId id = new BookGenreId();

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("bookId")
    @JoinColumn(name = "book_id", nullable = false)
    @ToString.Exclude
    private Book book;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("genreId")
    @JoinColumn(name = "genre_id", nullable = false)
    @ToString.Exclude
    private Genre genre;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BookGenre bookGenre)) return false;
        return Objects.equals(id, bookGenre.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}