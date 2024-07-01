package carsharing;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class dbClient {
    private final DataSource dataSource;

    public dbClient(DataSource dataSource){
        this.dataSource = dataSource;
    }

    public boolean run (String str, Object[] params){
        try(
            Connection con = dataSource.getConnection();
            PreparedStatement pstmt = con.prepareStatement(str);) {

            if(params.length != 0) {

                for (int i = 0; i < params.length; i++) {
                    if (params[i] instanceof String) {
                        pstmt.setString(i + 1, (String) params[i]);
                    } else if (params[i] instanceof Integer) {
                        pstmt.setInt(i + 1, (Integer) params[i]);
                    }
                }
            }
            pstmt.executeUpdate();
            return true;
        }
        catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public List<Map<String, Object>> extractData(String query, Object[] params) throws SQLException {
        try(Connection con = dataSource.getConnection();
        PreparedStatement pstmt = con.prepareStatement(query);){
            if(params.length != 0) {
                for (int i = 0; i < params.length; i++) {
                    if (params[i] instanceof Integer) {
                        pstmt.setInt(i + 1, (Integer) params[i]);
                    } else if (params[i] instanceof String) {
                        pstmt.setString(i + 1, (String) params[i]);
                    }
                }
            }

            ResultSet results = pstmt.executeQuery();

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


    public <T> List<T> selectForList (String query, Object[] params, ResultSetMapper<T> mapper) {
        List<T> results = new ArrayList<>();

        try(Connection con = dataSource.getConnection();
            PreparedStatement pstmt = con.prepareStatement(query);
            ){

            if(params.length != 0) {
                for (int i = 0; i < params.length; i++) {
                    if (params[i] instanceof Integer) {
                        pstmt.setInt(i + 1, (Integer) params[i]);
                    } else if (params[i] instanceof String) {
                        pstmt.setString(i + 1, (String) params[i]);
                    }
                }
            }

            ResultSet resultSetItem = pstmt.executeQuery();
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

    public <T> T select (String query, Object[] params, ResultSetMapper<T> mapper ) {
        List<T> items = selectForList(query, params, mapper);
        if(items.size() == 1){
            return items.get(0);
        } else if (items.isEmpty()) {
            return null;
        } else {
            throw new IllegalStateException("Query returned more than one object");
        }
    }
}
