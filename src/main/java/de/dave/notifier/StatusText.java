package de.dave.notifier;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class StatusText extends Text {
	
	private static final Color COLOR_STANDARD = Color.GREEN;
	private static final Color COLOR_ERROR = Color.RED;
	
	public StatusText(String text) {
		super(text);
		this.setFill(COLOR_STANDARD);
		this.setFont(Font.font ("Verdana", 10));
	}
	
	public void setError(String text) {
		this.setFill(COLOR_ERROR);
		this.setText(text);
	}
	
	public void setMessage(String text) {
		this.setFill(COLOR_STANDARD);
		this.setText(text);
	}
	
	public void appendWait() {
		this.setText(this.getText() + ".");
	}
}
