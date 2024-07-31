import Card from "@/components/game/Card";
import "./CardHolder.scss";
import useGameStore from "@/stores/gameStore";
import classNames from "classnames";

type Props = {
  playerNumber: number;
  className?: string;
};

const CardHolder = (props: Props) => {
  const store = useGameStore();
  const { playerNumber, className } = props;

  return (
    <div className={classNames(className, "cardHolder")}>
      <p>{store.members[playerNumber].name}</p>
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
