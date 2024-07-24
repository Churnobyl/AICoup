import Cookies from "js-cookie";
import { useEffect, useState } from "react";
import { clientData } from "../apis/websocketConnect";

const GamePage = () => {
  const [messages, setMessages] = useState([]);

  useEffect(() => {
    // Connect to WebSocket when the component mounts
    clientData.onConnect = () => {
      console.log("Connected to WebSocket");

      clientData.subscribe("/sub/chat/room/" + 1, (message) => {
        const parsedMessage = JSON.parse(message.body);
        console.log("Received message: ", parsedMessage);

        setMessages((prevMessages) => [...prevMessages, parsedMessage]);

        switch (parsedMessage.state) {
          case "cookieSet":
            Cookies.set("gameId", parsedMessage.roomId, { expires: 1 });
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
    };

    clientData.activate();

    return () => {
      // Disconnect from WebSocket when the component unmounts
      clientData.deactivate();
    };
  }, []);

  return (
    <div>
      <h1>Game Page</h1>
      <ul>
        {messages.map((msg, index) => (
          <li key={index}>{JSON.stringify(msg)}</li>
        ))}
      </ul>
    </div>
  );
};

export default GamePage;
