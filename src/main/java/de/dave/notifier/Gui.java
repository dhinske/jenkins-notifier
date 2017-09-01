package de.dave.notifier;

import java.util.Properties;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import com.github.plushaze.traynotification.animations.Animations;
import com.github.plushaze.traynotification.notification.Notification;
import com.github.plushaze.traynotification.notification.Notifications;
import com.github.plushaze.traynotification.notification.TrayNotification;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

public class Gui extends Application {

	private static final String NEW_BUILD_MESSAGE = "New build has finished";

	private static final String JSON_BUILDS = "builds";
	private static final String JSON_NUMBER = "number";
	private static final String JENKINS_SUCCESS = "SUCCESS";
	private static final String JENKINS_FAILURE = "FAILURE";
	private static final String HTTP_PREFIX = "http://";

	private static final String BUTTON_START = "Start";

	private StatusText statusText;

	private String url;
	private String userName;
	private String token;
	int currentBuildNumber;
	boolean isRunning = false;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage theStage) throws Exception {
		BorderPane borderPane = new BorderPane();

		GridPane grid = new GridPane();
		grid.setHgap(20);
		grid.setVgap(20);
		grid.setPadding(new Insets(10, 10, 10, 10));

		borderPane.setTop(grid);
		statusText = new StatusText("Welcome to jenkins-notifier");
		Properties properties = PropertiesManager.getProperties();
		TextField textField_url = new TextField(properties.getProperty(PropertiesManager.URL));
		textField_url.setMinWidth(300);
		TextField textField_username = new TextField(properties.getProperty(PropertiesManager.USERNAME));
		TextField textField_token = new TextField(properties.getProperty(PropertiesManager.TOKEN));
		Button startButton = new Button(BUTTON_START);
		startButton.setMinWidth(50);
		startButton.setOnAction((ActionEvent e) -> {
				isRunning = !isRunning;
				if (isRunning) {
					url = textField_url.getText().replaceAll(HTTP_PREFIX, "");
					userName = textField_username.getText();
					token = textField_token.getText();
					
					PropertiesManager.writeProperties(url, userName, token);

					textField_url.setEditable(false);
					textField_username.setEditable(false);
					textField_token.setEditable(false);
					startButton.setText("Stop");
				} else {
					textField_url.setEditable(true);
					textField_username.setEditable(true);
					textField_token.setEditable(true);
					statusText.setMessage("jenkins-notifier stopped");
					startButton.setText(BUTTON_START);
					return;
				}

				JSONObject result = getResult();
				if (result == null) {
					isRunning = false;
					startButton.setText(BUTTON_START);
				} else {
					currentBuildNumber = getBuildNumber(result);
					statusText.setMessage("Current build found " + currentBuildNumber);
				}
			});

		Timeline gameLoop = new Timeline();
		gameLoop.setCycleCount(Timeline.INDEFINITE);

		KeyFrame kf = new KeyFrame(Duration.seconds(10), (ActionEvent ae) -> {
				if (isRunning) {
					JSONObject result = getResult();
					int buildNumber = getBuildNumber(result);
					if (currentBuildNumber != buildNumber && currentBuildNumber != 0) {
						switch (getLastBuildResult(result)) {
						case JENKINS_SUCCESS:
							sendNotification(NEW_BUILD_MESSAGE + " " + buildNumber, textField_url.getText(),
									Notifications.SUCCESS);
							break;
						case JENKINS_FAILURE:
							sendNotification(NEW_BUILD_MESSAGE + " " + buildNumber, textField_url.getText(),
									Notifications.ERROR);
							break;
						default:
							sendNotification(NEW_BUILD_MESSAGE + " " + buildNumber, textField_url.getText(),
									Notifications.INFORMATION);
							break;
						}
						currentBuildNumber = buildNumber;
						statusText.setMessage("Current build found " + currentBuildNumber);
					} else {
						statusText.appendWait();
					}
				}
			});

		gameLoop.getKeyFrames().add(kf);
		gameLoop.play();

		grid.add(new Text("Job-URL"), 0, 0);
		grid.add(textField_url, 0, 1);
		grid.add(new Text("Username"), 1, 0);
		grid.add(textField_username, 1, 1);
		grid.add(new Text("Token"), 2, 0);
		grid.add(textField_token, 2, 1);

		grid.add(startButton, 3, 1);
		grid.add(statusText, 0, 3);

		theStage.setHeight(170);
		theStage.centerOnScreen();
		theStage.getIcons().add(new Image(getClass().getResourceAsStream("/jenkins_icon.jpg")));
		theStage.setScene(new Scene(borderPane));
		theStage.setTitle("jenkins-notifier");

		theStage.setOnCloseRequest((WindowEvent t) -> {Platform.exit(); System.exit(0);});
		theStage.show();
	}

	private void sendNotification(String title, String message, Notification notification) {
		TrayNotification tray = new TrayNotification();
		tray.setTitle(title);
		tray.setMessage(message);
		tray.setNotification(notification);
		tray.setAnimation(Animations.POPUP);
		tray.showAndDismiss(new Duration(3000));
	}

	private int getBuildNumber(JSONObject job) {
		if (!job.has(JSON_BUILDS) || job.getJSONArray(JSON_BUILDS).length() == 0) {
			return 0;
		}
		return job.getJSONArray(JSON_BUILDS).getJSONObject(0).getInt(JSON_NUMBER);
	}

	private String getLastBuildResult(JSONObject job) {
		if (job.getJSONObject("lastSuccessfulBuild").getInt(JSON_NUMBER) == job.getJSONObject("lastBuild")
				.getInt(JSON_NUMBER)) {
			return JENKINS_SUCCESS;
		}
		return JENKINS_FAILURE;
	}

	JSONObject getResult() {
		HttpGet request = new HttpGet(HTTP_PREFIX + userName + ":" + token + "@" + url + "api/json");
		HttpResponse response = null;

		DefaultHttpClient client = new DefaultHttpClient();
		JSONObject result = null;
		try {
			response = client.execute(request);

			result = new JSONObject(EntityUtils.toString(response.getEntity()));
		} catch (Exception e) {
			statusText.setError("HTTP-Request cannot be executed, please check your values.");
		}
		return result;
	}
}
