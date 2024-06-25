package carsharing;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class dbClient {
    private final DataSource dataSource;

    public dbClient(DataSource dataSource){
        this.dataSource = dataSource;
    }

    public boolean run (String str){
        try(
            Connection con = dataSource.getConnection();
            Statement statement = con.createStatement();) {
            statement.executeUpdate(str);
            return true;
        }
        catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public List<Map<String, Object>> extractData(String query) throws SQLException {
        try(Connection con = dataSource.getConnection();
        Statement statement = con.createStatement();
        ResultSet results = statement.executeQuery(query)){
            List<Map<String, Object>> result = new ArrayList<>();

            while(results.next()){
                Map<String, Object> row = new HashMap<>();
                int columnCount = results.getMetaData().getColumnCount();

                for(int i = 1; i <= columnCount; i++) {
                    String columnName = results.getMetaData().getColumnLabel(i);
                    Object columnValue = results.getObject(i);
                    row.put(columnName, columnValue);

                }
//                System.out.println(row);
                  result.add(row);
            }

            return result;
        }

        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }


    public <T> List<T> selectForList (String query, ResultSetMapper<T> mapper) {
        List<T> results = new ArrayList<>();

        try(Connection con = dataSource.getConnection();
        Statement statement = con.createStatement();
            ResultSet resultSetItem = statement.executeQuery(query)){
            while (resultSetItem.next()){
                T result = mapper.map(resultSetItem);
                results.add(result);
            }
        }
        catch(SQLException e){
            e.printStackTrace();
        }

        return results;

    }

    public <T> T select (String query, ResultSetMapper<T> mapper ) {
        List<T> items = selectForList(query, mapper);
        if(items.size() == 1){
            return items.get(0);
        } else if (items.isEmpty()) {
            return null;
        } else {
            throw new IllegalStateException("Query returned more than one object");
        }
    }
}
