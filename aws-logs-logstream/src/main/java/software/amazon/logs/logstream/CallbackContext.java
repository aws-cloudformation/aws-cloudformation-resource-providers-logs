package software.amazon.logs.logstream;

import software.amazon.cloudformation.proxy.StdCallbackContext;

@lombok.Getter
@lombok.Setter
@lombok.ToString
@lombok.EqualsAndHashCode(callSuper = true)
public class CallbackContext extends StdCallbackContext {
    private boolean isItFirstTime = true;
    private boolean propagationDelay = false;
    private String generatedLogStreamName;
}
