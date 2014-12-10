package geekshop.model;

/*
 * Created by Basti on 09.12.2014.
 */

import org.salespointframework.core.SalespointRepository;
import org.salespointframework.order.OrderIdentifier;

/**
 * Repository to store {@link GSOrder}s.
 *
 * @author Sebastian D&ouml;ring
 */

public interface GSOrderRepository extends SalespointRepository<GSOrder, OrderIdentifier> {

    Iterable<GSOrder> findByType(OrderType type);
}
