package org.example.trainticketplatform.entities;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@ToString
public class Train {
    public String id;
    public String name;
    public Map<String, Station> stations;
    public String fromStation;
    public String toStation;
    public List<Compartment> compartments;

    public Train(String name, Map<String, Station> stations, List<Compartment> compartments, String fromStation, String toStation){
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.stations = stations;
        this.compartments = compartments;
        this.toStation = toStation;
        this.fromStation = fromStation;
    }

    @Getter
    @Setter
    @ToString
    public static class Station {
        public String stnName;
        public int distance;
        public int stationOrder;

        public Station(String stnName, int distance, int stationOrder){
            this.stationOrder = stationOrder;
            this.stnName = stnName;
            this.distance = distance;
        }
    }
}
