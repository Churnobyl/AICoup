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
};

const useGameStore = create<ReturnType & Actions>()(
  devtools(
    immer((set) => ({
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
    }))
  )
);

export default useGameStore;
