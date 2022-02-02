package org.tn5250j.sessionsettings;
/*
 * Title: ColorAttributesPanel
 * Copyright:   Copyright (c) 2001
 * Company:
 *
 * @author Kenneth J. Pouncey
 * @version 0.5
 * <p>
 * Description:
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this software; see the file COPYING.  If not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307 USA
 */

import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

import org.tn5250j.SessionConfig;
import org.tn5250j.gui.TitledBorderedPane;
import org.tn5250j.gui.UiUtils;
import org.tn5250j.tools.LangTool;

import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

class ColorAttributesController extends AbstractAttributesController {
    @FXML
    Pane view;
    @FXML
    TitledBorderedPane topPanel;
    @FXML
    TitledBorderedPane colorListPanel;
    @FXML
    TitledBorderedPane previewPanel;

    @FXML
    ComboBox<Schema> colorSchemaList;
    @FXML
    ComboBox<String> colorList;
    @FXML
    ColorPicker colorPicker;

    //preview
    //first column
    @FXML
    Rectangle previewRect11;
    @FXML
    Rectangle previewRect12;
    @FXML
    Rectangle previewRect13;

    @FXML
    Rectangle previewRect14;

    //second column
    @FXML
    Rectangle previewRect21;
    @FXML
    Rectangle previewRect22;
    @FXML
    Rectangle previewRect23;

    @FXML
    Rectangle previewRect24;
    @FXML
    Rectangle previewRect25;

    //third column
    @FXML
    Rectangle previewRect31;
    @FXML
    Rectangle previewRect32;
    @FXML
    Rectangle previewRect33;

    @FXML
    Rectangle previewRect34;
    @FXML
    Rectangle previewRect35;

    //preview labels
    @FXML
    Label previewLabel1;
    @FXML
    Label previewLabel2;
    @FXML
    Label previewLabel3;

    //preview big rects
    @FXML
    Rectangle previewRect41;
    @FXML
    Rectangle previewRect42;

    private final Properties schemaProps = new Properties();

    ColorAttributesController(final SessionConfig config) {
        super(config, "Colors");
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        //north
        initNorthPane();

        //center
        initColorPickerPanel();
        initPreview();

        // set the default color for display as that being for background
        colorPicker.setValue(getColorProperty("colorBg"));
    }

    private void initColorPickerPanel() {
        colorListPanel.setTitle(LangTool.getString("sa.colors"));

        //create colors combobox
        colorList.getItems().add(LangTool.getString("sa.bg"));
        colorList.getItems().add(LangTool.getString("sa.blue"));
        colorList.getItems().add(LangTool.getString("sa.red"));
        colorList.getItems().add(LangTool.getString("sa.pink"));
        colorList.getItems().add(LangTool.getString("sa.green"));
        colorList.getItems().add(LangTool.getString("sa.turq"));
        colorList.getItems().add(LangTool.getString("sa.yellow"));
        colorList.getItems().add(LangTool.getString("sa.white"));
        colorList.getItems().add(LangTool.getString("sa.guiField"));
        colorList.getItems().add(LangTool.getString("sa.cursorColor"));
        colorList.getItems().add(LangTool.getString("sa.columnSep"));
        colorList.getItems().add(LangTool.getString("sa.hexAttrColor"));

        colorList.getSelectionModel().selectedItemProperty().addListener((src, old, value) -> selectedColorChanged());
        colorList.getSelectionModel().selectFirst();
    }

