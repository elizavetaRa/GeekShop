package geekshop.model;

import org.salespointframework.catalog.Product;
import org.salespointframework.catalog.ProductIdentifier;
import org.salespointframework.inventory.Inventory;
import org.salespointframework.order.Order;
import org.salespointframework.order.OrderLine;
import org.salespointframework.order.OrderStatus;
import org.salespointframework.payment.Cheque;
import org.salespointframework.payment.CreditCard;
import org.salespointframework.payment.PaymentMethod;
import org.salespointframework.time.BusinessTime;
import org.salespointframework.useraccount.UserAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.persistence.*;
import java.lang.reflect.Field;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;

/**
 * An extension of {@link Order} extended by order number, {@link OrderType} and, if this order is a reclaim, the initial order this order is referring to.
 *
 * @author Sebastian Döring
 * @author Elizaveta Ragozina
 */

@Entity
@Component // needed for autowired static fields
public class GSOrder extends Order implements Comparable<GSOrder> {
    /*
     * BusinessTime, GSOrderRepository, Inventory and MessageRepository are static because JPA is creating a separate entity instance,
     * i.e. not using the Spring managed bean and so it's required for the context to be shared.
     */
    @Transient
    private static BusinessTime businessTime;
    @Transient
    private static GSOrderRepository orderRepo;
    @Transient
    private static Inventory<GSInventoryItem> inventory;
    @Transient
    private static MessageRepository messageRepo;

    @Transient
    private static long orderCounter = 0L;

    private long orderNumber;

    @Enumerated(EnumType.STRING)
    private OrderType type;

    @OneToOne
    private final GSOrder reclaimedOrder;


    @Deprecated
    protected GSOrder() {
        this.reclaimedOrder = null;
    }

    /**
     * Creates a new {@link GSOrder}.
     */
    public GSOrder(UserAccount ua) {
        this(ua, null, null);
    }

    /**
     * Creates a new reclaim {@link GSOrder} with the given initial order.
     *
     * @param reclaimedOrder may be {@literal null}. If it is, this order's type is set to {@literal NORMAL}.
     *                       Else, the reclaimed order must not be a reclaim, but a paid order being not older than 14 days.
     *
     * @throws IllegalArgumentException if the given reclaimed order is a reclaim, is open, cancelled or completed.
     */
    public GSOrder(UserAccount ua, GSOrder reclaimedOrder) {
        this(ua, null, reclaimedOrder);
    }

    /**
     * Creates a new {@link GSOrder} with the given {@link PaymentMethod}.
     */
    public GSOrder(UserAccount ua, PaymentMethod paymentMethod) {
        this(ua, paymentMethod, null);
    }

    /**
     * Creates a new reclaim {@link GSOrder} with the given {@link PaymentMethod} and the initial order.
     *
     * @param reclaimedOrder may be {@literal null}. If it is, this order's type is set to {@literal NORMAL}.
     *                       Else, the reclaimed order must not be a reclaim, but a paid order being not older than 14 days.
     *
     * @throws IllegalArgumentException if the given reclaimed order is a reclaim, is open, cancelled or completed.
     */
    public GSOrder(UserAccount ua, PaymentMethod paymentMethod, GSOrder reclaimedOrder) {
        super(ua);

        if (paymentMethod != null)
            super.setPaymentMethod(paymentMethod);

        this.reclaimedOrder = reclaimedOrder;

        if (reclaimedOrder == null) {
            this.type = OrderType.NORMAL;
        } else {
            if (reclaimedOrder.getOrderType() == OrderType.RECLAIM)
                throw new IllegalArgumentException("A reclaim must not relate to a reclaim!");

            if (reclaimedOrder.isOpen() || reclaimedOrder.isCanceled())
                throw new IllegalArgumentException("A reclaim must not relate to an open or cancelled order!");

            if (reclaimedOrder.isCompleted())
                throw new IllegalArgumentException("An order which is older than 14 days cannot be reclaimed!");

            this.type = OrderType.RECLAIM;
        }

        setOrderStatus(OrderStatus.OPEN);

        if (orderRepo.count() > 0L && orderCounter == 0L)
            orderCounter = orderRepo.count();
        orderNumber = ++orderCounter;

        setDateCreated(businessTime.getTime());
    }


    /**
     * Adds an {@link OrderLine} to the {@link GSOrder}, the {@link OrderStatus} must be {@code OPEN}.
     * <p>
     * If this order is a reclaim and the given {@link OrderLine} is a {@link GSOrderLine},
     * the order line's type is also set to reclaim.
     *
     * @throws IllegalArgumentException if the given reclaim order line contains a product which does not exist in the reclaimed order
     *                                  or if the quantity of the given reclaim order line exceeds the quantity in reclaimed order.
     */
    @Override
    public void add(OrderLine orderLine) {
        if (type == OrderType.RECLAIM) {
            if (reclaimedOrder.findOrderLineByProductIdentifier(orderLine.getProductIdentifier()) == null)
                throw new IllegalArgumentException("The reclaimed product must also be in the reclaimed order!");

            if (reclaimedOrder.findOrderLineByProductIdentifier(orderLine.getProductIdentifier()).getQuantity().subtract(orderLine.getQuantity()).isNegative())
                throw new IllegalArgumentException("It cannot be relaimed more units of a product than there are bought in the reclaimed order!");

            if (orderLine instanceof GSOrderLine)
                ((GSOrderLine) orderLine).setType(OrderType.RECLAIM);
        }
        super.add(orderLine);
    }

