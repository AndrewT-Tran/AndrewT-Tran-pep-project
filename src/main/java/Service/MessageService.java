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

    // GET BY ID
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

    // GET ALL
    public List<Message> getAllMessages() {
        LOGGER.info("Fetching all messages");
        try {
            List<Message> messages = messageDao.getAllMessages();
            LOGGER.info("Fetched {} messages", messages.size());
            return messages;
        } catch (DaoException e) {
            throw new ServiceException(DB_ACCESS_ERROR_MSG, e);
        }
    }

    // GET MESSAGE BY ACCOUNT ID
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

    // CREATE MESSAGE
    public Message createMessage(Message message, Account account) {
        LOGGER.info("Creating message: {}", message);

        // Validate the message
        validateMessage(message);

        // Ensure that the account exists
        if (account == null) {
            throw new ServiceException("Account must exist when posting a new message");
        }

        try {
            // Insert the message into the database
            Message createdMessage = messageDao.createMessage(message);
            LOGGER.info("Created message: {}", createdMessage);
            return createdMessage;
        } catch (DaoException e) {
            throw new ServiceException(DB_ACCESS_ERROR_MSG, e);
        }
    }

    // UPDATE 
    public Message updateMessage(Message message) {
        LOGGER.info("Updating message: {}", message.getMessage_id());

        // Validate the message
        validateMessage(message);

        try {
            Optional<Message> existingMessageOpt = messageDao.getMessageById(message.getMessage_id());
            if (existingMessageOpt.isPresent()) {
                Message existingMessage = existingMessageOpt.get();
                message.setPosted_by(existingMessage.getPosted_by());
                message.setTime_posted_epoch(existingMessage.getTime_posted_epoch());

                boolean isUpdated = messageDao.update(message);
                if (isUpdated) {
                    return message;
                } else {
                    throw new ServiceException("Failed to update message");
                }
            } else {
                throw new ServiceException("Message not found");
            }
        } catch (DaoException e) {
            throw new ServiceException(DB_ACCESS_ERROR_MSG, e);
        }
    }

    public void deleteMessage(Message message) {
        LOGGER.info("Deleting message: {}", message);
        try {
            boolean hasDeletedMessage = messageDao.deleteMessage(message.getMessage_id());
            if (hasDeletedMessage) {
                LOGGER.info("Deleted message: {}", message);
            } else {
                throw new ServiceException("Message not found");
            }
        } catch (DaoException e) {
            throw new ServiceException(DB_ACCESS_ERROR_MSG, e);
        }
    }

    // VALIDATE MESSAGE
    private void validateMessage(Message message) {
        LOGGER.info("Validating message: {}", message);
        if (message.getMessage_text() == null || message.getMessage_text().trim().isEmpty()) {
            throw new ServiceException("Message text cannot be null or empty");
        }
        if (message.getMessage_text().length() > 254) {
            throw new ServiceException("Message text cannot exceed 254 characters");
        }
    }
}
