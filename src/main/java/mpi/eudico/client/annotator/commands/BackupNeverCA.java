package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ViewerManager2;


/**
 * A CommandAction to set the backup delay to 0, meaning no automatic backup.
 *
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class BackupNeverCA extends CommandAction {
    /** the argument array, containing the constant for backup never */
    private final Object[] arg = new Object[] { Constants.BACKUP_NEVER };

    /**
     * Creates a new BackupNeverCA instance
     *
     * @param viewerManager the viewer manager
     */
    public BackupNeverCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.BACKUP_NEVER);
    }

    /**
     * Creates a new backup command
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.BACKUP);
    }

    /**
     * The receiver is BackupCA.
     *
     * @return {@code BackupCA}
     */
    @Override
	protected Object getReceiver() {
        return ELANCommandFactory.getCommandAction(vm.getTranscription(),
            ELANCommandFactory.BACKUP);
    }

    /**
     * @return the one item array containing the {@link Constants#BACKUP_NEVER} constant  
     */
    @Override
	protected Object[] getArguments() {
        return arg;
    }
}
