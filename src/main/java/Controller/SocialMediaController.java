package Controller;

import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;

import Model.Account;
import Model.Message;
import Service.AccountService;
import Service.MessageService;
import Service.ServiceException;
import io.javalin.Javalin;

public class SocialMediaController {
    private AccountService accountService = new AccountService();
    private MessageService messageService = new MessageService();
    private ObjectMapper objectMapper = new ObjectMapper();

    public Javalin startAPI() throws ServiceException {
        Javalin app = Javalin.create();

        app.post("/register", ctx -> {
            try {
                Account account = objectMapper.readValue(ctx.body(), Account.class);
                Account registeredAccount = accountService.createAccount(account);
                ctx.json(objectMapper.writeValueAsString(registeredAccount));
            } catch (Exception e) {
                ctx.status(400).result("Error processing registration: " + e.getMessage());
            }
        });

        app.post("/login", ctx -> {
            try {
                Account account = objectMapper.readValue(ctx.body(), Account.class);
                if (account.getUsername() == null || account.getUsername().isBlank() ||
                        account.getPassword() == null || account.getPassword().isBlank()) {
                    ctx.status(400).result("Username and password cannot be blank.");
                } else {
                    Optional<Account> loggedInAccount = accountService.validateLogin(account);
                    if (loggedInAccount.isPresent()) {
                        ctx.status(200).json(loggedInAccount.get());
                    } else {
                        ctx.status(401).result("Invalid username or password.");
                    }
                }
            } catch (Exception e) {
                ctx.status(400).result("Error processing login: " + e.getMessage());
            }
        });

        app.post("/messages", ctx -> {
            try {
                Message message = objectMapper.readValue(ctx.body(), Message.class);
                if (message.getMessage_text() == null || message.getMessage_text().isBlank() ||
                        message.getMessage_text().length() > 255 || message.getPosted_by() <= 0 ||
                        !accountService.accountExists(message.getPosted_by())) {
                    ctx.status(400).result("Invalid message details.");
                } else {
                    Message createdMessage = messageService.createMessage(message);
                    ctx.status(201).json(createdMessage); // Use 201 Created for new resource
                }
            } catch (Exception e) {
                ctx.status(400).result("Error processing message creation: " + e.getMessage());
            }
        });

        app.get("/messages", ctx -> {
            try {
                ctx.status(200).json(messageService.getAllMessages());
            } catch (Exception e) {
                ctx.status(500).result("Error retrieving messages: " + e.getMessage());
            }
        });

        app.get("/messages/{id}", ctx -> {
            try {
                int id = Integer.parseInt(ctx.pathParam("id"));
                Optional<Message> message = messageService.getMessageById(id);
                if (message.isPresent()) {
                    ctx.json(message.get());
                } else {
                    ctx.status(200).result(""); // Return 200 status with empty body if not found
                }
            } catch (NumberFormatException e) {
                ctx.status(400).result("Invalid message ID");
            }
        });

        app.delete("/messages/{id}", ctx -> {
            try {
                int id = Integer.parseInt(ctx.pathParam("id"));
                messageService.deleteMessage(id);
                ctx.status(200).result("Message deleted");
            } catch (ServiceException e) {
                ctx.status(404).result(e.getMessage());
            } catch (NumberFormatException e) {
                ctx.status(400).result("Invalid message ID");
            }
        });

        app.patch("/messages/{id}", ctx -> {
            try {
                int id = Integer.parseInt(ctx.pathParam("id"));
                Message mappedMessage = objectMapper.readValue(ctx.body(), Message.class);
                mappedMessage.setMessage_id(id);

                if (mappedMessage.getMessage_text() == null || mappedMessage.getMessage_text().isBlank() ||
                        mappedMessage.getMessage_text().length() > 255 || mappedMessage.getPosted_by() <= 0 ||
                        !accountService.accountExists(mappedMessage.getPosted_by())) {
                    ctx.status(400).result("Invalid message details.");
                } else {
                    Message messageUpdated = messageService.updateMessage(mappedMessage);
                    ctx.status(200).json(messageUpdated); // Return updated message
                }
            } catch (NumberFormatException e) {
                ctx.status(400).result("Invalid message ID");
            } catch (Exception e) {
                ctx.status(400).result("Error updating message: " + e.getMessage());
            }
        });

        app.get("/accounts/{id}/messages", ctx -> {
            try {
                int accountId = Integer.parseInt(ctx.pathParam("id"));
                ctx.status(200).json(messageService.getMessagesByAccountId(accountId));
            } catch (Exception e) {
                ctx.status(400).result("Error retrieving messages: " + e.getMessage());
            }
        });

        return app;
    }
}