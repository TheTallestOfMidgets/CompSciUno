package client;

import client.card.Card;
import client.card.Hand;
import client.card.Player;
import client.client.ConnectionHandler;
import server.Game;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Scanner;

public class Uno {

    private ConnectionHandler connectionHandler;
    private Scanner scanner;
    private Game game;
    private Player myPlayer;
    int turnNumber = -2;

    public Uno(){
        scanner = new Scanner(System.in);
        System.out.print("Hello, welcome to uno!\nWhat's your name? ");
        String username = scanner.nextLine();
        myPlayer = new Player(username, 7);
        connectionHandler = new ConnectionHandler(myPlayer);

        String createOrJoin = null;
        while(true){
            System.out.print("Would you like to [j]oin a game or [c]reate one? ");
            String choice = scanner.nextLine();
            if(choice.equalsIgnoreCase("j") || choice.equalsIgnoreCase("c")){
                createOrJoin = choice;
                break;
            }
            System.out.println("Please type a \"j\" or a \"c\"");
        }

        if(createOrJoin.equalsIgnoreCase("j")){
            joinGame();
        }else{
            createGame();
        }
    }

    public void play() {
        while (!game.hasGameStarted()){
            try {
                Thread.sleep(500);
                System.out.println("Waiting for players...");
            } catch (Exception ignored){}
            finally {
                updateGame();
            }
        }
        updateGame(true);
        while(true) {
            try {
                Thread.sleep(500);
            } catch (Exception ignored){}
            if(updateGame()) { //TODO wont update screen :/
                myPlayer = game.getPlayer(myPlayer.getId());
                boolean myTurn = false;
                if (game.playersTurn() == myPlayer.getId()) {
                    myTurn = true;
                }
                render(myTurn);
                if (checkWinner()) break;
            }
        }
    }

    private boolean checkWinner(){
        if(game.hasPlayerWon()){
            if(game.getPlayer(myPlayer.getId()).getHand().cards.size() == 0){
                //TODO
                //you won!!!
            }else{
                //TODO
                //you lose :(
            }
            return true;
        }
        return false;
    }

    private void playCard(){
        System.out.println("Select a card to player (from left to right, 0, 1, 2, ect) or press enter to draw and skip");
        while (true){
            int selection = 0;
            String input = scanner.nextLine();
            if(input.equals("")){
                try {
                    connectionHandler.takeTurn(-1);
                } catch (IOException e) {
                    System.out.println("FATAL SERVER ERROR");
                    throw new RuntimeException(e);
                }
                break;
            }

            try {
                selection = Integer.parseInt(input);
                if(selection < 0 || selection > myPlayer.getHand().cards.size()-1) throw new Exception();
            }catch (Exception e){
                System.out.println("Please type a number from 0 to " + (myPlayer.getHand().cards.size()-1));
                continue;
            }
            if(Card.isCompatible(myPlayer.getHand().cards.get(selection), game.getTopCard())){
                try {
                    connectionHandler.takeTurn(selection);
                } catch (IOException e) {
                    System.out.println("FATAL SERVER ERROR");
                    throw new RuntimeException(e);
                }
                break;
            }
            System.out.println("Cannot place that card! pick another one");
        }


    }

    private void render(boolean isMyTurn){
        for(Player player: game.getPlayers()){
            if(player.getId() == myPlayer.getId()) continue;
            System.out.print(player);
        }
        System.out.println("\n\n");
        Hand hand = new Hand();
        hand.addCard(game.getTopCard());
        System.out.println(hand);
        System.out.println("\n\n");
        System.out.println(myPlayer.getHand());
        if(isMyTurn){
            playCard();
        }
    }
    private boolean updateGame(boolean force){
        try {
            Game currentGame = connectionHandler.update();
            if(turnNumber < currentGame.getTurnNumber() || force){
                turnNumber = currentGame.getTurnNumber();
                game = currentGame;
                return true;
            }
            return false;
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("FATAL SERVER ERROR");
        }
    }

    private boolean updateGame(){
        try {
            Game currentGame = connectionHandler.update();
            if(turnNumber < currentGame.getTurnNumber()){
                turnNumber = currentGame.getTurnNumber();
                game = currentGame;
                return true;
            }
            return false;
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("FATAL SERVER ERROR");
        }
    }


    private void createGame(){
        int numPlayers;
        while (true) {
            System.out.println("How many players would you like to play with? (2-4)");
            String numPlayersIn = scanner.nextLine();
            try {
                numPlayers = Integer.parseInt(numPlayersIn);
                if(numPlayers > 1 && numPlayers <= 4){
                    break;
                }
                throw new Exception(); //lazy way of triggering the catch block
            }catch (Exception ignore){
                System.out.println("Please type a value from 2-4");
            }
        }
        try {
            Game myGame = new Game(numPlayers);
            connectionHandler.makeGame(myGame);
            game = myGame;
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("FATAL SERVER ERROR");
        }
        play();
    }

    private void joinGame(){
        LinkedHashMap<Long, Game> games;

        try {
            games = connectionHandler.getGames();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("FATAL SERVER ERROR");
        }

        Long[] rawKeys = new Long[games.keySet().size()];
        games.keySet().toArray(rawKeys);
        ArrayList<Long> keys = new ArrayList<>(Arrays.asList(rawKeys));
        keys.removeIf(n -> games.get(n).hasGameStarted());

        if(keys.size() > 0) {
            System.out.println("Here are some of the current games available. Please type the number on the left to select a game...");
            for (int i = 0; i < keys.size(); i++) {
                if (!games.get(keys.get(i)).hasGameStarted()) {
                    System.out.println(i + " " + games.get(keys.get(i)).toString());
                }
            }
        }else{
            System.out.println("Sorry, no games available, you can rerun to make one!");
            return;
        }

        int gameNum = 10000;
        while(true) {
            try {
                String choice = scanner.nextLine();
                gameNum = Integer.parseInt(choice);
                if (!(gameNum >= 0 && gameNum <= keys.size() - 1)) throw new Exception();
                break;
            } catch (Exception e) {
                System.out.println("Please type a valid number");
            }
        }

        try {
            connectionHandler.joinGame(keys.get(gameNum));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("FATAL SERVER ERROR");
        }
        updateGame();
        play();
    }
}
