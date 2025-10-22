let ws;
const ports = [];

function initWebSocket() {
    ws = new WebSocket(`ws://${self.location.host}/dashboard-ws`);
    ws.onopen = () => {
        console.log("SharedWorker: WebSocket connected");
    };
    ws.onmessage = (event) => {
        ports.forEach(port => port.postMessage(event.data));
    };
    ws.onerror = (error) => {
        console.error("SharedWorker: WebSocket error", error);
    };
    ws.onclose = () => {
        console.log("SharedWorker: WebSocket closed, retrying in 5 seconds");
        setTimeout(initWebSocket, 5000);
    };
}

initWebSocket();

onconnect = (e) => {
    const port = e.ports[0];
    ports.push(port);
    port.onmessage = (e) => {
        if (ws && ws.readyState === WebSocket.OPEN) {
            ws.send(e.data);
        }
    };
    port.start();
};
