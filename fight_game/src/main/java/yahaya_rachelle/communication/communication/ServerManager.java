package yahaya_rachelle.communication.communication;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import yahaya_rachelle.actor.Player;
import yahaya_rachelle.communication.message.IpMessage;
import yahaya_rachelle.communication.message.Message;
import yahaya_rachelle.communication.message.Message.MessageType;
import yahaya_rachelle.utils.GameCallback;

/**
 * représente une communication en étant le premier serveur
 */
public class ServerManager extends Communicator{
    private int countOfPlayersToWait;

    private ArrayList<String> ipListToReduceOnReceiveConnexion;
    private ArrayList<String> ipListToReduceOnConfirmConnexion;
    private ArrayList<String> ipListToReduceOnConfirmSharePlayers;

    private HashMap<Socket,Integer> clientsServerSocketPortMap;

    private GameCallback toDoWhenAllJoin;

    public ServerManager(HashMap<MessageType, MessageManager> messagesLinkedActionsMap,int countOfPlayersToWait,Player internalPlayer) {
        super(messagesLinkedActionsMap,internalPlayer);
        this.countOfPlayersToWait = countOfPlayersToWait;
        this.clientsServerSocketPortMap = new HashMap<Socket,Integer>();
    }

    @Override
    protected HashMap<MessageType, MessageManager> getInternalManagedMessages() {
        HashMap<MessageType,MessageManager> internalManagedMessages = new HashMap<MessageType,MessageManager>();

        // gestion de la réception du message indiquant la possibilité de recevoir des connexions
        internalManagedMessages.put(MessageType.CONFIRM_CAN_RECEIVE_CONNEXIONS,(clientSocketDataMessage) -> {
            IpMessage socketData = (IpMessage) clientSocketDataMessage.getMessageData();

            this.clientsServerSocketPortMap.put(clientSocketDataMessage.getSource(),socketData.getPort() );

            // si la taille est à 0 alors tout le monde à confirmé pouvoir recevoir les connexions
            if(this.removeOnList(ipListToReduceOnReceiveConnexion,socketData.getIp() ) == 0)
                this.shareOthersSocketDataToEach();
        });
        // gestion de la réception du message indiquant la connexion aux autres
        internalManagedMessages.put(MessageType.CONFIRM_CONNECT_TO_OTHERS,(clientIpMessage) -> {
            // si la taille est à 0 alors tout le monde à confirmé s'être connecté aux autres
            if(this.removeOnList(ipListToReduceOnConfirmConnexion,(String) clientIpMessage.getMessageData() ) == 0)
                this.sendSharePlayersMessageToEach();
        });
        // gestion de réception de confirmation (tous les joueurs reçu)
        internalManagedMessages.put(MessageType.CONFIRM_RECEIVE_ALL_PLAYERS,(clientIpMessage) -> {
            // si la taille est à 0 alors tout le monde à confirmé avoir reçu les joueurs des autres
            if(this.removeOnList(ipListToReduceOnConfirmSharePlayers,(String) clientIpMessage.getMessageData() ) == 0)
                this.sendStartGameToEach();
        });
        // gestion de la réception d'un joueur
        internalManagedMessages.put(MessageType.RECEIVE_PLAYER,(playerMessage) -> {
            Player player = (Player) playerMessage.getMessageData();

            player.getCharacter().rebuildActionsMapSerializable();

            MessageManager toDo = this.messagesLinkedActionsMap.get(MessageType.RECEIVE_PLAYER);
            
            if(toDo != null)
                toDo.manageMessage(playerMessage);
        });

        return internalManagedMessages;
    }

