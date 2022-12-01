package mpi.eudico.client.annotator.commands.global;

import java.awt.event.ActionEvent;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.gui.ActivityMonitoringDialog;

/**
 * A menu action that creates and shows a dialog to configure Activity 
 * Monitoring.
 *
 * @author Aarthy Somasundaram
 */
@SuppressWarnings("serial")
public class ActivityMonitoringMA extends FrameMenuAction {
   
    public ActivityMonitoringMA(String name, ElanFrame2 frame) {
        super(name, frame);
    }

    /**
     * Shows the dialog.
     *
     * @param e the event
     */
    @Override
	public void actionPerformed(ActionEvent e) {
    	ActivityMonitoringDialog.getInstance().setLocationRelativeTo(frame);
    	ActivityMonitoringDialog.getInstance().setVisible(true);      
    }
}