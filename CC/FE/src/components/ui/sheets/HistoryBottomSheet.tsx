import useGameStore from "@/stores/gameStore";
import { useEffect, useRef, useState } from "react";
import { BottomSheet, BottomSheetRef } from "react-spring-bottom-sheet";
import "react-spring-bottom-sheet/dist/style.css";
import "./HistoryBottomSheet.scss";
import useHistoryStore from "@/stores/historyMessageStore";

function HistoryBottomSheet() {
  const sheetRef = useRef<BottomSheetRef>(null);
  const lastItemRef = useRef<HTMLLIElement>(null);
  const [open, setOpen] = useState(true);
  const store = useGameStore();
  const { history } = store;
  const { historyMessage } = useHistoryStore();

  const handleDismiss = () => {
    setOpen(false);
  };

  useEffect(() => {
    if (lastItemRef.current) {
      lastItemRef.current.scrollIntoView({ behavior: "smooth" });
    }
  }, [history]);

  return (
    <>
      <BottomSheet
        className="bottomSheet"
        open={open}
        ref={sheetRef}
        blocking={false}
        onDismiss={handleDismiss}
        snapPoints={({ minHeight, maxHeight }) => [minHeight, maxHeight * 0.1]}
        defaultSnap={({ maxHeight }) => maxHeight / 4}
        expandOnContentDrag={true}
      >
        <ul className="history-list">
          {historyMessage.map((value, index) => (
            <li
              key={index}
              ref={index === historyMessage.length - 1 ? lastItemRef : null}
              className={index % 2 === 0 ? "gray" : ""}
            >
              {value}
            </li>
          ))}
        </ul>
      </BottomSheet>
    </>
  );
}

export default HistoryBottomSheet;
