package digital.upgrade.raft;

import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import digital.upgrade.raft.Model.MessageContainer;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * The message listener provides features for receiving messages from other
 * peers in a distributed election group.
 *
 * @author damien@upgrade-digital.com
 */
public class MessageTransport {

  private volatile InputStream in;
  private volatile List<MessageHandler> handlers = Lists.newArrayList();

  public MessageTransport() {}

  public void receive() throws IOException {
    byte[] bytes = new byte[4];
    int readCount = in.read(bytes);
    if (bytes.length > readCount) {
      throw new IOException("Failed to read message length prefix");
    }
    int length = Ints.fromByteArray(bytes);
    byte[] data = new byte[length];
    readCount = in.read(data);
    if (data.length > readCount) {
      throw new IOException("Failed to read complete message body");
    }
    MessageContainer container = MessageContainer.parseFrom(data);
    for (MessageHandler handler : handlers) {
      if (container.hasAppendEntry()) {
        handler.appendEntry(container.getAppendEntry());
      }
      // TODO (dka) Route response
      if (container.hasRequestVote()) {
        handler.requestVote(container.getRequestVote());
      }
      // TODO (dka) route response
    }
  }

  public static class Builder {

    private MessageTransport result = new MessageTransport();

    private Builder() {}

    public Builder setInputStream(InputStream in) {
      result.in = in;
      return this;
    }

    public Builder registerHandler(MessageHandler handler) {
      result.handlers.add(handler);
      return this;
    }

    public MessageTransport build() {
      return result;
    }
  }
}
