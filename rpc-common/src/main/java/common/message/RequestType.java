package common.message;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum RequestType {
    NORMAL(0), HEARTBEAT(1);
    private int code;

}
