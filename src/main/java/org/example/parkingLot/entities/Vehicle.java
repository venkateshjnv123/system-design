package org.example.parkingLot.entities;

import lombok.Data;

import java.util.UUID;

@Data
public class Vehicle {
    private UUID id;
    private String number;
    private ParkingFloor.VehicleType type;
    private UUID parkingTicketId;

    public Vehicle(String number, ParkingFloor.VehicleType type){
        this.id = UUID.randomUUID();
        this.number = number;
        this.type = type;
    }
}
