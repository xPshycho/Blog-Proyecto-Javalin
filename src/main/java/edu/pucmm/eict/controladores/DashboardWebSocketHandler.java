package edu.pucmm.eict.controladores;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import io.javalin.Javalin;
import io.javalin.websocket.WsContext;
import edu.pucmm.eict.services.ChatService;
import edu.pucmm.eict.modelos.Chat;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DashboardWebSocketHandler {

    private static Set<WsContext> dashboardSessions = ConcurrentHashMap.newKeySet();

    private static Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
                @Override
                public JsonElement serialize(LocalDateTime src, Type typeOfSrc, com.google.gson.JsonSerializationContext context) {
                    return new JsonPrimitive(src.toString());
                }
            })
            .registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
                @Override
                public LocalDateTime deserialize(JsonElement json, Type typeOfT, com.google.gson.JsonDeserializationContext context) throws JsonParseException {
                    return LocalDateTime.parse(json.getAsString());
                }
            })
            .create();

    public void registerRoutes(Javalin app) {
        app.ws("/dashboard-ws", ws -> {
            ws.onConnect(ctx -> {
                dashboardSessions.add(ctx);
                System.out.println("Dashboard WebSocket connected: " + ctx.hashCode());
                // Enviar la lista de chats activos al cliente que se acaba de conectar
                Collection<Chat> activeChats = ChatService.getInstance().getActiveChats();
                String activeChatsJson = gson.toJson(activeChats);
                String initialMessage = "{\"type\":\"ACTIVE_CHATS\",\"chats\":" + activeChatsJson + "}";
                ctx.send(initialMessage);
            });
            ws.onMessage(ctx -> {
                String msg = ctx.message();
                try {
                    // Se espera un mensaje JSON, por ejemplo para PING
                    Message message = gson.fromJson(msg, Message.class);
                    if ("PING".equals(message.type)) {
                        ctx.send("{\"type\":\"PONG\"}");
                    }
                } catch (Exception e) {
                    System.err.println("Error processing message in Dashboard WebSocket: " + e.getMessage());
                }
            });
            ws.onClose(ctx -> {
                dashboardSessions.remove(ctx);
                System.out.println("Dashboard WebSocket disconnected: " + ctx.hashCode());
            });
            ws.onError(ctx -> {
                System.err.println("Error in Dashboard WebSocket: " + ctx.hashCode());
            });
        });
    }

    // Metodo para enviar notificaciones a todas las sesiones conectadas
    public static void sendNotification(String message) {
        dashboardSessions.forEach(session -> {
            if (session.session.isOpen()) {
                session.send(message);
            }
        });
    }

    // Clase auxiliar para parsear mensajes entrantes
    private static class Message {
        String type;
    }
}
