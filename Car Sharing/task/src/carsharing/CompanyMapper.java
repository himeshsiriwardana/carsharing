package carsharing;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CompanyMapper implements ResultSetMapper<Company>{

    @Override
    public Company map(ResultSet resultSet) throws SQLException {
        return new Company(resultSet.getString("name"));
    }
}
