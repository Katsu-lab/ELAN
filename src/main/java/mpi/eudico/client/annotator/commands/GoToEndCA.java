package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;


/**
 * An action to move the media position to the end of the media file.
 */
@SuppressWarnings("serial")
public class GoToEndCA extends CommandAction {
    private Icon icon;

    /**
     * Creates a new GoToEndCA instance
     *
     * @param theVM the viewer manager
     */
    public GoToEndCA(ViewerManager2 theVM) {
        //super();
        super(theVM, ELANCommandFactory.GO_TO_END);

        icon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/GoToEndButton.gif"));
        putValue(SMALL_ICON, icon);
        putValue(Action.NAME, "");
    }

    /**
     * Creates a new {@code GoToEndCommand}. 
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.GO_TO_END);
    }

    /**
     * @return the media player
     */
    @Override
	protected Object getReceiver() {
        return vm.getMasterMediaPlayer();
    }
}
