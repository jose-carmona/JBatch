package es.jose.entities;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;

@Entity
@Table(name = "DETAIL")
public class Detail {

    @Id
    @GeneratedValue 
    @Column(name = "ID")
    private int id;
    
    @ManyToOne
    @JoinColumn(name = "ENTITY_ID")
    private MyEntity entity;

    @Column(name = "AMOUNT")
    private BigDecimal amount;

    public Detail()  {
    }


    public Detail(MyEntity entity, double amount)  {
        this.entity = entity;
        this.amount = new BigDecimal(amount).setScale(2, RoundingMode.HALF_UP);
    }

    public int getId() {
        return id;
    }

    public MyEntity getEntity() {
        return entity;
    }

    public void setEntity(MyEntity entity) {
        this.entity = entity;
    }

    public double getAmount() {
        return amount.doubleValue();
    }

    public void setAmount(double amount) {
        this.amount = new BigDecimal(amount).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String toString() {
        return "Id: " + id + "; Entity = " + entity + ". Amount = " + amount;
    }
}