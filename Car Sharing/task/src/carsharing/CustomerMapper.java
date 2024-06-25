package carsharing;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomerMapper implements ResultSetMapper<Customer>{

    @Override
    public Customer map(ResultSet resultSet) throws SQLException {
        return new Customer(resultSet.getString("name"));
    }
}
