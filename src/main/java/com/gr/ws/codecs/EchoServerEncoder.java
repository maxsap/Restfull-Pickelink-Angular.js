package com.gr.ws.codecs;

import java.io.StringWriter;

import javax.json.Json;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import com.gr.ws.domain.MyMessage;

/**
 * Encoder which encodes the object data into messages
 * which can be transported over the websocket connection.
 */
public class EchoServerEncoder implements Encoder.Text<MyMessage> {

  /**
   * Encode the instance of MyMessage into a JSON string.
   */
  @Override
  public String encode(MyMessage myMsg) throws EncodeException {
    
    StringWriter writer = new StringWriter();
    //Makes use of the JSON Streaming API to build the JSON string.
    Json.createGenerator(writer)
            .writeStartObject()
              .write("message", myMsg.message)
              .write("time", myMsg.receivedAt.toString())
            .writeEnd()
            .flush();
    System.out.println(writer.toString());
    return writer.toString();
  }

  @Override
  public void init(EndpointConfig config) {
  }

  @Override
  public void destroy() {
  }
}

