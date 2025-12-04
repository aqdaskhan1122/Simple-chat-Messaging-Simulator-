import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 * Full Java Code combining Singleton, Factory, Builder, Decorator, and Observer 
 * patterns into a Swing-based Chat Simulator.
 * Note: All nested classes are declared 'static' for clarity and standard practice 
 * when grouping multiple related classes in a single file for compilation.
 **/
public class ChatSimulator extends JFrame implements ChatObserver {

    // ==========================================
    // CORE COMPONENT: ABSTRACT MESSAGE
    // ==========================================

    /**
     * The core component interface for the Factory and Decorator patterns.
     */
    abstract static class Message { // Made static
        protected String sender;
        protected String content;

        public Message(String sender, String content) {
            this.sender = sender;
            this.content = content;
        }

        public String getSender() { return sender; }
        public String getContent() { return content; }
        public abstract String getDisplayText();
    }

    // Concrete Message Products
    static class TextMessage extends Message { // Made static
        public TextMessage(String sender, String content) {
            super(sender, content);
        }
        @Override
        public String getDisplayText() { return sender + ": " + content; }
    }

    static class SystemMessage extends Message { // Made static
        public SystemMessage(String sender, String content) {
            super(sender, content);
        }
        @Override
        public String getDisplayText() { return "[SYSTEM] " + content; }
    }


    // ==========================================
    // 2. FACTORY PATTERN: MessageFactory (Creational)
    // ==========================================

    /**
     * MessageFactory simplifies the creation of different Message types.
     */
    static class MessageFactory { // Made static
        public static Message createMessage(String type, String sender, String content) {
            if (type.equalsIgnoreCase("text")) {
                return new TextMessage(sender, content);
            } else if (type.equalsIgnoreCase("system")) {
                return new SystemMessage("SYSTEM", content);
            } else {
                return new TextMessage(sender, "Error: Unknown message type.");
            }
        }
    }


    // ==========================================
    // 4. DECORATOR PATTERN: TimestampDecorator (Structural)
    // ==========================================

    /**
     * Base Decorator class. Wraps a Message object.
     */
    abstract static class MessageDecorator extends Message { // Made static
        protected Message wrappedMessage;

        public MessageDecorator(Message message) {
            super(message.getSender(), message.getContent()); 
            this.wrappedMessage = message;
        }

        @Override
        public String getDisplayText() {
            return wrappedMessage.getDisplayText();
        }
        
        // Helper method to access the original item
        public Message getWrappedMessage() {
            return wrappedMessage;
        }
    }

    /**
     * Concrete Decorator that adds a timestamp prefix.
     */
    static class TimestampDecorator extends MessageDecorator { // Made static
        private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
        
        // CORRECTION: Fixed syntax error (Public -> public)
        public TimestampDecorator(Message message) { 
            super(message);
        }

        @Override
        public String getDisplayText() {
            String time = LocalTime.now().format(TIME_FORMAT);
            return "[" + time + "] " + wrappedMessage.getDisplayText();
        }
    }


    // ==========================================
    // 5. OBSERVER PATTERN: Subject/Observer (Behavioral)
    // ==========================================

    /**
     * The Observer interface, implemented by the ChatSimulator UI.
     */
    interface ChatObserver {
        void update(Message message);
    }

    /**
     * The Subject class, extended by the ChatEngine.
     */
    static class Subject { // Made static
        private List<ChatObserver> observers = new ArrayList<>();

        public void attach(ChatObserver observer) { observers.add(observer); }
        public void notifyObservers(Message message) {
            for (ChatObserver observer : observers) {
                observer.update(message);
            }
        }
    }


    // ==========================================
    // 1. SINGLETON PATTERN: ChatEngine (Creational) & Subject
    // ==========================================

    /**
     * ChatEngine manages all messages. It is a Singleton to ensure 
     * a single, globally accessible hub for message handling.
     * It also acts as the Subject in the Observer pattern.
     */
    static class ChatEngine extends Subject { // Made static
        private static ChatEngine instance;

        // Private constructor prevents direct instantiation
        private ChatEngine() { 
            System.out.println("ChatEngine initialized (Singleton).");
        }

        /**
         * Public static method to get the single instance.
         */
        public static synchronized ChatEngine getInstance() {
            if (instance == null) {
                instance = new ChatEngine();
            }
            return instance;
        }

