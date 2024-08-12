import { create } from "zustand";
import { devtools } from "zustand/middleware";
import { immer } from "zustand/middleware/immer";

type State = {
  isPending: boolean;
};

type Action = {
  setIsPending: (s: boolean) => void;
};

const useMessagePendingStore = create<State & Action>()(
  devtools(
    immer((set) => ({
      isPending: false,
      setIsPending: (s) => {
        set((state) => {
          state.isPending = s;
        });
      },
    }))
  )
);

export default useMessagePendingStore;
