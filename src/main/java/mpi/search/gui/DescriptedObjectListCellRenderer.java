package mpi.search.gui;

import java.awt.Component;

import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

@SuppressWarnings("serial")
public class DescriptedObjectListCellRenderer extends BasicComboBoxRenderer {
    /**
     * Adds the description of the value as tooltip to the list
     *
     */
    @Override
	public Component getListCellRendererComponent(
        JList list, Object value, int index, boolean isSelected, boolean celHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, celHasFocus);
        
        if (isSelected && value instanceof DescriptedObject) {
            list.setToolTipText(((DescriptedObject)value).getDescription());            
        }
        
        return this;
    }

}
