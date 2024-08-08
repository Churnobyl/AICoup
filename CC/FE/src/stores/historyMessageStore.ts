import { create } from "zustand";
import { devtools } from "zustand/middleware";
import { immer } from "zustand/middleware/immer";

type HistoryMessage = {
  history: string[];
};

type Action = {
  addMessage: (messageNum: number, a: string, b?: string) => string;
};

const historyMessagingConverter = (
  messageNum: number,
  a: string,
  b: string
) => {
  let convertedMessage = "";

  switch (messageNum) {
    case 1:
      convertedMessage = `${a}가 `;
      break;
    case 2:
      convertedMessage = `게임이 시작되었습니다.`;
      break;
    case 3:
      convertedMessage = `게임이 시작되었습니다.`;
      break;
    case 4:
      convertedMessage = `게임이 시작되었습니다.`;
      break;
    case 5:
      convertedMessage = `게임이 시작되었습니다.`;
      break;
    case 6:
      convertedMessage = `게임이 시작되었습니다.`;
      break;
    case 7:
      convertedMessage = `게임이 시작되었습니다.`;
      break;
    case 8:
      convertedMessage = `게임이 시작되었습니다.`;
      break;
    case 9:
      convertedMessage = `게임이 시작되었습니다.`;
      break;
    case 10:
      convertedMessage = `게임이 시작되었습니다.`;
      break;
    case 11:
      convertedMessage = `게임이 시작되었습니다.`;
      break;
    case 12:
      convertedMessage = `게임이 시작되었습니다.`;
      break;
    case 13:
      convertedMessage = `게임이 시작되었습니다.`;
      break;
    case 14:
      convertedMessage = `게임이 시작되었습니다.`;
      break;
    case 15:
      convertedMessage = `게임이 시작되었습니다.`;
      break;
    case 16:
      convertedMessage = `게임이 시작되었습니다.`;
      break;
    case 17:
      convertedMessage = `게임이 시작되었습니다.`;
      break;
    case 18:
      convertedMessage = `플레이어 턴입니다.`;
      break;
  }
  return convertedMessage;
};

const useHistoryStore = create<HistoryMessage & Action>()(
  devtools(
    immer((set) => ({
      history: [],
      addMessage: (messageNum, a, b?) => {
        set((state) => state.history.push());
      },
    }))
  )
);

export default useHistoryStore;
