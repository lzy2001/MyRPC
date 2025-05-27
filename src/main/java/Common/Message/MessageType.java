package Common.Message;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum MessageType {
    // 分别表示消息请求和消息响应
    REQUEST(0),RESPONSE(1);
    private final int code;
}