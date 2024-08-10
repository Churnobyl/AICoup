import { create } from "zustand";
import { devtools } from "zustand/middleware";
import { immer } from "zustand/middleware/immer";

type State = {
  isClickable: boolean;
  selectedOption: number;
  selectedTarget: string;
  sendingState: string;
};

type Action = {
  setIsClickable: () => void;
  setSelectedOption: (select: number) => void;
  setSelectedTarget: (select: string) => void;
  setSendingState: (state: string) => void;
};

const useActionStore = create<State & Action>()(
  devtools(
    immer((set) => ({
      isClickable: false,
      selectedOption: -1,
      selectedTarget: "",
      sendingState: "",
      setIsClickable: () => {
        set((state) => {
          state.isClickable = !state.isClickable;
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
    }))
  )
);

export default useActionStore;
