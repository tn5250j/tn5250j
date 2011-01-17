package org.tn5250j.settings;
/**
 * Title: DisplayAttributesPanel
 * Copyright:   Copyright (c) 2001
 * Company:
 * @author  Kenneth J. Pouncey
 * @version 0.5
 *
 * Description:
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software; see the file COPYING.  If not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307 USA
 *
 */

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import org.tn5250j.SessionConfig;
import org.tn5250j.tools.AlignLayout;
import org.tn5250j.tools.LangTool;

import static org.tn5250j.settings.ColumnSeparator.Line;
import static org.tn5250j.settings.ColumnSeparator.Hide;
import static org.tn5250j.settings.ColumnSeparator.ShortLine;
import static org.tn5250j.settings.ColumnSeparator.Dot;

public class DisplayAttributesPanel extends AttributesPanel {

	private static final String NO = "No";
	private static final String YES = "Yes";
	
	private static final long serialVersionUID = 1L;
	private JRadioButton csHide;
	private JRadioButton csLine;
	private JRadioButton csDot;
	private JRadioButton csShortLine;
	private JRadioButton saNormal;
	private JCheckBox guiCheck;
	private JCheckBox guiShowUnderline;

	public DisplayAttributesPanel(SessionConfig config) {
		super(config, "Display");
	}

	/**Component initialization*/
	@Override
	public void initPanel() throws Exception {

		setLayout(new BorderLayout());
		contentPane = new JPanel();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
		add(contentPane, BorderLayout.NORTH);

		// define column separator panel
		JPanel csp = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
		TitledBorder tb =
			BorderFactory.createTitledBorder(LangTool.getString("sa.cs"));
		csp.setBorder(tb);

		csHide = new JRadioButton(LangTool.getString("sa.csHide"));
		csHide.setActionCommand("Hide");
		csLine = new JRadioButton(LangTool.getString("sa.csLine"));
		csLine.setActionCommand("Line");
		csDot = new JRadioButton(LangTool.getString("sa.csDot"));
		csDot.setActionCommand("Dot");
		csShortLine = new JRadioButton(LangTool.getString("sa.csShortLine"));
		csShortLine.setActionCommand("ShortLine");

		// Group the radio buttons.
		ButtonGroup csGroup = new ButtonGroup();
		csGroup.add(csHide);
		csGroup.add(csLine);
		csGroup.add(csDot);
		csGroup.add(csShortLine);

		csLine.setSelected(Line == ColumnSeparator.getFromName(getStringProperty("colSeparator")));
		csDot.setSelected(Dot == ColumnSeparator.getFromName(getStringProperty("colSeparator")));
		csShortLine.setSelected(ShortLine == ColumnSeparator.getFromName(getStringProperty("colSeparator")));
		csHide.setSelected(Hide == ColumnSeparator.getFromName(getStringProperty("colSeparator")));

		csp.add(csHide);
		csp.add(csLine);
		csp.add(csDot);
		csp.add(csShortLine);

		// define show attributs panel
		JPanel sap = new JPanel();
		sap.setBorder(
				BorderFactory.createTitledBorder(
						LangTool.getString("sa.showAttr")));

		saNormal = new JRadioButton(LangTool.getString("sa.showNormal"));
		saNormal.setActionCommand("Normal");
		JRadioButton saHex = new JRadioButton(LangTool.getString("sa.showHex"));
		saHex.setActionCommand("Hex");

		// Group the radio buttons.
		ButtonGroup saGroup = new ButtonGroup();
		saGroup.add(saNormal);
		saGroup.add(saHex);

		if (getStringProperty("showAttr").equals("Hex"))
			saHex.setSelected(true);
		else
			saNormal.setSelected(true);

		sap.add(saNormal);
		sap.add(saHex);

		// define gui panel
		JPanel cgp = new JPanel();
		cgp.setBorder(BorderFactory.createTitledBorder(LangTool.getString("sa.cgp")));
		cgp.setLayout(new AlignLayout(1, 5, 5));

		guiCheck = new JCheckBox(LangTool.getString("sa.guiCheck"));
		guiCheck.setSelected(YES.equals(getStringProperty("guiInterface")));
		cgp.add(guiCheck);

		// since this is a new property added then it might not exist in existing
		//    profiles and it should be defaulted to yes.
		guiShowUnderline = new JCheckBox(LangTool.getString("sa.guiShowUnderline"));
		guiShowUnderline.setSelected(YES.equals(getStringProperty("guiShowUnderline")));
		cgp.add(guiShowUnderline);

		contentPane.add(csp);
		contentPane.add(sap);
		contentPane.add(cgp);

	}

	@Override
	public void save() {

	}

	@Override
	public void applyAttributes() {

		if (csHide.isSelected()) {
			changes.firePropertyChange(this, "colSeparator",
					getStringProperty("colSeparator"), "Hide");
			setProperty("colSeparator", "Hide");
		}
		else if (csLine.isSelected()) {
			changes.firePropertyChange(this, "colSeparator",
					getStringProperty("colSeparator"), "Line");
			setProperty("colSeparator", "Line");
		}
		else if (csShortLine.isSelected()) {
			changes.firePropertyChange(this, "colSeparator",
					getStringProperty("colSeparator"), "ShortLine");
			setProperty("colSeparator", "ShortLine");
		}
		else {
			changes.firePropertyChange(this, "colSeparator",
					getStringProperty("colSeparator"), "Dot");
			setProperty("colSeparator", "Dot");
		}

		if (saNormal.isSelected()) {
			changes.firePropertyChange(
					this,
					"showAttr",
					getStringProperty("showAttr"),
			"Normal");
			setProperty("showAttr", "Normal");
		} else {
			changes.firePropertyChange(
					this,
					"showAttr",
					getStringProperty("showAttr"),
			"Hex");
			setProperty("showAttr", "Hex");

		}

		if (guiCheck.isSelected()) {
			changes.firePropertyChange(
					this,
					"guiInterface",
					getStringProperty("guiInterface"),
			YES);
			setProperty("guiInterface", YES);
		} else {
			changes.firePropertyChange(
					this,
					"guiInterface",
					getStringProperty("guiInterface"),
			NO);
			setProperty("guiInterface", NO);
		}

		if (guiShowUnderline.isSelected()) {
			changes.firePropertyChange(
					this, "guiShowUnderline",
					getStringProperty("guiShowUnderline"), YES);
			setProperty("guiShowUnderline", YES);
		} else {
			changes.firePropertyChange(
					this, "guiShowUnderline",
					getStringProperty("guiShowUnderline"), NO);
			setProperty("guiShowUnderline", NO);
		}

	}
}