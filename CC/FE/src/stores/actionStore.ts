import { create } from "zustand";
import { devtools } from "zustand/middleware";
import { immer } from "zustand/middleware/immer";

type State = {
  selectedOption: number;
  selectedTarget: number;
  sendingState: string;
};

type Action = {
  setSelectedOption: (select: number) => void;
  setSelectedTarget: (select: number) => void;
  setSendingState: (state: string) => void;
};

const useActionStore = create<State & Action>()(
  devtools(
    immer((set) => ({
      selectedOption: -1,
      selectedTarget: -1,
      sendingState: "",

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
