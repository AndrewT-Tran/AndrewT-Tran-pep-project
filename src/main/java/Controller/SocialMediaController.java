package Controller;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import DAO.DaoException;
import Model.Account;
import Model.Message;
import Service.AccountService;
import Service.MessageService;
import Service.ServiceException;
import io.javalin.Javalin;
import io.javalin.http.Context;







public class SocialMediaController {

    private final AccountService accountService;
    private final MessageService messageService;

    // Initialize the account and message instances
    public SocialMediaController() {
        this.accountService = new AccountService();
        this.messageService = new MessageService();
    }

    public Javalin startAPI() throws ServiceException {
        Javalin app = Javalin.create();
        app.post("/register", this::registerAccount);
        app.post("/login", this::loginAccount);
        app.post("/messages", this::createMessage);
        app.get("/messages", this::getAllMessages);
        app.get("/messages/{message_id}", this::getMessageById);
        app.delete("/messages/{message_id}", this::deleteMessageById);
        app.patch("/messages/{message_id}", this::updateMessageById);
        app.get("/accounts/{account_id}/messages", this::getMessagesByAccountId);

        return app;
    }

    // USER REG
    private void registerAccount(Context ctx) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Account account = mapper.readValue(ctx.body(), Account.class);

        // Validation
        if (account.getUsername() == null || account.getUsername().isEmpty()) {
            ctx.status(400).result("");
            return;
        }

        if (account.getPassword() == null || account.getPassword().length() < 4) {
            ctx.status(400).result("");
            return;
        }

        if (accountService.isUsernameTaken(account.getUsername())) {
            ctx.status(400).result("");
            return;
        }

        try {
            Account registeredAccount = accountService.createAccount(account);
            ctx.status(200).json(registeredAccount);
        } catch (ServiceException e) {
            ctx.status(400).result("Error creating account");
        }
    }

    // LOGIN
    private void loginAccount(Context ctx) throws JsonProcessingException, DaoException {
        ObjectMapper mapper = new ObjectMapper();
        Account account = mapper.readValue(ctx.body(), Account.class);

        try {
            Optional<Account> loggedInAccount = accountService.validateLogin(account);
            if (loggedInAccount.isPresent()) {
                ctx.sessionAttribute("logged_in_account", loggedInAccount.get());
                ctx.status(200).json(loggedInAccount.get());
            } else {
                ctx.status(401).result("");
            }
        } catch (ServiceException e) {
            ctx.status(401).result("");
        }
    }

    // Create new Message
    private void createMessage(Context ctx) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Message mappedMessage = mapper.readValue(ctx.body(), Message.class);

        // Validation
        if (mappedMessage.getMessage_text() == null || mappedMessage.getMessage_text().trim().isEmpty()) {
            ctx.status(400).result("");
            return;
        }

        if (mappedMessage.getMessage_text().length() > 254) {
            ctx.status(400).result("");
            return;
        }

        try {
            Optional<Account> account = accountService.getAccountById(mappedMessage.getPosted_by());
            if (account.isPresent()) {
                Message message = messageService.createMessage(mappedMessage, account.get());
                ctx.status(200).json(message);
            } else {
                ctx.status(400).result("");
            }
        } catch (ServiceException e) {
            ctx.status(400).result("Error creating message");
        }
    }

    // Get all messages
    private void getAllMessages(Context ctx) {
        List<Message> messages = messageService.getAllMessages();
        ctx.status(200).json(messages);
    }

    // Get message by id
    private void getMessageById(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("message_id"));
            Optional<Message> message = messageService.getMessageById(id);
            if (message.isPresent()) {
                ctx.status(200).json(message.get());
            } else {
                ctx.status(200).result("");
            }
        } catch (NumberFormatException e) {
            ctx.status(400).result("Invalid message ID");
        } catch (ServiceException e) {
            ctx.status(400).result("Error retrieving message");
        }
    }

    // Delete message by id
    private void deleteMessageById(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("message_id"));
            Optional<Message> message = messageService.getMessageById(id);
            if (message.isPresent()) {
                messageService.deleteMessage(message.get());
                ctx.status(200).json(message.get());
            } else {
                ctx.status(200).result("");
            }
        } catch (NumberFormatException e) {
            ctx.status(200).result("Invalid message ID");
        } catch (ServiceException e) {
            ctx.status(200).result("Error deleting message");
        }
    }

    // Update message by id
    private void updateMessageById(Context ctx) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Message mappedMessage = mapper.readValue(ctx.body(), Message.class);

        // Validation
        if (mappedMessage.getMessage_text() == null || mappedMessage.getMessage_text().trim().isEmpty()) {
            ctx.status(400).result("");
            return;
        }

        if (mappedMessage.getMessage_text().length() > 254) {
            ctx.status(400).result("");
            return;
        }

        try {
            int id = Integer.parseInt(ctx.pathParam("message_id"));
            mappedMessage.setMessage_id(id);
            Optional<Message> existingMessage = messageService.getMessageById(id);
            if (existingMessage.isPresent()) {
                Message messageUpdated = messageService.updateMessage(mappedMessage);
                ctx.status(200).json(messageUpdated);
            } else {
                ctx.status(400).result("");
            }
        } catch (NumberFormatException e) {
            ctx.status(400).result("Invalid message ID");
        } catch (ServiceException e) {
            ctx.status(400).result("");
        }
    }

    // Get messages by account id
    private void getMessagesByAccountId(Context ctx) {
        try {
            int accountId = Integer.parseInt(ctx.pathParam("account_id"));
            List<Message> messages = messageService.getMessagesByAccountId(accountId);
            ctx.status(200).json(messages);
        } catch (NumberFormatException e) {
            ctx.status(200).result("Invalid account ID");
        } catch (ServiceException e) {
            ctx.status(200).result("Error retrieving messages");
        }
    }
}
