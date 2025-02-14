package com.Forex.server;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

public class FXDataServer {

    private final int SERVER_PORT;
    private Selector selector;

    private final Set<String> currencyPairs;
    private final Map<SocketChannel,String> authenticatedClients;
    private final Map<String,String> authRepository;
    private final Map<String,Set<SocketChannel>> subscriptions;
    private final Map<SocketChannel,StringBuilder> clientsStringBuilders;


    public FXDataServer(int server_port,
                        Set<String> currency_pairs,
                        Map<String, Set<SocketChannel>> subscriptions,
                        Map<String, String> auth_repository) {
        this.SERVER_PORT = server_port;
        this.currencyPairs = currency_pairs;
        this.subscriptions = subscriptions;
        this.authRepository = auth_repository;

        this.authenticatedClients = new HashMap<>(); !
        this.clientsStringBuilders = new HashMap<>();
    }

    public void startServer() {
        try(ServerSocketChannel serverChannel = ServerSocketChannel.open())
        {
            this.selector = Selector.open();

            serverChannel.bind(new InetSocketAddress(SERVER_PORT));
            serverChannel.configureBlocking(false);
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);


            while (selector.isOpen() && serverChannel.isOpen()){
                selector.select(); // Listening for events
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectedKeys.iterator();

                while(iterator.hasNext()){
                    SelectionKey key = iterator.next();
                    try {
                        if (key.isAcceptable()) {
                            handleConnectionRequest(serverChannel);
                        } else if (key.isReadable()) {
                            handleClientMessage(key);     // Key keeps channel and its events
                        }
                    } catch (IOException e) {
                        System.err.println("IOException during event handling: " + e.getMessage());
                        shutDownClient(key);
                    }
                    iterator.remove();
                }

            }

            selector.close();

        } catch (IOException e) {
            System.err.println("IOException occurred in startServer: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected exception in startServer: " + e.getMessage());
        }
    }



    private void handleConnectionRequest(ServerSocketChannel serverChannel) throws IOException {
        SocketChannel clientChannel = serverChannel.accept();
        if (clientChannel != null) {

            clientChannel.configureBlocking(false);
            clientChannel.register(selector, SelectionKey.OP_READ);
            clientsStringBuilders.put(clientChannel, new StringBuilder());

            System.out.println("Client connected: " + clientChannel.getRemoteAddress());
        }
    }


    private void handleClientMessage(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(256);
        int bytesRead = clientChannel.read(buffer);

        if (bytesRead > 0) {
            buffer.flip();
            byte[] byteData = new byte[buffer.remaining()];
            buffer.get(byteData);

            StringBuilder stringBuilder = clientsStringBuilders.get(clientChannel);
            stringBuilder.append(new String(byteData));

            String fullMessage = stringBuilder.toString();

            String[] messages = fullMessage.split("\n");

            for (int i = 0; i < messages.length - 1; i++) {
                validateMessageAndTakeAction(clientChannel, messages[i].trim());
            }

            if (fullMessage.endsWith("\n")) {
                validateMessageAndTakeAction(clientChannel, messages[messages.length - 1].trim());
                stringBuilder.setLength(0);
            } else {
                stringBuilder.setLength(0);
                stringBuilder.append(messages[messages.length - 1]); //mesaj \n ile itmemis ise sakliyoruz.
            }
        } else if (bytesRead == -1) {
            shutDownClient(key);
        }
    }





    private void validateMessageAndTakeAction(SocketChannel clientChannel, String message) {
        System.out.println("Message received: " + message);

        if (!authenticatedClients.containsKey(clientChannel) && !message.startsWith("connect|")) {
            sendInfoMessageToClient(clientChannel, "ERROR|You are not authenticated");
            return;
        }

        String[] messageParts = message.split("\\|");
        String command = messageParts[0];

        switch (command) {
            case "connect":
                handleConnect(clientChannel, messageParts);
                break;

            case "disconnect":
                handleDisconnect(clientChannel, messageParts);
                break;

            case "subscribe":
                handleSubscribe(clientChannel, messageParts);
                break;

            case "unsubscribe":
                handleUnsubscribe(clientChannel, messageParts);
                break;

            default:
                sendInfoMessageToClient(clientChannel, "ERROR|Invalid message format");
                break;
        }
    }

    private void handleSubscribe(SocketChannel clientChannel, String[] messageParts) {
        if (messageParts.length != 2) {
            sendInfoMessageToClient(clientChannel, "ERROR|Invalid message format");
            return;
        }

        String currencyPair = messageParts[1].trim().toUpperCase();

        if (!currencyPairs.contains(currencyPair)) {
            sendInfoMessageToClient(clientChannel, "ERROR|Invalid currency pair: " + currencyPair);
            return;
        }

        Set<SocketChannel> clients = subscriptions.get(currencyPair);
        if (clients.contains(clientChannel)) {
            sendInfoMessageToClient(clientChannel, "ERROR|Already subscribed to currency pair: " + currencyPair);
        } else {
            clients.add(clientChannel);
            System.out.println("Client subscribed to currency pair: " + currencyPair);
            sendInfoMessageToClient(clientChannel, "SUCCESS|Subscribed to currency pair: " + currencyPair);
        }
    }

    private void handleUnsubscribe(SocketChannel clientChannel, String[] messageParts) {
        if (messageParts.length != 2) {
            sendInfoMessageToClient(clientChannel, "ERROR|Invalid message format");
            return;
        }
        String currencyPair = messageParts[1].trim().toUpperCase();

        if (!currencyPairs.contains(currencyPair)) {
            sendInfoMessageToClient(clientChannel, "ERROR|Invalid currency pair: " + currencyPair);
            return;
        }

        Set<SocketChannel> clients = subscriptions.get(currencyPair);
        if (clients.contains(clientChannel)) {
            clients.remove(clientChannel);
            System.out.println("Client unsubscribed from currency pair: " + currencyPair);
            sendInfoMessageToClient(clientChannel, "SUCCESS|Unsubscribed from currency pair: " + currencyPair);
        } else {
            sendInfoMessageToClient(clientChannel, "ERROR|Not subscribed to currency pair: " + currencyPair);
        }
    }

    private void handleConnect(SocketChannel clientChannel, String[] messageParts) {
        if (messageParts.length != 3) {
            sendInfoMessageToClient(clientChannel, "ERROR|Invalid message format");
            return;
        }

        String username = messageParts[1].trim();
        String password = messageParts[2].trim();

        if (authenticatedClients.containsKey(clientChannel)) {
            sendInfoMessageToClient(clientChannel, "ERROR|Already authenticated");
            return;
        }

        if (authenticatedClients.containsValue(username)) {
            sendInfoMessageToClient(clientChannel, "ERROR|User already logged in from another session");
            return;
        }


        if (authRepository.containsKey(username) && authRepository.get(username).equals(password)) {
            authenticatedClients.put(clientChannel, username);
            sendInfoMessageToClient(clientChannel, "SUCCESS|Connected as: " + username);
            System.out.println("User connected: "  + username);
        } else {
            sendInfoMessageToClient(clientChannel, "ERROR|Invalid credentials");
        }
    }




    private void handleDisconnect(SocketChannel clientChannel, String[] messageParts) {
        if (messageParts.length != 3) {
            sendInfoMessageToClient(clientChannel, "ERROR|Invalid message format");
            return;
        }

        String username = messageParts[1].trim();
        String password = messageParts[2].trim();

        if (!authenticatedClients.containsKey(clientChannel)) {
            sendInfoMessageToClient(clientChannel, "ERROR|Not authenticated");
            return;
        }

        String authenticatedUsername = authenticatedClients.get(clientChannel);


        if (!authenticatedUsername.equals(username)) {
            sendInfoMessageToClient(clientChannel, "ERROR|You can only disconnect your own session");
            return;
        }

        if (!authRepository.containsKey(username) || !authRepository.get(username).equals(password)) {
            sendInfoMessageToClient(clientChannel, "ERROR|Invalid credentials for disconnect");
            return;
        }

        authenticatedClients.remove(clientChannel);
        sendInfoMessageToClient(clientChannel, "SUCCESS|Disconnected");

        SelectionKey key = clientChannel.keyFor(selector);
        shutDownClient(key);
    }




    private void shutDownClient(SelectionKey key){
        try {

            SocketChannel clientChannel = (SocketChannel) key.channel();
            System.out.println("Shutting down client: " + clientChannel.getRemoteAddress());

            clientsStringBuilders.remove(clientChannel);

            authenticatedClients.remove(clientChannel);

            subscriptions.values()
                    .forEach(clients -> clients.remove(clientChannel));

            key.cancel();

            clientChannel.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendInfoMessageToClient(SocketChannel clientChannel, String message) {
        ByteBuffer buffer = ByteBuffer.wrap((message + "\r\n").getBytes());
        try {
            clientChannel.write(buffer);
        } catch (IOException e) {

            System.err.println("IOException while sending message: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected exception while sending info message to the client: " + e.getMessage());
        }
    }

}
