import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class QuickChat {
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private static final ArrayList<Message> messages = new ArrayList<>();
    private static boolean loggedIn = false;
    private static int totalMessagesSent = 0;
    
    public static void main(String[] args) {
        // Login required before sending messages
        try (Scanner scanner = new Scanner(System.in)) {
            // Login required before sending messages
            System.out.print("Please log in to continue.\nUsername: ");
            String username = scanner.nextLine();
            System.out.print("Password: ");
            String password = scanner.nextLine();
            
            // Simple login check
            if (username.equals("user") && password.equals("pass")) {
                loggedIn = true;
            } else {
                System.out.println("Login failed. Exiting application.");
                return;
            }
            
            if (!loggedIn) {
                System.out.println("Access denied. Please log in to use QuickChat.");
                return;
            }
            
            // Welcome message
            System.out.println("\nWelcome to QuickChat.");
            
            // User defines how many messages they wish to enter
            System.out.print("How many messages do you wish to send? ");
            int numMessages = scanner.nextInt();
            scanner.nextLine();
            
            boolean running = true;
            while (running) {
                // Display numeric menu
                System.out.println("\n=== QuickChat Menu ===");
                System.out.println("1. Send Messages");
                System.out.println("2. Show recently sent messages");
                System.out.println("3. Quit");
                System.out.print("Choose an option: ");
                
                int choice = scanner.nextInt();
                scanner.nextLine();
                
                switch (choice) {
                    case 1 -> sendMessages(numMessages, scanner);
                    case 2 -> System.out.println("\nComing Soon.");
                    case 3 -> {
                        System.out.println("\nGoodbye!");
                        running = false;
                    }
                    default -> System.out.println("\nInvalid option. Please try again.");
                }
            }
        }
    }
    
    private static void sendMessages(int numMessages, Scanner scanner) {
        int messagesProcessed = 0;
        
        for (int i = 1; i <= numMessages; i++) {
            System.out.println("\n--- Message " + i + " of " + numMessages + " ---");
            
            // Get recipient
            System.out.print("Enter recipient cell number (with international code, e.g., +27718693002): ");
            String recipient = scanner.nextLine();
            
            // Get message
            System.out.print("Enter your message (max 250 characters): ");
            String messageText = scanner.nextLine();
            
            // Validate message length
            if (messageText.length() > 250) {
                int excess = messageText.length() - 250;
                System.out.println("Message exceeds 250 characters by " + excess + "; please reduce the size.");
                System.out.println("Message not sent.");
                continue;
            } else {
                System.out.println("Message ready to send.");
            }
            
            // Create message object
            Message msg = new Message(recipient, messageText);
            
            // Validate recipient
            String recipientResult = msg.checkRecipientCell();
            System.out.println(recipientResult);
            
            if (!recipientResult.equals("Cell phone number successfully captured.")) {
                System.out.println("Message not sent due to invalid recipient number.");
                continue;
            }
            
            // Check message ID
            if (!msg.checkMessageID()) {
                System.out.println("Error: Invalid message ID.");
                continue;
            }
            
            // Generate and display message hash
            String hash = msg.createMessageHash();
            System.out.println("Message ID generated: " + msg.getMessageID());
            System.out.println("Message Hash generated: " + hash);
            
            // Ask user to choose send, disregard, or store
            System.out.println("\nOptions:");
            System.out.println("1. Send Message");
            System.out.println("2. Disregard Message");
            System.out.println("3. Store Message to send later");
            System.out.print("Choose an option (1-3): ");
            int option = scanner.nextInt();
            scanner.nextLine();
            
            String result = msg.sentMessage(option);
            System.out.println(result);
            
            // Store message if sent or stored
            if (option == 1 || option == 3) {
                messages.add(msg);
                if (option == 1) {
                    totalMessagesSent++;
                }
                
                // Display full message details after sending
                System.out.println("\n=== Message Details ===");
                System.out.println(msg.printMessages());
            }
            
            messagesProcessed++;
        }
        
        // Display total number of messages accumulated
        System.out.println("\n=== Summary ===");
        System.out.println("Total messages sent: " + totalMessagesSent);
        System.out.println("Total number of messages: " + Message.returnTotalMessagesStatic());
    }
}

// Message Class with all required methods
final class Message {
    private final String messageID;
    private final int numMessagesSent;
    private final String recipient;
    private final String message;
    private final String messageHash;
    private static int totalMessages = 0;
    
    public Message(String recipient, String message) {
        this.messageID = generateMessageID();
        this.numMessagesSent = ++totalMessages;
        this.recipient = recipient;
        this.message = message;
        this.messageHash = createMessageHash();
    }
    
    private String generateMessageID() {
        Random rand = new Random();
        StringBuilder id = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            id.append(rand.nextInt(10));
        }
        return id.toString();
    }
    
    // Method 1: checkMessageID()
    public boolean checkMessageID() {
        return messageID != null && messageID.length() <= 10;
    }
    
    // Method 2: checkRecipientCell()
    public String checkRecipientCell() {
        if (recipient == null || recipient.isEmpty()) {
            return "Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.";
        }
        
        String cleaned = recipient.replaceAll("\\s", "");
        
        if (!cleaned.startsWith("+")) {
            return "Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.";
        }
        
        if (cleaned.length() > 11) { // + plus max 10 digits
            return "Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.";
        }
        
        String digits = cleaned.substring(1);
        if (!digits.matches("\\d+")) {
            return "Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.";
        }
        
        return "Cell phone number successfully captured.";
    }
    
    // Method 3: createMessageHash()
    public String createMessageHash() {
        String idPrefix = messageID.substring(0, 2);
        String[] words = message.trim().split("\\s+");
        String firstWord = words.length > 0 ? words[0] : "";
        String lastWord = words.length > 1 ? words[words.length - 1] : firstWord;
        
        String hash = idPrefix + ":" + numMessagesSent + ":" + firstWord + lastWord;
        return hash.toUpperCase();
    }
    
    // Method 4: sentMessage()
    public String sentMessage(int choice) {
        return switch (choice) {
            case 1 -> "Message successfully sent.";
            case 2 -> "Press 0 to delete the message.";
            case 3 -> "Message successfully stored.";
            default -> "Invalid option.";
        };
    }
    
    // Method 5: printMessages()
    public String printMessages() {
        return "Message ID: " + messageID + 
               "\nMessage Hash: " + messageHash +
               "\nRecipient: " + recipient +
               "\nMessage: " + message;
    }
    
    // Method 6: returnTotalMessages()
    public static int returnTotalMessagesStatic() {
        return totalMessages;
    }
    
    // Getters
    public String getMessageID() { return messageID; }
    public String getMessageHash() { return messageHash; }
    public String getRecipient() { return recipient; }
    public String getMessage() { return message; }
}
