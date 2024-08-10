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

  return (
    <div
      className={classNames(
        className,
        actionStore.isClickable && playerNumber !== 0 ? "clickable" : ""
      )}
    >
      <Card
        player={store.members[playerNumber].player}
        cardNumber={store.members[playerNumber].leftCard}
      />
      <Card
        player={store.members[playerNumber].player}
        cardNumber={store.members[playerNumber].rightCard}
      />
    </div>
  );
};

export default CardHolder;
