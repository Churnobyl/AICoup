import { create } from "zustand";
import { devtools } from "zustand/middleware";
import { immer } from "zustand/middleware/immer";

type State = {
  isClickable: boolean;
  selectedOption: number;
  selectedTarget: string;
  sendingState: string;
  isPlayerCardClickable: boolean;
  selectedPlayerCard: number;
};

type Action = {
  setIsClickable: (select: boolean) => void;
  setSelectedOption: (select: number) => void;
  setSelectedTarget: (select: string) => void;
  setSendingState: (state: string) => void;
  setIsPlayerCardClickable: () => void;
  setSelectedPlayerCard: (select: number) => void;
};

const useActionStore = create<State & Action>()(
  devtools(
    immer((set) => ({
      isClickable: false,
      selectedOption: -1,
      selectedTarget: "",
      sendingState: "",
      isPlayerCardClickable: false,
      selectedPlayerCard: -1,
      setIsClickable: (select) => {
        set((state) => {
          state.isClickable = select;
        });
      },
      setSelectedOption: (select) => {
        set((state) => {
          state.selectedOption = select;
        });
      },
      setSelectedTarget: (select) => {
        set((state) => {
          state.selectedTarget = select;
        });
      },
      setSendingState: (a) => {
        set((state) => {
          state.sendingState = a;
        });
      },
      setIsPlayerCardClickable: () => {
        set((state) => {
          state.isPlayerCardClickable = !state.isPlayerCardClickable;
        });
      },
      setSelectedPlayerCard: (select) => {
        set((state) => {
          state.selectedPlayerCard = select;
        });
      },
    }))
  )
);

export default useActionStore;
