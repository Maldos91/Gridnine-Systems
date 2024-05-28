package com.gridnine.testing;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        List<Flight> flights = FlightBuilder.createFlights();

        // Фильтрация перелетов по правилам
        filterByDepartureTime(flights);
        filterByArrivalBeforeDeparture(flights);
        filterByGroundTime(flights);
    }

    private static void filterByDepartureTime(List<Flight> flights) {
        System.out.println("Перелеты, вылет которых произошел до текущего момента времени:");
        for (Flight flight : flights) {
            for (int i = 0; i < flight.getSegments().size() - 1; i++) {
                if (flight.getSegments().get(i).getDepartureDate().isBefore(flight.getSegments().get(i).getArrivalDate())) {
                    System.out.println(flight);
                }
            }
        }
    }
    private static void filterByArrivalBeforeDeparture(List<Flight> flights) {
        System.out.println("Перелеты с сегментами, где дата прилета раньше даты вылета:");
        for (Flight flight : flights) {
            boolean hasInvalidSegment = false;
            for (int i = 0; i < flight.getSegments().size() - 1; i++) {
                if (flight.getSegments().get(i).getArrivalDate().isAfter(flight.getSegments().get(i + 1).getDepartureDate())) {
                    hasInvalidSegment = true;
                    break;
                }
            }
            if (hasInvalidSegment) {
                System.out.println(flight);
            }
        }
    }

    private static void filterByGroundTime(List<Flight> flights) {
        System.out.println("Перелеты, где общее время на земле превышает два часа:");
        for (Flight flight : flights) {
            long groundTime = calculateGroundTime(flight);
            if (groundTime > 2 * 60 * 60 * 1000) { // 2 часа в миллисекундах
                System.out.println(flight);
            }
        }
    }

    private static long calculateGroundTime(Flight flight) {
        long groundTime = 0;
        for (int i = 0; i < flight.getSegments().size() - 1; i++) {
            groundTime += flight.getSegments().get(i).getArrivalDate().until(flight.getSegments().get(i + 1).getDepartureDate(), ChronoUnit.MILLIS);
        }
        return groundTime;
    }
}

// Интерфейс для представления перелета
class Flight {
    private final List<Segment> segments;

    Flight(final List<Segment> seg) {
        segments = seg;
    }

    List<Segment> getSegments() {
        return segments;
    }

    @Override
    public String toString() {
        return segments.stream().map(Object::toString)
                .collect(Collectors.joining(" "));
    }
}

// Класс для представления сегмента перелета
class Segment {
    private final LocalDateTime departureDate;

    private final LocalDateTime arrivalDate;

    Segment(final LocalDateTime dep, final LocalDateTime arr) {
        departureDate = Objects.requireNonNull(dep);
        arrivalDate = Objects.requireNonNull(arr);
    }

    LocalDateTime getDepartureDate() {
        return departureDate;
    }

    LocalDateTime getArrivalDate() {
        return arrivalDate;
    }

    @Override
    public String toString() {
        DateTimeFormatter fmt =
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        return '[' + departureDate.format(fmt) + '|' + arrivalDate.format(fmt)
                + ']';
    }
}

// Фабрика для создания тестовых данных
class FlightBuilder {
static List<Flight> createFlights() {
        LocalDateTime threeDaysFromNow = LocalDateTime.now().plusDays(3);
        return Arrays.asList(
        //A normal flight with two hour duration
        createFlight(threeDaysFromNow, threeDaysFromNow.plusHours(2)),
        //A normal multi segment flight
        createFlight(threeDaysFromNow, threeDaysFromNow.plusHours(2),
        threeDaysFromNow.plusHours(3), threeDaysFromNow.plusHours(5)),
        //A flight departing in the past
        createFlight(threeDaysFromNow.minusDays(6), threeDaysFromNow),
        //A flight that departs before it arrives
        createFlight(threeDaysFromNow, threeDaysFromNow.minusHours(6)),
        //A flight with more than two hours ground time
        createFlight(threeDaysFromNow, threeDaysFromNow.plusHours(2),
        threeDaysFromNow.plusHours(5), threeDaysFromNow.plusHours(6)),
        //Another flight with more than two hours ground time
        createFlight(threeDaysFromNow, threeDaysFromNow.plusHours(2),
        threeDaysFromNow.plusHours(3), threeDaysFromNow.plusHours(4),
        threeDaysFromNow.plusHours(6), threeDaysFromNow.plusHours(7)));
        }

private static Flight createFlight(final LocalDateTime... dates) {
        if ((dates.length % 2) != 0) {
        throw new IllegalArgumentException(
        "you must pass an even number of dates");
        }
        List<Segment> segments = new ArrayList<>(dates.length / 2);
        for (int i = 0; i < (dates.length - 1); i += 2) {
        segments.add(new Segment(dates[i], dates[i + 1]));
        }
        return new Flight(segments);
        }
}