package Service;

import java.util.List;
import java.util.Optional;

import DAO.AccountDAO;
import DAO.DaoException;
import Model.Account;

public class AccountService {
    private final AccountDAO accountDao;

    public AccountService() {
        this.accountDao = new AccountDAO();
    }

    public AccountService(AccountDAO accountDao) {
        this.accountDao = accountDao;
    }

    public Optional<Account> getAccountById(int id) {
        try {
            return accountDao.getAccountById(id);
        } catch (DaoException e) {
            throw new ServiceException("Something went wrong while fetching account", e);
        }
    }

    public List<Account> getAllAccounts() {
        try {
            return accountDao.getAll();
        } catch (DaoException e) {
            throw new ServiceException("Sorry, there is an issue fetching the accounts", e);
        }
    }

    public Account createAccount(Account account) throws ServiceException {
        validateAccount(account);

        try {
            if (isUsernameTaken(account.getUsername())) {
                throw new ServiceException("Username already taken");
            }
            return accountDao.createAccount(account);
        } catch (DaoException e) {
            throw new ServiceException("Error creating account", e);
        }
    }

    public Optional<Account> validateLogin(Account account) throws ServiceException {
        try {
            Optional<Account> storedAccount = getAccountByUsername(account.getUsername());

            if (storedAccount.isPresent() && storedAccount.get().getPassword().equals(account.getPassword())) {
                return storedAccount;
            } else {
                return Optional.empty();
            }
        } catch (ServiceException e) {
            throw new ServiceException("Error validating login", e);
        }
    }

    public boolean updateAccount(Account account) throws ServiceException {
        try {
            return accountDao.update(account);
        } catch (DaoException e) {
            throw new ServiceException("There was an issue updating the account", e);
        }
    }

    public boolean isUsernameTaken(String username) throws ServiceException {
        try {
            return accountDao.doesUsernameExist(username);
        } catch (DaoException e) {
            throw new ServiceException("Error checking if username exists", e);
        }
    }

    private Optional<Account> getAccountByUsername(String username) throws ServiceException {
        try {
            return accountDao.getAccountByUsername(username);
        } catch (DaoException e) {
            throw new ServiceException("Exception occurred while finding account by username " + username, e);
        }
    }

    private void validateAccount(Account account) throws ServiceException {
        if (account.getUsername() == null || account.getUsername().trim().isEmpty()) {
            throw new ServiceException("Username must not be empty");
        }

        if (account.getPassword() == null || account.getPassword().length() < 4) {
            throw new ServiceException("Password must be at least 4 characters long");
        }
    }
}