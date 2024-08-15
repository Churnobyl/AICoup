import useCardInfoStore from "@/stores/cardInfoStore";
import useCardSelectStore from "@/stores/cardSelectStore";
import "./Card.scss";
import { useCallback } from "react";

type Props = {
  cardNumber: number;
  player: boolean;
  playerCardIdForSelect: number;
};

const Card = (props: Props) => {
  const { cardNumber, player, playerCardIdForSelect } = props;
  const store = useCardInfoStore();
  const cardSelectStore = useCardSelectStore();

  const setPlayerCard = useCallback(() => {
    if (useCardSelectStore.getState().isPlayerCardClickable && cardNumber > 0) {
      cardSelectStore.setIsPlayerCardClickable();
      cardSelectStore.setSelectedPlayerCard(playerCardIdForSelect);
    }
  }, [cardSelectStore, cardNumber, playerCardIdForSelect]);

  return (
    <div
      className={`cardItem card card-${
        player
          ? cardNumber < 0
            ? -cardNumber
            : cardNumber
          : cardNumber < 0
          ? -cardNumber
          : 0
      } ${cardNumber < 0 ? "dead" : ""} ${
        useCardSelectStore.getState().isPlayerCardClickable &&
        player &&
        cardNumber > 0
          ? "clickable"
          : ""
      }`}
      onClick={setPlayerCard}
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
