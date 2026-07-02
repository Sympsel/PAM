package client.utils;

import common.utils.Config;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class PlaceholderPasswordField extends JPasswordField {
    private String placeholder;
    private boolean isPlaceholder = true;

    public PlaceholderPasswordField(String placeholder) {
        super(20);
        this.placeholder = placeholder;
        setFont(new Font(Config.FONT, Font.PLAIN, 16));
        setForeground(Color.GRAY);
        setText(placeholder);

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (isPlaceholder) {
                    setText("");
                    setForeground(Color.BLACK);
                    isPlaceholder = false;
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                char[] password = getPassword();
                if (password.length == 0 || new String(password).equals(placeholder)) {
                    setText(placeholder);
                    setForeground(Color.GRAY);
                    isPlaceholder = true;
                }
            }
        });
    }

    public String getPlaceholderText() {
        if (isPlaceholder) {
            return "";
        }
        return new String(getPassword());
    }

    public boolean isPlaceholderMode() {
        return isPlaceholder;
    }
}