        public void sendMessage(Message message) {
            // Broadcast the new message to all attached observers (UI windows)
            notifyObservers(message);
        }
    }


    // ==========================================
    // 3. BUILDER PATTERN: ChatSession (Creational)
    // ==========================================

    /**
     * Product class: Represents the configuration for the current user's session.
     */
    static class ChatSession { // Made static
        private String username;
        private String theme;
        
        // Setters must be available for the Builder
        public void setUsername(String username) { this.username = username; }
        public void setTheme(String theme) { this.theme = theme; }
        
        public String getUsername() { return username; }
        public String getTheme() { return theme; }
    }

    /**
     * Builder class: Provides a fluent interface for constructing a ChatSession.
     */
    static class ChatSessionBuilder { // Made static
        private ChatSession session = new ChatSession();
        
        public ChatSessionBuilder setUsername(String username) {
            session.setUsername(username);
            return this; // Return the builder instance for chaining
        }
        public ChatSessionBuilder setTheme(String theme) {
            session.setTheme(theme);
            return this; // Return the builder instance for chaining
        }
        
        public ChatSession build() { 
            // Ensure required fields have a default value
            if (session.getUsername() == null || session.getUsername().trim().isEmpty()) {
                session.setUsername("Guest-" + new Random().nextInt(1000));
            }
            if (session.getTheme() == null) {
                session.setTheme("Light Mode");
            }
            return session; 
        }
    }


    // ==========================================
    // GUI IMPLEMENTATION (Swing) & OBSERVER (Concrete Observer)
    // ==========================================

    private final ChatEngine engine;
    private final ChatSession session;
    private JTextPane chatArea;
    private JTextField inputField;
    private JCheckBox timestampCheck;
    private StyledDocument doc;

    public ChatSimulator() {
        // 1. Singleton: Get the single instance of the chat engine
        engine = ChatEngine.getInstance(); 
        // 5. Observer: Attach this UI (the ChatObserver) to the Engine (the Subject)
        engine.attach(this); 
        
        // 3. Builder: Construct the session configuration
        session = new ChatSessionBuilder()
                .setUsername("Student_User")
                .setTheme("Dark Mode")
                .build();
                
        setupUI();
        startSimulation();
    }

