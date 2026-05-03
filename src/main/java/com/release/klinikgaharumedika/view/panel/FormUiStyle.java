package com.release.klinikgaharumedika.view.panel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicTextAreaUI;
import javax.swing.plaf.basic.BasicTextFieldUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

final class FormUiStyle {

    static final Color PRIMARY_GREEN = new Color(28, 112, 77);
    static final Color TEXT = new Color(34, 34, 34);
    static final Color LABEL = new Color(36, 36, 36);
    static final Color MUTED = new Color(120, 120, 120);
    static final Color PLACEHOLDER = new Color(150, 150, 150);
    static final Color REQUIRED = new Color(198, 78, 78);
    static final Color BORDER = new Color(222, 218, 208);
    static final Color INPUT_BG = Color.WHITE;
    static final Color DISABLED_BG = new Color(245, 245, 245);
    static final int FIELD_HEIGHT = 40;
    static final int BUTTON_HEIGHT = 36;
    static final int BUTTON_MIN_WIDTH = 120;
    static final int DATE_SPINNER_MIN_WIDTH = 160;
    private static final Insets TEXT_AREA_PADDING = new Insets(8, 10, 8, 10);
    private static final String TEXT_FIELD_PLACEHOLDER = "JTextField.placeholderText";
    private static final String TEXT_AREA_PLACEHOLDER = "JTextArea.placeholderText";

    private FormUiStyle() {
    }

    static void applyFormStyle(Container root) {
        applyComponentStyle(root);
        for (Component child : root.getComponents()) {
            if (child instanceof Container container) {
                applyFormStyle(container);
            } else {
                applyComponentStyle(child);
            }
        }
    }

    static void placeholder(JTextField field, String text) {
        field.putClientProperty(TEXT_FIELD_PLACEHOLDER, text);
        installPlaceholderSupport(field);
    }

    static void placeholder(JTextArea area, String text) {
        area.putClientProperty(TEXT_AREA_PLACEHOLDER, text);
        installPlaceholderSupport(area);
    }

    static void dateSpinner(JSpinner spinner) {
        spinner.setModel(new SpinnerDateModel());
        spinner.setEditor(new JSpinner.DateEditor(spinner, "dd/MM/yyyy"));
        spinner.setToolTipText("Format tanggal: dd/MM/yyyy");
        JTextField editorField = spinnerEditorField(spinner);
        if (editorField != null) {
            placeholder(editorField, "dd/MM/yyyy");
        }
        styleSpinner(spinner);
    }

    static void timeSpinner(JSpinner spinner) {
        spinner.setModel(new SpinnerDateModel());
        spinner.setEditor(new JSpinner.DateEditor(spinner, "HH:mm"));
        spinner.setToolTipText("Format jam: HH:mm");
        JTextField editorField = spinnerEditorField(spinner);
        if (editorField != null) {
            placeholder(editorField, "HH:mm");
        }
        styleSpinner(spinner);
    }

    static void stylePrimaryButton(JButton button) {
        button.setUI(new BasicButtonUI());
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setBackground(PRIMARY_GREEN);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        setButtonSize(button, BUTTON_MIN_WIDTH);
    }

