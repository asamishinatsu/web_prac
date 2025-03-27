package ru.msu.cmc.webapp.entities;

import lombok.*;
import javax.persistence.*;
import java.io.Serializable;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class OrderBookId implements Serializable {
    private Long orderId;
    private Long bookId;
}
