package Service;

import Model.Account;
import DAO.AccountDAO;
import DAO.DaoException;

import java.util.List;
import java.util.Optional;

public class AccountService {
    private AccountDAO accountDao;

    public AccountService() {
        this.accountDao = new AccountDAO();
    }

    public AccountService(AccountDAO accountDao) {
        this.accountDao = accountDao;
    }

    public AccountDAO getAccountDao() {
        return accountDao;
    }

    public void setAccountDao(AccountDAO accountDao) {
        this.accountDao = accountDao;
    }

    public List<Account> getAllAccounts() throws ServiceException {
        try {
            List<Account> accounts = accountDao.getAll();
            return accounts;
        } catch (DaoException e) {
            throw new ServiceException("Exception occurred while fetching accounts", e);
        }
    }

    public Account createAccount(Account account) throws ServiceException {
        if (isUsernameTaken(account.getUsername())) {
            throw new ServiceException("Username already taken");
        }

        account.setAccount_id(generateNewAccountId());
        saveAccountToDatabase(account);
        return account;
    }

    public Optional<Account> validateLogin(Account account) throws ServiceException {
        Optional<Account> storedAccount = getAccountByUsername(account.getUsername());

        if (storedAccount.isPresent() && storedAccount.get().getPassword().equals(account.getPassword())) {
            return storedAccount;
        } else {
            return Optional.empty();
        }
    }

    public boolean accountExists(int accountId) {
        return getAccountById(accountId).isPresent();
    }

    private boolean isUsernameTaken(String username) {
        return false;
    }

    private int generateNewAccountId() {
        return 1;
    }

    private void saveAccountToDatabase(Account account) {
    }

    private Optional<Account> getAccountByUsername(String username) {
        return Optional.empty();
    }

    private Optional<Account> getAccountById(int accountId) {
        return Optional.empty();
    }
}