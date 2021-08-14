package ru.romanow.orders.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.romanow.orders.domain.Order;
import ru.romanow.orders.model.OrderRequest;
import ru.romanow.orders.repository.OrdersRepository;

import javax.annotation.Nonnull;
import javax.persistence.EntityNotFoundException;
import java.util.UUID;

import static com.google.common.base.Joiner.on;
import static java.lang.String.format;
import static org.slf4j.LoggerFactory.getLogger;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl
        implements OrderService {
    private static final Logger logger = getLogger(OrderServiceImpl.class);

    private final OrdersRepository ordersRepository;

    @Override
    @Transactional
    public void createOrder(@Nonnull UUID orderUid, @Nonnull OrderRequest request) {
        Order order =
                new Order()
                        .setUid(orderUid)
                        .setItems(on(",").join(request.getItemUids()))
                        .setFirstName(request.getFirstName())
                        .setLastName(request.getLastName())
                        .setAddress(request.getAddress());

        order = ordersRepository.save(order);
        if (logger.isDebugEnabled()) {
            logger.debug("Create new order '{}'", order);
        }
    }

    @Nonnull
    @Override
    @Transactional(readOnly = true)
    public Order getOrderByUid(@Nonnull UUID orderUid) {
        return ordersRepository.findByUid(orderUid)
                .orElseThrow(() -> new EntityNotFoundException(format("Order '%s' not found", orderUid)));
    }
}
