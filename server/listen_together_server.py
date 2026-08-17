#!/usr/bin/env python3
"""
SONZA - Listen Together Real-Time WebSocket Server
Authoritative room synchronization, clock offset calculation, and drift management.
"""

import asyncio
import json
import logging
import time
from typing import Dict, Set

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s")

class Room:
    def __init__(self, room_id: str, host_id: str, host_name: str):
        self.room_id = room_id
        self.host_id = host_id
        self.host_name = host_name
        self.current_track_id: str = ""
        self.playback_position_ms: int = 0
        self.playback_state: str = "PAUSED"
        self.last_update_server_time_ms: int = int(time.time() * 1000)
        self.members: Dict[str, dict] = {}
        self.clients: Set = set()

    def add_member(self, user_id: str, display_name: str, ws):
        self.members[user_id] = {
            "id": user_id,
            "displayName": display_name,
            "role": "HOST" if user_id == self.host_id else "LISTENER",
            "joinedAt": int(time.time() * 1000),
            "latencyMs": 20
        }
        self.clients.add(ws)

    def remove_member(self, user_id: str, ws):
        self.members.pop(user_id, None)
        self.clients.discard(ws)

    def calculate_current_position(self) -> int:
        if self.playback_state == "PLAYING":
            elapsed = int(time.time() * 1000) - self.last_update_server_time_ms
            return self.playback_position_ms + elapsed
        return self.playback_position_ms

class ListenTogetherServer:
    def __init__(self, host: str = "0.0.0.0", port: int = 8765):
        self.host = host
        self.port = port
        self.rooms: Dict[str, Room] = {}

    async def handle_client(self, websocket, path):
        # Query format: /ws/<room_id>?userId=<id>&name=<name>
        try:
            parts = path.split("?")
            room_id = parts[0].strip("/").replace("ws/", "")
            query_params = {}
            if len(parts) > 1:
                for param in parts[1].split("&"):
                    if "=" in param:
                        k, v = param.split("=", 1)
                        query_params[k] = v

            user_id = query_params.get("userId", f"user_{int(time.time())}")
            user_name = query_params.get("name", "Audiophile")

            if room_id not in self.rooms:
                self.rooms[room_id] = Room(room_id, host_id=user_id, host_name=user_name)

            room = self.rooms[room_id]
            room.add_member(user_id, user_name, websocket)
            logging.info(f"User {user_name} ({user_id}) joined room {room_id}. Total: {len(room.members)}")

            # Broadcast room joined event
            await self.broadcast_room_state(room)

            async for message in websocket:
                data = json.loads(message)
                action = data.get("action")

                if action == "CLOCK_PING":
                    # NTP-like clock ping response
                    t0 = data.get("t0", 0)
                    now_ms = int(time.time() * 1000)
                    response = json.dumps({
                        "action": "CLOCK_PONG",
                        "t0": t0,
                        "serverTime": now_ms
                    })
                    await websocket.send(response)

                elif action == "SYNC_PLAYBACK":
                    # Host authoritative sync event
                    if user_id == room.host_id:
                        room.current_track_id = data.get("trackId", "")
                        room.playback_position_ms = data.get("positionMs", 0)
                        room.playback_state = data.get("state", "PAUSED")
                        room.last_update_server_time_ms = int(time.time() * 1000)
                        await self.broadcast_room_state(room)

        except Exception as e:
            logging.error(f"Client error: {e}")
        finally:
            if 'room' in locals() and room:
                room.remove_member(user_id, websocket)
                if len(room.members) == 0:
                    self.rooms.pop(room_id, None)
                else:
                    await self.broadcast_room_state(room)

    async def broadcast_room_state(self, room: Room):
        payload = json.dumps({
            "action": "ROOM_UPDATE",
            "roomId": room.room_id,
            "hostId": room.host_id,
            "trackId": room.current_track_id,
            "positionMs": room.calculate_current_position(),
            "state": room.playback_state,
            "serverTimestampMs": int(time.time() * 1000),
            "members": list(room.members.values())
        })
        tasks = [client.send(payload) for client in room.clients if client.open]
        if tasks:
            await asyncio.gather(*tasks, return_exceptions=True)

if __name__ == "__main__":
    import websockets
    server = ListenTogetherServer()
    start_server = websockets.serve(server.handle_client, server.host, server.port)
    logging.info(f"SONZA Listen Together server running on ws://{server.host}:{server.port}")
    asyncio.get_event_loop().run_until_complete(start_server)
    asyncio.get_event_loop().run_forever()
