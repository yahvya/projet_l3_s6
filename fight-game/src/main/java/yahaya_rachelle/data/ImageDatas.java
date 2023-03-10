package yahaya_rachelle.data;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

import org.json.simple.parser.ParseException;

import javafx.scene.image.Image;
import yahaya_rachelle.configuration.Configurable;

/**
 * représente les class représentant une donnée sous forme d'image
 */
public abstract class ImageDatas extends Configurable {
    public ImageDatas() throws FileNotFoundException, ParseException, IOException, URISyntaxException{
        this.setConfig();   
    }

    public ImageDatas(boolean dontLoad){}

    /**
     * 
     * @param key
     * @return l'image ou null
     */
    public Image getImage(String key){
        try{
            return new Image(this.getClass().getResource(new ConfigGetter<String>(this).getValueOf(key) ).toURI().toString() );
        }
        catch(Exception e){
            return null;
        }
    }
}