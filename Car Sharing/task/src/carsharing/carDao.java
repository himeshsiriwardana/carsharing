package carsharing;

import java.util.List;

public interface carDao{
    List<Car> listCars();
    void createCar(String carName);


}
