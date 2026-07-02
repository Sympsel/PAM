package client.utils;

import common.utils.Config;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class PlaceholderTextField extends JTextField {
    private String placeholder;
    private static final Font font = new Font(Config.FONT, Font.PLAIN, 16);

    public PlaceholderTextField(String placeholder) {
        super(15);
        this.placeholder = placeholder;
        setForeground(Color.GRAY);
        setText(placeholder);
        setFont(font);

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (getText().equals(placeholder)) {
                    setText("");
                    setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (getText().isEmpty()) {
                    setText(placeholder);
                    setForeground(Color.GRAY);
                }
            }
        });
    }
}