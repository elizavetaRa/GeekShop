package geekshop.model;

import org.salespointframework.order.OrderLine;

import javax.persistence.*;

/**
 * Created by Lisa on 30.11.2014.
 */

@Entity
public class GSOrderLine extends OrderLine{

    @Id
    @GeneratedValue
    private Long id;
    @Enumerated(EnumType.STRING)
    private OrderLineState state;

    @Deprecated
    protected GSOrderLine() {
    }

    public GSOrderLine(OrderLineState state){
        this.state= state;
    }

    public OrderLineState getOrderLineState() {
        return state;
    }

    public void setOrderLineState(OrderLineState state) {            /*Im EKD in GSOrder??*/
        this.state = state;
    }
}
