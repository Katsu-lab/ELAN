package mpi.eudico.client.annotator;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JOptionPane;

import mpi.eudico.client.annotator.linkedmedia.MediaDescriptorUtil;
import mpi.eudico.client.annotator.player.ElanMediaPlayer;
import mpi.eudico.client.annotator.player.EmptyMediaPlayer;
import mpi.eudico.client.annotator.player.PlayerFactory;
import mpi.eudico.client.annotator.util.FrameConstants;
import mpi.eudico.client.util.SelectableObject;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.util.MediaDescriptorUtility;
import nl.mpi.util.FileUtility;


/**
 * A manager that creates and updates menu items for visible/connected players
 * and viewers and that handles the action events generated by these items. It
 * saves and restores the  user's configuration.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class PlayerViewerMenuManager {
    private ElanFrame2 frame;
    private Transcription transcription;

    // all players that are present in the menu, stored as Selectable objects,
    // holding a MediaDescriptor and a selection flag
    private List<SelectableObject<MediaDescriptor>> menuPlayers;
    private final String HIDDEN_PLAYERS_KEY = "HiddenPlayers";

    /**
     * Creates a new PlayerViewerMenuManager instance
     *
     * @param frame the frame containing the menu's, players and viewers
     * @param transcription the transcription loaded in the frame, containing
     *        the media descriptors
     */
    public PlayerViewerMenuManager(ElanFrame2 frame, Transcription transcription) {
        super();
        this.frame = frame;
        this.transcription = transcription;
        menuPlayers = new ArrayList<SelectableObject<MediaDescriptor>>();
    }

    /**
     * Returns the list of visible players as stored in the preferences files.
     * In the preferences actually the hidden players are stored, by default
     * each player is visible.
     *
     * @return the list of visible players
     */
    public List<MediaDescriptor> getStoredVisiblePlayers() {
        // get the stored hidden players list, the url's are stored
        List<String> tempList = Preferences.getListOfString(HIDDEN_PLAYERS_KEY,
                transcription);

        final List<MediaDescriptor> mediaDescriptors = transcription.getMediaDescriptors();
		if (tempList == null) {
            return mediaDescriptors;
        } else {
            int size = mediaDescriptors.size();
            List<MediaDescriptor> visList = new ArrayList<MediaDescriptor>(size);

            for (MediaDescriptor md : mediaDescriptors) {

                if ((md.mediaURL != null) && !tempList.contains(md.mediaURL)) {
                    visList.add(md);
                }
            }

            return visList;
        }
    }

    /**
     * Adds an action for each video media descriptor to the {@code View -> Media
     * Player} menu and sets the selected and enabled state if possible.
     */
    public void initPlayerMenu() {
        List<MediaDescriptor> visuals = frame.getLayoutManager().getVisualPlayers();
        List<MediaDescriptor> descriptors = transcription.getMediaDescriptors();
        String fileName;
        int visibles = 0;

        for (MediaDescriptor md : descriptors) {

            if ((((md.mimeType != null) &&
                    (md.mimeType.equals(MediaDescriptor.WAV_MIME_TYPE))) ||
                    md.mimeType.equals(MediaDescriptor.GENERIC_AUDIO_TYPE))) {
                continue;
            }

            boolean curVisual = visuals.contains(md);
            boolean curValid = MediaDescriptorUtility.checkLinkStatus(md);

            if (!curVisual && (md.mimeType != null) &&
                    md.mimeType.equals(MediaDescriptor.UNKNOWN_MIME_TYPE)) {
                if (curValid && !isVideo(md)) {
                    continue;
                }
            }

            // if we get here create an action and menu item
            fileName = FileUtility.fileNameFromPath(md.mediaURL);

            PlayerAction action = new PlayerAction(md.mediaURL, fileName);

            if (!curValid) {
                action.setEnabled(false);
            }

            frame.addActionToMenu(action, FrameConstants.MEDIA_PLAYER, -1);

            if (curVisual && (visibles < 4)) {
                frame.setMenuSelected(md.mediaURL, FrameConstants.MEDIA_PLAYER);
                menuPlayers.add(new SelectableObject<MediaDescriptor>(md, true));
                visibles++;
            } else {
                menuPlayers.add(new SelectableObject<MediaDescriptor>(md, false));
            }
        }
    }

    /**
     * Called after a change in the linked media files.
     * This<ul>
     * <li>first stores the currently/previously hidden players
     * <li>removes all player menu items (that are no longer in the media descriptors)
     * <li>adds new menu items for new players - disables and destroys hidden
     * players
     * </ul>
     */
    public void reinitializePlayerMenu() {
        // store the current hidden players and remove current menu items
        List<String> hidden = new ArrayList<String>(6);

        for (SelectableObject sob : menuPlayers) {
        	MediaDescriptor md = (MediaDescriptor) sob.getValue();

            if (!sob.isSelected()) {
                hidden.add(md.mediaURL);
            }

            frame.removeActionFromMenu(md.mediaURL, FrameConstants.MEDIA_PLAYER);
        }

        if (hidden.size() > 0) {
            removeHiddenPlayers(hidden);
        }

        // new players have been created, populate the players menu again 
        // and remove the hidden players
        menuPlayers.clear();
        initPlayerMenu();
    }

    /**
     * Tests whether a media player can be created and whether it has a visual
     * component.
     *
     * @param md the media descriptor
     *
     * @return true if it is a video file
     */
    private boolean isVideo(MediaDescriptor md) {
        if (md == null) {
            return false;
        }

        ElanMediaPlayer player = null;

        try {
            player = PlayerFactory.createElanMediaPlayer(md);

            if (player.getVisualComponent() == null) {
                return false;
            } else {
                return true;
            }
        } catch (Exception ex) {
            // any exception, return false
            return false;
        } finally {
            if (player != null) {
                player.cleanUpOnClose(); //??
                player = null;
            }
        }
    }

    /**
     * Stores the video's that are hidden rather than the visible video's. By
     * default each video is visible.
     */
    private void savePreferences() {
        List<String> hidden = new ArrayList<String>();

        for (SelectableObject sob : menuPlayers) {
        	MediaDescriptor md = (MediaDescriptor) sob.getValue();

            if (!sob.isSelected()) {
                hidden.add(md.mediaURL);
            }
        }

        Preferences.set(HIDDEN_PLAYERS_KEY, hidden, transcription);
    }

    /**
     * Creates or destroys a player. Checks the number of current visual
     * players. Pops up a message when it is attempted to create more than 4
     * players.
     *
     * @param action the action that received the event
     * @param e the event
     */
    void playerActionPerformed(AbstractAction action, ActionEvent e) {
        if (e.getSource() instanceof AbstractButton) {
            boolean selected = ((AbstractButton) e.getSource()).isSelected();
            List<MediaDescriptor> visuals = frame.getLayoutManager().getVisualPlayers();

            if (!selected) {
                String url = (String) action.getValue(Action.LONG_DESCRIPTION);

                // update the menuPlayers list
                MediaDescriptor md = null;
                SelectableObject<MediaDescriptor> sob = null;

                for (int i = 0; i < menuPlayers.size(); i++) {
                    sob = menuPlayers.get(i);
                    md = sob.getValue();

                    if (md.mediaURL.equals(url)) {
                        sob.setSelected(false);

                        break;
                    }

                    if (i == (menuPlayers.size() - 1)) {
                        // nullify md if we get here
                        md = null;
                    }
                }
                
                if (md != null) {
                	removePlayer(md);
                }

            } else {
                String url = (String) action.getValue(Action.LONG_DESCRIPTION);

                // update the menuPlayers list
                MediaDescriptor md = null;
                SelectableObject<MediaDescriptor> sob = null;

                for (int i = 0; i < menuPlayers.size(); i++) {
                    sob = menuPlayers.get(i);
                    md = sob.getValue();

                    if (md.mediaURL.equals(url)) {
                        sob.setSelected(true);

                        break;
                    }

                    if (i == (menuPlayers.size() - 1)) {
                        // nullify md if we get here
                        md = null;
                    }
                }

                long mediaTime = frame.getViewerManager().getMasterMediaPlayer()
                                      .getMediaTime();

                // item is selected, if there are not already 4 visuals create a new player
                // else show warning and deselect the menuitem 
                if (visuals.size() < Constants.MAX_VISIBLE_PLAYERS) {
                    // create a player.
                    if (md != null) {
                        ElanMediaPlayer pl = MediaDescriptorUtil.createMediaPlayer((TranscriptionImpl) transcription,
                                md);

                        if (pl != null) {
                            if (frame.getViewerManager().getMasterMediaPlayer() instanceof EmptyMediaPlayer) {
                                frame.getViewerManager().setMasterMediaPlayer(pl);
                            } else if ((transcription.getMediaDescriptors()
                                                         .size() > 0) &&
                                    transcription.getMediaDescriptors().get(0)
                                                     .equals(md)) {
                                frame.getViewerManager().setMasterMediaPlayer(pl);
                            }

                            frame.getLayoutManager().add(pl);
                            pl.setMediaTime(mediaTime);
                        }
                    }
                } else {
                    // don't create a player, warning message and deselect menu item
                    JOptionPane.showMessageDialog(frame,
                        ElanLocale.getString("Player.MaxNumber") + " "+ Constants.MAX_VISIBLE_PLAYERS,
                        ElanLocale.getString("Message.Warning"),
                        JOptionPane.WARNING_MESSAGE);
                    ((AbstractButton) e.getSource()).setSelected(false);
                }
            }

            savePreferences();
        }
    }

    /**
     * Removes the players present in the list of hidden players from  the
     * viewer manager and layout manager.
     *
     * @param hidden a list of media url's
     */
    private void removeHiddenPlayers(List<String> hidden) {
        if ((hidden == null) || (hidden.size() == 0)) {
            return;
        }
        
        List<MediaDescriptor> visuals = frame.getLayoutManager().getVisualPlayers();

        for (MediaDescriptor md : visuals) {

            if (hidden.contains(md.mediaURL)) {
                // remove
                removePlayer(md);
            }
        }
    }

    /**
     * Removes the player corresponding to the descriptor from the viewer
     * manager as well as the layout manager, while ensuring that there is a
     * master player.
     *
     * @param md the media descriptor
     */
    private void removePlayer(MediaDescriptor md) {
        if ((md == null) || (md.mediaURL == null)) {
            return;
        }

        ElanMediaPlayer player = null;
        List<MediaDescriptor> visuals = frame.getLayoutManager().getVisualPlayers();
        MediaDescriptor otherMd = frame.getViewerManager().getMasterMediaPlayer()
                                       .getMediaDescriptor();

        if ((otherMd != null) && md.mediaURL.equals(otherMd.mediaURL)) {
            // remove the master, replace it by another
            player = frame.getViewerManager().getMasterMediaPlayer();

            long mediaTime = player.getMediaTime();
            List<ElanMediaPlayer> connectPlayers = frame.getViewerManager().getConnectedMediaPlayers();

            if (connectPlayers.size() == 0) {
                // new empty media player
                // the master media player cannot be removed directly
                // replace the master; the master is added to the connected
                frame.getViewerManager()
                     .setMasterMediaPlayer(new EmptyMediaPlayer(
                        Integer.MAX_VALUE));
                frame.getViewerManager().destroyMediaPlayer(player);
                frame.getLayoutManager().remove(player);
                frame.getViewerManager().getMasterMediaPlayer()
                     .setMediaTime(mediaTime);
            } else if (connectPlayers.size() == 1) { // probably EmptyMediaPlayer, should test??
            	if( connectPlayers.get(0) instanceof EmptyMediaPlayer){
            		long time = frame.getViewerManager().getTranscription().getLatestTime();
            		long minTime = 60000;
            		if(time < minTime){
            			time = minTime;
            		}
                	((EmptyMediaPlayer)connectPlayers.get(0)).setMediaDuration(time);
                }
            	
                frame.getViewerManager()
                     .setMasterMediaPlayer(connectPlayers.get(0));
                frame.getViewerManager().destroyMediaPlayer(player);
                frame.getLayoutManager().remove(player);
                frame.getViewerManager().getMasterMediaPlayer()
                     .setMediaTime(mediaTime);
                
            } else { // more than 1 connected players, one of them maybe an empty media player

                ElanMediaPlayer connPl = connectPlayers.get(0);

                if (connPl instanceof EmptyMediaPlayer ||
                        !visuals.contains(connPl.getMediaDescriptor())) {
                    ElanMediaPlayer nextPl;
connectedloop:  // find the first visual non empty media player
                    for (int i = 1; i < connectPlayers.size(); i++) {
                        if (!(connectPlayers.get(i) instanceof EmptyMediaPlayer)) {
                            nextPl = connectPlayers.get(i);      

                            for (int j = 0; j < visuals.size(); j++) {
                                if (visuals.get(j)
                                               .equals(nextPl.getMediaDescriptor())) {
                                    connPl = nextPl;

                                    break connectedloop;
                                }
                            }
                        }
                    }
                }

                frame.getViewerManager().setMasterMediaPlayer(connPl);
                frame.getViewerManager().destroyMediaPlayer(player);
                frame.getLayoutManager().remove(player);
                frame.getViewerManager().getMasterMediaPlayer()
                     .setMediaTime(mediaTime);
            }
        } else {
            // check the connected players           
            List<ElanMediaPlayer> connectPlayers = frame.getViewerManager().getConnectedMediaPlayers();
            ElanMediaPlayer connPl = null;

            for (int i = 0; i < connectPlayers.size(); i++) {
                connPl = connectPlayers.get(i);

                if (md.equals(connPl.getMediaDescriptor())) {
                    frame.getViewerManager().destroyMediaPlayer(connPl);
                    frame.getLayoutManager().remove(connPl);
                }
            }
        }
    }

    /**
     * An action class for menu items in the media players menu.
     *
     * @author Han Sloetjes
     */
    @SuppressWarnings("serial")
	class PlayerAction extends AbstractAction {
        /**
         * Creates a new PlayerAction instance.
         *
         * @param fileUrl the full url of the media file
         * @param fileName the file name of the media file
         */
        PlayerAction(String fileUrl, String fileName) {
            putValue(Action.NAME, fileName);
            // use LONG_DESCRIPTION or DEFAULT ?
            putValue(Action.LONG_DESCRIPTION, fileUrl);
        }

        /**
         * Handles selection and deselection of a player.  Delegates to this
         * manager.
         *
         * @param e action event
         */
        @Override
		public void actionPerformed(ActionEvent e) {
            PlayerViewerMenuManager.this.playerActionPerformed(this, e);
        }
    }
}
