export default interface History {
  actionId: number;
  id: string;
  playerTrying: string;
  playerTried: string;
  turn: number;
  actionState: boolean;

  // 새로 추가
  gptLine: string;
}
