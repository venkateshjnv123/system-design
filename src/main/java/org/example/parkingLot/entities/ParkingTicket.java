package org.example.parkingLot.entities;

import lombok.Data;

import java.util.UUID;

@Data
public class ParkingTicket {
    private UUID id;
    private Long entryTime;
    private Long exitTime;
    private Vehicle vehicle;
    private ParkingFloor.ParkingSpot parkingSpot;
    private double finalPrice;
    private State parkingState;

    public ParkingTicket(Long entryTime, Vehicle vehicle, ParkingFloor.ParkingSpot parkingSpot){
        this.id = UUID.randomUUID();
        this.entryTime = entryTime;
        this.vehicle = vehicle;
        this.parkingSpot = parkingSpot;
        this.parkingState = State.PARKED;
    }

    public enum State{
        PARKED,
        EXITING,
        UNPARKED
    }
}
