/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spotifyassignmentv2;

import com.sun.javafx.collections.ObservableListWrapper;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Slider;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javafx.util.Duration;
import javax.imageio.ImageIO;




/**
 *
 * @author bergeron
 */
public class FXMLDocumentController implements Initializable {
    @FXML
    TableView tracksTableView;
    
    @FXML
    Slider trackSlider;
    
    @FXML
    Label labelAlbum;
    
    @FXML
    Label labelArtist;
    
    @FXML
    TextField textFieldSearchBar;
    
    @FXML
    Button buttonPrev;
    
    
    @FXML
    Button buttonNext;
    
    @FXML
    Button buttonPlay;
    
    @FXML
    Label labelDenominator;
    
    @FXML 
    Label labelSongTime;
    
    @FXML
    ImageView albumImageView;
    
    @FXML
    ProgressIndicator indicator;
    
    @FXML 
    Button buttonSave;
    
    // Other Fields...
    ScheduledExecutorService sliderExecutor = null;
    MediaPlayer mediaPlayer = null;
    
    ArrayList<Album> albums = null;
    int currentAlbumIndex = 0;
    
    

    
    @FXML
    private void handleButtonPrev(Event event)
    {   
        currentAlbumIndex--;
        
        if (currentAlbumIndex < 0)
        {
            currentAlbumIndex = albums.size()-1;            
        }
        
        displayAlbum(currentAlbumIndex);
        System.out.println("Next Button Pressed!");
        System.out.println(currentAlbumIndex);
    }
    
    @FXML
    private void handleButtonNext(Event event)
    {
        currentAlbumIndex++;
        
        if (currentAlbumIndex > albums.size()-1)
        {
            currentAlbumIndex = 0;
        }
        
        
        displayAlbum(currentAlbumIndex);
        System.out.println("Next Button Pressed!");
        System.out.println(currentAlbumIndex);
    }
    
    @FXML 
    void handleButtonSave(ActionEvent event)
    {
        Image image = albumImageView.getImage();
        //Image image = new Image(albumImageView.getImage().toString());

        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
        
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Images", "*.*"),
                new FileChooser.ExtensionFilter("JPG", "*.jpg"),
                new FileChooser.ExtensionFilter("PNG", "*.png")
        );
        
        chooser.setTitle("  Save Files");
        File file = chooser.showSaveDialog(labelAlbum.getScene().getWindow());
        
        if(file != null)
        {
            
        }

