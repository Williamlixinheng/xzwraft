package in.xzw.xzwraft.kvstore.server;

import com.google.protobuf.ByteString;
import com.google.protobuf.MessageLite;
import in.xzw.xzwraft.kvstore.MessageConstants;
import in.xzw.xzwraft.kvstore.Protos;
import in.xzw.xzwraft.kvstore.message.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.io.IOException;
import java.util.Objects;

public class Encoder extends MessageToByteEncoder<Object> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        if (msg instanceof Success) {
            this.writeMessage(MessageConstants.MSG_TYPE_SUCCESS, Protos.Success.newBuilder().build(), out);
        } else if (msg instanceof Failure) {
            Failure failure = (Failure) msg;
            Protos.Failure protoFailure = Protos.Failure.newBuilder().setErrorCode(failure.getErrorCode()).setMessage(failure.getMessage()).build();
            this.writeMessage(MessageConstants.MSG_TYPE_FAILURE, protoFailure, out);
        } else if (msg instanceof Redirect) {
            Redirect redirect = (Redirect) msg;
            String leaderId = Objects.toString(redirect.getLeaderId(), "");
            Protos.Redirect protoRedirect = Protos.Redirect.newBuilder().setLeaderId(leaderId).build();
            this.writeMessage(MessageConstants.MSG_TYPE_REDIRECT, protoRedirect, out);
        }  else if (msg instanceof GetCommand) {
            GetCommand command = (GetCommand) msg;
            Protos.GetCommand protoGetCommand = Protos.GetCommand.newBuilder().setKey(command.getKey()).build();
            this.writeMessage(MessageConstants.MSG_TYPE_GET_COMMAND, protoGetCommand, out);
        } else if (msg instanceof GetCommandResponse) {
            GetCommandResponse response = (GetCommandResponse) msg;
            byte[] value = response.getValue();
            Protos.GetCommandResponse protoResponse = Protos.GetCommandResponse.newBuilder()
                    .setFound(response.isFound())
                    .setValue(value != null ? ByteString.copyFrom(value) : ByteString.EMPTY).build();
            this.writeMessage(MessageConstants.MSG_TYPE_GET_COMMAND_RESPONSE, protoResponse, out);
        } else if (msg instanceof SetCommand) {
            SetCommand command = (SetCommand) msg;
            Protos.SetCommand protoSetCommand = Protos.SetCommand.newBuilder()
                    .setKey(command.getKey())
                    .setValue(ByteString.copyFrom(command.getValue()))
                    .build();
            this.writeMessage(MessageConstants.MSG_TYPE_SET_COMMAND, protoSetCommand, out);
        }
    }

    private void writeMessage(int messageType, MessageLite message, ByteBuf out) throws IOException {
        out.writeInt(messageType);
        byte[] bytes = message.toByteArray();
        out.writeInt(bytes.length);
        out.writeBytes(bytes);
    }

}
