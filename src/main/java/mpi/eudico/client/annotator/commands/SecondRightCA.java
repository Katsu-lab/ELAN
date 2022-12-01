package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;


/**
 * Action to move the media playhead one second to the right, one second 
 * forward.
 */
@SuppressWarnings("serial")
public class SecondRightCA extends CommandAction {
    private Icon icon;

    /**
     * Creates a new SecondRightCA instance
     *
     * @param theVM the viewer manager
     */
    public SecondRightCA(ViewerManager2 theVM) {
        //super();
        super(theVM, ELANCommandFactory.SECOND_RIGHT);

        icon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/1SecRightButton.gif"));
        putValue(SMALL_ICON, icon);
        putValue(Action.NAME, "");
    }

    /**
     * Creates a new {@code SecondRightCommand}.
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.SECOND_RIGHT);
    }

    /**
     * @return the media player
     */
    @Override
	protected Object getReceiver() {
        return vm.getMasterMediaPlayer();
    }
}
