import History from "@/types/HistoryInf";
import Member from "@/types/MemberInf";

export default interface ReturnType {
  history: History[];
  members: Member[];
  message: string;
  turn: number;
  whoseTurn: number;
  roomId: string;
  state: string;
  deck: number[];
  lastContext: History[];
}
