import * as StompJs from "@stomp/stompjs";
import Cookies from "js-cookie";
import { useCallback } from "react";

const usePublishMessage = (clientData: StompJs.Client) => {
  const publishMessage = useCallback(
    (roomId: number, writer: string, state: string, mainMessage = {}) => {
      clientData.publish({
        destination: "/pub/chat/message",
        body: JSON.stringify({
          roomId,
          writer,
          mainMessage: {
            ...mainMessage,
            cookie: Cookies.get("aiCoup"),
          },
          state,
        }),
      });
    },
    [clientData]
  );

  return publishMessage;
};

export default usePublishMessage;
