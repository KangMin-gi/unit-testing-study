public class DomainLogger implements IDomainLogger {
    private final ILogger _logger;

    public DomainLogger(ILogger logger) {
        _logger = logger;
    }

    public void userTypeHasChanged(int userId, UserType oldType, UserType newType) {
        _logger.info("User " + userId + " changed type from " + oldType + " to " + newType);
    }
}