    private void initPreview() {
        previewPanel.setTitle(LangTool.getString("sa.preview", "Preview"));

        final Text text = new Text();
        text.setFont(previewLabel1.getFont());

        final Bounds b = text.getLayoutBounds();
        final Insets insets = previewLabel1.getInsets();
        final double spacing = 2;

        double h = b.getHeight() + insets.getBottom() + insets.getTop();
        h = (b.getHeight() * 3. + spacing) / 2.;

        //set sizes
        //first column
        arrangeRects(0, 0, h, previewRect11, previewRect12, previewRect13);
        arrangeRects(0, 0, h, previewRect14);

        //second column
        arrangeRects(0, 0, h, previewRect21, previewRect22, previewRect23);
        arrangeRects(0, 0, h, previewRect24, previewRect25);

        //third column
        arrangeRects(0, 0, h, previewRect31, previewRect32, previewRect33);
        arrangeRects(0, 0, h, previewRect34, previewRect35);

        //preview big rects
        setRectSize(previewRect41, 0, 0, 2 * h, h);
        setRectSize(previewRect42, 0, 0, 2 * h, h);

        //set not mutable colors
        previewRect11.setFill(Color.WHITE);
        previewRect12.setFill(Color.WHITE);

        previewRect21.setFill(Color.BLACK);
        previewRect23.setFill(Color.WHITE);

        previewRect24.setFill(Color.WHITE);

        previewRect31.setFill(Color.WHITE);
        previewRect33.setFill(Color.BLACK);

        previewRect34.setFill(Color.BLACK);

        previewRect42.setFill(Color.BLACK);

        //bind to color picker value
        previewRect12.fillProperty().bind(colorPicker.valueProperty());
        previewRect14.fillProperty().bind(colorPicker.valueProperty());

        //second column
        previewRect22.fillProperty().bind(colorPicker.valueProperty());
        previewRect25.fillProperty().bind(colorPicker.valueProperty());

        previewRect35.fillProperty().bind(colorPicker.valueProperty());

        //preview big rects
        previewRect42.fillProperty().bind(colorPicker.valueProperty());

        //labels
        previewLabel1.textFillProperty().bind(colorPicker.valueProperty());
        colorPicker.valueProperty().addListener((src, old, value) -> {
            previewLabel2.setBackground(new Background(new BackgroundFill(
                value, CornerRadii.EMPTY, Insets.EMPTY)));
        });
        previewLabel3.textFillProperty().bind(colorPicker.valueProperty());
    }

    private static void arrangeRects(final double originX, final double originY,
            final double originSize, final Rectangle... rects) {
        final double step = originSize / rects.length;
        double size = originSize;

        for (final Rectangle r : rects) {
            final double x = (originSize - size) / 2;
            final double y = x;

            setRectSize(r, originX + x, originY + y, size, size);
            size -= step;
            r.toFront();
        }
    }

    private static void setRectSize(final Rectangle r, final double x, final double y, final double w, final double h) {
        r.setX(x);
        r.setY(y);
        r.setWidth(w);
        r.setHeight(h);
    }

    private boolean selectedColorChanged() {
        final String newSelection = colorList.getSelectionModel().getSelectedItem();
        final Schema colorSchema = colorSchemaList.getSelectionModel().getSelectedItem();
        final boolean isDefault = colorSchema.isDefault();

        // the variable 'exists' is declired just for provide the possibility to do || operation between methods
        final boolean exists = possibleUpdateColorPicker(newSelection, "sa.bg", "colorBg", colorSchema.getColorBg(), isDefault)
            || possibleUpdateColorPicker(newSelection, "sa.blue", "colorBlue", colorSchema.getColorBlue(), isDefault)
            || possibleUpdateColorPicker(newSelection, "sa.red", "colorRed", colorSchema.getColorRed(), isDefault)
            || possibleUpdateColorPicker(newSelection, "sa.pink", "colorPink", colorSchema.getColorPink(), isDefault)
            || possibleUpdateColorPicker(newSelection, "sa.green", "colorGreen", colorSchema.getColorGreen(), isDefault)
            || possibleUpdateColorPicker(newSelection, "sa.turq", "colorTurq", colorSchema.getColorTurq(), isDefault)
            || possibleUpdateColorPicker(newSelection, "sa.yellow", "colorYellow", colorSchema.getColorYellow(), isDefault)
            || possibleUpdateColorPicker(newSelection, "sa.white", "colorWhite", colorSchema.getColorWhite(), isDefault)
            || possibleUpdateColorPicker(newSelection, "sa.guiField", "colorGUIField", colorSchema.getColorGuiField(), isDefault,
                    Color.WHITE)
            || possibleUpdateColorPicker(newSelection, "sa.cursorColor", "colorCursor", colorSchema.getColorBg(), isDefault,
                    getColorProperty("colorBg"))
            || possibleUpdateColorPicker(newSelection, "sa.columnSep", "colorSep", colorSchema.getColorSeparator(), isDefault,
                    getColorProperty("colorWhite"))
            || possibleUpdateColorPicker(newSelection, "sa.hexAttrColor", "colorHexAttr", colorSchema.getColorHexAttr(), isDefault,
                    getColorProperty("colorWhite"));

        return exists;
    }