    static void styleGhostButton(JButton button) {
        button.setUI(new BasicButtonUI());
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(true);
        button.setFocusPainted(false);
        button.setBackground(Color.WHITE);
        button.setForeground(new Color(90, 90, 90));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(8, 18, 8, 18)
        ));
        setButtonSize(button, BUTTON_MIN_WIDTH);
    }

    static void styleTextField(JTextField field) {
        installPlaceholderSupport(field);
        field.setForeground(TEXT);
        field.setCaretColor(TEXT);
        field.setBackground(field.isEnabled() ? INPUT_BG : DISABLED_BG);
        field.setDisabledTextColor(MUTED);
        field.setMargin(new Insets(0, 10, 0, 10));
        field.setBorder(inputBorder());
        setHeight(field, FIELD_HEIGHT);
    }

    static void styleTextArea(JTextArea area) {
        installPlaceholderSupport(area);
        area.setForeground(TEXT);
        area.setCaretColor(TEXT);
        area.setBackground(area.isEnabled() ? INPUT_BG : DISABLED_BG);
        area.setDisabledTextColor(MUTED);
        area.setMargin(new Insets(0, 0, 0, 0));
        area.setBorder(BorderFactory.createEmptyBorder(
                TEXT_AREA_PADDING.top,
                TEXT_AREA_PADDING.left,
                TEXT_AREA_PADDING.bottom,
                TEXT_AREA_PADDING.right
        ));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
    }

    static void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setUI(new BasicComboBoxUI());
        comboBox.setForeground(TEXT);
        comboBox.setBackground(INPUT_BG);
        comboBox.setBorder(inputBorder());
        comboBox.setMaximumRowCount(12);
        setHeight(comboBox, FIELD_HEIGHT);
    }

    static void refreshButtonSize(JButton button) {
        setButtonSize(button, BUTTON_MIN_WIDTH);
    }

    static void styleSpinner(JSpinner spinner) {
        spinner.setForeground(TEXT);
        spinner.setBackground(INPUT_BG);
        spinner.setBorder(inputBorder());
        setHeight(spinner, FIELD_HEIGHT);
        Dimension preferredSize = spinner.getPreferredSize();
        spinner.setPreferredSize(new Dimension(Math.max(preferredSize.width, DATE_SPINNER_MIN_WIDTH), FIELD_HEIGHT));
        spinner.setMinimumSize(new Dimension(DATE_SPINNER_MIN_WIDTH, FIELD_HEIGHT));
        JTextField editorField = spinnerEditorField(spinner);
        if (editorField != null) {
            installPlaceholderSupport(editorField);
            editorField.setForeground(TEXT);
            editorField.setCaretColor(TEXT);
            editorField.setHorizontalAlignment(JTextField.LEFT);
            editorField.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        }
    }

    static void styleTable(JTable table) {
        table.setRowHeight(Math.max(table.getRowHeight(), 32));
        table.setShowGrid(true);
        table.setGridColor(new Color(237, 236, 232));
        table.setSelectionBackground(new Color(220, 235, 225));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setBackground(new Color(243, 240, 233));
        table.getTableHeader().setForeground(new Color(118, 118, 118));
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD, 10f));
    }

    private static void applyComponentStyle(Component component) {
        if (component instanceof JLabel label) {
            styleLabel(label);
        }
        if (component instanceof JButton button) {
            if (isPrimaryButton(button)) {
                stylePrimaryButton(button);
            } else {
                styleGhostButton(button);
            }
        }
        if (component instanceof JTextArea area) {
            styleTextArea(area);
        } else if (component instanceof JTextField field) {
            styleTextField(field);
        } else if (component instanceof JComboBox<?> comboBox) {
            styleComboBox(comboBox);
        } else if (component instanceof JSpinner spinner) {
            styleSpinner(spinner);
        } else if (component instanceof JScrollPane scrollPane) {
            styleScrollPane(scrollPane);
        }
    }

    private static void styleLabel(JLabel label) {
        String text = label.getText();
        if (text == null || text.isBlank() || text.startsWith("<html>")) {
            return;
        }
        if ("* Wajib diisi".equals(text.trim())) {
            label.setForeground(REQUIRED);
            label.setFont(label.getFont().deriveFont(Font.PLAIN, 11f));
            return;
        }
        if (text.trim().endsWith("*")) {
            String baseText = text.trim().substring(0, text.trim().length() - 1).trim();
            label.setText("<html>" + escapeHtml(baseText) + " <span style='color:" + toHtmlColor(REQUIRED) + "'>*</span></html>");
            label.setForeground(LABEL);
            label.setFont(label.getFont().deriveFont(Font.BOLD, 12f));
        }
    }

    private static void styleScrollPane(JScrollPane scrollPane) {
        Component view = scrollPane.getViewport().getView();
        if (view instanceof JTextArea) {
            scrollPane.setBorder(BorderFactory.createLineBorder(BORDER));
            scrollPane.getViewport().setBackground(INPUT_BG);
        }
    }

    private static Border inputBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        );
    }

    static void installPlaceholderSupport(JTextField field) {
        if (!(field.getUI() instanceof PlaceholderTextFieldUI)) {
            field.setUI(new PlaceholderTextFieldUI());
        }
        field.repaint();
    }

    static void installPlaceholderSupport(JTextArea area) {
        if (!(area.getUI() instanceof PlaceholderTextAreaUI)) {
            area.setUI(new PlaceholderTextAreaUI());
        }
        area.repaint();
    }

    private static void setHeight(JComponent component, int height) {
        Dimension preferredSize = component.getPreferredSize();
        component.setPreferredSize(new Dimension(preferredSize.width, height));
        component.setMinimumSize(new Dimension(0, height));
        component.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
    }

    private static void setButtonSize(JButton button, int minWidth) {
        Dimension preferredSize = button.getPreferredSize();
        int width = Math.max(preferredSize.width, minWidth);
        button.setPreferredSize(new Dimension(width, BUTTON_HEIGHT));
        button.setMinimumSize(new Dimension(width, BUTTON_HEIGHT));
    }

    private static boolean isPrimaryButton(JButton button) {
        Color background = button.getBackground();
        return PRIMARY_GREEN.equals(background) || Color.WHITE.equals(button.getForeground()) && background != null
                && background.getGreen() < 140 && background.getRed() < 80;
    }

    private static JTextField spinnerEditorField(JSpinner spinner) {
        if (spinner.getEditor() instanceof JSpinner.DefaultEditor editor) {
            return editor.getTextField();
        }
        return null;
    }

    private static String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private static String toHtmlColor(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    private static void paintPlaceholder(JTextComponent component, Graphics graphics, String propertyName, boolean multiline) {
        Object placeholder = component.getClientProperty(propertyName);
        if (!(placeholder instanceof String text) || text.isBlank()) {
            return;
        }
        if (!component.isEnabled() || !component.isEditable() || component.getDocument().getLength() > 0) {
            return;
        }

        Graphics2D g2 = (Graphics2D) graphics.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setFont(component.getFont());
            g2.setColor(PLACEHOLDER);

            FontMetrics metrics = g2.getFontMetrics();
            Insets insets = component.getInsets();
            Rectangle start = firstTextPosition(component);
            int x = start != null ? Math.max(start.x, insets.left) : insets.left;
            int y = multiline
                    ? (start != null
                            ? Math.max(start.y + metrics.getAscent(), insets.top + metrics.getAscent())
                            : insets.top + metrics.getAscent())
                    : (component.getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
            g2.drawString(text, x, y);
        } finally {
            g2.dispose();
        }
    }

    @SuppressWarnings("deprecation")
    private static Rectangle firstTextPosition(JTextComponent component) {
        try {
            return component.modelToView(0);
        } catch (BadLocationException ignored) {
            return null;
        }
    }

    private static final class PlaceholderTextFieldUI extends BasicTextFieldUI {
        @Override
        protected void paintSafely(Graphics graphics) {
            super.paintSafely(graphics);
            paintPlaceholder(getComponent(), graphics, TEXT_FIELD_PLACEHOLDER, false);
        }
    }

    private static final class PlaceholderTextAreaUI extends BasicTextAreaUI {
        @Override
        protected void paintSafely(Graphics graphics) {
            super.paintSafely(graphics);
            paintPlaceholder(getComponent(), graphics, TEXT_AREA_PLACEHOLDER, true);
        }
    }
}
