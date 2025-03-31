package flightapp;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
// import java.sql.Statement;

/**
 * Runs queries against a back-end database
 */
public class Query extends QueryAbstract {
  //
  // Canned queries
  //
  private static final String FLIGHT_CAPACITY_SQL = "SELECT capacity FROM Flights WHERE fid = ?";
  private PreparedStatement flightCapacityStmt;

  private static final String CLEAR_USERS_SQL = "DELETE FROM Users_yfeng33";
  private PreparedStatement clearUserStmt;

  private static final String CLEAR_RES_SQL = "DELETE FROM Reservations_yfeng33";
  private PreparedStatement clearResStmt;

  private static final String CREATE_USER_SQL = "INSERT INTO Users_yfeng33 VALUES(?, ?, ?)";
  private PreparedStatement createUserStmt;

  private static final String CHECK_USER_SQL = "SELECT * FROM Users_yfeng33 WHERE username = ?";
  private PreparedStatement checkUserStmt;

  private static final String DIRECT_FLIGHT_SQL = "SELECT * FROM FLIGHTS " +
      "WHERE origin_city = ? AND dest_city = ? AND day_of_month = ? AND canceled = 0 " +
      "ORDER BY actual_time ASC, fid ASC LIMIT ?";
  private PreparedStatement directFlightStmt;

  private static final String INDIRECT_FLIGHT_SQL = "SELECT " +
      "f1.fid AS fid1, " +
      "f2.fid AS fid2, " +
      "f1.day_of_month AS day, " +
      "f1.carrier_id AS carrier1, " +
      "f1.flight_num AS number1, " +
      "f1.origin_city AS origin, " +
      "f1.dest_city AS intermediate, " +
      "f2.carrier_id AS carrier2, " +
      "f2.flight_num AS number2, " +
      "f2.dest_city AS dest, " +
      "f1.actual_time AS time1, " +
      "f2.actual_time AS time2, " +
      "f1.actual_time + f2.actual_time AS total_time, " +
      "f1.price AS p1, " +
      "f2.price AS p2, " +
      "f1.capacity AS cap1, " +
      "f2.capacity AS cap2 " + 
      "FROM FLIGHTS f1 JOIN FLIGHTS f2 " +
      "ON f1.dest_city = f2.origin_city  AND f1.dest_state = f2.origin_state AND f1.day_of_month = f2.day_of_month  " +
      "WHERE f1.origin_city = ? AND f2.dest_city = ? AND f1.day_of_month = ? AND f1.canceled = 0 AND f2.canceled = 0 " +
      "ORDER BY total_time ASC, fid1 ASC, fid2 ASC LIMIT ?";
  private PreparedStatement indirectFlightStmt;

  private static final String CHECK_RESERVATION_DATE_SQL = "SELECT COUNT(*) FROM Reservations_yfeng33 WHERE username = ? AND day_of_month = ?";
  private PreparedStatement checkReservationDateStmt;

  private static final String CHECK_TOTAL_RESERVATIONS_SQL = "SELECT COUNT(*) FROM Reservations_yfeng33 WHERE fl_id1 = ? OR fl_id2 = ?";
  private PreparedStatement checkTotalReservationsStmt;

  private static final String GET_NUM_RESERVATIONS_SQL = "SELECT COUNT(*) FROM Reservations_yfeng33";
  private PreparedStatement getNumReservationsStmt;

  private static final String CREATE_RESERVATION_SQL = "INSERT INTO Reservations_yfeng33 VALUES(?, ?, ?, ?, ?, ?)";
  private PreparedStatement createReservationStmt;

  private static final String FIND_RES_SQL = "SELECT * FROM Reservations_yfeng33 WHERE username = ? AND res_id = ?";
  private PreparedStatement findResStmt;

  private static final String SELECT_BALANCE_SQL = "SELECT balance FROM Users_yfeng33 WHERE username = ?";
  private PreparedStatement selectBalanceStmt;

  private static final String SELECT_PRICE_SQL = "SELECT price FROM FLIGHTS WHERE fid = ?";
  private PreparedStatement selectPriceStmt;

  private static final String UPDATE_USER_BALANCE_SQL = "UPDATE Users_yfeng33 SET balance = ? WHERE username = ?";
  private PreparedStatement updateUserBalanceStmt;

  private static final String UPDATE_RESERVATION_PAYMENT_SQL = "UPDATE Reservations_yfeng33 SET paid = 1 WHERE res_id = ?";
  private PreparedStatement updateReservationPaymentStmt;

