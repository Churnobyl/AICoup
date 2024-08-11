import { optionKeyByNumber } from "@/stores/selectOptions";
import { create } from "zustand";
import { devtools } from "zustand/middleware";
import { immer } from "zustand/middleware/immer";

type HistoryMessage = {
  historyMessage: string[];
};

type Action = {
  addMessage: (
    messageNum: number,
    trying: string | "none" | "",
    tried?: string | "none" | "",
    actionState?: boolean
  ) => void;
};

export const historyMessagingConverter = (
  messageNum: number,
  trying: string,
  tried?: string,
  actionState?: boolean
) => {
  let convertedMessage = "";

  if (trying === "1") {
    trying = "플레이어";
  }

  if (tried === "1") {
    tried = "플레이어";
  }

  switch (messageNum) {
    case 1:
      if (actionState === null) {
        convertedMessage = `${trying}님이 1원을 얻기 위해 ${optionKeyByNumber[messageNum]}를 시도합니다.`;
      } else if (actionState) {
        convertedMessage = `${trying}님이 수입으로 1원을 얻습니다.`;
      }
      break;
    case 2:
      if (actionState === null) {
        convertedMessage = `${trying}님이 2원을 얻기 위해 ${optionKeyByNumber[messageNum]}를 시도합니다.`;
      } else if (actionState) {
        convertedMessage = `${trying}님이 ${optionKeyByNumber[messageNum]}를 성공해 2원을 얻습니다.`;
      } else if (!actionState) {
        convertedMessage = `${trying}님의 ${optionKeyByNumber[messageNum]}이 일어나지 않습니다.`;
      }

      break;
    case 3:
      if (actionState === null) {
        convertedMessage = `${trying}님이 3원을 얻기 위해 공작을 갖고 있다고 주장하여 ${optionKeyByNumber[messageNum]}를 시도합니다.`;
      } else if (actionState) {
        convertedMessage = `${trying}님이 ${optionKeyByNumber[messageNum]}로 3원을 얻습니다.`;
      } else if (!actionState) {
        convertedMessage = `${trying}님의 ${optionKeyByNumber[messageNum]}이 일어나지 않습니다.`;
      }

      break;
    case 4:
      if (actionState === null) {
        convertedMessage = `${trying}님이 사령관을 갖고 있다고 주장하여 ${tried}님으로부터 ${optionKeyByNumber[messageNum]}을 시도합니다.`;
      } else if (actionState) {
        convertedMessage = `${trying}님이 ${tried}에게서 2원을 빼앗습니다.`;
      } else if (!actionState) {
        convertedMessage = `${trying}님의 ${optionKeyByNumber[messageNum]}이 일어나지 않습니다.`;
      }

      break;
    case 5:
      if (actionState === null) {
        convertedMessage = `${trying}님이 암살자를 갖고 있다고 주장하여 ${tried}의 ${optionKeyByNumber[messageNum]}을 시도합니다.`;
      } else if (actionState) {
        // convertedMessage = `${tried}님이 ${characterCard}을 잃습니다.`;
        convertedMessage = `${tried}님이 카드를 잃습니다.`;
      } else if (!actionState) {
        convertedMessage = `${trying}님의 ${optionKeyByNumber[messageNum]}이 일어나지 않습니다.`;
      }

      break;
    case 6:
      if (actionState === null) {
        convertedMessage = `${trying}님이 외교관을 갖고 있다고 주장하여 ${optionKeyByNumber[messageNum]}을 시도합니다.`;
      } else if (actionState) {
        convertedMessage = `${trying}님이 덱에서 카드를 2장 뽑습니다.`;
      } else if (!actionState) {
        convertedMessage = `${trying}님의 ${optionKeyByNumber[messageNum]}이 일어나지 않습니다.`;
      }

      break;
    case 7:
      if (actionState === null) {
        convertedMessage = `${trying}님이 7원을 내고 ${tried}에게 ${optionKeyByNumber[messageNum]}을 실행합니다.`;
      } else if (actionState) {
        // convertedMessage = `${tried}님이 ${characterCard}을 잃습니다.`;
        convertedMessage = `${tried}님이 카드를 잃습니다.`;
      }

      break;
    case 8:
      if (actionState === null) {
        // convertedMessage = `${trying}님이 ${tried}에게 ${actionCharacter}을 공개하라는 도전을 걸었습니다.`;
        convertedMessage = `${trying}님이 ${tried}에게 카드 공개를 도전했습니다.`;
      }

      break;
    case 9:
      if (actionState === null) {
        convertedMessage = `${trying}님이 허용했습니다.`;
      }

      break;
    case 10:
      if (actionState === null) {
        convertedMessage = `${tried}님이 공작을 갖고 있다고 주장하여 ${trying}이 ${optionKeyByNumber[messageNum]}을 시도합니다.`;
      }
      break;
    case 11:
      if (actionState === null) {
        convertedMessage = `${tried}님이 사령관을 갖고 있다고 주장하여 ${trying}이 ${optionKeyByNumber[messageNum]}을 시도합니다.`;
      }

      break;
    case 12:
      if (actionState === null) {
        convertedMessage = `${tried}님이 외교관을 갖고 있다고 주장하여 ${trying}이 ${optionKeyByNumber[messageNum]}을 시도합니다.`;
      }

      break;
    case 13:
      if (actionState === null) {
        convertedMessage = `${tried}님이 귀부인을 갖고 있다고 주장하여 ${trying}이 ${optionKeyByNumber[messageNum]}을 시도합니다.`;
      }

      break;
    case 14:
      if (actionState) {
        // convertedMessage = `${trying}님이 ${characterCard}을 공개합니다.`;
        convertedMessage = `${trying}님의 거짓말이 들통났습니다.`;
      } else if (!actionState) {
        // convertedMessage = `${trying}님이 ${characterCard}을 공개하여 거짓말이 들통났습니다!`;
        convertedMessage = `${trying}님이 카드를 공개합니다.`;
      }

      break;
    case 15:
      convertedMessage = `${trying}님 승리!`;

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
      historyMessage: [],
      addMessage: (messageNum, trying, tried, actionState) => {
        set((state) => {
          const newMessage = historyMessagingConverter(
            messageNum,
            trying,
            tried,
            actionState
          );
          if (newMessage !== null) {
            state.historyMessage.push(newMessage);
          }
        });
      },
    }))
  )
);

export default useHistoryStore;
