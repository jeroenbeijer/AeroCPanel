package gui;

import java.util.*;

import javax.swing.JButton;
import javax.swing.JPanel;

class ButtonsPanel extends JPanel {
	  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public final List<JButton> buttons = Arrays.asList(new JButton("Unmute"), new JButton("Mute"));
	  protected ButtonsPanel() {
	      super();
	      setOpaque(true);
	      for (JButton b: buttons) {
	          b.setFocusable(false);
	          b.setRolloverEnabled(false);
	          add(b);
	      }
	  }

	}
