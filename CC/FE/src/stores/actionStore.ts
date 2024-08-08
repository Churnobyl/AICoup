import { create } from "zustand";
import { devtools } from "zustand/middleware";
import { immer } from "zustand/middleware/immer";

type State = {
  selectedOption: number;
  selectedTarget: number;
};

type Action = {
  setSelectedOption: (select: number) => void;
  setSelectedTarget: (select: number) => void;
};

const useActionStore = create<State & Action>()(
  devtools(
    immer((set) => ({
      selectedOption: -1,
      selectedTarget: -1,

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
    }))
  )
);

export default useActionStore;
