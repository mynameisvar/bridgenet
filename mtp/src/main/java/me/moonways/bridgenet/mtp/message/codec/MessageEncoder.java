package me.moonways.bridgenet.mtp.message.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.moonways.bridgenet.mtp.config.MTPConfiguration;
import me.moonways.bridgenet.mtp.message.ExportedMessage;
import me.moonways.bridgenet.mtp.message.MessageWrapper;
import me.moonways.bridgenet.mtp.message.encryption.MessageEncryption;
import me.moonways.bridgenet.mtp.message.exception.MessageCodecException;
import me.moonways.bridgenet.mtp.transfer.ByteCodec;
import me.moonways.bridgenet.mtp.transfer.ByteCompression;
import me.moonways.bridgenet.mtp.transfer.MessageTransfer;

@Log4j2
@RequiredArgsConstructor
public class MessageEncoder extends MessageToByteEncoder<ExportedMessage> {

    private final MTPConfiguration configuration;

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, ExportedMessage exportedMessage, ByteBuf byteBuf) {
        if (exportedMessage == null || exportedMessage.getMessage() == null || exportedMessage.getWrapper() == null) {
            log.error(new MessageCodecException("can not encode " + exportedMessage + " null"));
            return;
        }

        MessageWrapper wrapper = exportedMessage.getWrapper();
        Object message = exportedMessage.getMessage();

        try {
            MessageTransfer messageTransfer = MessageTransfer.encode(message);
            messageTransfer.buf();

            byteBuf.writeInt(wrapper.getId());
            ByteBuf buffer = messageTransfer.getByteBuf();

            if (wrapper.needsEncryption()) {

                MessageEncryption encryption = configuration.getEncryption();
                buffer = encryption.encode(buffer);
            }

            ByteCompression.write(ByteCodec.readBytesArray(buffer), byteBuf);
        }
        catch (Exception exception) {
            log.error(new MessageCodecException(exception));
        }
    }
}

