public interface IDomainLogger {
    void userTypeHasChanged(int userId, UserType oldType, UserType newType);
}
