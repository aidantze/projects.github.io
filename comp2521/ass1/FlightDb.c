// Implementation of the Flight DB ADT

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "FlightDb.h"
#include "List.h"
#include "Tree.h"

// constants
#define MAX_DAY         6
#define MAX_HOUR        23
#define MAX_MIN         59
#define MAX_DURATION    60*24

struct flightDb {               // tree order by:
    Tree byFlightNumber;        // flight number -> day/hr/min
    Tree byDepartureAirportDay; // dep. Airport -> day/hr/min -> flight number
    Tree byDepartureTimes;      // day/hr/min -> flight number
};

int compare_flight_number(Record r1, Record r2);
int compare_departure_airport(Record r1, Record r2);
int compare_departure_times(Record r1, Record r2);

int compare_by_flight_number(Record r1, Record r2);
int compare_by_departure_airport_day(Record r1, Record r2);
int compare_by_departure_times(Record r1, Record r2);

Record get_dummy2(FlightDb db, int day2, int hour2, int min2);



// helper function: compare two records flight numbers
int compare_flight_number(Record r1, Record r2) {
    return strcmp(RecordGetFlightNumber(r1), RecordGetFlightNumber(r2));
}

// helper function: compare two records departure airports
int compare_departure_airport(Record r1, Record r2) {
    return strcmp(RecordGetDepartureAirport(r1), RecordGetDepartureAirport(r2));
}

// helper function: compare two records departure day, hr, min
int compare_departure_times(Record r1, Record r2) {
    int cmp_departure_day = RecordGetDepartureDay(r1) - 
                            RecordGetDepartureDay(r2);
    if (cmp_departure_day != 0) { return cmp_departure_day; }

    int cmp_departure_hr = RecordGetDepartureHour(r1) - 
                            RecordGetDepartureHour(r2);
    if (cmp_departure_hr != 0) { return cmp_departure_hr; }

    return RecordGetDepartureMinute(r1) - RecordGetDepartureMinute(r2);
}

// helper function: order the flight number tree in the following order:
// order by flight number, then by day/hr/min
int compare_by_flight_number(Record r1, Record r2) {
    int cmp_flight_number = compare_flight_number(r1, r2);
    if (cmp_flight_number != 0) { return cmp_flight_number; }
    return compare_departure_times(r1, r2);
}

// helper function: order the departure airport day tree in the following order:
// order by departure airport, then by day/hr/min, then by flight number
int compare_by_departure_airport_day(Record r1, Record r2) {
    int cmp_departure_airport = compare_departure_airport(r1, r2);
    if (cmp_departure_airport != 0) { return cmp_departure_airport; }
    return compare_by_departure_times(r1, r2);
}

// helper function: order the departure times tree in the following order:
// order by day/hr/min, then by flight number
int compare_by_departure_times(Record r1, Record r2) {
    int cmp_departure_time = compare_departure_times(r1, r2);
    if (cmp_departure_time != 0) { return cmp_departure_time; }
    return compare_flight_number(r1, r2);
}


FlightDb DbNew(void) {
    FlightDb db = malloc(sizeof(*db));
    if (db == NULL) {
        fprintf(stderr, "error: out of memory\n");
        exit(EXIT_FAILURE);
    }
    db->byFlightNumber = TreeNew(compare_by_flight_number);
    db->byDepartureAirportDay = TreeNew(compare_by_departure_airport_day);
    db->byDepartureTimes = TreeNew(compare_by_departure_times);
    return db;
}

void DbFree(FlightDb db) {
    TreeFree(db->byFlightNumber, false);
    TreeFree(db->byDepartureAirportDay, false);
    TreeFree(db->byDepartureTimes, true);
    free(db);
}


bool DbInsertRecord(FlightDb db, Record r) {
    if (TreeInsert(db->byFlightNumber, r)) {
        if (TreeInsert(db->byDepartureAirportDay, r)) {
            return TreeInsert(db->byDepartureTimes, r);
        }
    }
    return false;
}

List DbFindByFlightNumber(FlightDb db, char *flightNumber) {
    // create a dummy record to use with compare function
    Record dummy1 = RecordNew(flightNumber, "", "", 0, 0, 0, 0);
    Record dummy2 = RecordNew(flightNumber, "", "", 
                                MAX_DAY, MAX_HOUR, MAX_MIN, MAX_DURATION);
    List l = TreeSearchBetween(db->byFlightNumber, dummy1, dummy2);
    RecordFree(dummy1);
    RecordFree(dummy2);
    return l;
}

List DbFindByDepartureAirportDay(FlightDb db, char *departureAirport,
                                 int day) {
    Record dummy1 = RecordNew("", departureAirport, "", day, 0, 0, 0);
    Record dummy2 = RecordNew("", departureAirport, "", day, 
                                MAX_HOUR, MAX_MIN, MAX_DURATION);
    List l = TreeSearchBetween(db->byDepartureAirportDay, dummy1, dummy2);
    RecordFree(dummy1);
    RecordFree(dummy2);
    return l;
}

List DbFindBetweenTimes(FlightDb db, 
                        int day1, int hour1, int min1, 
                        int day2, int hour2, int min2) {
    Record dummy1 = RecordNew("", "", "", day1, hour1, min1, 0);
    Record dummy2 = get_dummy2(db, day2, hour2, min2);
    List l = TreeSearchBetween(db->byDepartureTimes, dummy1, dummy2);
    RecordFree(dummy1);
    RecordFree(dummy2);
    return l;
}

// helper function: creates a dummy record 1 minute after day2 hr2 min2 
// to use with compare function
Record get_dummy2(FlightDb db, int day2, int hour2, int min2) {
    if (min2 == 59) {
        if (hour2 == 23) {
            if (day2 == 6) { day2 = 0; } else { day2++; }
            hour2 = 0;
        } else { hour2++; }
        min2 = 0;
    } else { min2++; }
    return RecordNew("", "", "", day2, hour2, min2, MAX_DURATION);
}

Record DbFindNextFlight(FlightDb db, char *flightNumber, 
                        int day, int hour, int min) {
    Record dummy = RecordNew(flightNumber, "", "", day, hour, min, 0);
    Record r = TreeNext(db->byFlightNumber, dummy);
    RecordFree(dummy);
    return r;
}

