import Modal from "react-modal";
import "./ModalComponent.scss";

Modal.setAppElement("#root"); // Make sure to bind modal to your appElement

const ModalComponent = ({
  isOpen,
  onRequestClose,
  content,
  onSelect,
  options,
}) => {
  return (
    <Modal
      isOpen={isOpen}
      onRequestClose={onRequestClose}
      contentLabel="Game Modal"
      className="modal"
      overlayClassName="overlay"
    >
      <div className="modal-content">
        <p>{content}</p>
        <div className="modal-actions">
          {options.map((option: string, index: number) => (
            <button key={index} onClick={() => onSelect({ option })}>
              {option}
            </button>
          ))}
        </div>
      </div>
    </Modal>
  );
};

export default ModalComponent;
