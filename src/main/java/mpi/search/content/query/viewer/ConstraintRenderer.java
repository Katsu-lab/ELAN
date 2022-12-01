package mpi.search.content.query.viewer;

import java.awt.Component;


import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;


import mpi.search.content.query.model.Constraint;


/**
 *
 * @author klasal
 */
public class ConstraintRenderer implements TreeCellRenderer {

    /**
     * Configures a renderer for a constraint.
     *
     * @param tree the tree
     * @param value the constraint to render
     * @param selected the selected state of the tree node
     * @param expanded whether the node is expanded
     * @param leaf whether the cell is a leaf
     * @param row the tree row of this cell 
     * @param hasFocus whether the cell has focus
     *
     * @return the configured cell editor
     */
    @Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
        boolean selected, boolean expanded, boolean leaf, int row,
        boolean hasFocus) {
        JLabel label = new JLabel();

        if (value instanceof Constraint) {
        	label.setFont(tree.getFont());
            label.setText(Query2HTML.translate((Constraint) value)); 
        }

        label.setOpaque(false);

        return label;
    }
}