    private void setupUI() {
        setTitle("Design Pattern Chat Simulator");
        setSize(700, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Header: Displays Builder output
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(200, 220, 255));
        JLabel statusLabel = new JLabel("Welcome, " + session.getUsername() + " | Theme: " + session.getTheme());
        statusLabel.setForeground(new Color(0, 51, 153));
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        headerPanel.add(statusLabel);
        add(headerPanel, BorderLayout.NORTH);

        // Chat Display Area
        chatArea = new JTextPane();
        chatArea.setEditable(false);
        chatArea.setBackground(new Color(245, 245, 245));
        chatArea.setText("--- Chat Session Started ---\n\n");
        doc = chatArea.getStyledDocument();
        
        JScrollPane scrollPane = new JScrollPane(chatArea);
        add(scrollPane, BorderLayout.CENTER);

        // Input and Controls
        inputField = new JTextField();
        inputField.setFont(new Font("Arial", Font.PLAIN, 12));
        inputField.setPreferredSize(new Dimension(500, 30));
        
        // --- BUTTONS ---
        JButton sendTextBtn = new JButton("Send Message");
        JButton sendSystemBtn = new JButton("Send System Message"); 
        
        sendTextBtn.setBackground(new Color(60, 140, 255));
        sendTextBtn.setForeground(Color.WHITE);
        sendTextBtn.setFocusPainted(false);
        
        sendSystemBtn.setBackground(new Color(255, 100, 100)); 
        sendSystemBtn.setForeground(Color.WHITE);
        sendSystemBtn.setFocusPainted(false);

        // Actions: Call sendMessage
        ActionListener sendTextAction = e -> sendMessage("text");
        sendTextBtn.addActionListener(sendTextAction);
        inputField.addActionListener(sendTextAction); 

        ActionListener sendSystemAction = e -> sendMessage("system"); 
        sendSystemBtn.addActionListener(sendSystemAction);
        
        // Button Panel: Hold both buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonPanel.add(sendSystemBtn); 
        buttonPanel.add(sendTextBtn);

        JPanel inputPanel = new JPanel(new BorderLayout(5, 0));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(buttonPanel, BorderLayout.EAST);

        // Options Panel
        JPanel optionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        // Decorator Control
        timestampCheck = new JCheckBox("Include Timestamp (Decorator)", true);
        optionsPanel.add(timestampCheck);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(inputPanel, BorderLayout.NORTH);
        bottomPanel.add(optionsPanel, BorderLayout.SOUTH);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    // Helper to safely unwrap the Decorator and find the base message
    private Message unwrapMessage(Message message) {
        if (message instanceof MessageDecorator) {
            // Recursively check the wrapped content 
            return unwrapMessage(((MessageDecorator) message).getWrappedMessage()); 
        }
        return message;
    }

    private void sendMessage(String messageType) {
        String content = inputField.getText().trim();
        // Allow sending a default system message if content is empty
        if (content.isEmpty() && messageType.equals("text")) return; 
        
        if (content.isEmpty() && messageType.equals("system")) {
            content = "Default system alert sent by user.";
        }
        
        // Sender comes from the ChatSession (Builder output)
        String sender = session.getUsername();
        
        // 2. Factory Pattern: Create the base message item
        Message msg = MessageFactory.createMessage(messageType, sender, content);
        
        // 4. Decorator Pattern: Conditionally wrap the message with the timestamp
        if (timestampCheck.isSelected()) {
            msg = new TimestampDecorator(msg);
        }
        
        // 1. Singleton: Use the engine to broadcast the message
        engine.sendMessage(msg);
        inputField.setText("");
    }

    // 5. Observer Pattern: Concrete ChatObserver implementation method
    @Override
    public void update(Message message) {
        // Ensure UI updates are done safely on the EDT
        SwingUtilities.invokeLater(() -> {
            try {
                SimpleAttributeSet style = new SimpleAttributeSet();
                String displayText = message.getDisplayText() + "\n";
                
                // CRITICAL: Determine the base type of the message (unwrap Decorator)
                Message baseMessage = unwrapMessage(message); 

                // Coloring logic based on base message type
                if (baseMessage instanceof SystemMessage) {
                    StyleConstants.setForeground(style, Color.RED.darker()); 
                    StyleConstants.setBold(style, true);
                    StyleConstants.setItalic(style, true);
                } 
                else if (baseMessage instanceof TextMessage) {
                    if (baseMessage.getSender().equals(session.getUsername())) {
                         StyleConstants.setForeground(style, new Color(0, 100, 0)); // Dark green for local user
                    } else {
                         StyleConstants.setForeground(style, Color.BLACK); // Black for other users/system bot
                    }
                }
                
                doc.insertString(doc.getLength(), displayText, style);
                chatArea.setCaretPosition(doc.getLength());
                
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        });
    }

    // Background thread to simulate incoming messages (Observer/Singleton demonstration)
    private void startSimulation() {
        new Thread(() -> {
            // Updated user names: "Ashh", "Bot_Support", "Aqdas"
            String[] userNames = {"Ashh", "Bot_Support", "Aqdas"};
            String[] commonMessages = {"Hello there!", "Is anyone seeing this?", "I think I found a bug.", "Just testing the system.", "Nice work!"};
            String[] systemMessages = {"Server is restarting...", "System maintenance scheduled.", "New patch deployed.", "User session timed out."};
            Random rand = new Random();
            
            try {
                Thread.sleep(2000);
                for (int i = 0; i < 5; i++) {
                    Thread.sleep(2000 + rand.nextInt(3000));
                    
                    Message msg;
                    // 20% chance of a System Message
                    if (rand.nextInt(10) < 2) { 
                        // Factory: Create System Message
                        String content = systemMessages[rand.nextInt(systemMessages.length)];
                        msg = MessageFactory.createMessage("system", "SYSTEM", content);
                    } else {
                        // Factory: Create Text Message
                        String sender = userNames[rand.nextInt(userNames.length)];
                        String content = commonMessages[rand.nextInt(commonMessages.length)];
                        msg = MessageFactory.createMessage("text", sender, content);
                    }
                    
                    // 4. Decorator: Always apply timestamp for simulated messages
                    msg = new TimestampDecorator(msg);
                    
                    // 1. Singleton/Subject: Send the message
                    engine.sendMessage(msg);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); 
                System.err.println("Simulation thread interrupted.");
            }
        }).start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ChatSimulator app = new ChatSimulator();
            app.setVisible(true);
        });
    }
}