package ru.msu.cmc.webapp.entities;

import com.google.common.primitives.UnsignedLong;
import lombok.*;
import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderBook {

    @EmbeddedId
    private OrderBookId id = new OrderBookId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("orderId")
    @JoinColumn(name = "order_id", nullable = false)
    @ToString.Exclude
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("bookId")
    @JoinColumn(name = "book_id", nullable = false)
    @ToString.Exclude
    private Book book;

    @Column(name = "quantity", nullable = false)
    private UnsignedLong quantity;

    @Column(name = "price", precision = 10, scale = 2, nullable = false)
    private BigDecimal price;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderBook orderBook)) return false;
        return Objects.equals(id, orderBook.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}