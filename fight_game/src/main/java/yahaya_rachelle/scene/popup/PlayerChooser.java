package yahaya_rachelle.scene.popup;

import java.util.ArrayList;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import yahaya_rachelle.actor.Character;
import yahaya_rachelle.configuration.Config;
import yahaya_rachelle.configuration.Configurable.ConfigGetter;
import yahaya_rachelle.data.GameDataManager;
import yahaya_rachelle.scene.scene.GameScene;
import yahaya_rachelle.utils.GameContainerCallback;

public class PlayerChooser extends ScenePopup{

    public PlayerChooser(GameScene linkedScene, GameContainerCallback toDoOnConfirm) {
        super(linkedScene, toDoOnConfirm);
    }

    @Override
    protected Parent buildPopup() {
        GameDataManager manager = this.linkedScene.getGameDataManager();

        ConfigGetter<Long> configLongGetter = new ConfigGetter<Long>(this.linkedScene.getGame() );

        double width = configLongGetter.getValueOf(Config.App.WINDOW_WIDTH.key).doubleValue();
        double height = configLongGetter.getValueOf(Config.App.WINDOW_HEIGHT.key).doubleValue();

        // création du conteneur
        VBox container = new VBox(30);

        container.setPrefSize(width / 2,height / 2);
        container.setBackground(new Background(new BackgroundImage(manager.getItems().getImage(Config.Items.PARCHMENT_TEXTURE.key),BackgroundRepeat.REPEAT,BackgroundRepeat.REPEAT,BackgroundPosition.DEFAULT,new BackgroundSize(100,100,true,true,true, false) ) ) );
        container.setTranslateX(width / 4);
        container.setTranslateY(height / 4);
        container.setPadding(new Insets(10,10,10,10) );

        ObservableList<Node> children = container.getChildren();

        Label zoneTitle = new Label("Choisissez un pseudo et votre personnage");

        zoneTitle.setFont(manager.getFonts().getFont(Config.Fonts.BASIC.key,15) );

        TextField pseudoChooser = new TextField();

        pseudoChooser.setPromptText("Choisir un pseudo");
        pseudoChooser.setMaxWidth(width / 4);
        pseudoChooser.setMinWidth(width / 4);

        children.addAll(zoneTitle,pseudoChooser,this.createCharactersChooserZone(container,pseudoChooser),this.addExitButton(container) );

        return container;
    }
    
    /**
     * 
     * @return la zone scrollable d'affichage des personnages
     */
    private ScrollPane createCharactersChooserZone(VBox parent,TextField pseudoChooser){
        ArrayList<Character> characters = this.linkedScene.getGameDataManager().getCharacters();

        HBox container = new HBox(10);

        ObservableList<Node> children = container.getChildren();

        characters.forEach(character -> this.addNewCharacter(parent,children,character,pseudoChooser) );        

        ScrollPane scrollableZone = new ScrollPane(container);

        scrollableZone.setPadding(new Insets(10,5,10,5) );
        scrollableZone.setMinHeight(130);

        return scrollableZone;
    }

    /**
     * crée le boutton d'annulation
     * @return
     */
    private Button addExitButton(VBox container){
        Button button = new Button("Quitter");

        button.setOnMouseClicked((e) -> this.toDoOnConfirm.action(new ChoosedData(container),true) );

        String color = "#C77F4F";
        String hoverColor = "#98572c";

        // design du boutton
        button.setBackground(new Background(new BackgroundFill(Paint.valueOf(color),CornerRadii.EMPTY,Insets.EMPTY) ) );
        button.setFont(this.linkedScene.getGameDataManager().getFonts().getFont(Config.Fonts.BASIC.key,15) );
        button.setWrapText(true);
        button.setOnMouseExited((e) -> {
            button.setBackground(new Background(new BackgroundFill(Paint.valueOf(color),CornerRadii.EMPTY,Insets.EMPTY) ) );
        });
        button.setOnMouseEntered((e) -> {
            button.setBackground(new Background(new BackgroundFill(Paint.valueOf(hoverColor),CornerRadii.EMPTY,Insets.EMPTY) ) );
        });

        return button;
    }

    /**
     * ajoute un personnsage dans la zone de choix 
     * @param children
     * @param character
     */
    private void addNewCharacter(VBox container,ObservableList<Node> children,Character character,TextField pseudoChooser){
        VBox characterContainer = new VBox(10);

        Label nameLabel = new Label(character.getName() );

        nameLabel.setFont(this.linkedScene.getGameDataManager().getFonts().getFont(Config.Fonts.BASIC.key,12) );

        ImageView imageViewer = new ImageView(character.getActionSequence(Config.PlayerAction.STATIC_POSITION).get(0) );

        imageViewer.setFitWidth(70);
        imageViewer.setFitHeight(70);

        imageViewer.setOnMouseEntered((e) -> {
            imageViewer.setOpacity(0.7);
        });

        imageViewer.setOnMouseExited((e) -> {
            imageViewer.setOpacity(1);
        });

        // confirmation de choix au click
        imageViewer.setOnMouseClicked((e) -> {
            String choosedPseudo = pseudoChooser.getText();

            if(choosedPseudo.length() < 2)
                return;

            this.toDoOnConfirm.action(new ChoosedData(container,character,choosedPseudo), false);
        });

        characterContainer.getChildren().addAll(imageViewer,nameLabel); 

        children.add(characterContainer);
    }

    public class ChoosedData{
        private VBox container;
        
        private Character choosedCharacter;
        
        private String choosedPseudo;

        public ChoosedData(VBox container){
            this.container = container;
        }

        public ChoosedData(VBox container,Character choosedCharacter,String choosedPseudo){
            this(container);
            this.choosedCharacter = choosedCharacter;
            this.choosedPseudo = choosedPseudo;
        }

        public VBox getContainer(){
            return this.container;
        }

        public Character getChoosedCharacter(){
            return this.choosedCharacter;
        }

        public String getChoosedPseudo(){
            return this.choosedPseudo;
        }
    }
}