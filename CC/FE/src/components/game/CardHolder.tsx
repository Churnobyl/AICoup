import Card from "@/components/game/Card";
import "./CardHolder.scss";
import useGameStore from "@/stores/gameStore";
import classNames from "classnames";
import useActionStore from "@/stores/actionStore";
import { useEffect, useState } from "react";

type Props = {
  playerNumber: number;
  className?: string;
};

const CardHolder = (props: Props) => {
  const store = useGameStore();
  const actionStore = useActionStore();
  const { playerNumber, className } = props;
  const [isClickable, setIsClickable] = useState<boolean>(false);

  useEffect(() => {
    const unsubscribe = useActionStore.subscribe((state) => {
      const clickable =
        state.isClickable &&
        playerNumber !== 0 &&
        (store.members[playerNumber].leftCard === 0 ||
          store.members[playerNumber].rightCard === 0);

      setIsClickable(clickable);
    });

    return () => unsubscribe(); // 컴포넌트 언마운트 시 구독 해제
  }, [playerNumber, store.members, actionStore, actionStore.isClickable]);

  return (
    <div className={classNames(className, isClickable ? "clickable" : "")}>
      <Card
        player={store.members[playerNumber].player}
        cardNumber={store.members[playerNumber].leftCard}
        playerCardIdForSelect={playerNumber === 0 ? 0 : -1}
      />
      <Card
        player={store.members[playerNumber].player}
        cardNumber={store.members[playerNumber].rightCard}
        playerCardIdForSelect={playerNumber === 0 ? 1 : -1}
      />
    </div>
  );
};

export default CardHolder;
