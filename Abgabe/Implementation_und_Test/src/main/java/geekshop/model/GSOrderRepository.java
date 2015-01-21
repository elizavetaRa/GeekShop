package geekshop.model;

import org.salespointframework.core.SalespointRepository;
import org.salespointframework.order.OrderIdentifier;

import java.util.Optional;

/**
 * Repository to store {@link GSOrder}s.
 *
 * @author Sebastian Döring
 */

public interface GSOrderRepository extends SalespointRepository<GSOrder, OrderIdentifier> {

    Optional<GSOrder> findByOrderNumber(long orderNumber);
    Iterable<GSOrder> findByType(OrderType type);
    Iterable<GSOrder> findByReclaimedOrder(GSOrder reclaimedOrder);
}
