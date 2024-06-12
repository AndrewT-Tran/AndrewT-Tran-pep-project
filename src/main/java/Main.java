import Controller.SocialMediaController;
import Service.ServiceException;
import io.javalin.Javalin;

public class Main {
    public static void main(String[] args) {
        try {
            SocialMediaController controller = new SocialMediaController();
            Javalin app = controller.startAPI();
            app.start(8080);
        } catch (ServiceException e) {
            e.printStackTrace();
            System.err.println("Failed to start the API: " + e.getMessage());
        }
    }
}