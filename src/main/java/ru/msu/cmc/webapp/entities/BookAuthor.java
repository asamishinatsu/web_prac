package ru.msu.cmc.webapp.entities;

import lombok.*;
import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "book_authors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookAuthor {

    @EmbeddedId
    private BookAuthorId id = new BookAuthorId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("bookId")
    @JoinColumn(name = "book_id", nullable = false)
    @ToString.Exclude
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("authorId")
    @JoinColumn(name = "author_id", nullable = false)
    @ToString.Exclude
    private Author author;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BookAuthor bookAuthor)) return false;
        return Objects.equals(id, bookAuthor.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}