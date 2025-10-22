let chatSocket;
let chatId;
let authorId;
let articleId;
let chatRole; // "user", "admin" o "author"
let heartbeatInterval;

function initializeChat(providedChatId, role, authorIdParam, articleIdParam) {
    if (chatSocket && chatSocket.readyState === WebSocket.OPEN) {
        chatSocket.close();
    }
    chatId = providedChatId;
    chatRole = role;
    authorId = authorIdParam;
    articleId = articleIdParam;

    chatSocket = new WebSocket(`ws://${window.location.host}/chat-ws/${chatId}`);

    chatSocket.onopen = function () {
        console.log("Conexión WebSocket establecida");
        let initData = {
            type: "INIT",
            chatId: chatId,
            userId: role + "-" + Date.now(),
            senderId: role,
            authorId: authorId,
            articleId: articleId,
            userData: {},
            mensaje: "",
            timestamp: new Date().toISOString()
        };

        if (role === "user") {
            initData.userData = {
                nombre: document.getElementById("nombreCompleto") ? document.getElementById("nombreCompleto").value : "",
                correo: document.getElementById("correo") ? document.getElementById("correo").value : "",
                telefono: document.getElementById("telefono") ? document.getElementById("telefono").value : ""
            };
            initData.mensaje = document.getElementById("razonConsulta") ? document.getElementById("razonConsulta").value : "";
        }
        chatSocket.send(JSON.stringify(initData));
        setupMessageListeners();

        heartbeatInterval = setInterval(() => {
            if (chatSocket && chatSocket.readyState === WebSocket.OPEN) {
                chatSocket.send(JSON.stringify({ type: "PING" }));
            }
        }, 30000);
    };

    function refreshAdminChat() {
        fetch("/responder-chat?ajax=true&id=" + chatId)
            .then(response => {
                if (!response.ok) {
                    throw new Error("Error al cargar los mensajes");
                }
                return response.text();
            })
            .then(html => {
                document.getElementById("chatMessages").innerHTML = html;
            })
            .catch(err => console.error("Error al recargar el chat:", err));
    }

    chatSocket.onmessage = function (event) {
        let mensaje = JSON.parse(event.data);
        if (mensaje.type === "PONG") {
            console.log("Recibido PONG del servidor");
            return;
        }
        // Para roles admin y author, se actualiza la vista usando AJAX
        if (chatRole === "admin" || chatRole === "author") {
            refreshAdminChat();
        } else {
            displayMessage(mensaje);
        }
    };

    chatSocket.onclose = function () {
        console.log("Conexión WebSocket cerrada");
        if (heartbeatInterval) clearInterval(heartbeatInterval);
    };

    chatSocket.onerror = function (error) {
        console.error("Error en la conexión WebSocket:", error);
        displaySystemMessage("Error en la conexión. Por favor, inténtalo de nuevo.");
    };
}

function setupMessageListeners() {
    const sendBtn = document.getElementById("sendMessageBtn");
    const messageInput = document.getElementById("messageInput");
    if (!sendBtn || !messageInput) return;
    sendBtn.removeEventListener("click", sendUserMessage);
    messageInput.removeEventListener("keypress", handleKeyPress);
    sendBtn.addEventListener("click", sendUserMessage);
    messageInput.addEventListener("keypress", handleKeyPress);
}

function handleKeyPress(e) {
    if (e.key === "Enter") {
        sendUserMessage();
    }
}

function sendChatMessage(mensaje) {
    if (chatSocket && chatSocket.readyState === WebSocket.OPEN) {
        const messageData = {
            type: "MESSAGE",
            chatId: chatId,
            senderId: chatRole,
            authorId: authorId,
            articleId: articleId,
            mensaje: mensaje,
            timestamp: new Date().toISOString()
        };
        chatSocket.send(JSON.stringify(messageData));
        return true;
    }
    return false;
}

function sendUserMessage() {
    const messageInput = document.getElementById("messageInput");
    if (!messageInput) return;
    const mensaje = messageInput.value.trim();
    if (mensaje) {
        if (sendChatMessage(mensaje)) {
            messageInput.value = "";
        } else {
            displaySystemMessage("Error al enviar el mensaje. Por favor, inténtalo de nuevo.");
        }
    } else {
        console.log("Mensaje vacío no enviado.");
    }
}

function displayMessage(mensaje) {
    const chatMessages = document.getElementById("chatMessages");
    if (!chatMessages) return;
    const messageElement = document.createElement("div");
    const isUserMessage = mensaje.type === "MESSAGE" && mensaje.senderId === "user";
    messageElement.className = isUserMessage ? "user-message" : "author-message";

    const messageContent = document.createElement("div");
    messageContent.className = "message-content";
    messageContent.textContent = mensaje.mensaje;

    const messageTime = document.createElement("div");
    messageTime.className = "message-time";
    let msgDate = new Date(mensaje.timestamp);
    messageTime.textContent = msgDate.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

    messageElement.appendChild(messageContent);
    messageElement.appendChild(messageTime);
    chatMessages.appendChild(messageElement);
    chatMessages.scrollTop = chatMessages.scrollHeight;
}

function displaySystemMessage(mensaje) {
    const chatMessages = document.getElementById("chatMessages");
    if (!chatMessages) return;
    const messageElement = document.createElement("div");
    messageElement.className = "system-message";
    messageElement.textContent = mensaje;
    chatMessages.appendChild(messageElement);
    chatMessages.scrollTop = chatMessages.scrollHeight;
}

function showLiveChatWithParams(chatIdParam, authorIdParam, articleIdParam) {
    const url = `/live-chat?articleId=${articleIdParam}&authorId=${authorIdParam}&chatId=${chatIdParam}`;
    fetch(url)
        .then(response => {
            if (!response.ok) {
                throw new Error("Error al cargar la interfaz live-chat");
            }
            return response.text();
        })
        .then(html => {
            const modalBody = document.getElementById("chatModalBody");
            if (modalBody) {
                modalBody.innerHTML = html;
                initializeChat(chatIdParam, "user", authorIdParam, articleIdParam);
            }
        })
        .catch(err => {
            console.error("Error al cargar live-chat:", err);
            const modalBody = document.getElementById("chatModalBody");
            if (modalBody) {
                modalBody.innerHTML = "<p>Error al cargar el chat en vivo.</p>";
            }
        });
}

function setupChatForm(articleIdParam, authorIdParam) {
    const form = document.getElementById("chatInitForm");
    if (form) {
        form.addEventListener("submit", function (e) {
            e.preventDefault();
            e.stopPropagation();
            if (!form.checkValidity()) {
                form.classList.add("was-validated");
                return;
            }
            const formData = new FormData(form);
            fetch("/iniciar-chat", {
                method: "POST",
                body: formData
            })
                .then(response => {
                    if (!response.ok) {
                        throw new Error("Error al iniciar el chat");
                    }
                    return response.json();
                })
                .then(chat => {
                    console.log("Chat iniciado: " + chat.id);
                    localStorage.setItem(`chat-${formData.get("articleId")}`, chat.id);
                    window.history.replaceState({}, document.title, window.location.pathname + "?id=" + formData.get("articleId"));
                    showLiveChatWithParams(chat.id, formData.get("authorId"), formData.get("articleId"));
                })
                .catch(error => {
                    console.error("Error:", error);
                    alert("Error al iniciar el chat. Por favor, inténtalo de nuevo.");
                });
        });
        form.classList.add("needs-validation");
    } else {
        console.error("Elemento form con id 'chatInitForm' no encontrado");
    }
}

window.chatModule = {
    setupChatForm,
    initializeChat,
    sendChatMessage
};
