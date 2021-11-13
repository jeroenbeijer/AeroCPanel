package gui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JTable;

import subscriber.SubscriberFactory;

class UnmuteAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JTable table;

	protected UnmuteAction(JTable table) {
		super("Unmute");
		this.table = table;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		  int row = table.convertRowIndexToModel(table.getEditingRow());
	      table.getModel().setValueAt("Unmuted", row, 2);
	      
	      SubscriberFactory.getSubscriber(row+1).setMuted(false);

	}
}