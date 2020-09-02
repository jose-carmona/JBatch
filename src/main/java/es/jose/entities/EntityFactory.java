package es.jose.entities;

import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.Random;
import java.util.Set;
import java.util.HashSet;


public class EntityFactory {
    private static final Logger logger = Logger.getLogger("EntityFactory");

    private double maxAmount;
    private int maxDetails;
    private int partitions;

    private int leftLimit = 48; // numeral '0'
    private int rightLimit = 57; // numeral '9'
    private int targetStringLength = 12;
        
    private Random random = new Random();

    public EntityFactory(double maxAmount, int maxDetails) {
        this.maxAmount = maxAmount;
        this.maxDetails = maxDetails;
        this.partitions = 0;
    }

    public EntityFactory(double maxAmount, int maxDetails, int partitions) {
        this.maxAmount = maxAmount;
        this.maxDetails = maxDetails;
        this.partitions = partitions;
    }

    public String randomIdEntity() {
        String generatedId = random.ints(leftLimit, rightLimit + 1)
                    .limit(targetStringLength)
                    .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                    .toString();
        return generatedId;
    }

    public Detail randomDetail(MyEntity e) {
        Detail d = new Detail(e, maxAmount*random.nextDouble());
        return d;
    }

    public Set<Detail> randomDetails(MyEntity e) {
        Set<Detail> details = new HashSet<>();
        int n = random.nextInt(maxDetails);
        for(int i=0; i<n; i++) {
            details.add(randomDetail(e));
        }
        return details;
    }

    public int computePartition(String str) {
        int p = 0;
        if(partitions > 0) {
            p = (int)(Double.valueOf(str) % partitions);
        }
        return p;
    }

    public MyEntity createEntity(String id, int partition) {
        MyEntity e = new MyEntity(id, partition);
        e.setDetails(randomDetails(e));
        return e;
    }

    public MyEntity randomEntity() {
        String id = randomIdEntity();
        return createEntity(id, computePartition(id));
    }

    public MyEntity randomEntity(int partition) {
        String id = randomIdEntity();
        return createEntity(id, partition);
    }
}