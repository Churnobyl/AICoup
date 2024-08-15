import CardHolder from "@/components/game/CardHolder";
import useActionStore from "@/stores/actionStore";
import useGameStore from "@/stores/gameStore";
import { IconContext } from "react-icons";
import { PiCoinVerticalFill } from "react-icons/pi";
import "./Player.scss";
import { useEffect, useRef, useState } from "react";
import MessageBubble from "@/components/ui/bubble/MessageBubble";

type Props = {
  playerNumber: number;
  playerId: string;
  className?: string;
};

export const Player = (props: Props) => {
  const store = useGameStore();
  const actionStore = useActionStore();
  const { playerNumber, className, playerId } = props;

  const [localCoin, setLocalCoin] = useState(store.members[playerNumber].coin);
  const [animationClass, setAnimationClass] = useState("");
  const [message, setMessage] = useState("");
  const [show, setShow] = useState<boolean>(false);
  const [zIndex, setZIndex] = useState<number>(0);

  const lastHistoryRef = useRef<string | null>(null);
  const timeoutRef = useRef<NodeJS.Timeout | null>(null);

  const setTarget = () => {
    if (
      actionStore.isClickable &&
      (store.members[playerNumber].leftCard === 0 ||
        store.members[playerNumber].rightCard === 0)
    ) {
      actionStore.setSelectedTarget(playerId);
      actionStore.setIsClickable();
      console.log("setTarget :", playerId);
    }
  };

  useEffect(() => {
    const targetCoin = store.members[playerNumber].coin;
    let interval: NodeJS.Timeout | undefined;

    if (localCoin !== targetCoin) {
      setAnimationClass(localCoin < targetCoin ? "explode" : "shrink");

      interval = setInterval(() => {
        setLocalCoin((prevCoin) => {
          if (prevCoin < targetCoin) {
            return prevCoin + 1;
          } else if (prevCoin > targetCoin) {
            return prevCoin - 1;
          } else {
            clearInterval(interval!);
            return prevCoin;
          }
        });
      }, 150);
    }

    return () => {
      if (interval) {
        clearInterval(interval);
      }
    };
  }, [localCoin, playerNumber, store.members]);

  useEffect(() => {
    if (animationClass) {
      const timeout = setTimeout(() => setAnimationClass(""), 500);
      return () => clearTimeout(timeout);
    }
  }, [animationClass]);

  useEffect(() => {
    const history = store.history;
    const lastHistoryItem = history[history.length - 1];

    if (
      lastHistoryItem &&
      lastHistoryItem.dialog &&
      lastHistoryItem.playerTrying === playerId &&
      lastHistoryItem.dialog !== lastHistoryRef.current
    ) {
      lastHistoryRef.current = lastHistoryItem.dialog;

      setZIndex(10);

      setMessage(lastHistoryItem.dialog);
      setShow(true);

      if (timeoutRef.current) {
        clearTimeout(timeoutRef.current);
      }

      timeoutRef.current = setTimeout(() => {
        setShow(false);
        setZIndex(0);
      }, 5000);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [store.history]);

  return (
    <div
      className={`player ${className}`}
      onClick={setTarget}
      style={{ zIndex }}
    >
      <MessageBubble message={message} triggerShow={show} />
      <span>
        {store.members[playerNumber].name === "userA"
          ? ""
          : store.members[playerNumber].name}
      </span>
      <span className={`coin-value ${animationClass}`}>
        <IconContext.Provider value={{ color: "yellow", size: "24px" }}>
          <PiCoinVerticalFill />
        </IconContext.Provider>
        {localCoin}
      </span>
      <CardHolder
        key={playerNumber}
        playerNumber={playerNumber}
        className={`cardHolder`}
      />
    </div>
  );
};

export default Player;
