import "./Card.scss";
import useCardInfoStore from "@/stores/cardInfoStore";

type Props = {
  cardNumber: number;
  player: boolean;
};

const Card = (props: Props) => {
  const { cardNumber, player } = props;
  const store = useCardInfoStore();

  return (
    <div
      className={`cardItem card card-${
        player ? cardNumber : cardNumber < 0 ? -cardNumber : 0
      }`}
    >
      {player
        ? store.cardname[cardNumber > 0 ? cardNumber : -cardNumber]
        : cardNumber < 0
        ? store.cardname[-cardNumber]
        : ""}
    </div>
  );
};

export default Card;
