import * as StompJs from "@stomp/stompjs";

const brokerURL =
  "ws://" +
  import.meta.env.VITE_BACKEND_SERVER +
  ":" +
  import.meta.env.VITE_BACKEND_PORT +
  "/api/game";

export const clientData: StompJs.Client = new StompJs.Client({
  brokerURL: brokerURL,
  reconnectDelay: 10000,
  heartbeatIncoming: 4000,
  heartbeatOutgoing: 4000,
  onConnect: function (frame) {
    console.log("Connected: " + frame);
  },
  onStompError: function (frame) {
    console.error("Broker reported error: " + frame.headers["message"]);
    console.error("Additional details: " + frame.body);
  },
  onWebSocketClose: function (evt) {
    console.error("WebSocket connection closed:", evt);
  },
});

export const connect = () => {
  clientData.activate();
};
