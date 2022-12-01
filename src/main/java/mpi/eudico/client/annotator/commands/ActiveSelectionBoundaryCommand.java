package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ElanMediaPlayerController;
import mpi.eudico.client.annotator.Selection;
import mpi.eudico.client.annotator.player.ElanMediaPlayer;


/**
 * A Command to set the active boundary of the selection.
 * @see ActiveSelectionBoundaryCA
 */
public class ActiveSelectionBoundaryCommand implements Command {
    private String commandName;

    /**
     * Creates a new ActiveSelectionBoundaryCommand instance
     *
     * @param theName the name of the command
     */
    public ActiveSelectionBoundaryCommand(String theName) {
        commandName = theName;
    }

    /**
     * @param receiver the media player controller
     * @param arguments <ul><li>[0] = ElanMediaPlayer</li>
     * <li>[1] = the Selection</li>
     * <li>[2] = the ActiveSelectionBoundaryCA</li></ul>
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        // receiver is master ElanMediaPlayerController
        // arguments[0] is ElanMediaPlayer
        // arguments[1] is Selection
        // arguments[2] is ActiveSelectionBoundaryCA
        ElanMediaPlayerController mediaPlayerController = (ElanMediaPlayerController) receiver;
        ElanMediaPlayer player = (ElanMediaPlayer) arguments[0];
        Selection selection = (Selection) arguments[1];
        ActiveSelectionBoundaryCA ca = (ActiveSelectionBoundaryCA) arguments[2];

        if (player == null) {
            return;
        }

        if (player.isPlaying()) {
            return;
        }

        long beginTime = selection.getBeginTime();
        long endTime = selection.getEndTime();

        if (beginTime == endTime) {
            return;
        }

        mediaPlayerController.toggleActiveSelectionBoundary();

        if (mediaPlayerController.isBeginBoundaryActive()) {
            //		ca.setLeftIcon(false);
            player.setMediaTime(beginTime);
        } else {
            //		ca.setLeftIcon(true);
            player.setMediaTime(endTime);
        }
    }

    /**
     * @return the name
     */
    @Override
	public String getName() {
        return commandName;
    }
}
