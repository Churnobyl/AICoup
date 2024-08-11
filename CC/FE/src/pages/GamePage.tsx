/* eslint-disable no-case-declarations */
import { clientData, connect } from "@/apis/websocketConnect";
import ModalComponent from "@/components/modals/ModalComponent";
import HistoryBottomSheet from "@/components/ui/sheets/HistoryBottomSheet";
import Spinner from "@/components/ui/spinner/Spinner";
import usePublishMessage from "@/hooks/usePublishMessage";
import useActionStore from "@/stores/actionStore";
import useHistoryStore from "@/stores/historyMessageStore";
import { ActionType } from "@/types/ActionType";
import History from "@/types/HistoryInf";
import Board from "@components/game/Board";
import { IMessage } from "@stomp/stompjs";
import useGameStore from "@stores/gameStore";
import Cookies from "js-cookie";
import { useCallback, useEffect, useRef, useState } from "react";
import "./GamePage.scss";

const shouldHaveTarget = [4, 5, 7]; // 타겟이 필요한 액션

const GamePage = () => {
  /**
   * 연결 여부 확인
   */
  const [isConnected, setIsConnected] = useState(false);
  const [isMessageSending, setIsMessageSending] = useState(false);

  /**
   * Spinner State
   */
  const [isSpinnerOpen, setIsSpinnerOpen] = useState(false);
  const [spinnerText, setSpinnerText] = useState("");

  /**
   * Store 관련
   */
  const store = useGameStore(); // 게임 정보 Zustand Store
  const storeRef = useRef(store); // 리랜더링 방지 위해 Ref로 감싸서 사용
  const actionStore = useActionStore(); // Action 관련 정보 Zustand Store
  const historyStore = useHistoryStore(); // 히스토리 Zustand Store

  /**
   * 모달 관련
   */
  const [isModalOpen, setIsModalOpen] = useState(false); // 선택지 모달 ON/OFF 관리
  const [modalContent, setModalContent] = useState(""); // 모달에 들어갈 설명
  const [options, setOptions] = useState<ActionType>({}); // 선택지

  /**
   * 커스텀 훅
   */
  const publishMessage = usePublishMessage(clientData); // 서버로 메세지 전송

  /**
   * 메시지 처리
   */
  const messageQueue = useRef<{ message: IMessage; receivedTime: number }[]>(
    []
  );
  const [isProcessing, setIsProcessing] = useState<boolean>(false);

  const selectOptions = useCallback(
    (canAction: ActionType | -1 | -2) => {
      // 결정 초기화

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

  const updateHistory = useCallback(
    (newHistory: History[], bfHistory: History[]) => {
      const newHistoryItems = newHistory.filter(
        (newItem: History) =>
          !bfHistory.some((oldItem) => oldItem.id === newItem.id)
      );

      newHistoryItems.forEach((item: History) =>
        historyStore.addMessage(
          item.actionId,
          store.getMemberNameById(item.playerTrying) || "",
          store.getMemberNameById(item.playerTried),
          item.actionState
        )
      );
    },
    [historyStore, store]
  );

  const setupGameState = useCallback(
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    (parsedMessage: any) => {
      const mainMessage = parsedMessage.mainMessage;
      const { members, turn, history, deck } = mainMessage;

      storeRef.current.setRoomId(parsedMessage.roomId);
      storeRef.current.setState(parsedMessage.state);
      storeRef.current.setMembers(members);
      storeRef.current.incrementTurn(turn);

      const bfHistory = [...store.history];
      storeRef.current.setHistory(history);

      updateHistory(history, bfHistory); // Call the history update method

      storeRef.current.setDeck(deck);
    },
    [store.history, updateHistory]
  );

  /**
   * Sub에서 온 메시지 처리 메서드
   */
  const handleMessage = useCallback(
    (message: IMessage | undefined) => {
      // 메시지 undefined면 취소
      if (message == undefined) return;

      const parsedMessage = JSON.parse(message.body);
      console.log("Received message: ", parsedMessage);
      const { mainMessage } = parsedMessage;

      if (isSpinnerOpen) {
        setIsSpinnerOpen(false);
      }

      switch (parsedMessage.state) {
        case "noGame":
          Cookies.remove("aiCoup");
          publishMessage(1, "userA", "gameInit");
          break;
        case "cookieSet":
          setSpinnerText("새 게임 정보를 저장하는 중..");
          setIsSpinnerOpen(true);
          Cookies.set("aiCoup", parsedMessage.mainMessage.message, {
            expires: 1,
          });
          break;
        case "exist":
          setSpinnerText("기존 게임을 불러오는 중..");
          setIsSpinnerOpen(true);
          publishMessage(1, "userA", "gameState");
          break;
        case "gameMade":
          setSpinnerText("새 게임을 불러오는 중..");
          setIsSpinnerOpen(true);
          publishMessage(1, "userA", "gameState");
          break;
        case "noExist":
          setSpinnerText("새 게임을 만드는 중..");
          setIsSpinnerOpen(true);
          publishMessage(1, "userA", "gameInit");
          break;
        case "gameState":
          if (isSpinnerOpen) {
            setIsSpinnerOpen(false);
          }
          setupGameState(parsedMessage);

          if (parsedMessage.mainMessage.turn === 0) {
            selectOptions(-1);
          } else {
            selectOptions(-2);
          }

          break;
        case "action":
          setupGameState(parsedMessage);
          actionStore.setSendingState("action");
          selectOptions({
            수입: 1,
            해외원조: 2,
            징세: 3,
            강탈: 4,
            암살: 5,
            교환: 6,
            쿠: 7,
          });
          break;
        case "actionPending":
          setupGameState(parsedMessage);
          setSpinnerText("GPT의 도전 여부를 기다리는 중..");
          setIsSpinnerOpen(true);
          publishMessage(1, "userA", "anyChallenge");
          break;
        case "endGame": // 한턴 끝남 평가 메시지 날려줘
          setupGameState(parsedMessage);
          setSpinnerText("서버의 평가를 기다리는 중..");
          setIsSpinnerOpen(true);
          publishMessage(1, "userA", "performGame");
          break;
        case "gptChallenge":
          setupGameState(parsedMessage);
          actionStore.setIsPlayerCardClickable();
          actionStore.setSelectedPlayerCard(-1);
          break;
        case "gptChallengeNone":
          setupGameState(parsedMessage);
          setSpinnerText("GPT의 대응 여부를 기다리는 중..");
          setIsSpinnerOpen(true);
          publishMessage(1, "userA", "anyCounterAction");
          break;
        case "gptCounterAction":
          setupGameState(parsedMessage);
          selectOptions({
            도전: 8,
            허용: 9,
          });
          // 허용 -> counterActionPermit
          // 반박 -> counterActionChallenge

          break;
        case "counterActionChallengeSuccess":
          setupGameState(parsedMessage);
          break;
        case "counterActionChallengeFail":
          setupGameState(parsedMessage);
          break;
        case "gptAction":
          setupGameState(parsedMessage);
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
        case "cardOpen": // GPT의 카드 오픈
          publishMessage(1, "userA", "performChallenge");
          break;

        default:
          break;
      }
    },
    [actionStore, isSpinnerOpen, publishMessage, selectOptions, setupGameState]
  );

  /**
   * 메시지 큐 처리
   */
  const processMessageQueue = useCallback(() => {
    // 지연시간 설정
    const DELAY_TIME = 3000;

    if (isProcessing || messageQueue.current.length === 0) return;

    const { message, receivedTime } = messageQueue.current[0]; // 큐에서 첫 번째 메시지와 시간 가져오기
    const currentTime = Date.now();
    const timeDiff = currentTime - receivedTime;

    setIsProcessing(true); // 메시지 처리 중 상태 설정

    if (timeDiff >= DELAY_TIME) {
      // 1초가 지났으면 처리
      messageQueue.current.shift(); // 메시지 큐에서 제거
      handleMessage(message); // 메시지 처리
      setIsProcessing(false); // 처리 후 상태 해제
    } else {
      // 1초가 안 지났으면 남은 시간만큼 대기 후 처리
      setTimeout(() => {
        messageQueue.current.shift(); // 메시지 큐에서 제거
        handleMessage(message); // 메시지 처리
        setIsProcessing(false); // 처리 후 상태 해제
      }, DELAY_TIME - timeDiff);
    }
  }, [handleMessage, isProcessing]);

  useEffect(() => {
    connect(); // 웹소켓 연결

    // 연결됐을 때
    clientData.onConnect = () => {
      console.log("Connected to WebSocket"); // 디버깅 메세지
      setIsConnected(true);
      clientData.subscribe("/sub/chat/room/1", (message) => {
        messageQueue.current.push({
          message,
          receivedTime: Date.now(),
        });
      }); // room1 subscribe

      setSpinnerText("게임이 있는지 확인하는 중..");
      setIsSpinnerOpen(true);
      publishMessage(1, "userA", "gameCheck", {
        // 게임 상태 체크 "gameCheck"
        cookie: Cookies.get("gameId"), // 쿠키 정보 담아서 보냄
      });
    };

    clientData.onDisconnect = () => {
      console.warn("Disconnected from WebSocket");
      setIsConnected(false);
    };

    const intervalId = setInterval(() => {
      processMessageQueue(); // 0.1초마다 큐에 메시지 있는지 확인
    }, 100);

    // 웹소켓 disconnect없이 지속
    // 컴포넌트 언마운트할 때 인터벌 정리
    return () => {
      clearInterval(intervalId);
      setIsConnected(false);
    };
  }, [handleMessage, publishMessage, processMessageQueue]);

  // 선택 결과 보내기
  const handleSelect = (option: number) => {
    console.log("Selected option:", option);

    actionStore.setSelectedOption(option);

    if (shouldHaveTarget.includes(option)) {
      actionStore.setIsClickable();
      actionStore.setSelectedTarget("");
      setIsModalOpen(false);
      return;
    }

    handleDirectSelect(option);
  };

  /**
   * 타겟 설정 메서드
   */
  const handleDirectSelect = (option: number) => {
    const currentState = store.state;
    console.log("currentState : ", store.state);

    setSpinnerText("서버의 응답을 기다리는 중..");
    setIsSpinnerOpen(true);

    switch (option) {
      case 0:
        setSpinnerText("보드의 일치 여부를 AIoT에게 묻는중..");
        setIsSpinnerOpen(true);
        publishMessage(1, "userA", "nextTurn", {});
        break;
      case -2:
        setSpinnerText("보드의 일치 여부를 AIoT에게 묻는중..");
        setIsSpinnerOpen(true);
        publishMessage(1, "userA", "nextTurn", {});
        break;
      case 8:
        if (currentState === "gptCounterAction") {
          publishMessage(1, "userA", "counterActionChallenge", {});
        } else if (currentState === "gptAction") {
          publishMessage(1, "userA", "challenge", {});
        }
        break;
      case 9:
        if (currentState === "gptCounterAction") {
          publishMessage(1, "userA", "counterActionPermit", {});
        } else if (currentState === "gptAction") {
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

  const handleSelectWithTarget = useCallback(() => {
    publishMessage(1, "userA", actionStore.sendingState, {
      cookie: Cookies.get("gameId"),
      action: actionStore.selectedOption.toString(),
      targetPlayerId: actionStore.selectedTarget.toString(),
    });

    setIsModalOpen(false);
  }, [publishMessage, actionStore]);

  const handleSelectWithMyCard = useCallback(() => {
    publishMessage(1, "userA", "cardOpen", {
      cookie: Cookies.get("gameId"),
      cardOpen: actionStore.selectedPlayerCard.toString(),
    });
  }, [publishMessage, actionStore]);

  // 상대편 찍어야 할 때
  useEffect(() => {
    if (!actionStore.isClickable && actionStore.selectedTarget) {
      // actionStore.setSelectedTarget(actionStore.selectedTarget);
      handleSelectWithTarget();
    }
  }, [
    actionStore,
    actionStore.isClickable,
    actionStore.selectedTarget,
    handleSelectWithTarget,
  ]);

  // 내 카드 찍어야 할 때
  useEffect(() => {
    if (
      actionStore.selectedPlayerCard === 0 ||
      actionStore.selectedPlayerCard === 1
    ) {
      // actionStore.setSelectedPlayerCard(actionStore.selectedPlayerCard);
      handleSelectWithMyCard();
    }
  }, [actionStore.selectedPlayerCard, handleSelectWithMyCard, isConnected]);

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
      <Spinner isLoading={isSpinnerOpen} text={spinnerText} />
    </div>
  );
};

export default GamePage;
