import useGameStore from "@/stores/gameStore";

type Prop = {
  className: string;
};

function HistoryRoll(props: Prop) {
  const store = useGameStore();
  const { history } = store;

  return (
    <div>
      <ul>
        {history.map((data) => (
          <li key={data.id}>{JSON.stringify(data)}</li>
        ))}
      </ul>
    </div>
  );
}

export default HistoryRoll;
