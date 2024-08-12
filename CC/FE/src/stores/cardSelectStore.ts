import { create } from "zustand";
import { devtools } from "zustand/middleware";
import { immer } from "zustand/middleware/immer";

type State = {
  isPlayerCardClickable: boolean;
  selectedPlayerCard: number;
};

type Action = {
  setIsPlayerCardClickable: () => void;
  setSelectedPlayerCard: (select: number) => void;
};

const useCardSelectStore = create<State & Action>()(
  devtools(
    immer((set) => ({
      isPlayerCardClickable: false,
      selectedPlayerCard: -1,
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

export default useCardSelectStore;
