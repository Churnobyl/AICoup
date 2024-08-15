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
import useCardSelectStore from "@/stores/cardSelectStore";
import SignBoard from "@/components/ui/signBoard/SignBoard";
import useMessagePendingStore from "@/stores/messagePendingStore";
import { useNavigate } from "react-router-dom";

const shouldHaveTarget = [4, 5, 7]; // 타겟이 필요한 액션

const GamePage = () => {
  /**
   * 연결 여부 확인
   */
  const [isConnected, setIsConnected] = useState(false);

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
  const cardSelectStore = useCardSelectStore(); // 카드 선택 Zustand Store
  const messagePendingStore = useMessagePendingStore(); // 메시지 팬딩 Zustand Store

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

  /**
   * 처리 중 다른 처리 금지
   */
  const [isProcessing, setIsProcessing] = useState<boolean>(false);

  /**
   * mouseClick topBar
   */
  const [isTopBarShow, setIsTopBarShow] = useState<boolean>(false);
  const [topBarText, setTopBarText] = useState<string>("");

  const navigate = useNavigate();

  const selectOptions = useCallback((canAction: ActionType | -1 | -2 | -3) => {
    // 결정 초기화

    if (canAction === -1) {
      setOptions({ "게임 시작": 0 });
      setModalContent("게임을 시작하겠습니다.");
      setIsModalOpen(true);
    } else if (canAction === -2) {
      setOptions({ "다음 턴": -2 });
      setModalContent("다음 턴으로");
      setIsModalOpen(true);
    } else if (canAction === -3) {
      setOptions({ 종료: -3 });
      setIsModalOpen(true);
    } else {
      setOptions(canAction);
      setModalContent("행동을 선택해주세요.");
      setIsModalOpen(true);
    }
  }, []);

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
          if (
            mainMessage.history[mainMessage.history.length - 1].playerTried ===
            "1"
          ) {
            cardSelectStore.setIsPlayerCardClickable();
            setTopBarText("공개할 카드를 선택해 주세요.");
            setIsTopBarShow(true);
            cardSelectStore.setSelectedPlayerCard(-1);
          }
          break;
        case "challengeSuccess":
          setupGameState(parsedMessage);
          setSpinnerText("의심이 성공했습니다. 결과 반영중..");
          setIsSpinnerOpen(true);
          break;
        case "challengeFail":
          setupGameState(parsedMessage);
          // if (
          //   mainMessage.history[mainMessage.history.length - 1].playerTrying ===
          //   "1"
          // ) {
          //   cardSelectStore.setIsPlayerCardClickable();
          //   setTopBarText("의심이 실패했습니다. 공개할 카드를 선택해 주세요.");
          //   setIsTopBarShow(true);
          //   cardSelectStore.setSelectedPlayerCard(-1);
          // }
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
          setSpinnerText("의심이 성공했습니다. 결과 반영중..");
          setIsSpinnerOpen(true);
          break;
        case "counterActionChallengeFail":
          setupGameState(parsedMessage);
          setSpinnerText("의심이 실패했습니다. 결과 반영중..");
          setIsSpinnerOpen(true);
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
              setOptions({ 허용: 9, 의심: 8 });
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
          setupGameState(parsedMessage);
          publishMessage(1, "userA", "performChallenge");
          break;
        case "playerCardOpen":
          setupGameState(parsedMessage);
          
          const { history, members, cardOpen } = parsedMessage.mainMessage;
          const lastHistory = history[history.length - 1];
          const playerTried = lastHistory.playerTried;
          
          if (playerTried !== '1') {
            const memberIndex = store.members.findIndex(m => m.id === playerTried);
            
            if (memberIndex !== -1) {
              const cardKey = cardOpen === 0 ? 'leftCard' : 'rightCard';
              const newCardValue = members[memberIndex][cardKey];
              
              // 카드 값 변경 및 isRevealed 설정
              store.updateMemberCard(playerTried, cardKey, newCardValue, true);
            }
          }
          
          cardSelectStore.setIsPlayerCardClickable();
          setTopBarText("도전이 실패했습니다. 공개할 카드를 선택해 주세요.");
          setIsTopBarShow(true);
          cardSelectStore.setSelectedPlayerCard(-1);
          break;
        case "deadCardOpen":
          setupGameState(parsedMessage);
          if (
            mainMessage.history[mainMessage.history.length - 1].playerTried ===
            "1"
          ) {
            cardSelectStore.setIsPlayerCardClickable();
            setTopBarText("공격을 당했습니다. 공개할 카드를 선택해 주세요.");
            setIsTopBarShow(true);
            cardSelectStore.setSelectedPlayerCard(-1);
          }
          break;
        case "playerDown":
          setupGameState(parsedMessage);
          setModalContent(`플레이어가 패배했습니다.`);
          selectOptions(-3);
          setIsModalOpen(true);
          break;
        case "gameOver":
          setupGameState(parsedMessage);
          const hi = mainMessage.history;
          setModalContent(
            `${store.getMemberNameById(
              hi[hi.length - 1].playerTrying
            )}님이 승리했습니다.`
          );
          selectOptions(-3);
          setIsModalOpen(true);
          break;
        default:
          break;
      }

      if (messagePendingStore.isPending) {
        messagePendingStore.setIsPending(false);
      }
    },
    [
      cardSelectStore,
      isSpinnerOpen,
      messagePendingStore,
      publishMessage,
      selectOptions,
      setupGameState,
      store,
    ]
  );

  /**
   * 메시지 큐 처리
   */
  const processMessageQueue = useCallback(() => {
    // 지연시간 설정
    const DELAY_TIME = 1000;

    if (isProcessing || messageQueue.current.length === 0) return;

    if (messagePendingStore.isPending) return; // 처리중이면 메시지큐 처리 중단

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
  }, [handleMessage, isProcessing, messagePendingStore.isPending]);

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
        cookie: Cookies.get("aiCoup"), // 쿠키 정보 담아서 보냄
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

  /**
   * 타겟 설정 메서드
   */
  const handleDirectSelect = useCallback(
    (option: number) => {
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
          if (store.state !== "gameOver") {
            setSpinnerText("보드의 일치 여부를 AIoT에게 묻는중..");
            setIsSpinnerOpen(true);
            publishMessage(1, "userA", "nextTurn", {});
          } else {
            Cookies.remove("aiCoup");
            setTimeout(() => {}, 2000);
            navigate("/", { replace: true });
          }
          break;
        case -3:
          Cookies.remove("aiCoup");
          setTimeout(() => {}, 2000);
          navigate("/", { replace: true });
          break;
        case 1:
        case 2:
        case 3:
        case 4:
        case 5:
        case 6:
        case 7:
          publishMessage(1, "userA", "action", {
            cookie: Cookies.get("aiCoup"),
            action: option.toString(),
            targetPlayerId: actionStore.selectedTarget.toString(),
          });
          break;
        case 8:
          console.log(currentState);
          if (currentState === "gptCounterAction") {
            publishMessage(1, "userA", "counterActionChallenge", {});
          } else if (currentState === "gptAction") {
            publishMessage(1, "userA", "challenge", {});
          }
          break;
        case 9:
          console.log(currentState);
          if (currentState === "gptCounterAction") {
            publishMessage(1, "userA", "counterActionPermit", {});
          } else if (currentState === "gptAction") {
            publishMessage(1, "userA", "permit", {});
          }
          break;
        case 10:
        case 11:
        case 12:
        case 13:
          publishMessage(1, "userA", "counterAction", {
            cookie: Cookies.get("aiCoup"),
            action: option.toString(),
            targetPlayerId: actionStore.selectedTarget.toString(),
          });
          break;
      }

      setIsModalOpen(false);
    },
    [actionStore.selectedTarget, navigate, publishMessage, store.state]
  );

  // 선택 결과 보내기
  const handleSelect = useCallback(
    (option: number) => {
      console.log("Selected option:", option);

      actionStore.setSelectedOption(option);

      if (shouldHaveTarget.includes(option)) {
        actionStore.setIsClickable();
        actionStore.setSelectedTarget("");

        setTopBarText("상대를 선택해 주세요.");
        setIsTopBarShow(true);

        setIsModalOpen(false);
      } else {
        handleDirectSelect(option);
      }
    },
    [actionStore, handleDirectSelect]
  );

  const handleSelectWithTarget = useCallback(() => {
    if (0 < actionStore.selectedOption && actionStore.selectedOption < 8) {
      setSpinnerText("서버의 응답을 기다리는 중..");
      setIsSpinnerOpen(true);
      publishMessage(1, "userA", "action", {
        cookie: Cookies.get("aiCoup"),
        action: actionStore.selectedOption.toString(),
        targetPlayerId: actionStore.selectedTarget.toString(),
      });
    }

    setIsModalOpen(false);
  }, [publishMessage, actionStore]);

  const handleSelectWithMyCard = useCallback(() => {
    setSpinnerText("서버의 응답을 기다리는 중..");
    setIsSpinnerOpen(true);
    console.log("handleSelectWithMyCard : ", store.state);
    if (store.state === "gptChallenge") {
      publishMessage(1, "userA", "cardOpen", {
        cookie: Cookies.get("aiCoup"),
        cardOpen: cardSelectStore.selectedPlayerCard.toString(),
      });
    } else if (store.state === "playerCardOpen") {
      publishMessage(1, "userA", "playerCardOpenResult", {
        cookie: Cookies.get("aiCoup"),
        cardOpen: cardSelectStore.selectedPlayerCard.toString(),
      });
    } else if (store.state === "deadCardOpen") {
      publishMessage(1, "userA", "deadCardOpen", {
        cookie: Cookies.get("aiCoup"),
        cardOpen: cardSelectStore.selectedPlayerCard.toString(),
      });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [publishMessage, cardSelectStore.selectedPlayerCard]);

  // 상대편 찍어야 할 때
  useEffect(() => {
    if (!actionStore.isClickable && actionStore.selectedTarget) {
      actionStore.setSelectedTarget(actionStore.selectedTarget);
      handleSelectWithTarget();
      setIsTopBarShow(false); // topbar 안 보이게
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
      cardSelectStore.selectedPlayerCard === 0 ||
      cardSelectStore.selectedPlayerCard === 1
    ) {
      cardSelectStore.setSelectedPlayerCard(cardSelectStore.selectedPlayerCard);
      handleSelectWithMyCard();
      setIsTopBarShow(false); // topbar 안 보이게
    }
  }, [
    cardSelectStore,
    cardSelectStore.selectedPlayerCard,
    handleSelectWithMyCard,
    isConnected,
  ]);

  return (
    <div className="gamePage" id="gamePage">
      {isTopBarShow ? (
        <div className="top-bar">
          <SignBoard title={topBarText} />
        </div>
      ) : (
        ""
      )}

      <div>
        <Board className="board" />
        <ModalComponent
          isOpen={isModalOpen}
          onRequestClose={() => setIsModalOpen(true)}
          content={modalContent}
          options={options}
          onSelect={handleSelect}
        />
        <Spinner isLoading={isSpinnerOpen} text={spinnerText} />
      </div>

      <HistoryBottomSheet />
    </div>
  );
};

export default GamePage;
