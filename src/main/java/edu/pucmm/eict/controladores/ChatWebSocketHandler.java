package edu.pucmm.eict.controladores;

import com.google.gson.*;
import edu.pucmm.eict.modelos.Chat;
import edu.pucmm.eict.modelos.Mensaje;
import edu.pucmm.eict.services.ChatService;
import io.javalin.Javalin;
import io.javalin.websocket.WsContext;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChatWebSocketHandler {

    private static ConcurrentHashMap<String, Set<WsContext>> activeChatsSessions = new ConcurrentHashMap<>();
    private Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
                @Override
                public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                        throws JsonParseException {
                    String dateStr = json.getAsString();
                    try {
                        if (dateStr.endsWith("Z")) {
                            return LocalDateTime.ofInstant(Instant.parse(dateStr), ZoneId.systemDefault());
                        } else {
                            return LocalDateTime.parse(dateStr);
                        }
                    } catch (Exception e) {
                        throw new JsonParseException("Error parsing date: " + dateStr, e);
                    }
                }
            }).create();

    // Add message validation
    private boolean validateMessage(JsonObject jsonObj) {
        return jsonObj.has("type") &&
                (jsonObj.get("type").getAsString().equals("PING") ||
                        jsonObj.get("type").getAsString().equals("MESSAGE") ||
                        jsonObj.get("type").getAsString().equals("INIT"));
    }

    public void registerRoutes(Javalin app) {
        app.ws("/chat-ws/{chatId}", ws -> {
            ws.onConnect(ctx -> {
                String chatId = ctx.pathParam("chatId");
                activeChatsSessions.computeIfAbsent(chatId, k -> ConcurrentHashMap.newKeySet()).add(ctx);
                System.out.println("WebSocket connected for chat: " + chatId);
            });

            ws.onMessage(ctx -> {
                String chatId = ctx.pathParam("chatId");
                String mensajeJson = ctx.message();

                JsonObject jsonObj;
                try {
                    jsonObj = JsonParser.parseString(mensajeJson).getAsJsonObject();
                } catch(Exception e){
                    System.err.println("Error parsing JSON message: " + e.getMessage());
                    return;
                }
                String type = jsonObj.get("type").getAsString();

                // Responder a mensajes PING con un PONG
                if ("PING".equals(type)) {
                    ctx.send("{\"type\":\"PONG\"}");
                    return;
                }

                // Transmitir el mensaje a todos los clientes conectados al chat
                Set<WsContext> sessions = activeChatsSessions.get(chatId);
                if (sessions != null) {
                    sessions.forEach(session -> session.send(mensajeJson));
                }

                // Solo procesar y almacenar mensajes de tipo "MESSAGE" en el historial del chat
                if ("MESSAGE".equals(type)) {
                    Chat chat = ChatService.getInstance().getActiveChat(chatId);
                    if (chat != null) {
                        Mensaje msg = gson.fromJson(mensajeJson, Mensaje.class);
                        msg.setTimestamp(LocalDateTime.now());
                        chat.getMensajes().add(msg);
                    }
                }
                // Los mensajes de tipo "INIT" se transmiten pero no se almacenan en el historial
            });

            ws.onClose(ctx -> {
                String chatId = ctx.pathParam("chatId");
                Set<WsContext> sessions = activeChatsSessions.get(chatId);
                if (sessions != null) {
                    sessions.remove(ctx);
                    if (sessions.isEmpty()) {
                        activeChatsSessions.remove(chatId);
                        // Persistir y cerrar el chat si ya no hay sesiones activas
                        ChatService.getInstance().closeChat(chatId);
                    }
                }
                System.out.println("WebSocket closed for chat: " + chatId);
            });

            ws.onError(ctx -> {
                System.err.println("WebSocket error for chat: " + ctx.pathParam("chatId"));
            });
        });
    }
}
