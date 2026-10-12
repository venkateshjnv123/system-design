package org.example.parkingLot.entities;

import lombok.Data;

import java.util.UUID;

@Data
public class ParkingCharges {
    private ParkingFloor.VehicleType type;
    private int fixedCost;
    private int hourlyCost;

    public ParkingCharges(ParkingFloor.VehicleType type, int fixedCost, int hourlyCost){
        this.type = type;
        this.fixedCost = fixedCost;
        this.hourlyCost = hourlyCost;
    }
}
