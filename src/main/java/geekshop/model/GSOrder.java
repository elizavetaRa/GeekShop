package geekshop.model;

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
 * An extension of {@link Order} extended by {@link OrderType}.
 *
 * @author Elizaveta Ragozina
 * @author Sebastian D&ouml;ring
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

    private long orderNumber;

    @Enumerated(EnumType.STRING)
    private OrderType type;

    @OneToOne
    private final GSOrder reclaimedOrder;


    @Deprecated
    protected GSOrder() {
        this.reclaimedOrder = null;
        initializeGSOrder(null);
    }

    public GSOrder(UserAccount ua) {
        super(ua);

        this.reclaimedOrder = null;
        initializeGSOrder(reclaimedOrder);
    }

    public GSOrder(UserAccount ua, GSOrder reclaimedOrder) {
        super(ua);

        this.reclaimedOrder = reclaimedOrder;
        initializeGSOrder(reclaimedOrder);
    }

    public GSOrder(UserAccount ua, PaymentMethod paymentMethod) {
        super(ua, paymentMethod);

        this.reclaimedOrder = null;
        initializeGSOrder(reclaimedOrder);
    }

    public GSOrder(UserAccount ua, PaymentMethod paymentMethod, GSOrder reclaimedOrder) {
        super(ua, paymentMethod);

        this.reclaimedOrder = reclaimedOrder;
        initializeGSOrder(reclaimedOrder);
    }

    private void initializeGSOrder(GSOrder reclaimedOrder) {

        ////////////////////////////////////// Validierung! ////////////////////////////////////////

        if (reclaimedOrder == null) {
            this.type = OrderType.NORMAL;
        } else {
            if (reclaimedOrder.getOrderType() == OrderType.RECLAIM)
                throw new IllegalArgumentException("Eine Reklamation darf sich nicht auf eine Reklamation beziehen!");

            this.type = OrderType.RECLAIM;
        }

        setOrderStatus(OrderStatus.OPEN);

        orderNumber = 0L;
    }


    @Override
    public void add(OrderLine orderLine) {
        if (type == OrderType.RECLAIM && orderLine instanceof GSOrderLine) {
            ((GSOrderLine) orderLine).setType(OrderType.RECLAIM);
        }
        super.add(orderLine);
    }

    public void pay() {
        if (getOrderStatus() != OrderStatus.OPEN)
            throw new IllegalStateException("Order may only be paid if the OrderStatus is OPEN!");

        setOrderStatus(OrderStatus.PAID);

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

    public void complete() {
        if (getOrderStatus() != OrderStatus.PAID)
            throw new IllegalStateException("Only paid orders may be completed!");

        setOrderStatus(OrderStatus.COMPLETED);
    }

    public void cancel() {
        if (getOrderStatus() != OrderStatus.OPEN)
            throw new IllegalStateException("Order may only be cancelled if the OrderStatus is OPEN!");

        setOrderStatus(OrderStatus.CANCELLED);
    }


    @Override
    public boolean isOpen() {
        return getOrderStatus() == OrderStatus.OPEN;
    }

    @Override
    public boolean isPaid() {
        return getOrderStatus() == OrderStatus.PAID || isCompleted();
    }

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

    @Override
    public boolean isCanceled() {
        return getOrderStatus() == OrderStatus.CANCELLED;
    }


    private void setOrderStatus(OrderStatus status) {
        setOrderField("orderStatus", status);
    }

    private void setOrderField(String fieldName, Object newValue) {
        Field field;
        try {
            field = getClass().getSuperclass().getDeclaredField(fieldName);
        } catch (NoSuchFieldException ex) {
            System.out.println("!!!!!!!!!!!!!! " + fieldName + " of Order could not be found. !!!!!!!!!!!!!!");
            ex.printStackTrace();
            return;
        }
        try {
            field.setAccessible(true);
            field.set(this, newValue);
        } catch (IllegalAccessException ex) {
            System.out.println("!!!!!!!!!!!!!! " + fieldName + " of Order could not be set. !!!!!!!!!!!!!!");
            ex.printStackTrace();
        }
    }

    public long getOrderNumber() {
        if (orderNumber <= 0) {
            long orderNr = 1;
            while (orderRepo.findByOrderNumber(orderNr).isPresent())
                orderNr++;
            orderNumber = orderNr;
            orderRepo.save(this);
        }
        return orderNumber;
    }

    public void setOrderType(OrderType type) {
        this.type = type;
    }

    public Date getCreationDate() {
        LocalDateTime ldt = getDateCreated();
        if (ldt == null)
            return null;
        ZonedDateTime zdt = ldt.atZone(ZoneId.systemDefault());
        return Date.from(zdt.toInstant());
    }

    private void setDateCreated(LocalDateTime date) {
        setOrderField("dateCreated", date);
    }

    public OrderType getOrderType() {
        return type;
    }

    public GSOrder getReclaimedOrder() {
        return reclaimedOrder;
    }

    public PaymentType getPaymentType() {
        if (getPaymentMethod() instanceof Cheque)
            return PaymentType.CHEQUE;
        else if (getPaymentMethod() instanceof CreditCard)
            return PaymentType.CREDITCARD;
        else
            return PaymentType.CASH;
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

        setDateCreated(businessTime.getTime());
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

//        String orderNumberString;
//        int digits = 0;
//        do {
//            digits++;
//        } while((orderNumber = orderNumber / 10) != 0);
//        switch (digits){
//            case 1: orderNumberString = "000000" + String.valueOf(orderNumber);
//                    break;
//            case 2: orderNumberString = "00000" + String.valueOf(orderNumber);
//                    break;
//            case 3: orderNumberString = "0000" + String.valueOf(orderNumber);
//                    break;
//            case 4: orderNumberString = "000" + String.valueOf(orderNumber);
//                    break;
//            case 5: orderNumberString = "00" + String.valueOf(orderNumber);
//                    break;
//            case 6: orderNumberString = "0" + String.valueOf(orderNumber);
//                    break;
//            default: orderNumberString = String.valueOf(orderNumber);
//                    break;
//        }
//        return orderNumberString;
    }

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
}
