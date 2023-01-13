package yahaya_rachelle.game.scene.popup;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import yahaya_rachelle.game.game.Game;
import yahaya_rachelle.game.game.GameDataManager;
import yahaya_rachelle.game.game.GameLoader.Key;
import yahaya_rachelle.game.player.Player.PlayerAction;
import yahaya_rachelle.game.scene.scene.GameScene;
import yahaya_rachelle.game.utils.GameContainerCallback;

public class CreatePlayer extends ScenePopup{

    private HashMap<PlayerAction,ArrayList<Image> > actionsSequences;

    private double width;
    private double height;

    private ReentrantLock locker;

    public CreatePlayer(GameScene linkedScene,GameContainerCallback toDoOnConfirm) {
        super(linkedScene,toDoOnConfirm);
    }

    @Override
    protected Pane buildPopup() {
        this.actionsSequences = new HashMap<PlayerAction,ArrayList<Image> >();
        this.width = Game.GAME_WINDOW_WIDTH;
        this.height = Game.GAME_WINDOW_HEIGHT;
        this.locker = new ReentrantLock();
        
        GameDataManager manager = this.linkedScene.getGameDataManager();

        VBox container = new VBox(20);

        container.setPadding(new Insets(10,20,10,20) );
        container.setBackground(new Background(new BackgroundImage(manager.getItemsMap().get(Key.ITEM_PARCHMENT_TEXTURE),BackgroundRepeat.REPEAT,BackgroundRepeat.REPEAT,BackgroundPosition.DEFAULT,BackgroundSize.DEFAULT) ) );

        ObservableList<Node> children = container.getChildren();

        ScrollPane scrollableZone = new ScrollPane(container);

        scrollableZone.setVbarPolicy(ScrollBarPolicy.NEVER);
        scrollableZone.setMaxHeight(this.height - 30);

        Font specialNormal = Font.loadFont(manager.getFontsMap().get(Key.FONT_NORMAL),14);

        Label title = new Label("Creer un personnage");
        Label message = new Label("Veuillez entrez les images");

        title.setFont(Font.loadFont(manager.getFontsMap().get(Key.FONT_SPECIAL),25) );
        message.setFont(Font.font(null,FontWeight.NORMAL,FontPosture.ITALIC, 13) );

        children.addAll(title,message);

        this.addAttackZone(specialNormal,children,manager);
        this.addSuperAttackZone(specialNormal,children,manager);
        this.addRunZone(specialNormal,children,manager);
        this.addDeathZone(specialNormal,children,manager);
        this.addFallZone(specialNormal,children,manager);
        this.addJumpZone(specialNormal,children,manager);
        this.addStaticZone(specialNormal,children,manager);
        this.addTakeHitZone(specialNormal,children,manager);
        this.addConfirmation(specialNormal,children,message,manager);

        return new VBox(scrollableZone);
    }

    /**
     * crée une ligne d'ajout
     * le titre prend 15% de la taille
     * la zone d'affichage prend 55%
     * la zone de preview et le boutton d'ajout prend 25%
     * @return le conteneur final
     */
    public HBox createZone(Font font,String zoneTitle,ArrayList<Image> imageList,GameDataManager manager){
        HashMap<Key,String> fontsMap = manager.getFontsMap();

        HBox container = new HBox(10);

        ObservableList<Node> children = container.getChildren();

        // création du titre de la ligne
        Label zoneTitleLabel = new Label(zoneTitle);

        zoneTitleLabel.setFont(Font.loadFont(fontsMap.get(Key.FONT_NORMAL),14) );
        zoneTitleLabel.setMaxWidth((15.0 / 100.0) * this.width);
        zoneTitleLabel.setMinWidth((15.0 / 100.0) * this.width);
        zoneTitleLabel.setWrapText(true);

        children.add(zoneTitleLabel);

        // création de la zone d'affichage des images
        HBox imagesListContainer = new HBox(30);

        ScrollPane scrollableZone = new ScrollPane(imagesListContainer);

        double size = (55.0 / 100.0) * this.width;

        scrollableZone.setMinWidth(size);
        scrollableZone.setMaxWidth(size);
        scrollableZone.setPadding(new Insets(5,2,10,5) );
        scrollableZone.setVbarPolicy(ScrollBarPolicy.NEVER);

        children.add(scrollableZone);

        // ajout de la zone d'ajout et de preview

        HBox apZone = new HBox(5);

        Button addButton = this.getCustomButton("Ajout",font);

        size = (25.0 / 100.0) * this.width;

        StackPane previewContainer = new StackPane();

        final double imageWidth = size / 2;

        // afficheur de l'image
        ImageView imageView = new ImageView();

        imageView.setFitWidth(imageWidth);
        imageView.setFitHeight(90);
        
        previewContainer.getChildren().add(imageView);

        previewContainer.setMinSize(imageWidth,130);
        previewContainer.setMaxSize(imageWidth,130);
        
        apZone.getChildren().addAll(previewContainer,addButton);
        apZone.setAlignment(Pos.CENTER_LEFT);
        apZone.setMinWidth(size);
        apZone.setMaxWidth(size);

        children.add(apZone);

        container.setMinHeight(90);
        container.setAlignment(Pos.CENTER_LEFT);

        ArrayList<Image> previewImageList = new ArrayList<Image>();

        Timeline previewTimeline = new Timeline(new KeyFrame(Duration.millis(100),(e) -> {
            // on bloque l'accès à la liste d'images
            this.locker.lock();

            try
            {   
                Image image = previewImageList.remove(0);

                imageView.setImage(image);
                
                previewImageList.add(image);
            }
            catch(IndexOutOfBoundsException boundException){}

            // on remet l'accès
            this.locker.unlock();
        }) );

        previewTimeline.setCycleCount(Animation.INDEFINITE);

        // ajout de l'ajout d'images
        this.addNewImageZone(addButton,imageList,previewImageList,imagesListContainer,PlayerAction.ATTACK,manager,imageWidth,previewTimeline);

        return container;
    }

