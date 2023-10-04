package login;

import com.sun.istack.internal.NotNull;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import main.AdminMainController;
import okhttp3.*;
import util.http.HttpClientUtil;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

import static main.include.Constants.MAIN_FXML_RESOURCE;

public class AdminLoginController {
    private Stage primaryStage;
    private AdminMainController adminMainController;
    private String currentUser = null;

    @FXML
    public TextField userNameTextField;

    @FXML
    public Label errorMessageLabel;

    private final StringProperty errorMessageProperty = new SimpleStringProperty();

    @FXML
    public void initialize(Stage primaryStage) {
        this.primaryStage = primaryStage;
        errorMessageLabel.textProperty().bind(errorMessageProperty);
    }

    @FXML
    private void loginButtonClicked(ActionEvent event) {

        String userName = userNameTextField.getText();
        if (userName.isEmpty()) {
            errorMessageProperty.set("User name is empty. You can't login with empty user name");
            return;
        }

        String finalUrl = HttpUrl
                        .parse("http://localhost:8080/WebApplication_Web_exploded/login")
                        .newBuilder()
                        .addQueryParameter("adminUsername", userName)
                        .build()
                        .toString();


        HttpClientUtil.runAsync(finalUrl,"GET",null ,new Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() ->
                        errorMessageProperty.set("Something went wrong: " + e.getMessage())
                );
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.code() != 200) {
                    String responseMessage = response.body().string();
                    Platform.runLater(() ->
                            errorMessageProperty.set("Login failed: " + responseMessage));
                } else {
                    Platform.runLater(() -> {
                        try{
                            currentUser = userName;
                            URL url = getClass().getResource(MAIN_FXML_RESOURCE);
                            FXMLLoader fxmlLoader = new FXMLLoader();
                            fxmlLoader.setLocation(url);
                            BorderPane mainMenuComponent = null;
                            mainMenuComponent = fxmlLoader.load(url.openStream());
                            AdminMainController adminMainController = fxmlLoader.getController();

                            Scene scene = new Scene(mainMenuComponent,1280, 800);

                            primaryStage.hide();
                            primaryStage.setScene(scene);
                            primaryStage.centerOnScreen();
                            primaryStage.show();

                            adminMainController.setUserName(currentUser);
                            adminMainController.initialize(primaryStage);
                        }
                        catch(Exception ignore) {}
                    });
                }
                Objects.requireNonNull(response.body()).close();
                response.close();
            }
        });
    }

    @FXML
    private void userNameKeyTyped(KeyEvent event) {
        errorMessageProperty.set("");
    }

    @FXML
    private void quitButtonClicked(ActionEvent e) {
        Platform.exit();
    }

    public String getCurrentUser() { return currentUser; }

    @FXML
    void EnterButtonClicked(KeyEvent event) {
        if (event.getCode().equals(KeyCode.ENTER)){
            loginButtonClicked(new ActionEvent());
        }
    }

}
