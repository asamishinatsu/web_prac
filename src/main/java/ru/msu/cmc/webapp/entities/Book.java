package ru.msu.cmc.webapp.entities;

import lombok.*;
import javax.persistence.*;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "books")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Book implements CommonEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "publisher")
    private String publisher;

    @Column(name = "year_of_publication")
    private Long year;

    @Column(name = "pages")
    private Long pages;

    @Enumerated(EnumType.STRING)
    @Column(name = "cover_type", nullable = false)
    private CoverType cover;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "stock", nullable = false)
    private Long stock;

    @OneToMany(mappedBy = "book", fetch = FetchType.EAGER)
    @ToString.Exclude
    private Set<BookGenre> bookGenreList = new HashSet<>();

    @OneToMany(mappedBy = "book", fetch = FetchType.EAGER)
    @ToString.Exclude
    private Set<BookAuthor> bookAuthorList = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Book book)) return false;
        return Objects.equals(id, book.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
