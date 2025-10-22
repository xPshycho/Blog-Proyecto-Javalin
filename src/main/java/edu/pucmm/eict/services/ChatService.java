package edu.pucmm.eict.services;

import edu.pucmm.eict.modelos.Chat;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatService {
    private static ChatService instance;

    // Almacén en memoria para chats activos
    private ConcurrentHashMap<String, Chat> activeChats = new ConcurrentHashMap<>();

    // EntityManagerFactory para persistencia en base de datos
    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("AjaxPU");

    // Executor para tareas asíncronas
    private ExecutorService executor = Executors.newFixedThreadPool(2);

    private ChatService() { }

    public static synchronized ChatService getInstance() {
        if (instance == null) {
            instance = new ChatService();
        }
        return instance;
    }

    // Métodos para el manejo en memoria
    public void addActiveChat(Chat chat) {
        activeChats.put(chat.getId(), chat);
    }

    public Chat getActiveChat(String id) {
        return activeChats.get(id);
    }

    public void removeActiveChat(String id) {
        activeChats.remove(id);
    }

    // Metodo para obtener todos los chats persistidos (opcional, para dashboard, etc.)
    public List<Chat> getAllPersistedChats() {
        EntityManager em = emf.createEntityManager();
        List<Chat> chats = em.createQuery("SELECT c FROM Chat c", Chat.class).getResultList();
        em.close();
        return chats;
    }

    // Metodo asíncrono para persistir o actualizar un chat en BD
    public void persistChatAsync(Chat chat) {
        executor.submit(() -> {
            EntityManager em = emf.createEntityManager();
            try {
                em.getTransaction().begin();
                // Si el chat ya existe, se hace merge; si no, se persiste.
                Chat persistedChat = em.find(Chat.class, chat.getId());
                if (persistedChat == null) {
                    em.persist(chat);
                } else {
                    em.merge(chat);
                }
                em.getTransaction().commit();
            } catch (Exception e) {
                e.printStackTrace(); // o loggear adecuadamente
            } finally {
                em.close();
            }
        });
    }

    // Metodo para cerrar un chat: persiste y remueve de la memoria
    public void closeChat(String id) {
        Chat chat = activeChats.get(id);
        if (chat != null) {
            // Persistir el chat de forma asíncrona
            persistChatAsync(chat);
            // Remover de los chats activos
            activeChats.remove(id);
        }
    }

    public Collection<Chat> getActiveChats() {
        return activeChats.values();
    }

    // En ChatService.java

    public Chat getPersistedChat(String id) {
        EntityManager em = emf.createEntityManager();
        try {
            Chat chat = em.find(Chat.class, id);
            return chat;
        } finally {
            em.close();
        }
    }

    public Chat getActiveChatByArticleAndAuthor(String articleId, String authorId) {
        for (Chat chat : activeChats.values()) {
            if (chat.getArticleId().equals(articleId) && chat.getAuthorId().equals(authorId)) {
                return chat;
            }
        }
        return null;
    }


    public Chat getPersistedChatByArticleAndAuthor(String articleId, String authorId) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Chat> query = em.createQuery(
                    "SELECT c FROM Chat c WHERE c.articleId = :articleId AND c.authorId = :authorId", Chat.class);
            query.setParameter("articleId", articleId);
            query.setParameter("authorId", authorId);
            // Si existen varios, devolvemos el primero
            List<Chat> results = query.getResultList();
            return results.isEmpty() ? null : results.get(0);
        } finally {
            em.close();
        }
    }

    public boolean eliminarChat(String chatId) {
        // Usamos GestionDb para eliminar el chat de la BD
        GestionDb<Chat> gestion = new GestionDb<>(Chat.class);
        boolean resultado = gestion.eliminar(chatId);
        // También lo eliminamos del almacén en memoria, si está presente.
        activeChats.remove(chatId);
        return resultado;
    }



}