    public void addNewImageZone(Button addButton,ArrayList<Image> imageList,ArrayList<Image> previewList,HBox imagesListContainer,PlayerAction actionType,GameDataManager manager,final double imageWidth,Timeline previewTimeline)
    {
        addButton.setOnMouseClicked((e) -> {

            // on lance la timeline de preview à la première image
            if(imageList.size() == 0)
                previewTimeline.play();

            
            // on crée la boxe conteneur de l'image
            VBox addZone = new VBox(10);

            ImageView imageView = new ImageView();

            imageView.setFitWidth(90);
            imageView.setFitHeight(90);

            Button chooser = new Button("Choisir");

            addZone.setAlignment(Pos.CENTER_LEFT);
            addZone.getChildren().addAll(imageView,chooser);
            addZone.setMaxSize(imageWidth,100);
            addZone.setMinSize(imageWidth,100);
            
            imagesListContainer.getChildren().add(addZone);

            FileChooser fileChooser = new FileChooser();

            PreviewHelper helper = new PreviewHelper();

            // évenement d'ajout d'image
            chooser.setOnMouseClicked((clickEvent) -> {
                File choosedFile = fileChooser.showOpenDialog(this.linkedScene.getGame().getWindow() );

                if(choosedFile == null)
                    return;

                Image choosedImage = new Image(choosedFile.getAbsolutePath() );

                this.locker.lock();

                // alors l'image avait déjà été ajouté à la liste, on modifie
                if(!helper.imageIsSet() )
                {
                    imageList.add(choosedImage);
                    previewList.add(choosedImage);
                }
                else 
                {
                    Image previousImage = helper.getImage();

                    imageList.set(imageList.indexOf(previousImage),choosedImage);
                    previewList.set(previewList.indexOf(previousImage),choosedImage);
                }

                helper.setImage(choosedImage);

                imageView.setImage(choosedImage);

                this.locker.unlock();
            });
        });
    }

    /**
     * ajout de la zone d'ajout des images d'attaque
     * @param font
     * @param children
     */
    public void addAttackZone(Font font,ObservableList<Node> children,GameDataManager manager){
        // création de la liste d'image des attaques pour la preview
        ArrayList<Image> imageList = new ArrayList<Image>();
        this.actionsSequences.put(PlayerAction.ATTACK,imageList);

        children.add(this.createZone(font,"Attaque",imageList,manager) );
    }

    /**
     * ajout de la zone d'ajout des images de super attaque
     * @param font
     * @param children
     */
    public void addSuperAttackZone(Font font,ObservableList<Node> children,GameDataManager manager){
        // création de la liste d'image des supers attaques pour la preview
        ArrayList<Image> imageList = new ArrayList<Image>();
        this.actionsSequences.put(PlayerAction.SUPER_ATTACK,imageList);

        children.add(this.createZone(font,"Super attaque",imageList,manager) );
    }

