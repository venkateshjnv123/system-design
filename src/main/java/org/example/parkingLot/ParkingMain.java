package org.example.parkingLot;

import org.example.parkingLot.entities.ParkingFloor;
import org.example.parkingLot.entities.Vehicle;

public class ParkingMain {
    public static void main(String[] args) {
        ParkingService parkingService = new ParkingService();
        parkingService.initialize();

        Vehicle vehicle1 = new Vehicle("HR7168",ParkingFloor.VehicleType.CAR);
        Vehicle vehicle2 = new Vehicle("HR7169", ParkingFloor.VehicleType.CAR);

        System.out.println(parkingService.parkVehicle(vehicle1).toString());
        System.out.println(parkingService.parkVehicle(vehicle2).toString());

        parkingService.unparkVehicle(vehicle1, "razorpay");
        parkingService.unparkVehicle(vehicle2, "payu");

        System.out.println(parkingService.spotsAvailable(ParkingFloor.VehicleType.CAR));
    }
}
