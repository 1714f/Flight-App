This is a Java-based flight booking system utilizing JDBC and PostgreSQL for database management. The application supports user registration, login, flight search, booking, and payment processing, with secure authentication through hashed and salted passwords. Transactions are ACID-compliant, ensuring data consistency. Comprehensive JUnit tests and a custom test harness validate core functionalities.

To run the program (in the project directory), do:

$ mvn clean compile assembly:single

$ java -jar target/FlightApp-1.0-jar-with-dependencies.jar

or (run directly without first creating a jar):

$ mvn compile exec:java

Then you will see the command-line interface.


To run the tests, for example:

$ mvn test -Dtest.cases="cases/transaction/"