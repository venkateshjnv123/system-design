package org.example.trainticketplatform.entities;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@ToString
public class Booking {
    public String id;
    public String user;
    public String train;
    public String fromStation;
    public String toStation;
    public Integer totalCost;
    public Integer totalPassenger;
    public List<Compartment.Seat> seats;
    public Compartment.Type compartmentType;

    public Booking(User user, Train train, String fromStation, String toStation, int totalCost, int totalPassenger, Compartment.Type compartmentType, List<Compartment.Seat> seats){
        this.id = UUID.randomUUID().toString();
        this.user = user.getId();
        this.train = train.getId();
        this.fromStation = fromStation;
        this.toStation = toStation;
        this.totalCost = totalCost;
        this.totalPassenger = totalPassenger;
        this.compartmentType = compartmentType;
        this.seats = seats;
    }
}
