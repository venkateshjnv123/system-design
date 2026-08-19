package org.example.trainticketplatform;

import org.example.trainticketplatform.entities.Booking;
import org.example.trainticketplatform.entities.Compartment;
import org.example.trainticketplatform.entities.Train;
import org.example.trainticketplatform.entities.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TicketBooking {
    public static void main(String[] args) {
        System.out.println("Train ticket booking");
        TicketBookingService ticketBookingService = new TicketBookingService();

        User user = ticketBookingService.addUser("Venky", "8688749458");
        User user2 = ticketBookingService.addUser("Venky2", "8688749459");
        System.out.println(user);
//        ticketBookingService.addUser("Venkatesh 2", "8688749458");

        Map<String, Train.Station> stations = new HashMap<>();

        Train.Station sta1 = new Train.Station("delhi", 0, 1);
        Train.Station sta2 = new Train.Station("agra", 100, 2);
        Train.Station sta3 = new Train.Station("jhansi", 163, 3);
        Train.Station sta4 = new Train.Station("bopal", 250, 4);
        Train.Station sta5 = new Train.Station("vizag", 330, 5);
        stations.put(sta1.stnName, sta1);
        stations.put(sta2.stnName, sta2);
        stations.put(sta3.stnName, sta3);
        stations.put(sta4.stnName, sta4);
        stations.put(sta5.stnName, sta5);

        Train train = ticketBookingService.addTrain("Samta","vizag", "delhi", stations);
        System.out.println(train);

        List<Train> trains = ticketBookingService.getTrains("delhi", "jhansi");
        System.out.println(trains);

        Booking booking  = ticketBookingService.bookTicket("8688749458", trains.get(0).getId(), "delhi", "jhansi", 1, Compartment.Type.THIRDAC);
        System.out.println(booking);
        Booking booking2  = ticketBookingService.bookTicket("8688749459", trains.get(0).getId(), "delhi", "jhansi", 3, Compartment.Type.SECONDAC);
        System.out.println(booking2);

        Booking booking3  = ticketBookingService.bookTicket("8688749459", trains.get(0).getId(), "jhansi", "vizag", 3, Compartment.Type.SECONDAC);
        System.out.println(booking3);
        Booking booking4  = ticketBookingService.bookTicket("8688749459", trains.get(0).getId(), "jhansi", "vizag", 3, Compartment.Type.SECONDAC);
        System.out.println(booking4);
    }
}
