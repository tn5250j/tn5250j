/**
 *
 */
package org.tn5250j.gui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class TitledBorderedPane extends StackPane {
    private static final String FX_BACKGROUND_PREFIX = "-fx-background-color: ";

    private Label titleLabel = new Label();
    private StackPane contentPane = new StackPane();
    private Node content;

    public TitledBorderedPane() {
        getStylesheets().add("/application.css");

        titleLabel.setText("");
        titleLabel.getStyleClass().add("bordered-titled-title");

        setLabelPosition(Pos.TOP_CENTER);

        getStyleClass().add("bordered-titled-border");
        getChildren().addAll(titleLabel, contentPane);
    }

    public void setLabelPosition(final Pos align) {
        StackPane.setAlignment(titleLabel, align);
    }

    public Pos getLabelPosition() {
        return StackPane.getAlignment(titleLabel);
    }

    public void setContent(final Node content) {
        content.getStyleClass().add("bordered-titled-content");
        contentPane.getChildren().add(content);
    }

    public Node getContent() {
        return content;
    }

    public void setTitle(final String title) {
        titleLabel.setText(" " + title + " ");
    }

    public String getTitle() {
        return titleLabel.getText();
    }

    public void setTitleBackground(final String bg) {
        if (bg != null) {
            titleLabel.setStyle(FX_BACKGROUND_PREFIX + bg);
        }
    }

    public String getTitleBackground() {
        final String style = titleLabel.getStyle();
        if (style != null && style.startsWith(FX_BACKGROUND_PREFIX)) {
            return style.substring(FX_BACKGROUND_PREFIX.length());
        }
        return null;
    }
}
