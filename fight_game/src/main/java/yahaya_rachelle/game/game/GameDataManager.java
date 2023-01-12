package yahaya_rachelle.game.game;

import java.net.URL;
import java.util.HashMap;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.media.Media;
import yahaya_rachelle.game.exception.KeyNotExist;
import yahaya_rachelle.game.game.GameLoader.Key;
import yahaya_rachelle.game.utils.GameCallback;

public class GameDataManager {
    private HashMap<Key,String> resourcesPathMap;
    private HashMap<Key,String> fontsMap;
    private HashMap<Key,Media> songsMap;
    private HashMap<Key,Image> itemsMap;  

    /**
     * charge les données du jeux
     */
    public void loadDatas(GameCallback toCall){
        GameDataManager manager = this;

        Thread loadingThread = new Thread(){
            @Override
            public void run()
            {   
                new GameLoader(manager);
                
                Platform.runLater(new Runnable() {
                    @Override
                    public void run()
                    {
                        toCall.action();
                    }
                });
            }
        };

        loadingThread.start();
    }

    /**
     * 
     * @param key
     * @param resourcePath
     * @return l'URL d'une resource à partir de la map
     * @throws KeyNotExist
     */
    public URL getResource(Key key,String resourcePath) throws KeyNotExist{
        if(!this.resourcesPathMap.containsKey(key) )
            throw new KeyNotExist();

        return this.getClass().getResource(this.resourcesPathMap.get(key) + resourcePath);
    }


    public HashMap<Key,String> getResourcesPathMap() {
        return this.resourcesPathMap;
    }

    public void setResourcesPathMap(HashMap<Key,String> resourcesPathMap) {
        this.resourcesPathMap = resourcesPathMap;
    }

    public HashMap<Key,String> getFontsMap() {
        return this.fontsMap;
    }

    public void setFontsMap(HashMap<Key,String> fontsMap) {
        this.fontsMap = fontsMap;
    }

    public HashMap<Key,Media> getSongsMap() {
        return this.songsMap;
    }

    public void setSongsMap(HashMap<Key,Media> songsMap) {
        this.songsMap = songsMap;
    }

    public HashMap<Key,Image> getItemsMap() {
        return this.itemsMap;
    }

    public void setItemsMap(HashMap<Key,Image> itemsMap) {
        this.itemsMap = itemsMap;
    }
}
