package gui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

class ColorRenderer extends JLabel implements TableCellRenderer {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int color = 0;

	public ColorRenderer() {
		setOpaque(true);
		color = 0;
	}
	

	public int getColor() {
		return color;
	}


	public void setColor(int color) {
		this.color = color;
	}


	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		if (color == 1)
			setBackground(Color.red);
		else
			setBackground(Color.gray);

		return this;
	}
}