        try
        {
            ImageIO.write(bufferedImage, "png", file);
        }
        catch(IOException e)
        {        
            e.printStackTrace();
        }   
    }
    
    @FXML
    private void handleSearchBar(Event event)
    {
       
            textFieldSearchBar.setDisable(true);

            indicator.setVisible(true);

            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
            executor.submit(new Task<Void>(){
                @Override
                protected Void call() throws Exception {
                    try{
                    searchAlbumsFromArtist(textFieldSearchBar.getText().toString());        
                    
                    }catch(Exception e)
                    {
                        cancel();
                    }
                    return null;
                }

                @Override
                protected void succeeded()
                {
                    displayAlbum(0);            
                    textFieldSearchBar.setDisable(false);
                    indicator.setVisible(false);   
                    buttonSave.setDisable(false);
                   
                }
                
                @Override
                protected void cancelled()
                {
                    labelArtist.setText("ERROR");                        
                    labelAlbum.setText("Artist Not Found!");
                     //albumImageView.setImage(new Image("/images/error.png"));
                    
                    indicator.setVisible(false);  
                    textFieldSearchBar.setDisable(false);
                }
            });
       
    }

    private void playPauseTrackPreview( Button source, String trackPreviewUrl)
    {
        try
        {
            if (source.getText().equals("Play"))
            {
                if (mediaPlayer != null)
                {
                    mediaPlayer.stop();                
                }

                source.setText("Stop");
                trackSlider.setDisable(false);
                trackSlider.setValue(0.0);

                // Start playing music
                Media music = new Media(trackPreviewUrl);
                mediaPlayer = new MediaPlayer(music);
                mediaPlayer.play();
                
                
                // This runnable object will be called
                // when the track is finished or stopped
                Runnable stopTrackRunnable = new Runnable(){
                    @Override
                    public void run() {
                        source.setText("Play");
                        if (sliderExecutor != null)
                        {                                              
                            sliderExecutor.shutdownNow();
                        }
                    }                
                };                
                mediaPlayer.setOnEndOfMedia(stopTrackRunnable);
                mediaPlayer.setOnStopped(stopTrackRunnable);

                // Schedule the slider to move right every second
                sliderExecutor = Executors.newSingleThreadScheduledExecutor();
                sliderExecutor.scheduleAtFixedRate(new Runnable(){
                    @Override
                    public void run() {
                        // We can't update the GUI elements on a separate thread... 
                        // Let's call Platform.runLater to do it in main thread!!
                        Platform.runLater(new Runnable(){
                            @Override
                            public void run() {
                                // Move slider
                                trackSlider.setValue(trackSlider.getValue() + 0.1);
                            }
                        });
                    }
                }, 100, 100, TimeUnit.MILLISECONDS);
                    
                
            }
            else
            {
                if (mediaPlayer != null)
                {
                    mediaPlayer.stop();
                }                
            }
        }
        catch(Exception e)
        {
            System.err.println("error with slider executor... this should not happen!");
        }
    }
    
    private void displayAlbum(int albumNumber)
    {
        // TODO - Display all the informations about the album
        //
        //        Artist Name 
        //        Album Name
        //        Album Cover Image
        //        Enable next/previous album buttons, if there is more than one album
        
        
        // Display Tracks for the album passed as parameter
        if (albumNumber >=0 && albumNumber < albums.size())
        {
            currentAlbumIndex = albumNumber;
            Album album = albums.get(albumNumber);
            
            labelArtist.setText(album.getArtistName());
            labelAlbum.setText(album.getAlbumName());
            albumImageView.setImage(new Image(album.getImageURL()));
            
            if(albums.size() == 1)
            {
                buttonPrev.setDisable(true);
                buttonNext.setDisable(true);
            }else
            {
                buttonPrev.setDisable(false);
                buttonNext.setDisable(false);
            }
            
            
            // Set tracks
            ArrayList<TrackForTableView> tracks = new ArrayList<>();
            for (int i=0; i<album.getTracks().size(); ++i)
            {
                TrackForTableView trackForTable = new TrackForTableView();
                Track track = album.getTracks().get(i);
                trackForTable.setTrackNumber(track.getNumber());
                trackForTable.setTrackTitle(track.getTitle());
                trackForTable.setTrackPreviewUrl(track.getUrl());
                tracks.add(trackForTable);
            }
            tracksTableView.setItems(new ObservableListWrapper(tracks));

            trackSlider.setDisable(true);
            trackSlider.setValue(0.0);                       
        }
    }   
    
    
    private void searchAlbumsFromArtist(String artistName) throws Exception
    {
        // TODO - Make sure this is not blocking the UI
        currentAlbumIndex = 0;
        String artistId = SpotifyController.getArtistId(artistName);        
        albums = SpotifyController.getAlbumDataFromArtist(artistId);         
    }
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        // Setup Table View
        TableColumn<TrackForTableView, Number> trackNumberColumn = new TableColumn("#");
        trackNumberColumn.setCellValueFactory(new PropertyValueFactory("trackNumber"));
        
        TableColumn trackTitleColumn = new TableColumn("Title");
        trackTitleColumn.setCellValueFactory(new PropertyValueFactory("trackTitle"));
        trackTitleColumn.setPrefWidth(250);
        
         
        
        TableColumn playColumn = new TableColumn("Preview");
        playColumn.setCellValueFactory(new PropertyValueFactory("trackPreviewUrl"));
        Callback<TableColumn<TrackForTableView, String>, TableCell<TrackForTableView, String>> cellFactory = new Callback<TableColumn<TrackForTableView, String>, TableCell<TrackForTableView, String>>(){
            @Override
            public TableCell<TrackForTableView, String> call(TableColumn<TrackForTableView, String> param) {
                final TableCell<TrackForTableView, String> cell = new TableCell<TrackForTableView, String>(){
                    final Button playButton = new Button("Play");

                    @Override
                    public void updateItem(String item, boolean empty)
                    {
                        if (item != null && item.equals("") == false){
                            playButton.setOnAction(event -> {
                                playPauseTrackPreview(playButton, item);
                            });
    
                            setGraphic(playButton);
                        }
                        else{                        
                            setGraphic(null);
                        }

                        setText(null);                        
                    }
                };
                
                return cell;
            }
        };
        playColumn.setCellFactory(cellFactory);
        tracksTableView.getColumns().setAll(trackNumberColumn, trackTitleColumn, playColumn);

        
        trackSlider.valueProperty().addListener(new ChangeListener<Number>(){
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if(newValue == null){
                    return;                            
                }
                
                int value = (int)newValue.doubleValue();
                
                if(value < 10)
                {
                    labelSongTime.setText("0:0" + value + "");                    
                }
                else
                
                    labelSongTime.setText("0:" + value + "");
                }

            });

        
        
        // When slider is released, we must seek in the song
        trackSlider.setOnMouseReleased(new EventHandler() {
            @Override
            public void handle(Event event) {
                if (mediaPlayer != null)
                {
                    mediaPlayer.seek(Duration.seconds(trackSlider.getValue()));
                }
            }
        });
        
        //Initialize GUI
        
        try{
        searchAlbumsFromArtist("pink floyd");
        displayAlbum(0);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
       
        
    }        

}