    private boolean possibleUpdateColorPicker(final String selectedColor, final String langToolsKey,
            final String propertyName, final Color color, final boolean isDefault) {
        return possibleUpdateColorPicker(selectedColor, langToolsKey, propertyName, color, isDefault, null);
    }
    private boolean possibleUpdateColorPicker(final String selectedColor, final String langToolsKey,
            final String propertyName, final Color color, final boolean isDefault, final Color defaultColor) {
        if (selectedColor.equals(LangTool.getString(langToolsKey))) {
            if (!isDefault) {
                colorPicker.setValue(color);
            } else {
                colorPicker.setValue(getColorProperty(propertyName, defaultColor));
            }
            return true;
        }
        return false;
    }

    private void initNorthPane() {
        topPanel.setTitle(LangTool.getString("sa.colorSchema"));
        loadSchemas(colorSchemaList);
        colorSchemaList.getSelectionModel().selectFirst();
    }

    @Override
    public void applyAttributes() {
        final String newSelection = colorList.getValue();
        final Schema colorSchema = colorSchemaList.getSelectionModel().getSelectedItem();

        if (!colorSchema.isDefault()) {
            saveChangedColorProperty("colorBg", colorSchema.getColorBg());
            saveChangedColorProperty("colorBlue", colorSchema.getColorBlue());
            saveChangedColorProperty("colorRed", colorSchema.getColorRed());
            saveChangedColorProperty("colorPink", colorSchema.getColorPink());
            saveChangedColorProperty("colorGreen", colorSchema.getColorGreen());
            saveChangedColorProperty("colorTurq", colorSchema.getColorTurq());
            saveChangedColorProperty("colorYellow", colorSchema.getColorYellow());
            saveChangedColorProperty("colorWhite", colorSchema.getColorWhite());
            saveChangedColorProperty("colorGUIField", colorSchema.getColorGuiField());
            saveChangedColorProperty("colorCursor", colorSchema.getColorCursor());
            saveChangedColorProperty("colorSep", colorSchema.getColorSeparator());
            saveChangedColorProperty("colorHexAttr", colorSchema.getColorHexAttr());
        } else {
            // the variable 'exists' is declired just for provide the possibility to do || operation between methods
            // instead of elseif
            @SuppressWarnings("unused")
            final boolean exists = saveColorPickerValue(newSelection, "sa.bg", "colorBg")
                || saveColorPickerValue(newSelection, "sa.blue", "colorBlue")
                || saveColorPickerValue(newSelection, "sa.red", "colorRed")
                || saveColorPickerValue(newSelection, "sa.pink", "colorPink")
                || saveColorPickerValue(newSelection, "sa.green", "colorGreen")
                || saveColorPickerValue(newSelection, "sa.turq", "colorTurq")
                || saveColorPickerValue(newSelection, "sa.yellow", "colorYellow")
                || saveColorPickerValue(newSelection, "sa.white", "colorWhite")
                || saveColorPickerValue(newSelection, "sa.guiField", "colorGUIField")
                || saveColorPickerValue(newSelection, "sa.cursorColor", "colorCursor")
                || saveColorPickerValue(newSelection, "sa.columnSep", "colorSep")
                || saveColorPickerValue(newSelection, "sa.cursorColor", "colorCursor")
                || saveColorPickerValue(newSelection, "sa.hexAttrColor", "colorHexAttr");
        }
    }

    private boolean saveColorPickerValue(final String selectedColor, final String langToolKey, final String propertyName) {
        if (selectedColor.equals(LangTool.getString(langToolKey))) {
            final Color nc = colorPicker.getValue();
            if (!getColorProperty(propertyName).equals(nc)) {
                changes.firePropertyChange(this, propertyName,
                        getColorProperty(propertyName),
                        nc);

                setProperty(propertyName, Integer.toString(UiUtils.toRgb(nc)));
                return true;
            }
        }
        return false;
    }

    /**
     * @param name color property name.
     * @param color color.
     */
    private void saveChangedColorProperty(final String name, final Color color) {
        if (!getColorProperty(name).equals(color)) {
            changes.firePropertyChange(this, name, getColorProperty(name), color);
            setProperty(name, Integer.toString(UiUtils.toRgb(color)));
        }
    }

