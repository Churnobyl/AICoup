import { create } from "zustand";
import { devtools } from "zustand/middleware";
import { immer } from "zustand/middleware/immer";

type State = {
  cardname: string[];
};

const useCardInfoStore = create<State>()(
  devtools(
    immer(() => ({
      cardname: ["", "공작", "사령관", "암살자", "귀부인", "외교관"],
    }))
  )
);

export default useCardInfoStore;
