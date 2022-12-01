package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;


/**
 * An action to move the crosshair or media playhead to the beginning of the file. 
 */
@SuppressWarnings("serial")
public class GoToBeginCA extends CommandAction {
    private Icon icon;

    /**
     * Creates a new GoToBeginCA instance
     *
     * @param theVM the viewer manager
     */
    public GoToBeginCA(ViewerManager2 theVM) {
        //super();
        super(theVM, ELANCommandFactory.GO_TO_BEGIN);

        icon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/GoToBeginButton.gif"));
        putValue(SMALL_ICON, icon);
        putValue(Action.NAME, "");
    }

    /**
     * Creates a new {@code GoToBeginCommand}.
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.GO_TO_BEGIN);
    }

    /**
     * @return the master media player
     */
    @Override
	protected Object getReceiver() {
        return vm.getMasterMediaPlayer();
    }
}
