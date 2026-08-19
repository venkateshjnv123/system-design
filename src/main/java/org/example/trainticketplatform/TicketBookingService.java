package org.example.trainticketplatform;

import org.example.trainticketplatform.entities.Booking;
import org.example.trainticketplatform.entities.Compartment;
import org.example.trainticketplatform.entities.Train;
import org.example.trainticketplatform.entities.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TicketBookingService {
    private Map<String, User> users = new ConcurrentHashMap<>();
    private Map<String, Train> trains = new ConcurrentHashMap<>();
    private Map<String, Booking> bookings = new ConcurrentHashMap<>();

    public User addUser(String name, String phone){
        if(users.containsKey(phone)){
            throw new RuntimeException("phone number already exists");
        }
        User user = new User(name, phone);
        users.put(phone, user);
        return user;
    }

    public Train addTrain(String name, String toStation, String fromStation, Map<String, Train.Station> stations){
        List<Compartment> compartments = new ArrayList<>();
        compartments.add(addCompartment(Compartment.Type.SLEEPER, 300, stations.size()));
        compartments.add(addCompartment(Compartment.Type.THIRDAC, 700, stations.size()));
        compartments.add(addCompartment(Compartment.Type.SECONDAC, 900, stations.size()));

        Train train = new Train(name, stations, compartments, fromStation, toStation);
        trains.put(train.getId(), train);
        return train;
    }

    public Compartment addCompartment(Compartment.Type type, int cost, int size){
        List<Compartment.Seat> seats = new ArrayList<>();
        for (int i = 1; i<=4; i++){
            Compartment.Seat seat = new Compartment.Seat(i, Compartment.SeatType.LOWER, size);
            seats.add(seat);
        }
        return new Compartment(type, seats, cost);
    }

    public Booking bookTicket(String phone, String trainNo, String fromStation, String toStation, int totalPas, Compartment.Type type){
        if(!users.containsKey(phone)){
           System.out.println("Phone number doesn't exists");
           return null;
        }
        Train train = trains.get(trainNo);

        List<Compartment> compartments = train.compartments.stream()
                .filter(compartment -> compartment.type.equals(type))
                .toList();

        Train.Station fromStationObj = train.stations.get(fromStation);
        Train.Station toStationObj = train.stations.get(toStation);

        int fromStationOrder = fromStationObj.getStationOrder();
        int toStationOrder = toStationObj.getStationOrder();

        List<Compartment.Seat> seats = new ArrayList<>();
        for(Compartment compartment: compartments) {
            List<Compartment.Seat> tempseats = compartment.getSeats();
            for(Compartment.Seat seat: tempseats) {
                List<Boolean> status = seat.getIsBooked();
                boolean isEmpty = true;
                for(int i = fromStationOrder-1; i<toStationOrder-1; i++){
                    if (status.get(i)) isEmpty = false;
                }
                if (isEmpty) seats.add(seat);
            }
        }

        if (seats.size() < totalPas) {
            System.out.println("Tickets not available");
            return null;
        }

        List<Compartment.Seat> finalSeats = new ArrayList<>();
        int ticketCount = 0;
        int stationFair = (toStationObj.getDistance() - fromStationObj.getDistance()) * 10;
        int totalCost = (compartments.get(0).cost + stationFair) * totalPas;
        for(Compartment.Seat seat: seats) {
            List<Boolean> status = seat.getIsBooked();
            List<Boolean> nStatus = new ArrayList<>();
            for(int i = 0; i<status.size(); i++){
                if (i>=fromStationOrder-1 && i<toStationOrder-1){
                    nStatus.add(true);
                }
                else{
                    nStatus.add(status.get(i));
                }
            }
            seat.setIsBooked(nStatus);
            finalSeats.add(seat);
            ticketCount++;
            if(ticketCount == totalPas) break;
        }


        Booking booking = new Booking(users.get(phone), trains.get(trainNo), fromStation, toStation,totalCost, totalPas, type, finalSeats);
        bookings.put(booking.getId(), booking);
        return booking;
    }

    public List<Train> getTrains(String fromStation, String toStation){
        List<Train> output = new ArrayList<>();
        for(Map.Entry<String, Train> entry: trains.entrySet()){
            Map<String, Train.Station> stations = entry.getValue().getStations();
            if (stations.get(fromStation).stationOrder < stations.get(toStation).stationOrder) {
                output.add(entry.getValue());
            }
        }

        if(output.isEmpty()) System.out.println("No trains available");
        return output;
    }
}
