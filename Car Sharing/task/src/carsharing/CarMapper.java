package carsharing;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CarMapper implements ResultSetMapper<Car>{

    @Override
    public Car map(ResultSet resultSet) throws SQLException {
        return new Car(resultSet.getString("name"));
    }
}
