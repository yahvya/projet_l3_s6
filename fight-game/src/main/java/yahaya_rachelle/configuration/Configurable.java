package yahaya_rachelle.configuration;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import yahaya_rachelle.utils.JsonReader;

/**
 * représente une class lié à un fichier de configuration
 */
public abstract class Configurable {

    protected JSONObject config;

    /**
     * récupère le fichier de configuration
     * @param path
     * @throws URISyntaxException
     * @throws IOException
     * @throws ParseException
     * @throws FileNotFoundException
     */
    protected void setConfig() throws FileNotFoundException, ParseException, IOException, URISyntaxException{
        this.config = JsonReader.getJsonFrom(this.getClass().getResource(this.getConfigFilePath() ).toURI() );
    }

    /**
     * 
     * @return le chemin du fichier de configuration
     */
    abstract protected String getConfigFilePath();

    /**
     * permet la lecture par l'intermédiaire du json reader d'un fichier json
     */
    static public class ConfigGetter<ToCastIn>{
        private Configurable container;

        public ConfigGetter(Configurable container){
            this.container = container;
        }

        /**
         * 
         * @param key
         * @return la valeur de la clé
         */
        public ToCastIn getValueOf(String key){
            return new JsonReader<ToCastIn>(key,this.container.config).getValue();
        }
    }
}
