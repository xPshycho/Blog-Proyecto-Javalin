package edu.pucmm.eict.controladores;

import edu.pucmm.eict.modelos.Chat;
import edu.pucmm.eict.modelos.Usuario;
import edu.pucmm.eict.services.ChatService;
import io.javalin.Javalin;
import io.javalin.http.Context;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChatController {

    private ChatService chatService = ChatService.getInstance();

    public void registerRoutes(Javalin app) {
        // Middleware: Solo administradores o autores pueden acceder a la lista de chats
        app.before("/chats", ctx -> {
            Usuario usuario = ctx.sessionAttribute("usuario");
            if (usuario == null || (!usuario.isAdministrador() && !usuario.isAutor())) {
                ctx.redirect("/dashboard");
            }
        });

        // Ruta para listar los chats persistidos (para el dashboard, por ejemplo)
        app.get("/chats", this::handleChats);

        // Ruta para cargar el formulario de chat vía AJAX
        app.get("/chat-form", ctx -> {
            ctx.render("chat-form.html");
        });

        // Ruta para cargar la interfaz de chat en vivo (para usuarios y para admin/responder)
        app.get("/live-chat", ctx -> {
            String articleId = ctx.queryParam("articleId");
            String authorId = ctx.queryParam("authorId");
            String chatId = ctx.queryParam("chatId"); // Parámetro opcional
            Map<String, Object> model = new HashMap<>();
            model.put("articleId", articleId);
            model.put("authorId", authorId);

            Chat chat = null;
            // Si se pasa un chatId, se intenta recuperar el chat activo
            if (chatId != null && !chatId.trim().isEmpty()) {
                chat = chatService.getActiveChat(chatId);
            }
            // Si no se encontró o no se pasó chatId, se consulta por articleId y authorId
            if (chat == null) {
                chat = chatService.getActiveChatByArticleAndAuthor(articleId, authorId);
                if (chat == null) {
                    chat = chatService.getPersistedChatByArticleAndAuthor(articleId, authorId);
                }
            }
            if (chat != null) {
                model.put("chat", chat);
            }
            ctx.render("live-chat.html", model);
        });

        // Ruta para iniciar un chat vía POST
        app.post("/iniciar-chat", ctx -> {
            String nombreCompleto = ctx.formParam("nombreCompleto");
            String correo = ctx.formParam("correo");
            String telefono = ctx.formParam("telefono");
            String razonConsulta = ctx.formParam("razonConsulta");
            String articleId = ctx.formParam("articleId");
            String authorId = ctx.formParam("authorId");

            // Generar un ID único para el chat
            String chatId = UUID.randomUUID().toString();

            // Crear el chat y agregarlo al almacén en memoria
            Chat chat = new Chat(chatId, nombreCompleto, correo, telefono, razonConsulta, articleId, authorId);
            chatService.addActiveChat(chat);

            // Enviar notificación vía WebSocket a los dashboards conectados
            String notificationJson = "{\"type\":\"NEW_CHAT\",\"chat\":{" +
                    "\"id\":\"" + chat.getId() + "\"," +
                    "\"nombreCompleto\":\"" + chat.getNombreCompleto() + "\"," +
                    "\"correo\":\"" + chat.getCorreo() + "\"," +
                    "\"telefono\":\"" + chat.getTelefono() + "\"," +
                    "\"razonConsulta\":\"" + chat.getRazonConsulta() + "\"" +
                    "}}";
            DashboardWebSocketHandler.sendNotification(notificationJson);

            // Guardar información en la sesión
            ctx.sessionAttribute("chatStarted", true);
            ctx.sessionAttribute("chatUserName", nombreCompleto);
            ctx.sessionAttribute("chatUserEmail", correo);

            // Devolver el chat en JSON
            ctx.json(chat);
        });

        // Ruta para responder a un chat (vista en el dashboard)
        // Ruta para responder a un chat (vista en el dashboard)
        app.get("/responder-chat", ctx -> {
            String chatId = ctx.queryParam("id");
            if (chatId == null) {
                ctx.status(400).result("ID del chat no proporcionado.");
                return;
            }
            Chat chat = chatService.getActiveChat(chatId);
            if (chat == null) {
                ctx.status(404).result("Chat no encontrado.");
                return;
            }
            Map<String, Object> model = new HashMap<>();
            model.put("chat", chat);

            // Agregar el rol del usuario (admin, author o user)
            Usuario usuario = ctx.sessionAttribute("usuario");
            if (usuario != null) {
                if (usuario.isAdministrador()) {
                    model.put("rol", "admin");
                } else if (usuario.isAutor()) {
                    model.put("rol", "author");
                } else {
                    model.put("rol", "user");
                }
            } else {
                model.put("rol", "user");
            }

            // Si se solicita vía AJAX, renderiza solo el fragmento de mensajes
            if (ctx.queryParam("ajax") != null) {
                ctx.render("chat-messages.html", model);
            } else {
                ctx.render("responder-chat.html", model);
            }
        });

        // Ruta para cerrar un chat
        app.get("/cerrar-chat", ctx -> {
            String chatId = ctx.queryParam("id");
            if (chatId == null) {
                ctx.status(400).result("ID del chat no proporcionado.");
                return;
            }
            chatService.closeChat(chatId);
            ctx.redirect("/chats");
        });

        app.get("/borrar-chat", ctx -> {
            String chatId = ctx.queryParam("id");
            if (chatId == null) {
                ctx.status(400).result("ID del chat no proporcionado.");
                return;
            }
            boolean eliminado = chatService.eliminarChat(chatId);
            if (!eliminado) {
                ctx.status(404).result("Chat no encontrado o no se pudo eliminar.");
            } else {
                ctx.redirect("/chats");
            }
        });


    }



    private void handleChats(Context ctx) {
        Collection<Chat> chatsCollection = chatService.getAllPersistedChats();
        ArrayList<Chat> chats = new ArrayList<>(chatsCollection);
        Map<String, Object> model = new HashMap<>();
        model.put("chats", chats);
        model.put("usuario", ctx.sessionAttribute("usuario"));
        ctx.render("chats.html", model);
    }
}
