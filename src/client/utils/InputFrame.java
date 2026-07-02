package client.utils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class InputFrame extends JFrame {
    private final List<JLabel> labels;
    private final List<PlaceholderTextField> textFields;
    private final List<PlaceholderPasswordField> passwordFields;
    private final double leftPercent;
    private final Font labelFont = new Font("Jetbrains momo", Font.PLAIN, 14);

    public InputFrame() {
        this(0.3);
    }

    public InputFrame(double leftPercent) {
        this.labels = new ArrayList<>();
        this.textFields = new ArrayList<>();
        this.passwordFields = new ArrayList<>();
        if (leftPercent < 0 || leftPercent >= 1) {
            leftPercent = 0.3;
        }
        this.leftPercent = leftPercent;
    }

    public InputFrame addInputField(String labelText, String placeholderText) {
        return addInputField(new JLabel(labelText), new PlaceholderTextField(placeholderText));
    }

    public InputFrame addInputField(String labelText, PlaceholderTextField placeholderTextField) {
        return addInputField(new JLabel(labelText), placeholderTextField);
    }

    public InputFrame addPasswordField(String labelText, String placeholderText) {
        PlaceholderPasswordField passwordField = new PlaceholderPasswordField(placeholderText);
        return addInputField(new JLabel(labelText), passwordField);
    }

    public InputFrame addInputField(JLabel label, PlaceholderTextField textField) {
        label.setFont(labelFont);
        labels.add(label);
        textFields.add(textField);
        passwordFields.add(null);
        return this;
    }

    private InputFrame addInputField(JLabel label, PlaceholderPasswordField passwordField) {
        label.setFont(labelFont);
        labels.add(label);
        textFields.add(null);
        passwordFields.add(passwordField);
        return this;
    }

    public JPanel buildPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        for (int i = 0; i < labels.size(); i++) {
            if (textFields.get(i) != null) {
                gbc.gridx = 0;
                gbc.gridy = row;
                gbc.weightx = leftPercent;
                panel.add(labels.get(i), gbc);

                gbc.gridx = 1;
                gbc.weightx = 1 - leftPercent;
                panel.add(textFields.get(i), gbc);

                row++;
            } else if (passwordFields.get(i) != null) {
                gbc.gridx = 0;
                gbc.gridy = row;
                gbc.weightx = leftPercent;
                panel.add(labels.get(i), gbc);

                gbc.gridx = 1;
                gbc.weightx = 1 - leftPercent;
                panel.add(passwordFields.get(i), gbc);

                row++;
            }
        }
        return panel;
    }


    public String[] getFieldValues() {
        String[] values = new String[labels.size()];
        for (int i = 0; i < labels.size(); i++) {
            if (textFields.get(i) != null) {
                values[i] = textFields.get(i).getText();
            } else if (passwordFields.get(i) != null) {
                values[i] = passwordFields.get(i).getText();
            } else {
                values[i] = "";
            }
        }
        return values;
    }
}
