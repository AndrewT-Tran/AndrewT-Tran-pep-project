package Service;

import Model.Account;
import DAO.AccountDAO;
import DAO.DaoException;

import java.util.Optional;

public class AccountService {
    private final AccountDAO accountDao;

    // Initialize the AccountDAO
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

    public Account createAccount(Account account) throws ServiceException {
        try {
            if (isUsernameTaken(account.getUsername())) {
                throw new ServiceException("Username already taken");
            }
            return accountDao.createAccount(account);
        } catch (DaoException e) {
            throw new ServiceException("Error creating account", e);
        }
    }

    public Optional<Account> validateLogin(Account account) throws ServiceException, DaoException {
        Optional<Account> storedAccount = getAccountByUsername(account.getUsername());

        if (storedAccount.isPresent() && storedAccount.get().getPassword().equals(account.getPassword())) {
            return storedAccount;
        } else {
            return Optional.empty();
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
}
