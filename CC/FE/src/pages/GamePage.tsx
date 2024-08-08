/* eslint-disable no-case-declarations */
import { clientData, connect } from "@/apis/websocketConnect";
import ModalComponent from "@/components/modals/ModalComponent";
import HistoryBottomSheet from "@/components/ui/sheets/HistoryBottomSheet";
import { optionKeyByName } from "@/stores/selectOptions";
import History from "@/types/HistoryInf";
import Board from "@components/game/Board";
import useGameStore from "@stores/gameStore";
import Cookies from "js-cookie";
import { useCallback, useEffect, useRef, useState } from "react";
import "./GamePage.scss";
import useActionStore from "@/stores/actionStore";

const convertOption = (opt: string): number => {
  return optionKeyByName[opt] !== undefined ? optionKeyByName[opt] : -1;
};

const shouldHaveTarget = [4, 5, 7]; // 타겟이 필요한 액션

const GamePage = () => {
  const store = useGameStore();
  const storeRef = useRef(store);
  const actionStore = useActionStore();
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [modalContent, setModalContent] = useState("");
  const [options, setOptions] = useState<string[]>([]);

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
    []
  );

  const selectOptions = useCallback(
    (history: History[]) => {
      // 결정 초기화
      actionStore.setSelectedOption(0);
      actionStore.setSelectedTarget(0);

      switch (history[history.length - 1].actionId) {
        case 1:
          break;
        case 2:
          break;
        case 3:
          break;
        case 4:
          break;
        case 5:
          break;
        case 6:
          break;
        case 7:
          break;
        case 8:
          break;
        case 9:
          break;
        case 10:
          break;
        case 11:
          break;
        case 12:
          break;
        case 13:
          break;
        case 14:
          break;
        case 15:
          break;
        case 16:
          break;
        case 17:
          setOptions(["게임 시작"]);
          setModalContent("게임을 시작하겠습니다.");
          setIsModalOpen(true);
          break;
        default:
          break;
      }
    },
    [actionStore]
  );

  const handleMessage = useCallback(
    (message: { body: string }) => {
      const parsedMessage = JSON.parse(message.body);
      console.log("Received message: ", parsedMessage);
      const { mainMessage } = parsedMessage;
      const { members, turn, history, deck, lastContext, canAction } =
        mainMessage;

      switch (parsedMessage.state) {
        case "noGame":
          Cookies.remove("aiCoup");
          publishMessage(1, "userA", "gameInit");
          break;
        case "cookieSet":
          Cookies.set("aiCoup", parsedMessage.mainMessage.message, {
            expires: 1,
          });
          break;
        case "exist":
        case "gameMade":
          publishMessage(1, "userA", "gameState");
          break;
        case "noExist":
          publishMessage(1, "userA", "gameInit");
          break;
        case "gameState":
          storeRef.current.setRoomId(parsedMessage.roomId);
          storeRef.current.setState(parsedMessage.state);
          storeRef.current.setMembers(members);
          storeRef.current.incrementTurn(turn);
          storeRef.current.setHistory(history);
          storeRef.current.setDeck(deck);
          storeRef.current.setLastContext(lastContext);

          selectOptions(history);
          break;
        case "action":
          selectOptions();
          break;
        default:
          break;
      }
    },
    [publishMessage, selectOptions]
  );

  useEffect(() => {
    connect();

    clientData.onConnect = () => {
      console.log("Connected to WebSocket");
      clientData.subscribe("/sub/chat/room/1", handleMessage);
      publishMessage(1, "userA", "gameCheck", {
        cookie: Cookies.get("gameId"),
      });
    };

    return () => {
      clientData.deactivate();
    };
  }, [handleMessage, publishMessage]);

  // 선택 결과
  const handleSelect = (option: string) => {
    console.log("Selected option:", option);

    actionStore.setSelectedOption(convertOption(option));

    // if (shouldHaveTarget.filter((value) => value === actionStore.selectedOption)) {
    //   handleSelectTarget();
    // }

    if (actionStore.selectedOption === 0) {
      publishMessage(1, "userA", "nextTurn", {});
    } else {
      publishMessage(1, "userA", "myChoice", {
        c,
      });
    }

    setIsModalOpen(false);
  };

  return (
    <div className="gamePage" id="gamePage">
      <Board className="board" />
      <HistoryBottomSheet />
      <ModalComponent
        isOpen={isModalOpen}
        onRequestClose={() => setIsModalOpen(true)}
        content={modalContent}
        options={options}
        onSelect={handleSelect}
      />
    </div>
  );
};

export default GamePage;
