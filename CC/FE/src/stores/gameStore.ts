import { create } from "zustand";
import { immer } from "zustand/middleware/immer";
import Member from "@/types/MemberInf";
import ReturnType from "@/types/ReturnTypes";
import { devtools } from "zustand/middleware";
import History from "@/types/HistoryInf";

type Actions = {
  setHistory: (historyItem: History[]) => void;
  setMembers: (members: Member[]) => void;
  setMessage: (message: string) => void;
  incrementTurn: (turn: number) => void;
  setRoomId: (roomId: string) => void;
  setState: (states: string) => void;
  setDeck: (deck: number[]) => void;
  setLastContext: (historyItem: History[]) => void;
  getMemberNameById: (id: string) => string | undefined;
};

const useGameStore = create<ReturnType & Actions>()(
  devtools(
    immer((set, get) => ({
      history: [],
      members: [],
      message: "",
      turn: 0,
      roomId: "",
      state: "",
      deck: [],
      lastContext: [],

      // 수정
      setHistory: (historyItem: History[]) => {
        set((state) => {
          state.history = historyItem;
          // // 추가
          // // 최신 히스토리 항목 처리
          // const latestHistory = historyItem[historyItem.length - 1];
          // if (latestHistory) {
          //   const {
          //     playerTrying,
          //     playerTried,
          //     actionId,
          //     // History 필드에 gptLine gpt대사가 있다는 가정
          //     gptLine,
          //   } = latestHistory;

          //   // 플레이어 이름 가져오기
          //   const tryingPlayerName =
          //     state.members[playerTrying]?.name || `플레이어 ${playerTrying}`;
          //   const triedPlayerName =
          //     playerTried !== undefined
          //       ? state.members[playerTried]?.name || `플레이어 ${playerTried}`
          //       : null;

          //   // actionId에 따라 액션 설명을 정의 (playerTried가 있는 경우와 없는 경우)
          //   // TODO: actionId에 대응되는 실제 텍스트 추가하기
          //   const actionDescription = triedPlayerName
          //     ? `${tryingPlayerName}가 ${triedPlayerName}을(를) ${actionId}합니다.`
          //     : `${tryingPlayerName}가 ${actionId}를 했습니다.`;

          //   // 메시지 내용 정의 (gptLine이 있는 경우 추가)
          //   const messageContent = gptLine
          //     ? `${gptLine}\n\n${actionDescription}`
          //     : actionDescription;

          //   // 이전 메시지를 초기화
          //   state.members.forEach((member) => {
          //     member.message = ""; // 모든 플레이어의 메시지를 초기화
          //   });

          //   // playerTrying에 해당하는 멤버의 메시지 상태 업데이트
          //   if (state.members[playerTrying]) {
          //     state.members[playerTrying].message = messageContent;
          //   }
          // }
        });
      },

      setMembers: (members: Member[]) => {
        set((state) => {
          state.members = members;
        });
      },
      setMessage: (message: string) => {
        set((state) => {
          state.message = message;
        });
      },
      incrementTurn: (turn: number) => {
        set((state) => {
          state.turn = turn;
        });
      },
      setRoomId: (roomId: string) => {
        set((state) => {
          state.roomId = roomId;
        });
      },
      setState: (states: string) => {
        set((state) => {
          state.state = states;
        });
      },
      setDeck: (deck: number[]) => {
        set((state) => {
          state.deck = deck;
        });
      },
      setLastContext: (historyItem: History[]) => {
        set((state) => {
          state.lastContext = historyItem;
        });
      },
      getMemberNameById: (id: string | undefined) => {
        if (id === undefined) return "";

        const members = get().members;
        const member = members.find((m) => m.id === id);
        return member ? member.name : undefined;
      },
    }))
  )
);

export default useGameStore;
