import Card from "@/components/game/Card";
import "./CardHolder.scss";
import useGameStore from "@/stores/gameStore";
import classNames from "classnames";
import useActionStore from "@/stores/actionStore";

type Props = {
  playerNumber: number;
  className?: string;
};

const CardHolder = (props: Props) => {
  const store = useGameStore();
  const actionStore = useActionStore();
  const { playerNumber, className } = props;

  const member = store.members[playerNumber];

  return (
    <div
      className={classNames(
        className,
        actionStore.isClickable &&
          playerNumber !== 0 &&
          (store.members[playerNumber].leftCard === 0 ||
            store.members[playerNumber].rightCard === 0)
          ? "clickable"
          : ""
      )}
    >
      <Card
        player={store.members[playerNumber].player}
        cardNumber={store.members[playerNumber].leftCard}
        playerCardIdForSelect={playerNumber === 0 ? 0 : -1}
        isRevealed={member.leftCardRevealed}
      />
      <Card
        player={store.members[playerNumber].player}
        cardNumber={store.members[playerNumber].rightCard}
        playerCardIdForSelect={playerNumber === 0 ? 1 : -1}
        isRevealed={member.rightCardRevealed}
      />
    </div>
  );
};

export default CardHolder;
