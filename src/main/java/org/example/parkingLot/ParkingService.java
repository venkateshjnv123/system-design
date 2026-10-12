package org.example.parkingLot;

import org.example.parkingLot.entities.ParkingCharges;
import org.example.parkingLot.entities.ParkingFloor;
import org.example.parkingLot.entities.ParkingTicket;
import org.example.parkingLot.entities.Vehicle;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class ParkingService {
    Map<UUID, ParkingFloor> parkingFloorMap = new ConcurrentHashMap<>();
    Map<ParkingFloor.VehicleType, ParkingCharges> parkingChargesMap = new ConcurrentHashMap<>();
    List<ParkingFloor.VehicleType> vehicleTypes = new ArrayList<>(List.of(ParkingFloor.VehicleType.CAR, ParkingFloor.VehicleType.BIKE, ParkingFloor.VehicleType.TRUCK));
    Map<UUID, ParkingTicket> parkingTicketMap = new ConcurrentHashMap<>();

    // vehicle numbers currently inside the lot; add() is atomic so it doubles as the duplicate-park guard
    Set<String> parkedVehicles = ConcurrentHashMap.newKeySet();

    // one lock per vehicle type: car gates don't block bike gates, same-type gates still serialize
    Map<ParkingFloor.VehicleType, ReentrantLock> spotLocks = new EnumMap<>(ParkingFloor.VehicleType.class);

    PaymentService paymentService = new PaymentService();

    public ParkingService(){
        for (ParkingFloor.VehicleType type: ParkingFloor.VehicleType.values()){
            spotLocks.put(type, new ReentrantLock());
        }
    }

    void initialize(){
        List<ParkingFloor.ParkingSpot> parkingSpotList = new ArrayList<>();
        for(ParkingFloor.VehicleType vehicleType: vehicleTypes){
            for(int i=0; i<2; i++){
                parkingSpotList.add(new ParkingFloor.ParkingSpot(vehicleType));
            }
        }
        ParkingFloor parkingFloor1 = new ParkingFloor(parkingSpotList);
        parkingFloorMap.put(parkingFloor1.getId(), parkingFloor1);


        List<ParkingFloor.ParkingSpot> parkingSpotList2 = new ArrayList<>();
        for(ParkingFloor.VehicleType vehicleType: vehicleTypes){
            for(int i=0; i<2; i++){
                parkingSpotList2.add(new ParkingFloor.ParkingSpot(vehicleType));
            }
        }
        ParkingFloor parkingFloor2 = new ParkingFloor(parkingSpotList2);
        parkingFloorMap.put(parkingFloor2.getId(), parkingFloor2);

        addCharges(ParkingFloor.VehicleType.CAR, 100, 30);
        addCharges(ParkingFloor.VehicleType.BIKE, 50, 10);
        addCharges(ParkingFloor.VehicleType.TRUCK, 200, 50);

        System.out.println("Parking lot is initialized");
        System.out.println(parkingFloorMap.toString());
    }

    void addCharges(ParkingFloor.VehicleType type, int fixedCost, int hourlyCost){
        ParkingCharges parkingCharges = new ParkingCharges(type, fixedCost, hourlyCost);
        parkingChargesMap.put(parkingCharges.getType(), parkingCharges);
    }

    // find + mark occupied must be one atomic step, so both happen under the type lock
    private ParkingFloor.ParkingSpot allocateSpot(ParkingFloor.VehicleType type){
        ReentrantLock lock = spotLocks.get(type);
        lock.lock();
        try {
            for (ParkingFloor parkingFloor: parkingFloorMap.values()){
                for (ParkingFloor.ParkingSpot spot: parkingFloor.getParkingSpotList()){
                    if (spot.getType() == type && !spot.isOccupied()){
                        spot.setOccupied(true);
                        return spot;
                    }
                }
            }
            return null;
        } finally {
            lock.unlock();
        }
    }

    private void releaseSpot(ParkingFloor.ParkingSpot spot){
        ReentrantLock lock = spotLocks.get(spot.getType());
        lock.lock();
        try {
            spot.setOccupied(false);
        } finally {
            lock.unlock();
        }
    }

    public ParkingTicket parkVehicle(Vehicle vehicle){
        if (!parkedVehicles.add(vehicle.getNumber())){
            throw new IllegalArgumentException("Vehicle is already parked");
        }

        ParkingFloor.ParkingSpot spot = allocateSpot(vehicle.getType());
        if (spot == null){
            parkedVehicles.remove(vehicle.getNumber());
            throw new IllegalArgumentException("Parking spot is unavailable");
        }

        System.out.println("Parking successfull: " + vehicle.getNumber());

        ParkingTicket parkingTicket = new ParkingTicket(System.currentTimeMillis(), vehicle, spot);
        parkingTicketMap.put(parkingTicket.getId(), parkingTicket);
        vehicle.setParkingTicketId(parkingTicket.getId());
        return parkingTicket;
    }

    public void unparkVehicle(Vehicle vehicle, String paymentMethod){
        UUID ticketId = vehicle.getParkingTicketId();
        ParkingTicket parkingTicket = ticketId == null ? null : parkingTicketMap.get(ticketId);
        if (parkingTicket == null){
            throw new IllegalArgumentException("Vehicle is not parked, parking ticket is unavailable");
        }

        // claim the ticket so two exit gates can't both charge the same vehicle
        synchronized (parkingTicket){
            if (parkingTicket.getParkingState() != ParkingTicket.State.PARKED){
                throw new IllegalArgumentException("Vehicle is already exiting or exited");
            }
            parkingTicket.setParkingState(ParkingTicket.State.EXITING);
        }

        try {
            long exitTime = System.currentTimeMillis();
            long totalTime = exitTime - parkingTicket.getEntryTime();
            long totalMinutes = (totalTime + 60_000 - 1) / 60_000;
            long extraHours = Math.max(0, (totalMinutes - 240 + 59)/60);
            ParkingCharges charges = parkingChargesMap.get(vehicle.getType());
            double parkingCharge = charges.getFixedCost() + charges.getHourlyCost() * extraHours;

            // slow external call: no lock held here
            boolean paymentDone = paymentService.pay(parkingCharge, paymentMethod);
            if (!paymentDone){
                throw new IllegalArgumentException("Unable to process payment please try again");
            }

            parkingTicket.setExitTime(exitTime);
            parkingTicket.setFinalPrice(parkingCharge);
            releaseSpot(parkingTicket.getParkingSpot());
            parkedVehicles.remove(vehicle.getNumber());
            vehicle.setParkingTicketId(null);
            parkingTicket.setParkingState(ParkingTicket.State.UNPARKED);
            System.out.println("Parking charge collected: " + parkingCharge + " and vehicle unparked: " + vehicle.getNumber());
        } catch (RuntimeException e){
            // payment failed: vehicle is still inside, let the user retry
            if (parkingTicket.getParkingState() == ParkingTicket.State.EXITING){
                parkingTicket.setParkingState(ParkingTicket.State.PARKED);
            }
            throw e;
        }
    }

    public int spotsAvailable(ParkingFloor.VehicleType type){
        ReentrantLock lock = spotLocks.get(type);
        lock.lock();
        try {
            int answer = 0;
            for (ParkingFloor parkingFloor: parkingFloorMap.values()){
                for (ParkingFloor.ParkingSpot spot: parkingFloor.getParkingSpotList()){
                    if (spot.getType() == type && !spot.isOccupied()){
                        answer++;
                    }
                }
            }
            return answer;
        } finally {
            lock.unlock();
        }
    }
}