  private static final String SELECT_RESERVATION_SQL = "SELECT * FROM Reservations_yfeng33 WHERE username = ?";
  private PreparedStatement selectReservationStmt;

  private static final String SELECT_FLIGHT_SQL = "SELECT * FROM FLIGHTS WHERE fid = ?";
  private PreparedStatement selectFlightStmt;
  //
  // Instance variables
  //
  private String currLoggedInUser;
  private List<Itinerary> itineraries;


  protected Query() throws SQLException, IOException {
    prepareStatements();
    this.currLoggedInUser = null;
    this.itineraries = new ArrayList<>();
  }

  /**
   * Clear the data in any custom tables created.
   * 
   * WARNING! Do not drop any tables and do not clear the flights table.
   */
  public void clearTables() {
    try {
      clearResStmt.executeUpdate();
      clearUserStmt.executeUpdate();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /*
   * prepare all the SQL statements in this method. Will be called on constructor.
   */
  private void prepareStatements() throws SQLException {
    flightCapacityStmt = conn.prepareStatement(FLIGHT_CAPACITY_SQL);
    clearUserStmt = conn.prepareStatement(CLEAR_USERS_SQL);
    clearResStmt = conn.prepareStatement(CLEAR_RES_SQL);
    createUserStmt = conn.prepareStatement(CREATE_USER_SQL);
    checkUserStmt = conn.prepareStatement(CHECK_USER_SQL);
    directFlightStmt = conn.prepareStatement(DIRECT_FLIGHT_SQL);
    indirectFlightStmt = conn.prepareStatement(INDIRECT_FLIGHT_SQL);
    checkReservationDateStmt = conn.prepareStatement(CHECK_RESERVATION_DATE_SQL);
    checkTotalReservationsStmt = conn.prepareStatement(CHECK_TOTAL_RESERVATIONS_SQL);
    getNumReservationsStmt = conn.prepareStatement(GET_NUM_RESERVATIONS_SQL);
    createReservationStmt = conn.prepareStatement(CREATE_RESERVATION_SQL);
    findResStmt = conn.prepareStatement(FIND_RES_SQL);
    selectBalanceStmt = conn.prepareStatement(SELECT_BALANCE_SQL);
    selectPriceStmt = conn.prepareStatement(SELECT_PRICE_SQL);
    updateUserBalanceStmt = conn.prepareStatement(UPDATE_USER_BALANCE_SQL);
    updateReservationPaymentStmt = conn.prepareStatement(UPDATE_RESERVATION_PAYMENT_SQL);
    selectReservationStmt = conn.prepareStatement(SELECT_RESERVATION_SQL);
    selectFlightStmt = conn.prepareStatement(SELECT_FLIGHT_SQL);
  }

  /* See QueryAbstract.java for javadoc */
  public String transaction_login(String username, String password) {
    if (this.currLoggedInUser != null) {
      return "User already logged in\n";
    }
    this.itineraries.clear();
    try {
      username = username.toLowerCase();
      checkUserStmt.clearParameters();
      checkUserStmt.setString(1, username);
      ResultSet rs = checkUserStmt.executeQuery();

      if (rs.next()) {
        byte[] storedPassword = rs.getBytes("password");
        // Verify the password.
        if(PasswordUtils.plaintextMatchesSaltedHash(password, storedPassword)) {
          this.currLoggedInUser = username;
          rs.close();
          return "Logged in as " + username + "\n";
        } else {
          rs.close();
          throw new IllegalArgumentException("Incorrect password.");
        }
      } else {
        rs.close();
        throw new IllegalArgumentException("Username does not exist.");
      }
    } catch (Exception e) {
      e.printStackTrace();
      return "Login failed\n";
    }
  }

  /* See QueryAbstract.java for javadoc */
  public String transaction_createCustomer(String username, String password, int initAmount) {
    username = username.toLowerCase();
    // Verify if creating such account is valid.
    try {
      if (username.length() < 1 || username.length() > 20) {
        throw new IllegalArgumentException("Invalid username provided.");
      }

      if (password.length() < 0 || password.length() > 20) {
        throw new IllegalArgumentException("Invalid password provided.");
      }

      if (initAmount < 0) {
        throw new IllegalArgumentException("Invalid balance provided.");
      }

      conn.setAutoCommit(false);
      checkUserStmt.clearParameters();
      checkUserStmt.setString(1, username);
      ResultSet rs = checkUserStmt.executeQuery();

      if (rs.next()) {
        rs.close();
        conn.rollback();
        conn.setAutoCommit(true);
        throw new IllegalArgumentException("Username already exists.");
      }
      rs.close();

      byte[] hashedPassword = PasswordUtils.saltAndHashPassword(password);
      createUserStmt.clearParameters();
      createUserStmt.setString(1, username);
      createUserStmt.setBytes(2, hashedPassword);
      createUserStmt.setInt(3, initAmount);
      createUserStmt.executeUpdate();

      conn.commit();
      conn.setAutoCommit(true);
      return "Created user " + username + "\n";
    } catch (Exception e){
      try {
        conn.rollback();
        conn.setAutoCommit(true);
      } catch (Exception ex) {}
      if (e instanceof SQLException && isDeadlock((SQLException)e)) {
        return transaction_createCustomer(username, password, initAmount);
      }
      e.printStackTrace();
      return "Failed to create user\n";
    }
    
    // try {
      
    // } catch (Exception e) {
    //   e.printStackTrace();
    //   return "Failed to create user\n";
    // }
  }

  /* See QueryAbstract.java for javadoc */
  public String transaction_search(String originCity, String destinationCity, 
                                   boolean directFlight, int dayOfMonth,
                                   int numberOfItineraries) {

    StringBuffer sb = new StringBuffer();
    this.itineraries.clear();                                
    try {
      if (dayOfMonth < 1 || dayOfMonth > 31) {
        throw new IllegalArgumentException("Invalid date");
      }

      if (numberOfItineraries < 0) {
        throw new IllegalArgumentException("Invalid itinerary number");
      }

      // one hop itineraries
      directFlightStmt.clearParameters();
      directFlightStmt.setString(1, originCity);
      directFlightStmt.setString(2, destinationCity);
      directFlightStmt.setInt(3, dayOfMonth);
      directFlightStmt.setInt(4, numberOfItineraries);
      ResultSet oneHopResults = directFlightStmt.executeQuery();

      while (oneHopResults.next()) {
        int result_fid = oneHopResults.getInt("fid");
        int result_dayOfMonth = oneHopResults.getInt("day_of_month");
        String result_carrierId = oneHopResults.getString("carrier_id");
        String result_flightNum = oneHopResults.getString("flight_num");
        String result_originCity = oneHopResults.getString("origin_city");
        String result_destCity = oneHopResults.getString("dest_city");
        int result_time = oneHopResults.getInt("actual_time");
        int result_capacity = oneHopResults.getInt("capacity");
        int result_price = oneHopResults.getInt("price");

        Flight f = new Flight(result_fid, result_dayOfMonth, result_carrierId, result_flightNum, result_originCity, result_destCity, result_time, result_capacity, result_price);
        itineraries.add(new Itinerary(f));
      }
      oneHopResults.close();

      // Continue searching on indirect flights if necessary.
      if (itineraries.size() < numberOfItineraries && !directFlight) {
        indirectFlightStmt.clearParameters();
        indirectFlightStmt.setString(1, originCity);
        indirectFlightStmt.setString(2, destinationCity);
        indirectFlightStmt.setInt(3, dayOfMonth);
        indirectFlightStmt.setInt(4, numberOfItineraries - itineraries.size());
        ResultSet indirectResults = indirectFlightStmt.executeQuery();
      
        while (indirectResults.next()) {
          int fid1 = indirectResults.getInt("fid1");
          int fid2 = indirectResults.getInt("fid2");
          int result_dayOfMonth = indirectResults.getInt("day");
          String result_carrierId1 = indirectResults.getString("carrier1");
          String result_carrierId2 = indirectResults.getString("carrier2");
          String result_flightNum1 = indirectResults.getString("number1");
          String result_flightNum2 = indirectResults.getString("number2");
          String result_originCity1 = indirectResults.getString("origin");
          String result_originCity2 = indirectResults.getString("intermediate");
          String result_destCity1 = indirectResults.getString("intermediate");
          String result_destCity2 = indirectResults.getString("dest");
          int result_time1 = indirectResults.getInt("time1");
          int result_time2 = indirectResults.getInt("time2");
          int result_capacity1 = indirectResults.getInt("cap1");
          int result_capacity2 = indirectResults.getInt("cap2");
          int result_price1 = indirectResults.getInt("p1");
          int result_price2 = indirectResults.getInt("p2");

          Flight f1 = new Flight(fid1, result_dayOfMonth, result_carrierId1, result_flightNum1, result_originCity1, result_destCity1, result_time1, result_capacity1, result_price1);
          Flight f2 = new Flight(fid2, result_dayOfMonth, result_carrierId2, result_flightNum2, result_originCity2, result_destCity2, result_time2, result_capacity2, result_price2);
          itineraries.add(new Itinerary(f1, f2));
        }
        indirectResults.close();
      }
    } catch (Exception e) {
      e.printStackTrace();
      return "Failed to search\n";
    }

    if (itineraries.size() == 0) {
      return "No flights match your selection\n";
    }

    Collections.sort(this.itineraries);
    for (int i = 0; i < this.itineraries.size(); i++) {
      this.itineraries.get(i).id = i;
      sb.append(this.itineraries.get(i).toString());
    }
    return sb.toString();
  }

  /* See QueryAbstract.java for javadoc */
  public String transaction_book(int itineraryId) {
    if (currLoggedInUser == null) {
      return "Cannot book reservations, not logged in\n";
    }

    if (itineraries.size() == 0 || itineraryId < 0 || itineraryId >= itineraries.size()) {
      return "No such itinerary " + itineraryId + "\n";
    }

    try {
      Itinerary it = this.itineraries.get(itineraryId);
      conn.setAutoCommit(false);

      // Check if the user already has a reservation on the same day.
      checkReservationDateStmt.clearParameters();
      checkReservationDateStmt.setString(1, this.currLoggedInUser);
      checkReservationDateStmt.setInt(2, it.f1.dayOfMonth);
      ResultSet rs = checkReservationDateStmt.executeQuery();
      if(rs.next()) {
        int sameDateRes = rs.getInt(1);
        if (sameDateRes > 0) {
          rs.close();
          conn.rollback();
          conn.setAutoCommit(true);
          return "You cannot book two flights in the same day\n";
        }
      }
      rs.close();

      // Check if the flight's maximum capacity would be exceeded.
      int f2Cap = 1;
      int f2Reserved = 0;
      if (it.f2 != null) {
        f2Cap = checkFlightCapacity(it.f2.fid);
        checkTotalReservationsStmt.clearParameters();
        checkTotalReservationsStmt.setInt(1, it.f2.fid);
        checkTotalReservationsStmt.setInt(2, it.f2.fid);
        ResultSet rs2 = checkTotalReservationsStmt.executeQuery();
        rs2.next();
        f2Reserved = rs2.getInt(1);
        rs2.close();
      }

      int f1Cap = checkFlightCapacity(it.f1.fid);
      checkTotalReservationsStmt.clearParameters();
      checkTotalReservationsStmt.setInt(1, it.f1.fid);
      checkTotalReservationsStmt.setInt(2, it.f1.fid);
      ResultSet rs1 = checkTotalReservationsStmt.executeQuery();
      rs1.next();
      int f1Reserved = rs1.getInt(1);
      rs1.close();

      if (f1Cap - f1Reserved <= 0 || f2Cap - f2Reserved <= 0) {
        conn.rollback();
        conn.setAutoCommit(true);
        return "Booking failed\n";
      }

      ResultSet numRes = getNumReservationsStmt.executeQuery();
      numRes.next();
      int currRes = numRes.getInt(1);
      int newResId = currRes + 1;
      numRes.close();
      createReservationStmt.clearParameters();
      createReservationStmt.setString(1, newResId + "");
      createReservationStmt.setString(2, this.currLoggedInUser);
      createReservationStmt.setInt(3, it.f1.fid);
      if (it.f2 != null) {
        createReservationStmt.setInt(4, it.f2.fid);
      } else {
        createReservationStmt.setNull(4, Types.INTEGER);
      }
      createReservationStmt.setInt(5, 0);
      createReservationStmt.setInt(6, it.f1.dayOfMonth);
      createReservationStmt.executeUpdate();

      conn.commit();
      conn.setAutoCommit(true);
      return "Booked flight(s), reservation ID: " + newResId + "\n";
    } catch (Exception e) {
      try {
        conn.rollback();
        conn.setAutoCommit(true);
      } catch (Exception ex) {}
      if (e instanceof SQLException && isDeadlock((SQLException)e)) {
        return transaction_book(itineraryId);
      }
      e.printStackTrace();
      return "Booking failed\n";
    }
  }

  /* See QueryAbstract.java for javadoc */
  public String transaction_pay(int reservationId) {
    if (currLoggedInUser == null) {
      return "Cannot pay, not logged in\n";
    }

    try {
      conn.setAutoCommit(false);

      // Check if such reservation is valid.
      findResStmt.clearParameters();
      findResStmt.setString(1, this.currLoggedInUser);
      findResStmt.setString(2, reservationId + "");
      ResultSet reserve = findResStmt.executeQuery();
      if (!reserve.next()) {
        reserve.close();
        conn.rollback();
        conn.setAutoCommit(true);
        return "Cannot find unpaid reservation " + reservationId + " under user: " + this.currLoggedInUser + "\n";
      }

      int paid = reserve.getInt("paid");
      if (paid == 1) {
        reserve.close();
        conn.rollback();
        conn.setAutoCommit(true);
        return "Cannot find unpaid reservation " + reservationId + " under user: " + this.currLoggedInUser + "\n";
      }

      // Check if balance is sufficient, then pay.
      selectBalanceStmt.clearParameters();
      selectBalanceStmt.setString(1, this.currLoggedInUser);
      ResultSet balancSet = selectBalanceStmt.executeQuery();
      balancSet.next();
      int balance = balancSet.getInt("balance");
      balancSet.close();
      int fid1 = reserve.getInt("fl_id1");
      int price = getPrice(fid1);
      int fid2 = reserve.getInt("fl_id2");
      if(!reserve.wasNull()) {
        // Two flights in the reservation
        price += getPrice(fid2);
      }
      reserve.close();

      if (balance < price) {
        conn.rollback();
        conn.setAutoCommit(true);
        return "User has only " + balance + " in account but itinerary costs " + price + "\n";
      } else {
        int currBalance = balance - price;
        updateUserBalanceStmt.clearParameters();
        updateUserBalanceStmt.setInt(1, currBalance);
        updateUserBalanceStmt.setString(2, this.currLoggedInUser);
        updateUserBalanceStmt.executeUpdate();

        updateReservationPaymentStmt.clearParameters();
        updateReservationPaymentStmt.setString(1, reservationId + "");
        updateReservationPaymentStmt.executeUpdate();

        conn.commit();
        conn.setAutoCommit(true);
        return "Paid reservation: " + reservationId + " remaining balance: " + currBalance + "\n";
      }
    } catch (Exception e) {
      try {
        conn.rollback();
        conn.setAutoCommit(true);
      } catch (Exception ex) {}
      if (e instanceof SQLException && isDeadlock((SQLException)e)) {
        return transaction_pay(reservationId);
      }
      e.printStackTrace();
      return "Failed to pay for reservation " + reservationId + "\n";
    }
  }

  /* See QueryAbstract.java for javadoc */
  public String transaction_reservations() {
    if (this.currLoggedInUser == null) {
      return "Cannot view reservations, not logged in\n";
    }

    try {
      selectReservationStmt.clearParameters();
      selectReservationStmt.setString(1, this.currLoggedInUser);
      ResultSet rs = selectReservationStmt.executeQuery();
      if (!rs.isBeforeFirst()) {
        return "No reservations found\n";
      }
      StringBuffer sb = new StringBuffer();

      while (rs.next()) {
        String resId = rs.getString("res_id");
        sb.append("Reservation " + resId + " paid: ");
        if (rs.getInt("paid") == 1) {
          sb.append("true:\n");
        } else {
          sb.append("false:\n");
        }

        int fid1 = rs.getInt("fl_id1");
        selectFlightStmt.clearParameters();
        selectFlightStmt.setInt(1, fid1);
        ResultSet flight1 = selectFlightStmt.executeQuery();
        flight1.next();
        int result_fid = flight1.getInt("fid");
        int result_dayOfMonth = flight1.getInt("day_of_month");
        String result_carrierId = flight1.getString("carrier_id");
        String result_flightNum = flight1.getString("flight_num");
        String result_originCity = flight1.getString("origin_city");
        String result_destCity = flight1.getString("dest_city");
        int result_time = flight1.getInt("actual_time");
        int result_capacity = flight1.getInt("capacity");
        int result_price = flight1.getInt("price");
        Flight f1 = new Flight(result_fid, result_dayOfMonth, result_carrierId, result_flightNum,
                              result_originCity, result_destCity, result_time, result_capacity, result_price);
        flight1.close();
        sb.append(f1.toString());

        int fid2 = rs.getInt("fl_id2");
        if (!rs.wasNull()) {
          sb.append("\n");
          selectFlightStmt.clearParameters();
          selectFlightStmt.setInt(1, fid2);
          ResultSet flight2 = selectFlightStmt.executeQuery();
          flight2.next();
          int result_fid2 = flight2.getInt("fid");
          int result_dayOfMonth2 = flight2.getInt("day_of_month");
          String result_carrierId2 = flight2.getString("carrier_id");
          String result_flightNum2 = flight2.getString("flight_num");
          String result_originCity2 = flight2.getString("origin_city");
          String result_destCity2 = flight2.getString("dest_city");
          int result_time2 = flight2.getInt("actual_time");
          int result_capacity2 = flight2.getInt("capacity");
          int result_price2 = flight2.getInt("price");
          Flight f2 = new Flight(result_fid2, result_dayOfMonth2, result_carrierId2, result_flightNum2,
                              result_originCity2, result_destCity2, result_time2, result_capacity2, result_price2);
          flight2.close();
          sb.append(f2.toString());
        }
      }
      rs.close();
      return sb.toString();
    } catch (Exception e) {
      e.printStackTrace();
      return "Failed to retrieve reservations\n";
    }
  }

  /**
   * Example utility function that uses prepared statements
   */
  private int checkFlightCapacity(int fid) throws SQLException {
    flightCapacityStmt.clearParameters();
    flightCapacityStmt.setInt(1, fid);

    ResultSet results = flightCapacityStmt.executeQuery();
    results.next();
    int capacity = results.getInt("capacity");
    results.close();

    return capacity;
  }

  /**
   * Utility function to determine whether an error was caused by a deadlock
   */
  private static boolean isDeadlock(SQLException e) {
    return "40001".equals(e.getSQLState()) || "40P01".equals(e.getSQLState());
  }

  /**
   * Helper function to get the price for a given flight id
   *
   * @param fid the flight id
   * @return price for the flight
   * @exception SQLException if no price found
   */
  private int getPrice(int fid) throws SQLException {
    selectPriceStmt.clearParameters();
    selectPriceStmt.setInt(1, fid);
    ResultSet rs = selectPriceStmt.executeQuery();
    rs.next();
    int price = rs.getInt("price");
    rs.close();
    return price;
  }

  /**
   * A class to store information about a single flight
   *
   * TODO: move this into QueryAbstract
   */
  class Flight {
    public int fid;
    public int dayOfMonth;
    public String carrierId;
    public String flightNum;
    public String originCity;
    public String destCity;
    public int time;
    public int capacity;
    public int price;

    Flight(int id, int day, String carrier, String fnum, String origin, String dest, int tm,
           int cap, int pri) {
      fid = id;
      dayOfMonth = day;
      carrierId = carrier;
      flightNum = fnum;
      originCity = origin;
      destCity = dest;
      time = tm;
      capacity = cap;
      price = pri;
    }
    
    @Override
    public String toString() {
      return "ID: " + fid + " Day: " + dayOfMonth + " Carrier: " + carrierId + " Number: "
          + flightNum + " Origin: " + originCity + " Dest: " + destCity + " Duration: " + time
          + " Capacity: " + capacity + " Price: " + price + "\n";
    }
  }

  private class Itinerary implements Comparable<Itinerary>{
    public int id;
    public Flight f1;
    public Flight f2;
    public int time;

    public Itinerary(Flight f1) {
      this.f1 = f1;
      this.time = f1.time;
    }

    public Itinerary(Flight f1, Flight f2) {
      this.f1 = f1;
      this.f2 = f2;
      this.time = f1.time + f2.time;
    }

    public String toString() {
      int numFlights = 1;
      if (this.f2 != null) {
        numFlights += 1;
      }

      String result = "Itinerary " + this.id + ": " + numFlights + " flight(s), " +
           this.time + " minutes\n" + this.f1;
      if (this.f2 != null) {
        result += this.f2;
      }
      return result;
    }

    public int compareTo(Itinerary o) {
      if (this.time != o.time) {
        return this.time - o.time;
      } else {
        if (this.f1.fid != o.f1.fid) {
          return this.f1.fid - o.f1.fid;
        }
        // In this case, second flight guaranteed to exist 
        return this.f2.fid - o.f2.fid;
      }
    }
  }
}
