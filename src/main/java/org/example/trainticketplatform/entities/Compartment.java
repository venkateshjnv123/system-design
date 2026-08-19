package org.example.trainticketplatform.entities;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@ToString
public class Compartment {
    public String id;
    public Type type;
    public List<Seat> seats;
    public Integer cost;

    public Compartment(Type type, List<Seat> seats, int cost){
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.seats = seats;
        this.cost = cost;
    }

    public enum Type{
        SLEEPER,
        THIRDAC,
        SECONDAC
    }


    @Getter
    @Setter
    @ToString
    public static class Seat{
        public Integer seatNo;
        public SeatType seatType;
        public List<Boolean> isBooked;

        public Seat(int seatNo, SeatType seatType, int size){
            this.seatNo = seatNo;
            this.seatType = seatType;
            List<Boolean> book = new ArrayList<>();
            for (int i=0; i<size; i++){
                book.add(false);
            }
            this.isBooked = book;
        }
    }

    public enum SeatType{
        LOWER,
        MIDDLE,
        UPPER
    }
}