    /**
     * gère le processus de lancement du serveur et de partage des ips
     * gère certains messages et cherche dans la actionsMap les actions à réaliser sur les autres types de messages, si non spécifié alors message ignoré
     * @param toDoOnJoin
     * @param toDoOnFailure
     * @param actionsMap
     * @return this
     */
    public ServerManager createEntryPoint(GameCallback toDoOnJoin,GameCallback toDoOnFailure,GameCallback toDoWhenAllJoin){
        try{
            this.server = new ServerSocket(0); 
            this.toDoWhenAllJoin = toDoWhenAllJoin;  

            // création et lancement du thread d'acceptation des joueurs
            new Thread(){
                @Override
                public void run(){
                    boolean error = false;

                    ipListToReduceOnReceiveConnexion = new ArrayList<String>();
                
                    // on attend que les joueurs rejoignent
                    for(int playerCount = 0; playerCount < countOfPlayersToWait; playerCount++){
                        try{
                            // récupération du joueur
                            Socket playerSocket = server.accept();

                            String playerIp = playerSocket.getInetAddress().getHostAddress();

                            // on vérifie que le joueur n'est pas déjà dans la partie
                            // boolean alreadyExist = false;

                            // for(Socket p : otherPlayersSocket){
                            //     if(p.getInetAddress().getHostAddress() == playerIp){
                            //         alreadyExist = true;
                            //         break;
                            //     }
                            // }

                            // if(alreadyExist){
                            //     playerCount--;
                            //     continue;
                            // }

                            // sauvegarde de l'ip dans les listes à réduire
                            ipListToReduceOnReceiveConnexion.add(playerIp);
                            // sauvegarde du joueur et création de son objet de sortie
                            addNewPlayerSocket(playerSocket);
                            // appel de l'action à exécuter quand un joueur rejoins
                            toDoOnJoin.action();
                        }
                        catch(Exception e){
                            // on préviens les joueurs déjà connecté de l'arrêt de la partie
                            error = true;

                            break;
                        }
                    }

                    if(!error){
                        ipListToReduceOnConfirmConnexion = (ArrayList<String>) ipListToReduceOnReceiveConnexion.clone();
                        ipListToReduceOnConfirmSharePlayers = (ArrayList<String>) ipListToReduceOnReceiveConnexion.clone();

                        // commence à écouter les messages entrants et partage le nombre de participants
                        startListening();
                        shareCountOfParticipantToOtherPlayers();
                    }
                    else toDoOnFailure.action();
                }
            }.start();
        }
        catch(Exception e){
            // exécution de l'action à faire en cas d'erreur
            toDoOnFailure.action();
        }

        return this;
    }
    
    /**
     * @param toDoWhenAllJoin
     * @return this
     */
    private ServerManager shareCountOfParticipantToOtherPlayers(){
        // envoi du nombre de participants à accepter
        this.propagateMessage(new Message(MessageType.RECEIVE_COUNT_OF_PLAYERS_TO_ACCEPT,countOfPlayersToWait - 1) );

        return this;
    }

    /**
     * partage la liste des ip de chaque participant aux autres
     * @return this
     */
    private ServerManager shareOthersSocketDataToEach(){
        this.otherPlayersSocketOutput.forEach((socket,outputObject) -> {
            ArrayList<IpMessage> othersSocketData = new ArrayList<IpMessage>();

            // on récupère la liste des  ip
            this.clientsServerSocketPortMap.forEach((clientSocket,port) -> {
                if(clientSocket != socket)
                    othersSocketData.add(new IpMessage(socket.getInetAddress().getHostAddress(),port) );
            });

            // envoi de la liste d'ip des autres
            try{
                outputObject.writeObject(new Message(MessageType.RECEIVE_IP_LIST,othersSocketData) );
            }
            catch(Exception e){
                // on déconnecte ce joueur de la session
            }
        });

        return this;
    }

    /**
     * envoi lemessage / signal pour démarrer le partage des joueurs et partage ce joueur aux autres 
     * @return this
     */
    private ServerManager sendSharePlayersMessageToEach(){
        this   
            .propagateMessage(new Message(MessageType.RECEIVE_SIGNAL_TO_SHARE_PLAYER,null) )
            .shareMyPlayer();

        return this;
    }

    /**
     * envoi l'évenement de début de partie à tous et déclenche l'action de joueurs rejoins
     * @return this
     */
    private ServerManager sendStartGameToEach(){
        this.propagateMessage(new Message(MessageType.START_GAME,null) );
        
        toDoWhenAllJoin.action();
        
        return this;
    }

    /**
     * supprime une chaine dans la liste
     * @param list
     * @param toRemove
     * @return la nouvelle taile de la liste
     */
    synchronized private int removeOnList(ArrayList<String> list,String toRemove){
        int size = list.size();

        // on supprime l'ip donné dans la liste
        for(int key = 0; key < size; key++){
            String containedString = list.get(key);

            if(containedString.equals(toRemove) ){
                size--;

                list.remove(key);

                break;
            }
        }

        return size;
    }
}