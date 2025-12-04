Design Pattern Chat Simulator

This project is a single-file Java Swing application (ChatSimulator.java) designed to illustrate the practical implementation of five fundamental object-oriented design patterns: Singleton, Factory, Builder, Decorator, and Observer.

The application simulates a simple chat interface with real-time message updates, custom session configuration, dynamic message formatting, and a background simulation thread.

 Architectural Overview

The entire application is self-contained within the ChatSimulator.java file. All helper classes (like MessageFactory and ChatEngine) are declared as static inner classes to ensure they can be accessed and compiled correctly within the single file structure.

The core flow involves the Builder setting up the user session, the Singleton engine managing message traffic, the Factory creating messages, the Decorator applying formatting, and the Observer (the GUI) handling the display.

📐 Design Pattern Implementation

Pattern

Type

Classes Involved

Role in Application

1. Singleton

Creational

ChatEngine

Guarantees a single instance of the message hub. It is responsible for receiving all messages and broadcasting them to all Observers.

2. Factory

Creational

MessageFactory, TextMessage, SystemMessage

Centralizes message creation. It abstracts the instantiation logic, allowing the application to request a message by type ("text" or "system") without knowing the concrete class.

3. Builder

Creational

ChatSession, ChatSessionBuilder

Provides a fluent interface to construct complex configuration objects (ChatSession). Used to initialize the user's session with a username and theme in the main GUI constructor.

4. Decorator

Structural

MessageDecorator, TimestampDecorator

Dynamically adds optional behavior (a timestamp) to the base Message object. The GUI checkbox controls whether the TimestampDecorator wraps the message before it is sent.

5. Observer

Behavioral

Subject, ChatObserver, ChatSimulator

Enables real-time, event-driven updates. The ChatEngine acts as the Subject, and the ChatSimulator (the GUI) acts as the ChatObserver, ensuring the chat area updates instantly when the engine sends a message.

🛠️ Detailed Pattern Breakdown

1. Singleton (ChatEngine)

The ChatEngine class has a private constructor and a static method, getInstance(), to control access to the single instance (instance).

It extends the Subject class, making it the central message distributor.

Method: sendMessage(Message message)—this method serves as the single point of entry for all message traffic before broadcasting.

2. Factory (MessageFactory)

The static createMessage(String type, String sender, String content) method determines which concrete message class to instantiate.

The system distinguishes between TextMessage (standard black/green text) and SystemMessage (formatted in red, bold, and italic text), demonstrating polymorphism at creation time.

3. Builder (ChatSessionBuilder)

The ChatSessionBuilder uses chained setter methods (setUsername(), setTheme()) that return the builder instance (return this;).

The build() method finalizes the configuration, ensuring default values are set if not provided. The result is displayed in the main window header.

4. Decorator (TimestampDecorator)

The abstract base class MessageDecorator maintains a reference to the wrappedMessage.

The concrete TimestampDecorator overrides getDisplayText() to prepend the current time ([HH:mm:ss]) to the output of the message it wraps.

This decoration is applied conditionally based on the "Include Timestamp" checkbox in the UI.

5. Observer (Subject and ChatSimulator)

The ChatEngine (Subject) maintains a list of ChatObserver instances (observers).

The GUI (ChatSimulator) implements the ChatObserver interface, registering itself with the ChatEngine in its constructor via engine.attach(this);.

When a message is sent (engine.sendMessage()), the engine calls notifyObservers(), which invokes the update(Message message) method in the GUI, triggering the display logic.

⚠️ Concurrency and Thread Safety

Java Swing components are not thread-safe. Since the startSimulation() method runs on a background thread, direct manipulation of the JTextPane (the chat area) would lead to crashes or unstable behavior.

Solution in update(): The implementation of the update() method inside ChatSimulator uses:

SwingUtilities.invokeLater(() -> { ... });


This critical step ensures that the UI update logic is safely placed on the Event Dispatch Thread (EDT) queue, resolving thread safety issues and guaranteeing stable rendering.

▶️ How to Run the Code

Save the entire code block above into a file named ChatSimulator.java.

Open your command line or terminal.

Compile the code:

javac ChatSimulator.java


Run the application:

java ChatSimulator


A GUI window will appear, and simulated messages will start appearing after a short delay.

Expected Console Output

The only expected console output is the Singleton initialization confirmation:
