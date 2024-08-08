// src/components/ui/bubble/MessageBubble.tsx
import React from "react";
import "./MessageBubble.scss";

interface MessageBubbleProps {
    message: string;
    position: "top" | "right" | "left" | "bottom";
}

const MessageBubble: React.FC<MessageBubbleProps> = ({
    message,
    position,
}) => {
    return (
        <div className={`message-bubble ${position}`}>
            <p>{message}</p>
        </div>
    );
};

export default MessageBubble;
