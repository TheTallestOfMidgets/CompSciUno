package client.card;
import java.io.Serializable;
import java.util.*;

public class Card implements Serializable {
    public String[] colors = {"Red", "Blue", "Yellow", "Green"};
    public String color;
    public int value;
    public Random ran = new Random();

    /*
    * IDEA FOR SPECIAL CARDS
    *   have special cards be represented by higher/negative values. An example of this implementation
    *       0-9: cards 1-9
    *       10: skip
    *       11: reverse
    *       12: +2
    *       13: +4
    *       14: wild card
    * */
  
    public Card(int col, int value) {
        this.color = colors[col];
        this.value = value;

    }

    //Returns a random card
    public Card() {
      this.color = colors[ran.nextInt(4)];
      this.value = ran.nextInt(14)+1;

    }

    public String toString() {
        return "(" + color + ", " + value + ")";
    }

    //Checks if a card can be placed onto another card
    public static boolean isCompatible(Card card1, Card card2){
        return card1.color.equals(card2.color) || card1.value == card2.value;
    }
    
}
