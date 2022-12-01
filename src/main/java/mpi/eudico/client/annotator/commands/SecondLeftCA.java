package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;


/**
 * A command action to move the media playhead one second backward.
 */
@SuppressWarnings("serial")
public class SecondLeftCA extends CommandAction {
    private Icon icon;

    /**
     * Creates a new SecondLeftCA instance
     *
     * @param theVM the viewer manager
     */
    public SecondLeftCA(ViewerManager2 theVM) {
        //super();
        super(theVM, ELANCommandFactory.SECOND_LEFT);

        icon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/1SecLeftButton.gif"));
        putValue(SMALL_ICON, icon);
        putValue(Action.NAME, "");
    }

    /**
     * Creates a new {@code SecondLeftCommand}
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.SECOND_LEFT);
    }

    /**
     * @return the media player
     */
    @Override
	protected Object getReceiver() {
        return vm.getMasterMediaPlayer();
    }
}
