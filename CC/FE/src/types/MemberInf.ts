import History from "@/types/HistoryInf";
import CardInfo from "@/types/CardInfoInf";

export default interface Member {
  id: string;
  name: string;
  actionHistory: History[];
  coin: number;
  leftCard: number;
  leftCardInfo: CardInfo;
  player: boolean;
  rightCard: number;
  rightCardInfo: CardInfo;

  // 새로 추가
  message: string
}
