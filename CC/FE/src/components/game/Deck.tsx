import useGameStore from "@/stores/gameStore";
import { useMemo } from "react";
import "./Deck.scss";

const Deck = () => {
  const store = useGameStore();
  const { deck } = store;

  const deckNumber = useMemo(() => {
    return deck.length > 0 ? deck.reduce((a, b) => a + b, 0) : 0;
  }, [deck]);

  return (
    <div className="deck-cover">
      <span>덱 ({deckNumber})</span>
      <div className="deck"></div>
    </div>
  );
};

export default Deck;
