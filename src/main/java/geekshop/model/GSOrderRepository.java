package geekshop.model;

/*
 * Created by Basti on 09.12.2014.
 */

import org.salespointframework.core.SalespointIdentifier;
import org.salespointframework.core.SalespointRepository;
import org.salespointframework.order.OrderIdentifier;

import java.util.Optional;

/**
 * Repository to store {@link GSOrder}s.
 *
 * @author Sebastian D&ouml;ring
 */

public interface GSOrderRepository extends SalespointRepository<GSOrder, OrderIdentifier> {

    Optional<GSOrder> findByOrderNumber(SalespointIdentifier orderNumber);
    Iterable<GSOrder> findByType(OrderType type);
    Iterable<GSOrder> findByReclaimedOrder(GSOrder reclaimedOrder);
}
