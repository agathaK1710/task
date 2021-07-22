import java.nio.charset.StandardCharsets;
import java.util.*;
import java.security.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class RockPaperScissors {

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length*2);
        for(byte b: bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
    public static void showMenu(String[] args){
        for (int i = 0; i < args.length; i++) {
            System.out.println(i+1 + " - " + args[i]);
        }
        System.out.println("0 - exit");
    }
    public static boolean consist(Move userMove, String[] args){
        boolean flag = false;
        for (int i = 0; i < args.length ; i++) {
            flag = (userMove.number == i+1);
            if (flag){
                userMove.name = args[i];
                break;
            }
        }
        return flag;
    }
    public static boolean hasDuplicate(String[] args){
        for (int i = 0; i < args.length; i++) {
            for (int j = i+1; j < args.length; j++) {
                if(args[i].equals(args[j])){
                    return true;
                }
            }
        }
        return false;
    }
    public static boolean checkError(String[] args){
        if (args.length % 2 == 0 || args.length < 3 || hasDuplicate(args)){
            System.out.println("""
                    Error! Please write >=3 odd number of arguments and avoid duplicate!
                    Example:\s
                    1 - rock
                    2 - paper
                    3 - scissors""");
            return true;
        }
        return false;
    }

    public static void isWinner(String[] args, Move move1, Move move2) {
        int median = args.length / 2;
        boolean flag = false;
        int[] numbers = new int[args.length * 3];
        int j = 0;
        for (int i = 0; i < numbers.length / 3; i++) {
            numbers[i] = numbers[i + numbers.length / 3] = numbers[i + numbers.length / 3 * 2] = j++;
        }
        for (int i = 1; i <= median; i++) {
            if (numbers[move2.number - 1 + i] == move1.number - 1) {
                flag = true;
                break;
            }
        }
        if (numbers[move2.number - 1] == move1.number - 1) {
            System.out.println("Draw!");
        } else {
            if(flag){
                System.out.println("You win!");
            }
            else {
                System.out.println("You lose!");
            }
        }

    }

    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeyException {
        if(!checkError(args)) {
            Scanner reader = new Scanner(System.in);
            Move userMove = new Move();
            userMove.number = 1;
            int i = 1;
            while (userMove.number != 0) {
                SecureRandom secureRandom = new SecureRandom();
                byte[] bytes = new byte[16];
                secureRandom.nextBytes(bytes);
                Mac signer = Mac.getInstance("HmacSHA3-256");
                SecretKeySpec keySpec = new SecretKeySpec(bytes, "HmacSHA3-256");                       
                signer.init(keySpec);

                Move computerMove = new Move();
                computerMove.number = (int) (Math.random() * ((args.length - 1) + 1)) + 1;
                computerMove.name = args[computerMove.number - 1];
                byte[] digest = signer.doFinal(computerMove.name.getBytes(StandardCharsets.UTF_8));
                System.out.println("-----------\nRound number: " + i++ + "\n-----------");
                System.out.println("HMAC:");
                System.out.println(bytesToHex(digest));

                System.out.println("Available moves:");
                showMenu(args);
                System.out.println("Enter your move:");
                userMove.number = reader.nextInt();
                if (userMove.number == 0){
                    return;
                }
                else {
                    boolean flag;
                    flag = consist(userMove, args);
                    while (!flag) {
                        showMenu(args);
                        userMove.number = reader.nextInt();
                        flag = consist(userMove, args);
                    }
                    System.out.println("Your move: " + userMove.name);
                    System.out.println("Computer move: " + computerMove.name);
                    isWinner(args, userMove, computerMove);
                    System.out.println("HMAC key: " + bytesToHex(bytes));
                }

            }
        }
    }
    public static class Move{
        int number;
        String name;

        public Move(){};
        public Move(int number, String name){
            this.number = number;
            this.name = name;
        }
    }
}