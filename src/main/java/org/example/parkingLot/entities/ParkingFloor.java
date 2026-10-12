package org.example.parkingLot.entities;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ParkingFloor {
    private UUID id;
    private List<ParkingSpot> parkingSpotList;

    @Data
    public static class ParkingSpot {
        private UUID id;
        private VehicleType type;
        private boolean isOccupied;

        public ParkingSpot(VehicleType vehicleType){
            this.id = UUID.randomUUID();
            this.type = vehicleType;
            this.isOccupied = false;
        }
    }

    public ParkingFloor(List<ParkingSpot> parkingSpotList){
        this.id = UUID.randomUUID();
        this.parkingSpotList = parkingSpotList;
    }


    public enum VehicleType {
        CAR,
        BIKE,
        TRUCK;
    }
}
