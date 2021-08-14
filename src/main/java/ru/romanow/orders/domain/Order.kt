package ru.romanow.orders.domain;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "uid", nullable = false, updatable = false)
    private UUID uid;

    @Column(name = "items_uids", nullable = false, updatable = false)
    private String items;

    @Column(name = "first_name", length = 80)
    private String firstName;

    @Column(name = "last_name", length = 80)
    private String lastName;

    @Column(name = "address")
    private String address;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equal(uid, order.uid) &&
                Objects.equal(items, order.items);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uid, items);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("uid", uid)
                .add("items", items)
                .add("firstName", firstName)
                .add("lastName", lastName)
                .add("address", address)
                .toString();
    }
}
