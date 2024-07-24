import * as StompJs from "@stomp/stompjs";
import Cookies from "js-cookie";

const brokerURL =
  "ws://" +
  import.meta.env.VITE_BACKEND_SERVER +
  ":" +
  import.meta.env.VITE_BACKEND_PORT +
  "/game";

export const clientData: StompJs.Client = new StompJs.Client({
  brokerURL: brokerURL,
  reconnectDelay: 10000,
  onConnect: function (frame) {
    console.log("Connected: " + frame);
    clientData.subscribe("/sub/chat/room/" + 1, function (message) {
      console.log("Received message: ", JSON.parse(message.body));

      const a = JSON.parse(message.body);

      switch (a.state) {
        case "cookieSet":
          Cookies.set("gameId", a.roomId, { expires: 1 });
          break;

        default:
          break;
      }
    });

    clientData.publish({
      destination: "/pub/chat/message",
      body: JSON.stringify({
        roomId: 1,
        writer: "userA",
        message: "Hello World",
        state: "gameInit",
      }),
    });
  },
  onStompError: function (frame) {
    console.error("Broker reported error: " + frame.headers["message"]);
    console.error("Additional details: " + frame.body);
  },
  onWebSocketClose: function (evt) {
    console.error("WebSocket connection closed:", evt);
  },
});

clientData.activate();