    private void loadSchemas(final ComboBox<Schema> schemas) {
        try {
            final InputStream in = this.getClass().getClassLoader().getResourceAsStream("tn5250jSchemas.properties");
            try {
                schemaProps.load(in);
            } finally {
                in.close();
            }
        } catch (final Exception e) {
            System.err.println(e);
        }

        //create default schema
        final Schema defaultSchema = new Schema();
        defaultSchema.setDefault(true);;
        defaultSchema.setDescription(LangTool.getString("sa.colorDefault"));
        schemas.getItems().add(defaultSchema);

        final int numSchemas = Integer.parseInt((String) schemaProps.get("schemas"));
        for (int x = 1; x <= numSchemas; x++) {
            final Schema s = new Schema();
            final String prefix = "schema" + x;
            s.setDescription((String) schemaProps.get(prefix + ".title"));
            s.setColorBg(getSchemaProp(prefix + ".colorBg"));
            s.setColorRed(getSchemaProp(prefix + ".colorRed"));
            s.setColorTurq(getSchemaProp(prefix + ".colorTurq"));
            s.setColorCursor(getSchemaProp(prefix + ".colorCursor"));
            s.setColorGuiField(getSchemaProp(prefix + ".colorGUIField"));
            s.setColorWhite(getSchemaProp(prefix + ".colorWhite"));
            s.setColorYellow(getSchemaProp(prefix + ".colorYellow"));
            s.setColorGreen(getSchemaProp(prefix + ".colorGreen"));
            s.setColorPink(getSchemaProp(prefix + ".colorPink"));
            s.setColorBlue(getSchemaProp(prefix + ".colorBlue"));
            s.setColorSeparator(getSchemaProp(prefix + ".colorSep"));
            s.setColorHexAttr(getSchemaProp(prefix + ".colorHexAttr"));
            schemas.getItems().add(s);
        }
    }

    private int getSchemaProp(final String key) {

        if (schemaProps.containsKey(key)) {

            return Integer.parseInt((String) schemaProps.get(key));

        } else {
            return 0;
        }

    }

    @Override
    public Region getView() {
        return view;
    }

    class Schema {
        @Override
        public String toString() {
            return description;
        }
        public void setDescription(final String desc) {
            description = desc;
        }
        public void setColorBg(final int color) {
            bg = UiUtils.rgb(color);
        }
        public Color getColorBg() {
            return bg;
        }
        public void setColorBlue(final int color) {
            blue = UiUtils.rgb(color);
        }
        public Color getColorBlue() {
            return blue;
        }
        public void setColorRed(final int color) {
            red = UiUtils.rgb(color);
        }
        public Color getColorRed() {
            return red;
        }
        public void setColorPink(final int color) {
            pink = UiUtils.rgb(color);
        }
        public Color getColorPink() {
            return pink;
        }
        public void setColorGreen(final int color) {
            green = UiUtils.rgb(color);
        }
        public Color getColorGreen() {
            return green;
        }
        public void setColorTurq(final int color) {
            turq = UiUtils.rgb(color);
        }
        public Color getColorTurq() {
            return turq;
        }
        public void setColorYellow(final int color) {
            yellow = UiUtils.rgb(color);
        }
        public Color getColorYellow() {
            return yellow;
        }
        public void setColorWhite(final int color) {
            white = UiUtils.rgb(color);
        }
        public Color getColorWhite() {
            return white;
        }
        public void setColorGuiField(final int color) {
            gui = UiUtils.rgb(color);
        }
        public Color getColorGuiField() {
            return gui;
        }
        public void setColorCursor(final int color) {
            cursor = UiUtils.rgb(color);
        }
        public Color getColorCursor() {
            return cursor;
        }
        public void setColorSeparator(final int color) {
            columnSep = UiUtils.rgb(color);
        }
        public Color getColorSeparator() {
            return columnSep;
        }
        public void setColorHexAttr(final int color) {
            hexAttr = UiUtils.rgb(color);
        }
        public Color getColorHexAttr() {
            return hexAttr;
        }
        public void setDefault(final boolean isDefault) {
            this.isDefault = isDefault;
        }
        public boolean isDefault() {
            return isDefault;
        }

        private String description;
        private Color bg;
        private Color blue;
        private Color red;
        private Color pink;
        private Color green;
        private Color turq;
        private Color white;
        private Color yellow;
        private Color gui;
        private Color cursor;
        private Color columnSep;
        private Color hexAttr;
        private boolean isDefault;
    }
}
