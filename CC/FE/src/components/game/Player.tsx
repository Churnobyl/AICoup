import CardHolder from "@/components/game/CardHolder";
import useActionStore from "@/stores/actionStore";
import useGameStore from "@/stores/gameStore";
import { IconContext } from "react-icons";
import { PiCoinVerticalFill } from "react-icons/pi";
import "./Player.scss";
import { useEffect, useState } from "react";

type Props = {
  playerNumber: number;
  playerId: string;
  className?: string;
};

// 새로 추가
// playerNumber에 따라 위치를 결정하는 함수
const getBubblePosition = (playerNumber: number): "top" | "right" | "left" | "bottom" => {
  switch (playerNumber) {
    case 0:
      return "top";
    case 1:
      return "right";
    case 2:
      return "bottom";
    case 3:
      return "left";
    default:
      return "top"; // 임의 기본값
  }
};

export const Player = (props: Props) => {
  const store = useGameStore();
  const actionStore = useActionStore();
  const { playerNumber, className, playerId } = props;

  const [localCoin, setLocalCoin] = useState(store.members[playerNumber].coin);
  const [animationClass, setAnimationClass] = useState("");

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

  return (
    <div className={`player ${className}`} onClick={setTarget}>
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

      {/* 추가 */}
      {/* 메시지가 있을 경우에만 메시지 창을 표시 */}
      {playerMessage && (
        <MessageBubble message={playerMessage} position={bubblePositionClass} />
      )}
      
    </div>
  );
};

export default Player;
