import java.util.HashMap;
import java.util.Map;

public class Database {
    private final String connectionString;
    private Map<Integer, User> userMap = new HashMap<>();
    private Company company = null;

    public Database(String connectionString) {
        this.connectionString = connectionString;
    }

    public Object[] getUserById(int userId) {
        User user = userMap.get(userId);
        return new Object[]{user.getUserId(), user.getEmail(), user.getType()};
    }

    public Object[] getCompany() {
        return new Object[]{company.getDomainName(), company.getNumberOfEmployees()};
    }

    public void saveCompany(Company company) {
        this.company = company;
    }

    public void saveUser(User user) {
        userMap.put(user.getUserId(), user);
    }
}
