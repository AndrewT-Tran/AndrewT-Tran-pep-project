package Service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import DAO.DaoException;
import DAO.MessageDAO;
import Model.Account;
import Model.Message;

public class MessageService {
    private final MessageDAO messageDao;
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageService.class);
    private static final String DB_ACCESS_ERROR_MSG = "Error accessing the database";

    public MessageService() {
        this.messageDao = new MessageDAO();
    }

    public MessageService(MessageDAO messageDao) {
        this.messageDao = messageDao;
    }

    public Optional<Message> getMessageById(int id) {
        LOGGER.info("Fetching message with ID: {}", id);
        try {
            Optional<Message> message = messageDao.getMessageById(id);
            if (!message.isPresent()) {
                throw new ServiceException("Message not found");
            }
            LOGGER.info("Fetched message: {}", message.orElse(null));
            return message;
        } catch (DaoException e) {
            throw new ServiceException(DB_ACCESS_ERROR_MSG, e);
        }
    }

    public List<Message> getAllMessages() {
        LOGGER.info("Fetching all messages");
        try {
            List<Message> messages = messageDao.getAll();
            LOGGER.info("Fetched {} messages", messages.size());
            return messages;
        } catch (DaoException e) {
            throw new ServiceException(DB_ACCESS_ERROR_MSG, e);
        }
    }

    public List<Message> getMessagesByAccountId(int accountId) {
        LOGGER.info("Fetching messages posted by account ID: {}", accountId);
        try {
            List<Message> messages = messageDao.getMessagesByAccountId(accountId);
            LOGGER.info("Fetched {} messages", messages.size());
            return messages;
        } catch (DaoException e) {
            throw new ServiceException(DB_ACCESS_ERROR_MSG, e);
        }
    }

    public Message createMessage(Message message, Account account) {
        LOGGER.info("Creating message: {}", message);

        // Validate the message
        validateMessage(message);

        // Check account permission
        checkAccountPermission(account, message.getPosted_by());

        try {
            Message createdMessage = messageDao.insert(message);
            LOGGER.info("Created message: {}", createdMessage);
            return createdMessage;
        } catch (DaoException e) {
            throw new ServiceException(DB_ACCESS_ERROR_MSG, e);
        }
    }

    public Message updateMessage(Message message) {
        LOGGER.info("Updating message: {}", message.getMessage_id());

        Optional<Message> retrievedMessage = this.getMessageById(message.getMessage_id());
        if (!retrievedMessage.isPresent()) {
            throw new ServiceException("Message not found");
        }

        retrievedMessage.get().setMessage_text(message.getMessage_text());
        validateMessage(retrievedMessage.get());

        try {
            messageDao.update(retrievedMessage.get());
            LOGGER.info("Updated message: {}", retrievedMessage.get());
            return retrievedMessage.get();
        } catch (DaoException e) {
            throw new ServiceException(DB_ACCESS_ERROR_MSG, e);
        }
    }

    public void deleteMessage(Message message) {
        LOGGER.info("Deleting message: {}", message);
        try {
            boolean hasDeletedMessage = messageDao.delete(message);
            if (hasDeletedMessage) {
                LOGGER.info("Deleted message {}", message);
            } else {
                throw new ServiceException("Message to delete not found");
            }
        } catch (DaoException e) {
            throw new ServiceException(DB_ACCESS_ERROR_MSG, e);
        }
    }

    private void validateMessage(Message message) {
        LOGGER.info("Validating message: {}", message);
        if (message.getMessage_text() == null || message.getMessage_text().trim().isEmpty()) {
            throw new ServiceException("Message text cannot be null or empty");
        }
        if (message.getMessage_text().length() > 254) {
            throw new ServiceException("Message text cannot exceed 254 characters");
        }
    }

    private void checkAccountPermission(Account account, int postedBy) {
        LOGGER.info("Checking account permissions for messages");
        if (account.getAccount_id() != postedBy) {
            throw new ServiceException("Account not authorized to modify this message");
        }
    }


}
