import { create } from "zustand";
import { devtools } from "zustand/middleware";
import { immer } from "zustand/middleware/immer";

type historyMessage = {
  incomeMessage: (a: string, b: string) => string;
};

type Action = {
  historySet: object;
};

const useHistoryStore = create<Action>()(
  devtools(
    immer(() => )
  )
);

export default useHistoryStore;
