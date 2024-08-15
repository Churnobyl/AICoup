import React, { useEffect, useRef, useState } from "react";
import "./MessageBubble.scss";

interface MessageBubbleProps {
  message: string;
  triggerShow: boolean;
}

const MessageBubble: React.FC<MessageBubbleProps> = ({
  message,
  triggerShow,
}) => {
  const [show, setShow] = useState(false);
  const timeoutRef = useRef<NodeJS.Timeout | null>(null);

  useEffect(() => {
    if (triggerShow) {
      setShow(true);

      if (timeoutRef.current) {
        clearTimeout(timeoutRef.current);
      }

      timeoutRef.current = setTimeout(() => {
        setShow(false);
      }, 5000);
    }
  }, [triggerShow]);

  useEffect(() => {
    return () => {
      if (timeoutRef.current) {
        clearTimeout(timeoutRef.current);
      }
    };
  }, []);

  return (
    <div className={`message-bubble ${show ? "show" : "hide"}`}>
      <p>{message}</p>
    </div>
  );
};

export default MessageBubble;
