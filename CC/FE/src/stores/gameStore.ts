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
      setHistory: (historyItem: History[]) => {
        set((state) => {
          state.history = historyItem;
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
