import { useRef } from "react";
import { BottomSheet, BottomSheetRef } from "react-spring-bottom-sheet";
import "react-spring-bottom-sheet/dist/style.css";

function HistoryBottomSheet() {
  const sheetRef = useRef<BottomSheetRef>();

  return (
    <BottomSheet open ref={sheetRef}>
      <button
        onClick={() => {
          sheetRef.current?.snapTo(({ maxHeight }) => maxHeight);
        }}
      >
        Expand to full height
      </button>
    </BottomSheet>
  );
}

export default HistoryBottomSheet;
