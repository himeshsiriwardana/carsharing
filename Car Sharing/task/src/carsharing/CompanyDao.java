package carsharing;

import javax.xml.transform.Result;
import java.util.List;

public interface CompanyDao{
    List<Company> listCompanies();
    void createCompany(String company);
}