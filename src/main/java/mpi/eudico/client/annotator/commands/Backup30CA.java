package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ViewerManager2;


/**
 * A CommandAction to set the backup delay to 30 minutes.
 *
 * @author Han Sloetjes
 */
public class Backup30CA extends CommandAction {
    /** the argument array, containing the constant for backup every 30 minutes */
    private final Object[] arg = new Object[] { Constants.BACKUP_30 };

    /**
     * Creates a new Backup30CA instance
     *
     * @param viewerManager the viewer manager
     */
    public Backup30CA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.BACKUP_30);
    }

    /**
     * Creates a new {@link BackupCA}
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.BACKUP);
    }

    /**
     * The receiver is BackupCA.
     *
     * @return {@link BackupCA}
     */
    @Override
	protected Object getReceiver() {
        return ELANCommandFactory.getCommandAction(vm.getTranscription(),
            ELANCommandFactory.BACKUP);
    }

    /**
     *
     * @return the array containing the one constant
     */
    @Override
	protected Object[] getArguments() {
        return arg;
    }
}