    /**
     * Replaces {@code payOrder} of {@link org.salespointframework.order.OrderManager}.
     * <p>
     * Sets the {@link OrderStatus} to {@code PAID}.
     * If this is a reclaim, the reclaimed products are restored in {@link Inventory}.
     * Else, the bought products are taken from {@link Inventory} and,
     * if the current amount is falling below the specified minimal amount, a message will be sent to the owner.
     *
     * @throws IllegalStateException if order is not open, payment method is not set or order has no order lines.
     */
    public void pay() {
        if (getOrderStatus() != OrderStatus.OPEN)
            throw new IllegalStateException("Order may only be paid if the OrderStatus is OPEN!");

        if (getPaymentMethod() == null)
            throw new IllegalStateException("Order may only be paid if payment method is set!");

        if (!this.getOrderLines().iterator().hasNext())
            throw new IllegalStateException("Order may only be paid if it has at least one order line!");

        setOrderStatus(OrderStatus.PAID);

        if (getDateCreated() == null)
            setDateCreated(businessTime.getTime());

        if (type == OrderType.NORMAL) {

            for (OrderLine ol : getOrderLines()) {
                GSInventoryItem inventoryItem = inventory.findByProductIdentifier(ol.getProductIdentifier()).get();
                inventoryItem.decreaseQuantity(ol.getQuantity());
                inventory.save(inventoryItem);
                if (!inventoryItem.hasSufficientQuantity()) {
                    messageRepo.save(new Message(MessageKind.NOTIFICATION,
                            "Die verfügbare Menge des Artikels „" + ol.getProductName() + "“ " +
                                    "(Artikelnr. " + GSOrder.longToString(((GSProduct) inventoryItem.getProduct()).getProductNumber()) +
                                    ") hat mit " + inventoryItem.getQuantity().getAmount() + " Stück " +
                                    "die festgelegte Mindestanzahl von " + inventoryItem.getMinimalQuantity().getAmount() +
                                    " Stück unterschritten."));
                }
            }

        } else {

            for (OrderLine ol : getOrderLines()) {
                GSInventoryItem inventoryItem = inventory.findByProductIdentifier(ol.getProductIdentifier()).get();
                inventoryItem.increaseQuantity(ol.getQuantity());
                inventory.save(inventoryItem);
            }

        }
    }

    /**
     * Replaces {@code completeOrder} of {@link org.salespointframework.order.OrderManager} setting {@link OrderStatus} to {@code COMPLETED}.
     *
     * @throws IllegalStateException if order status is not {@literal PAID}.
     */
    public void complete() {
        if (getOrderStatus() != OrderStatus.PAID)
            throw new IllegalStateException("Only paid orders may be completed!");

        setOrderStatus(OrderStatus.COMPLETED);
    }

    /**
     * Replaces {@code cancelOrder} of {@link org.salespointframework.order.OrderManager} setting {@link OrderStatus} to {@code CANCELLED}.
     *
     * @throws IllegalStateException if order status is not {@literal OPEN}.
     */
    public void cancel() {
        if (getOrderStatus() != OrderStatus.OPEN)
            throw new IllegalStateException("Order may only be cancelled if the OrderStatus is OPEN!");

        setOrderStatus(OrderStatus.CANCELLED);
    }


    /**
     * Convenience method for checking if an order has the status {@code OPEN}.
     */
    @Override
    public boolean isOpen() {
        return getOrderStatus() == OrderStatus.OPEN;
    }

    /**
     * Convenience method for checking if an order has the status {@code PAID} or {@code COMPLETED}.
     */
    @Override
    public boolean isPaid() {
        return getOrderStatus() == OrderStatus.PAID || isCompleted();
    }

    /**
     * Convenience method for checking if an order has the status {@code COMPLETED}.
     * <p>
     * In addition, if this order is not a reclaim order and 14 days are already expired,
     * the {@link OrderStatus} is set to {@code COMPLETED}.
     */
    @Override
    public boolean isCompleted() {
        if (getOrderStatus() == OrderStatus.COMPLETED)
            return true;

        if (type == OrderType.NORMAL && getDateCreated() != null) {
            LocalDateTime todayMidnight = LocalDateTime.of(LocalDate.from(businessTime.getTime()), LocalTime.of(23, 59, 59));
            if (ChronoUnit.DAYS.between(getDateCreated(), todayMidnight) > 14) {
                setOrderStatus(OrderStatus.COMPLETED);
                return true;
            }
        }

        return false;
    }

    /**
     * Convenience method for checking if an order has the status {@code CANCELLED}.
     */
    @Override
    public boolean isCanceled() {
        return getOrderStatus() == OrderStatus.CANCELLED;
    }


