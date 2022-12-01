package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ViewerManager2;


/**
 * A CommandAction to set the backup delay to 10 minutes.
 *
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class Backup10CA extends CommandAction {
    /** the argument array, containing the constant for backup every 10 minutes */
    private final Object[] arg = new Object[] { Constants.BACKUP_10 };

    /**
     * Creates a new Backup10CA instance
     *
     * @param viewerManager the viewer manager
     */
    public Backup10CA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.BACKUP_10);
    }

    /**
     * Creates a new Backup command.
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.BACKUP);
    }

    /**
     * The receiver is BackupCA.
     *
     * @return the {@link BackupCA}
     */
    @Override
	protected Object getReceiver() {
        return ELANCommandFactory.getCommandAction(vm.getTranscription(),
            ELANCommandFactory.BACKUP);
    }

    /**
     *
     * @return the array with one element, {@link #arg}
     */
    @Override
	protected Object[] getArguments() {
        return arg;
    }
}
