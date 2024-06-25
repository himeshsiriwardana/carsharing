package carsharing;

public class Company {
    private String name;
    
    public Company (String name){
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
