package ru.romanow.orders.domain

import com.vladmihalcea.hibernate.type.array.ListArrayType
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "orders")
@TypeDef(name = "list-array", typeClass = ListArrayType::class)
data class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @Column(name = "uid", nullable = false, updatable = false)
    var uid: UUID? = null,

    @Type(type = "list-array")
    @Column(name = "items_uids", nullable = false, columnDefinition = "uuid[]")
    var items: List<UUID>? = null,

    @Column(name = "first_name", length = 80)
    var firstName: String? = null,

    @Column(name = "last_name", length = 80)
    var lastName: String? = null,

    @Column(name = "address")
    var address: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Order

        if (uid != other.uid) return false

        return true
    }

    override fun hashCode(): Int {
        return uid?.hashCode() ?: 0
    }

    override fun toString(): String {
        return "Order(id=$id, uid=$uid, items=$items, firstName=$firstName, lastName=$lastName, address=$address)"
    }
}