package mpi.search.content.query.viewer;

import java.awt.Component;
import java.awt.event.*;
import javax.swing.JPopupMenu;

/**
 * Base class for popup menusfor constraint panels.
 * 
 * @author Alexander Klassmann
 * @version Sep 28, 2004
 */
@SuppressWarnings("serial")
abstract public class AbstractPopupMenu extends JPopupMenu implements MouseListener, ActionListener{
	protected final Component component;
	protected final AbstractConstraintPanel constraintPanel;
	
	public AbstractPopupMenu(Component component, AbstractConstraintPanel constraintPanel){
		this.component = component;
		this.constraintPanel = constraintPanel;
		component.addMouseListener(this);
		fillMenu();
	}
	
	abstract protected void fillMenu();
}
