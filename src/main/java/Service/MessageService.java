package Service;

import java.util.List;
import java.util.Optional;

import DAO.MessageDAO;
import Model.Message;

public class MessageService {
    private MessageDAO messageDao;

    public MessageService() {
        this.messageDao = new MessageDAO();
    }

    public MessageService(MessageDAO messageDao) {
        this.messageDao = messageDao;
    }

    public Message createMessage(Message message) throws ServiceException {
        validateMessage(message);
        message.setMessage_id(generateNewMessageId());
        messageDao.createMessage(message);
        return message;
    }

    public List<Message> getAllMessages() {
        return messageDao.getAllMessages();
    }

    public Optional<Message> getMessageById(int messageId) {
        return messageDao.getMessageById(messageId);
    }

    public void deleteMessage(int messageId) throws ServiceException {
        Optional<Message> message = getMessageById(messageId);
        if (message.isPresent()) {
            messageDao.deleteMessage(messageId);
        } else {
            throw new ServiceException("Message not found");
        }
    }

    public Message updateMessage(Message message) throws ServiceException {
        if (message.getMessage_id() <= 0) {
            throw new ServiceException("Invalid message ID");
        }
        validateMessage(message);
        messageDao.updateMessage(message);
        return message;
    }

    public List<Message> getMessagesByAccountId(int accountId) {
        return messageDao.getMessagesByAccountId(accountId);
    }

    private void validateMessage(Message message) throws ServiceException {
        if (message.getMessage_text() == null || message.getMessage_text().isBlank()) {
            throw new ServiceException("Message text cannot be blank");
        }
        if (message.getMessage_text().length() > 255) {
            throw new ServiceException("Message text exceeds 255 characters");
        }
    }

    private int generateNewMessageId() {
        return (int) (Math.random() * 10000);
    }
}