    public long getOrderNumber() {
        return orderNumber;
    }

    protected void setOrderType(OrderType type) {
        this.type = type;
    }

    public OrderType getOrderType() {
        return type;
    }

    public GSOrder getReclaimedOrder() {
        return reclaimedOrder;
    }

    /**
     * Converts {@code paymentMethod} to {@link PaymentType}.
     */
    public PaymentType getPaymentType() {
        if (getPaymentMethod() instanceof Cheque)
            return PaymentType.CHEQUE;
        else if (getPaymentMethod() instanceof CreditCard)
            return PaymentType.CREDITCARD;
        else
            return PaymentType.CASH;
    }

    /**
     * Returns date created as {@code java.util.Date}.
     */
    public Date getCreationDate() {
        if (getDateCreated() == null)
            setDateCreated(businessTime.getTime());

        LocalDateTime ldt = getDateCreated();
        ZonedDateTime zdt = ldt.atZone(ZoneId.systemDefault());
        return Date.from(zdt.toInstant());
    }

    /**
     * Returns the {@link OrderLine} containing the given {@link org.salespointframework.catalog.Product}.
     */
    public OrderLine findOrderLineByProduct(Product product) {
        for (OrderLine orderLine : getOrderLines()) {
            if (orderLine.getProductIdentifier().equals(product.getId()))
                return orderLine;
        }
        return null;
    }

    /**
     * Returns the {@link OrderLine} containing the {@link org.salespointframework.catalog.Product} with the given {@link org.salespointframework.catalog.ProductIdentifier}.
     */
    public OrderLine findOrderLineByProductIdentifier(ProductIdentifier productId) {
        for (OrderLine orderLine : getOrderLines()) {
            if (orderLine.getProductIdentifier().equals(productId))
                return orderLine;
        }
        return null;
    }

    /**
     * Converts a long variable to string with leading zeros.
     */
    public static String longToString(long number) {
        String nr = Long.toString(number);
        int length = 7;
        if (nr.length() >= length)
            return nr;

        StringBuilder sb = new StringBuilder(length);
        char[] zeros = new char[length - nr.length()];
        Arrays.fill(zeros, '0');
        sb.append(zeros);
        sb.append(nr);
        return sb.toString();
    }

    /**
     * Helper function to access and change private fields of super class {@link Order}.
     */
    private void setOrderField(String fieldName, Object newValue) {
        Field field;
        try {
            field = getClass().getSuperclass().getDeclaredField(fieldName);
        } catch (NoSuchFieldException ex) {
            System.out.println(fieldName + " of order could not be found!");
            ex.printStackTrace();
            return;
        }
        try {
            field.setAccessible(true);
            field.set(this, newValue);
        } catch (IllegalAccessException ex) {
            System.out.println(fieldName + " of order could not be set!");
            ex.printStackTrace();
        }
    }

    private void setOrderStatus(OrderStatus status) {
        setOrderField("orderStatus", status);
    }

    private void setDateCreated(LocalDateTime date) {
        setOrderField("dateCreated", date);
    }

    /**
     * Compares two {@link GSOrder}s at first by date created and then by their order number.
     */
    @Override
    public int compareTo(GSOrder other) {
        if (this.getDateCreated() == null) {
            if (other.getDateCreated() == null) {
                return ((Long) this.orderNumber).compareTo(other.orderNumber);
            } else {
                return 1;
            }
        } else {
            if (other.getDateCreated() == null) {
                return -1;
            } else {
                return (this.getDateCreated()).compareTo(other.getDateCreated());
            }
        }
    }

    /**
     * {@code @PostConstruct} fires {@code init()} once the Entity has been instantiated and by referencing businessTime, orderRepo, inventory and messageRepo in it,
     * it forces – if not injected already – the injection on the static properties for the instance created.
     */
    @PostConstruct
    public void init() {
        System.out.println("Initializing BusinessTime as [" +
                GSOrder.businessTime + "]");
        System.out.println("Initializing GSOrderRepository as [" +
                GSOrder.orderRepo + "]");
        System.out.println("Initializing Inventory as [" +
                GSOrder.inventory + "]");
        System.out.println("Initializing MessageRepository as [" +
                GSOrder.messageRepo + "]");
    }

    /**
     * Sets the static field {@code businessTime}.
     */
    @Autowired
    public void setBusinessTime(BusinessTime businessTime) {
        GSOrder.businessTime = businessTime;
    }

    /**
     * Sets the static field {@code orderRepo}.
     */
    @Autowired
    public void setOrderRepo(GSOrderRepository orderRepo) {
        GSOrder.orderRepo = orderRepo;
    }

    /**
     * Sets the static field {@code inventory}.
     */
    @Autowired
    public void setInventory(Inventory<GSInventoryItem> inventory) {
        GSOrder.inventory = inventory;
    }

    /**
     * Sets the static field {@code messageRepo}.
     */
    @Autowired
    public void setMessageRepo(MessageRepository messageRepo) {
        GSOrder.messageRepo = messageRepo;
    }
}