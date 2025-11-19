import java.io.*;
import java.util.*;

public class NumberGuessingGame {

    private final Scanner scanner = new Scanner(System.in);
    private final Random random = new Random();
    private int score = 0;
    private String playerName;
    private static final String LEADERBOARD_FILE = "leaderboard.txt";

    public static void main(String[] args) {
        new NumberGuessingGame().startGame();
    }

    // -------------------- Main Game Flow --------------------
    public void startGame() {
        System.out.println("🎯 Welcome to the Number Guessing Game!");
        System.out.print("Enter your name: ");
        playerName = scanner.nextLine().trim();

        boolean playAgain = true;
        int round = 1;

        while (playAgain) {
            System.out.println("\n========== Round " + round + " ==========");
            Difficulty difficulty = selectDifficulty();
            playRound(difficulty);
            System.out.println("⭐ Your current score: " + score);
            playAgain = askToPlayAgain();
            round++;
        }

        System.out.println("\n🏁 Game Over! Final score: " + score);
        saveScoreToLeaderboard(playerName, score);
        displayLeaderboard();
        System.out.println("Thanks for playing, " + playerName + "! 👋");
    }

    // -------------------- Difficulty Selection --------------------
    private Difficulty selectDifficulty() {
        System.out.println("\nSelect a difficulty level:");
        System.out.println("1️⃣  Easy   (Range: 1–50, Attempts: 10)");
        System.out.println("2️⃣  Medium (Range: 1–100, Attempts: 7)");
        System.out.println("3️⃣  Hard   (Range: 1–200, Attempts: 5)");

        int choice;
        while (true) {
            System.out.print("Enter your choice (1-3): ");
            if (scanner.hasNextInt()) {
                choice = scanner.nextInt();
                if (choice >= 1 && choice <= 3) break;
            } else {
                scanner.next();
            }
            System.out.println("❌ Invalid choice. Please enter 1, 2, or 3.");
        }

        return switch (choice) {
            case 1 -> Difficulty.EASY;
            case 2 -> Difficulty.MEDIUM;
            default -> Difficulty.HARD;
        };
    }

    // -------------------- Round Logic --------------------
    private void playRound(Difficulty difficulty) {
        int numberToGuess = random.nextInt(difficulty.range) + 1;
        int attemptsLeft = difficulty.attempts;

        System.out.println("\n🎮 I'm thinking of a number between 1 and " + difficulty.range + ".");
        System.out.println("You have " + attemptsLeft + " attempts. Good luck!");

        while (attemptsLeft > 0) {
            int guess = getUserGuess(difficulty.range, attemptsLeft);

            if (guess == numberToGuess) {
                System.out.println("✅ Correct! You guessed it!");
                score += calculatePoints(difficulty, attemptsLeft);
                return;
            } else if (guess < numberToGuess) {
                System.out.println("🔼 Too low! Try a higher number.");
            } else {
                System.out.println("🔽 Too high! Try a lower number.");
            }

            attemptsLeft--;
        }

        System.out.println("❌ Out of attempts! The correct number was " + numberToGuess + ".");
    }

    // -------------------- Input Validation --------------------
    private int getUserGuess(int maxRange, int attemptsLeft) {
        int guess;
        while (true) {
            System.out.print("Enter your guess (1–" + maxRange + ") [Attempts left: " + attemptsLeft + "]: ");
            if (scanner.hasNextInt()) {
                guess = scanner.nextInt();
                if (guess >= 1 && guess <= maxRange) break;
            } else {
                scanner.next();
            }
            System.out.println("⚠️ Invalid input! Please enter a number between 1 and " + maxRange + ".");
        }
        return guess;
    }

    // -------------------- Replay Option --------------------
    private boolean askToPlayAgain() {
        System.out.print("\nDo you want to play another round? (yes/no): ");
        String response = scanner.next().trim().toLowerCase();
        return response.equals("yes") || response.equals("y");
    }

    // -------------------- Scoring System --------------------
    private int calculatePoints(Difficulty difficulty, int attemptsLeft) {
        int basePoints = switch (difficulty) {
            case EASY -> 10;
            case MEDIUM -> 20;
            case HARD -> 30;
        };
        int bonus = attemptsLeft * 2;
        return basePoints + bonus;
    }

    // -------------------- Leaderboard Management --------------------
    private void saveScoreToLeaderboard(String name, int score) {
        try (FileWriter writer = new FileWriter(LEADERBOARD_FILE, true)) {
            writer.write(name + "," + score + "\n");
        } catch (IOException e) {
            System.out.println("⚠️ Error saving to leaderboard: " + e.getMessage());
        }
    }

    private void displayLeaderboard() {
        System.out.println("\n🏆 LEADERBOARD 🏆");
        List<PlayerScore> scores = new ArrayList<>();

        // Read leaderboard file
        try (BufferedReader reader = new BufferedReader(new FileReader(LEADERBOARD_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 2) {
                    scores.add(new PlayerScore(data[0], Integer.parseInt(data[1])));
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("No leaderboard data found yet.");
            return;
        } catch (IOException e) {
            System.out.println("⚠️ Error reading leaderboard: " + e.getMessage());
            return;
        }

        // Sort scores (descending)
        scores.sort((a, b) -> Integer.compare(b.score, a.score));

        // Display top 5 scores
        for (int i = 0; i < Math.min(5, scores.size()); i++) {
            PlayerScore ps = scores.get(i);
            System.out.printf("%d. %-15s %5d pts%n", (i + 1), ps.name, ps.score);
        }
    }

    // -------------------- Supporting Classes --------------------
    private static class PlayerScore {
        String name;
        int score;

        PlayerScore(String name, int score) {
            this.name = name;
            this.score = score;
        }
    }

    private enum Difficulty {
        EASY(50, 10),
        MEDIUM(100, 7),
        HARD(200, 5);

        final int range;
        final int attempts;

        Difficulty(int range, int attempts) {
            this.range = range;
            this.attempts = attempts;
        }
    }
}