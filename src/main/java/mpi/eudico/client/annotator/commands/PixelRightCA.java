package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.TimeScale;
import mpi.eudico.client.annotator.ViewerManager2;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;


/**
 * An action to move the media playhead one pixel to the right.
 */
@SuppressWarnings("serial")
public class PixelRightCA extends CommandAction {
    private Icon icon;

    /**
     * Creates a new PixelRightCA instance
     *
     * @param theVM the viewer manager
     */
    public PixelRightCA(ViewerManager2 theVM) {
        //super();
        super(theVM, ELANCommandFactory.PIXEL_RIGHT);

        icon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/1PixelRightButton.gif"));
        putValue(SMALL_ICON, icon);
        putValue(Action.NAME, "");
    }

    /**
     * Creates a new {@code PixelRightCommand}.
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.PIXEL_RIGHT);
    }

    /**
     * @return the media player
     */
    @Override
	protected Object getReceiver() {
        return vm.getMasterMediaPlayer();
    }

    /**
     * @return an array containing the {@code TimeScale} instance
     */
    @Override
	protected Object[] getArguments() {
        Object[] args = new Object[1];
        args[0] = vm.getTimeScale();

        return args;
    }
}
