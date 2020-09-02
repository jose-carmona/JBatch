package es.jose.entities;

import java.util.Set;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.Column;
import javax.persistence.OneToMany;
import javax.persistence.JoinColumn;
import javax.persistence.CascadeType;

@Entity
@Table(name = "MY_ENTITY")
public class MyEntity {

    @Id
    @Column(name = "ID")
    private String id;
    @Column(name = "PARTITION")
    private int partition;
    @Column(name = "SUM")
    private BigDecimal sum;
    
    @OneToMany(mappedBy = "entity", cascade = CascadeType.PERSIST)
    @JoinColumn(name = "ID")
    private Set<Detail> details;

    public MyEntity()  {
        this.id = "0";
        this.partition = 0;
        this.setSum(0);
    }

    public MyEntity(String id)  {
        this.id = id;
        this.partition = 0;
        this.setSum(0);
    }

    public MyEntity(String id, int partition)  {
        this.id = id;
        this.partition = partition;
        this.setSum(0);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getPartition() {
        return partition;
    }

    public void setPartition(int partition) {
        this.partition = partition;
    }

    public double getSum() {
        return sum.doubleValue();
    }

    public void setSum(double sum) {
        this.sum = new BigDecimal(sum).setScale(2, RoundingMode.HALF_UP);
    }

    public Set<Detail> getDetails(){
        return details;
    }

    public void setDetails(Set<Detail> details){
        this.details = details;
    }

    @Override
    public String toString() {
        return "id: " + this.id;
    }
}