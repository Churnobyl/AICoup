import { optionKeyByNumber } from "@/stores/selectOptions";
import { create } from "zustand";
import { devtools } from "zustand/middleware";
import { immer } from "zustand/middleware/immer";

type HistoryMessage = {
  historyMessage: JSX.Element[];
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
  let convertedMessage: JSX.Element | null = null;

  if (trying === "1" || trying === "userA") {
    trying = "플레이어";
  }

  if (tried === "1" || trying === "userA") {
    tried = "플레이어";
  }

  switch (messageNum) {
    case 1:
      if (actionState === null) {
        convertedMessage = (
          <div>
            <strong>{trying}</strong>님이 <div className={"money"}>1원</div>을
            얻기 위해 {optionKeyByNumber[messageNum]}를 시도합니다.
          </div>
        );
      } else if (actionState) {
        convertedMessage = (
          <div>
            <strong>{trying}</strong>님이 수입으로{" "}
            <div className={"money"}>1원</div>을 얻습니다.
          </div>
        );
      }
      break;
    case 2:
      if (actionState === null) {
        convertedMessage = (
          <div>
            <strong>{trying}</strong>님이 <div className={"money"}>2원</div>을
            얻기 위해 {optionKeyByNumber[messageNum]}를 시도합니다.
          </div>
        );
      } else if (actionState) {
        convertedMessage = (
          <div>
            <strong>{trying}</strong>님이 {optionKeyByNumber[messageNum]}를
            성공해 <div className={"money"}>2원</div>을 얻습니다.
          </div>
        );
      } else if (!actionState) {
        convertedMessage = (
          <div>
            <strong>{trying}</strong>님의 {optionKeyByNumber[messageNum]}이
            일어나지 않습니다.
          </div>
        );
      }
      break;
    case 3:
      if (actionState === null) {
        convertedMessage = (
          <div>
            <strong>{trying}</strong>님이 <div className={"money"}>3원</div>을
            얻기 위해 <div className={"character-name character-1"}>공작</div>을
            갖고 있다고 주장하여 {optionKeyByNumber[messageNum]}를 시도합니다.
          </div>
        );
      } else if (actionState) {
        convertedMessage = (
          <div>
            <strong>{trying}</strong>님이 {optionKeyByNumber[messageNum]}로
            <div className={"money"}>3원</div>을 얻습니다.
          </div>
        );
      } else if (!actionState) {
        convertedMessage = (
          <div>
            <strong>{trying}</strong>님의 {optionKeyByNumber[messageNum]}이
            일어나지 않습니다.
          </div>
        );
      }
      break;
    case 4:
      if (actionState === null) {
        convertedMessage = (
          <div>
            <strong>{trying}</strong>님이{" "}
            <div className={"character-name character-4"}>사령관</div>을 갖고
            있다고 주장하여 <strong>{tried}</strong>님에게{" "}
            {optionKeyByNumber[messageNum]}을 시도합니다.
          </div>
        );
      } else if (actionState) {
        convertedMessage = (
          <div>
            <strong>{trying}</strong>님이 <strong>{tried}</strong>에게서{" "}
            <div className={"money"}>2원</div>을 빼앗습니다.
          </div>
        );
      } else if (!actionState) {
        convertedMessage = (
          <div>
            <strong>{trying}</strong>님의 {optionKeyByNumber[messageNum]}이
            일어나지 않습니다.
          </div>
        );
      }
      break;
    case 5:
      if (actionState === null) {
        convertedMessage = (
          <div>
            <strong>{trying}</strong>님이{" "}
            <div className={"character-name character-2"}>자객</div>를 갖고
            있다고 주장하여 <strong>{tried}</strong>님에게{" "}
            {optionKeyByNumber[messageNum]}을 시도합니다.
          </div>
        );
      } else if (actionState) {
        convertedMessage = (
          <div>
            <strong>{tried}</strong>님이 카드를 잃습니다.
          </div>
        );
      } else if (!actionState) {
        convertedMessage = (
          <div>
            <strong>{trying}</strong>님의 {optionKeyByNumber[messageNum]}이
            일어나지 않습니다.
          </div>
        );
      }
      break;
    case 6:
      if (actionState === null) {
        convertedMessage = (
          <div>
            <strong>{trying}</strong>님이{" "}
            <div className={"character-name character-3"}>외교관</div>을 갖고
            있다고 주장하여 {optionKeyByNumber[messageNum]}을 시도합니다.
          </div>
        );
      } else if (actionState) {
        convertedMessage = (
          <div>
            <strong>{trying}</strong>님이 덱에서 카드를 2장 뽑습니다.
          </div>
        );
      } else if (!actionState) {
        convertedMessage = (
          <div>
            <strong>{trying}</strong>님의 {optionKeyByNumber[messageNum]}이
            일어나지 않습니다.
          </div>
        );
      }
      break;
    case 7:
      if (actionState === null) {
        convertedMessage = (
          <div>
            <strong>{trying}</strong>님이 <div className={"money"}>7원</div>을
            내고 <strong>{tried}</strong>
            에게 {optionKeyByNumber[messageNum]}을 실행합니다.
          </div>
        );
      } else if (actionState) {
        convertedMessage = (
          <div>
            <strong>{tried}</strong>님이 카드를 잃습니다.
          </div>
        );
      }
      break;
    case 8:
      if (actionState === null) {
        convertedMessage = (
          <div>
            <strong>{trying}</strong>님이 <strong>{tried}</strong>에게 카드
            공개를 도전했습니다.
          </div>
        );
      }
      break;
    case 9:
      if (actionState === null) {
        convertedMessage = (
          <div>
            <strong>{trying}</strong>님이 허용했습니다.
          </div>
        );
      }
      break;
    case 10:
      if (actionState === null) {
        convertedMessage = (
          <div>
            <strong>{tried}</strong>님이{" "}
            <div className={"character-name character-1"}>공작</div>을 갖고
            있다고 주장하여 <strong>{trying}</strong>이{" "}
            {optionKeyByNumber[messageNum]}을 시도합니다.
          </div>
        );
      }
      break;
    case 11:
      if (actionState === null) {
        convertedMessage = (
          <div>
            <strong>{tried}</strong>님이{" "}
            <div className={"character-name character-4"}>사령관</div>을 갖고
            있다고 주장하여 <strong>{trying}</strong>이{" "}
            {optionKeyByNumber[messageNum]}을 시도합니다.
          </div>
        );
      }
      break;
    case 12:
      if (actionState === null) {
        convertedMessage = (
          <div>
            <strong>{tried}</strong>님이{" "}
            <div className={"character-name character-3"}>외교관</div>을 갖고
            있다고 주장하여 <strong>{trying}</strong>이{" "}
            {optionKeyByNumber[messageNum]}을 시도합니다.
          </div>
        );
      }
      break;
    case 13:
      if (actionState === null) {
        convertedMessage = (
          <div>
            <strong>{tried}</strong>님이{" "}
            <div className={"character-name character-5"}>귀부인</div>을 갖고
            있다고 주장하여 <strong>{trying}</strong>이{" "}
            {optionKeyByNumber[messageNum]}을 시도합니다.
          </div>
        );
      }
      break;
    case 14:
      if (actionState) {
        convertedMessage = (
          <div>
            <strong>{tried}</strong>님의 거짓말이 들통났습니다.
          </div>
        );
      } else if (!actionState) {
        convertedMessage = (
          <div>
            <strong>{tried}</strong>님이 해당 카드를 공개합니다.
          </div>
        );
      }
      break;
    case 15:
      convertedMessage = (
        <div>
          <strong>{trying}</strong>님 승리!
        </div>
      );
      break;
    case 17:
      convertedMessage = <div>게임이 시작되었습니다.</div>;
      break;
    case 18:
      convertedMessage = <div>플레이어 턴입니다.</div>;
      break;
    default:
      convertedMessage = null;
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