    /**
     * ajout de la zone d'ajout des images de course
     * @param font
     * @param children
     */
    public void addRunZone(Font font,ObservableList<Node> children,GameDataManager manager){
        // création de la liste d'image de course pour la preview
        ArrayList<Image> imageList = new ArrayList<Image>();
        this.actionsSequences.put(PlayerAction.RUN,imageList);

        children.add(this.createZone(font,"Course",imageList,manager) );
    }

    /**
     * ajout de la zone d'ajout des images de mort
     * @param font
     * @param children
     */
    public void addDeathZone(Font font,ObservableList<Node> children,GameDataManager manager){
        // création de la liste d'image de la mort pour la preview
        ArrayList<Image> imageList = new ArrayList<Image>();
        this.actionsSequences.put(PlayerAction.DEATH,imageList);

        children.add(this.createZone(font,"Mort",imageList,manager) );
    }

    /**
     * ajout de la zone d'ajout des images de saut
     * @param font
     * @param children
     */
    public void addJumpZone(Font font,ObservableList<Node> children,GameDataManager manager){
        // création de la liste d'image des sauts pour la preview
        ArrayList<Image> imageList = new ArrayList<Image>();
        this.actionsSequences.put(PlayerAction.JUMP,imageList);

        children.add(this.createZone(font,"Saut",imageList,manager) );
    }

    /**
     * ajout de la zone d'ajout des images de descende de saut
     * @param font
     * @param children
     */
    public void addFallZone(Font font,ObservableList<Node> children,GameDataManager manager){
        // création de la liste d'image de descente pour la preview
        ArrayList<Image> imageList = new ArrayList<Image>();
        this.actionsSequences.put(PlayerAction.FALL,imageList);

        children.add(this.createZone(font,"Descente",imageList,manager) );
    }

    /**
     * ajout de la zone d'ajout des images immobile
     * @param font
     * @param children
     */
    public void addStaticZone(Font font,ObservableList<Node> children,GameDataManager manager){
        // création de la liste d'image statiques pour la preview
        ArrayList<Image> imageList = new ArrayList<Image>();
        this.actionsSequences.put(PlayerAction.STATIC,imageList);

        children.add(this.createZone(font,"Statique",imageList,manager) );
    }   

    /**
     * ajout de la zone d'ajout des images de réception de dégâts
     * @param font
     * @param children
     */
    public void addTakeHitZone(Font font,ObservableList<Node> children,GameDataManager manager){
        // création de la liste d'image des dégats pour la preview
        ArrayList<Image> imageList = new ArrayList<Image>();
        this.actionsSequences.put(PlayerAction.TAKE_HIT,imageList);

        children.add(this.createZone(font,"Dégâts",imageList,manager) );
    }

    /**
     * ajoute et gère la confirmation de création
     * @param font
     * @param children
     * @param message
     * @param manager
     */
    public void addConfirmation(Font font,ObservableList<Node> children,Label message,GameDataManager manager){
        Button confirmationButton = this.getCustomButton("Ajouter mon personnage",font);

        confirmationButton.setOnMouseClicked((e) -> {
            this.tryToConfirmCreation(message);
        });

        children.add(confirmationButton);
    }

    /**
     * essaie de créer le personnage
     */
    public void tryToConfirmCreation(Label messageDisplayer){
        this.toDoOnConfirm.action(this.getPopup(),false);
    }
    
    /**
     * crée un button custom
     * @param title
     * @param font
     * @return le boutton
     */
    private Button getCustomButton(String title,Font font)
    {   
        Button button = new Button(title);

        String color = "#C77F4F";
        String hoverColor = "#98572c";

        // design du boutton
        button.setBackground(new Background(new BackgroundFill(Paint.valueOf(color),CornerRadii.EMPTY,Insets.EMPTY) ) );
        button.setFont(Font.font(font.getFamily(),15) );
        button.setWrapText(true);
        button.setOnMouseExited((e) -> {
            button.setBackground(new Background(new BackgroundFill(Paint.valueOf(color),CornerRadii.EMPTY,Insets.EMPTY) ) );
        });
        button.setOnMouseEntered((e) -> {
            button.setBackground(new Background(new BackgroundFill(Paint.valueOf(hoverColor),CornerRadii.EMPTY,Insets.EMPTY) ) );
        });

        return button;
    }

    class PreviewHelper
    {
        private Image image = null;

        /**
         * 
         * @return si l'image a été affecté
         */
        public boolean imageIsSet(){
            return this.image != null;
        }

        /**
         * 
         * @return l'image
         */
        public Image getImage(){
            return this.image;
        }

        /**
         * affecte l'image
         * @param image
         */
        public void setImage(Image image){
            this.image = image;
        }
    }
}
