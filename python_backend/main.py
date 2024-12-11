from fastapi import FastAPI, WebSocket, WebSocketDisconnect
import requests

app = FastAPI()


class ConnectionManager:
    def __init__(self):
        self.active_connections: dict[str: list[WebSocket]] = {}
        self.nodeId: str = 'nodemcuadmin'

    async def connect(self, websocket: WebSocket, userId: str):
        if userId not in self.active_connections:
            if self.nodeId in self.active_connections:
                self.active_connections[userId] = [websocket]
                await websocket.accept()
            elif userId == self.nodeId:
                self.active_connections[userId] = [websocket]
                await websocket.accept()
            else:
                requests.get('http://127.0.0.1:3000/')
                await websocket.accept()
                await websocket.close(
                    code=1008, reason="Server Disconnected, Please wait...")
            print("Connected to: ", userId)
        else:
            if len(self.active_connections[self.nodeId]) <= 1 and userId != self.nodeId:
                self.active_connections[userId].append(websocket)
                await websocket.accept()
                print(
                    f"Connected to(x{len(self.active_connections[self.nodeId])}): ", userId)
            else:
                await websocket.accept()
                await websocket.close(
                    code=1008, reason="You are not allowed!!!")

    def disconnect(self, websocket: WebSocket, userId: str):
        if self.active_connections[userId]:
            self.active_connections[userId].remove(websocket)
            if not self.active_connections[userId]:
                del self.active_connections[userId]

    async def send_personal_message(self, message: str, websocket: WebSocket, userId: str):
        if self.nodeId in self.active_connections:
            if userId in self.active_connections:
                await websocket.send_text(message)
        else:
            await websocket.send_text('Server Busy')

    async def broadcast(self, message: str, websocket: WebSocket, userId: str):
        if self.nodeId in self.active_connections:
            for connection in self.active_connections[userId]:
                if not connection == websocket:
                    await connection.send_text(message)
            await self.active_connections[self.nodeId][0].send_text(message)
        else:
            await websocket.send_text('Server Busy')


manager = ConnectionManager()


@app.get("/", tags=["Root"])
async def status():
    return {"status": "API working"}


@app.websocket("/ws/{client_id}")
async def websocket_endpoint(websocket: WebSocket, client_id: str):
    await manager.connect(websocket, client_id)
    try:
        while True:
            data = await websocket.receive_text()
            print(client_id, "->", data)
            await manager.broadcast(data, websocket, client_id)
    except WebSocketDisconnect:
        manager.disconnect(websocket, client_id)
