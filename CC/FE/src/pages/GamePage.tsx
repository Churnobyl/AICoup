/* eslint-disable no-case-declarations */
import { clientData, connect } from "@/apis/websocketConnect";
import ModalComponent from "@/components/modals/ModalComponent";
import HistoryBottomSheet from "@/components/ui/sheets/HistoryBottomSheet";
import useActionStore from "@/stores/actionStore";
import { ActionType } from "@/types/ActionType";
import Board from "@components/game/Board";
import useGameStore from "@stores/gameStore";
import Cookies from "js-cookie";
import { useCallback, useEffect, useRef, useState } from "react";
import "./GamePage.scss";
import usePublishMessage from "@/hooks/usePublishMessage";

const shouldHaveTarget = [4, 5, 7]; // 타겟이 필요한 액션

const GamePage = () => {
  /**
   * Store 관련
   */
  const store = useGameStore(); // 게임 정보 Zustand Store
  const storeRef = useRef(store); // 리랜더링 방지 위해 Ref로 감싸서 사용
  const actionStore = useActionStore(); // Action 관련 정보 Zustand Store

  /**
   * 모달 관련
   */
  const [isModalOpen, setIsModalOpen] = useState(false); // 선택지 모달 ON/OFF 관리
  const [modalContent, setModalContent] = useState(""); // 모달에 들어갈 설명
  const [options, setOptions] = useState<ActionType>({}); // 선택지

  /**
   * 선택 관련
   */
  const [isClickable, setIsClickable] = useState(false);

  /**
   * 커스텀 훅
   */
  const publishMessage = usePublishMessage(clientData); // 서버로 메세지 전송

  const selectOptions = useCallback(
    (canAction: ActionType | -1 | -2) => {
      // 결정 초기화
      actionStore.setSelectedOption(0);
      actionStore.setSelectedTarget(0);

      if (canAction === -1) {
        setOptions({ "게임 시작": 0 });
        setModalContent("게임을 시작하겠습니다.");
        setIsModalOpen(true);
      } else if (canAction === -2) {
        setOptions({ "다음 턴": -2 });
        setModalContent("다음 턴으로");
        setIsModalOpen(true);
      } else {
        setOptions(canAction);
        setModalContent("행동을 선택해주세요.");
        setIsModalOpen(true);
      }
    },
    [actionStore]
  );

  const handleMessage = useCallback(
    (message: { body: string }) => {
      const parsedMessage = JSON.parse(message.body);
      console.log("Received message: ", parsedMessage);
      const { mainMessage } = parsedMessage;

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
          const { members, turn, history, deck } = mainMessage;

          storeRef.current.setRoomId(parsedMessage.roomId);
          storeRef.current.setState(parsedMessage.state);
          storeRef.current.setMembers(members);
          storeRef.current.incrementTurn(turn);
          storeRef.current.setHistory(history);
          storeRef.current.setDeck(deck);

          if (turn === 0) {
            selectOptions(-1);
          } else {
            selectOptions(-2);
          }

          break;
        case "action":
          actionStore.setSendingState("action");
          selectOptions(mainMessage.canAction);
          break;
        case "actionPending":
          publishMessage(1, "userA", "anyChallenge");
          break;
        case "endGame": // 한턴 끝남 평가 메시지 날려줘
          publishMessage(1, "userA", "performGame");
          break;
        case "gptChallenge":
          // GPT가 뭐 챌린지함
          // gptChallengeSuccess or gptChallengeFail
          break;
        case "gptChallengeNone":
          publishMessage(1, "userA", "anyCounterAction");
          break;
        case "gptCounterAction":
          selectOptions({
            도전: 8,
            허용: 9,
          });
          // 허용 -> counterActionPermit
          // 반박 -> counterActionChallenge

          break;
        case "counterActionChallengeSuccess":
          break;
        case "counterActionChallengeFail":
          break;
        case "gptAction":
          const his = mainMessage.history;
          const gptActionId = his[his.length - 1].actionId;

          switch (gptActionId) {
            case 1: // income
              publishMessage(1, "userA", "performGame");
              break;
            case 2: // foreign aid
              setOptions({ 허용: 9, "공작으로 방해": 10 });
              setModalContent("행동을 선택해주세요.");
              setIsModalOpen(true);
              break;
            case 3: // tax
              setOptions({ 허용: 9, 의심: 8, "공작으로 방해": 10 });
              setModalContent("행동을 선택해주세요.");
              setIsModalOpen(true);
              break;
            case 4: // steal
              setOptions({
                허용: 9,
                의심: 8,
                "사령관으로 방해": 11,
                "외교관으로 방해": 12,
              });
              setModalContent("행동을 선택해주세요.");
              setIsModalOpen(true);
              break;
            case 5: // assassinate
              setOptions({
                허용: 9,
                의심: 8,
                "귀부인으로 방해": 13,
              });
              setModalContent("행동을 선택해주세요.");
              setIsModalOpen(true);
              break;
            case 6: // exchange
              setOptions({
                허용: 9,
                의심: 8,
              });
              setModalContent("행동을 선택해주세요.");
              setIsModalOpen(true);
              break;
            case 7: // coup
              publishMessage(1, "userA", "performGame");
              break;
          }
          break;

        default:
          break;
      }
    },
    [actionStore, publishMessage, selectOptions]
  );

  useEffect(() => {
    connect(); // 웹소켓 연결

    // 연결됐을 때
    clientData.onConnect = () => {
      console.log("Connected to WebSocket"); // 디버깅 메세지
      clientData.subscribe("/sub/chat/room/1", handleMessage); // room1 subscribe
      publishMessage(1, "userA", "gameCheck", {
        // 게임 상태 체크 "gameCheck"
        cookie: Cookies.get("gameId"), // 쿠키 정보 담아서 보냄
      });
    };

    return () => {}; // 웹소켓 disconnect없이 지속
  }, [handleMessage, publishMessage]);

  // 선택 결과
  const handleSelect = async (option: number) => {
    console.log("Selected option:", option);

    actionStore.setSelectedOption(option);

    // if (shouldHaveTarget.filter((value) => value === option)) {
    //   await handleSelectTarget();
    // }

    const currentState = store.state;

    switch (option) {
      case 0: // 게임 시작
        publishMessage(1, "userA", "nextTurn", {});
        break;
      case -2: // gameState시 다음 턴
        publishMessage(1, "userA", "nextTurn", {});
        break;
      case 9:
        if (currentState === "gptCounterAction") {
          publishMessage(1, "userA", "permit", {});
        }

        break;
      default:
        publishMessage(1, "userA", actionStore.sendingState, {
          cookie: Cookies.get("gameId"),
          action: option.toString(),
          targetPlayerId: actionStore.selectedTarget.toString(),
        });
    }

    setIsModalOpen(false);
  };

  const handleSelectTarget = () => {
    console.log("handleSelectTarget");
    setIsClickable(true);
  };

  const handleClick = (playerNumber: number) => {
    actionStore.setSelectedTarget(playerNumber);
    console.log(actionStore.selectedTarget);
  };

  return (
    <div className="gamePage" id="gamePage">
      <Board
        className="board"
        isClickable={isClickable}
        // onPlayerClick={handleClick}
      />
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
