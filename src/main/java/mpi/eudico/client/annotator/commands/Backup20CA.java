package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ViewerManager2;


/**
 * A CommandAction to set the backup delay to 20 minutes.
 *
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class Backup20CA extends CommandAction {
    /** the argument array, containing the constant for backup every 20 minutes */
    private final Object[] arg = new Object[] { Constants.BACKUP_20 };

    /**
     * Creates a new Backup20CA instance
     *
     * @param viewerManager the viewer manager
     */
    public Backup20CA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.BACKUP_20);
    }

    /**
     * Creates a new Backup Command.
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.BACKUP);
    }

    /**
     * The receiver is BackupCA.
     *
     * @return the {@link BackupCA action}
     */
    @Override
	protected Object getReceiver() {
        return ELANCommandFactory.getCommandAction(vm.getTranscription(),
            ELANCommandFactory.BACKUP);
    }

    /**
     * @return the final, one element argument array
     */
    @Override
	protected Object[] getArguments() {
        return arg;
    }
}
